package com.fraudshield.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fraudshield.model.dto.FactorImpactDto;
import com.fraudshield.model.dto.TransactionRequest;
import com.fraudshield.model.entity.UserProfileEntity;
import lombok.Builder;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class MlClientService {

    @Value("${fraud.ml-service.url:http://localhost:8000}")
    private String mlServiceBaseUrl;

    private final ObjectMapper objectMapper = new ObjectMapper();
    private final RestTemplate restTemplate = new RestTemplate();

    @Data
    @Builder
    public static class MlPredictionResult {
        private Double mlScore;
        private String riskTier;
        private List<FactorImpactDto> factors;
        private Double inferenceLatencyMs;
    }

    public MlPredictionResult predict(TransactionRequest request, UserProfileEntity profile,
                                     boolean isNewDevice, boolean isNewLocation,
                                     double geoVelocityKmh, int txns5Min) {
        long start = System.currentTimeMillis();
        try {
            String url = mlServiceBaseUrl + "/predict";
            Map<String, Object> payload = new HashMap<>();
            payload.put("transaction_id", request.getTransactionId());
            payload.put("user_id", request.getUserId());
            payload.put("amount", request.getAmount().doubleValue());
            payload.put("location", request.getLocation());
            payload.put("latitude", request.getLatitude() != null ? request.getLatitude() : 19.0760);
            payload.put("longitude", request.getLongitude() != null ? request.getLongitude() : 72.8777);
            payload.put("device_id", request.getDeviceId());
            payload.put("merchant_category", request.getMerchantCategory());
            payload.put("user_avg_amount", profile.getAvgAmount().doubleValue());
            payload.put("user_std_dev_amount", profile.getStdDevAmount().doubleValue());
            payload.put("is_new_device", isNewDevice);
            payload.put("is_new_location", isNewLocation);
            payload.put("geo_velocity_kmh", geoVelocityKmh);
            payload.put("txns_last_5min", txns5Min);
            payload.put("account_age_days", profile.getAccountAgeDays());
            payload.put("prev_failed_txns", 0);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(payload, headers);

            ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.POST, entity, String.class);
            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                JsonNode root = objectMapper.readTree(response.getBody());
                double score = root.path("fraud_probability").asDouble(0.05);
                String level = root.path("ml_risk_level").asText("LOW");
                double latency = root.path("inference_latency_ms").asDouble(System.currentTimeMillis() - start);

                List<FactorImpactDto> factors = new ArrayList<>();
                JsonNode factorsNode = root.path("top_risk_factors");
                if (factorsNode.isArray()) {
                    for (JsonNode f : factorsNode) {
                        factors.add(FactorImpactDto.builder()
                                .feature(f.path("feature").asText())
                                .impactPercentage(f.path("impact_percentage").asDouble())
                                .description(f.path("description").asText())
                                .rawValue(f.path("raw_value").asText())
                                .build());
                    }
                }

                return MlPredictionResult.builder()
                        .mlScore(score)
                        .riskTier(level)
                        .factors(factors)
                        .inferenceLatencyMs(latency)
                        .build();
            }
        } catch (Exception ex) {
            log.warn("ML Service unreachable ({}), applying built-in heuristic XGBoost approximation", ex.getMessage());
        }

        // Resilient Fallback Heuristic
        double fallbackScore = calculateHeuristicFallback(request, profile, isNewDevice, isNewLocation, geoVelocityKmh, txns5Min);
        return MlPredictionResult.builder()
                .mlScore(fallbackScore)
                .riskTier(fallbackScore > 0.70 ? "HIGH" : (fallbackScore > 0.30 ? "MEDIUM" : "LOW"))
                .factors(List.of())
                .inferenceLatencyMs((double) (System.currentTimeMillis() - start))
                .build();
    }

    private double calculateHeuristicFallback(TransactionRequest req, UserProfileEntity profile,
                                              boolean newDev, boolean newLoc, double speed, int burst) {
        double p = 0.04;
        double ratio = req.getAmount().doubleValue() / profile.getAvgAmount().doubleValue();
        if (ratio > 5.0) p += 0.35;
        if (newDev) p += 0.20;
        if (newLoc) p += 0.15;
        if (speed > 600) p += 0.35;
        if (burst >= 4) p += 0.20;
        return Math.min(0.99, p);
    }
}
