package com.fraudshield.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fraudshield.model.dto.*;
import com.fraudshield.model.entity.TransactionEntity;
import com.fraudshield.model.entity.UserProfileEntity;
import com.fraudshield.repository.TransactionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class TransactionProcessingOrchestrator {

    private final RedisStateService redisStateService;
    private final RuleEngineService ruleEngineService;
    private final MlClientService mlClientService;
    private final RiskScoreAggregatorService aggregatorService;
    private final TransactionRepository transactionRepository;
    private final WebSocketBroadcastService webSocketBroadcastService;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Transactional
    public TransactionResponse processTransaction(TransactionRequest request) {
        long startTime = System.currentTimeMillis();

        if (request.getTransactionId() == null || request.getTransactionId().isBlank()) {
            request.setTransactionId("TXN-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase());
        }

        // 1. Redis State retrieval & feature extraction
        UserProfileEntity profile = redisStateService.getUserProfile(request.getUserId());
        int txns5Min = redisStateService.incrementAndGet5MinVelocity(request.getUserId());
        double geoVelocityKmh = redisStateService.calculateGeoVelocity(
                profile, request.getLatitude(), request.getLongitude(), LocalDateTime.now()
        );

        // 2. Rule Engine Evaluation
        RuleEngineService.RuleEvaluationResult ruleResult = ruleEngineService.evaluateRules(
                request, profile, txns5Min, geoVelocityKmh
        );

        // 3. Machine Learning Inference & TreeSHAP
        MlClientService.MlPredictionResult mlResult = mlClientService.predict(
                request, profile,
                ruleResult.getIsNewDevice(), ruleResult.getIsNewLocation(),
                geoVelocityKmh, txns5Min
        );

        // 4. Risk Aggregation
        double elapsedMs = (double) (System.currentTimeMillis() - startTime);
        RiskAssessment assessment = aggregatorService.aggregate(ruleResult, mlResult, elapsedMs);

        // 5. Serialize Explanation to JSON
        String explanationJson = "[]";
        try {
            explanationJson = objectMapper.writeValueAsString(assessment.getTopRiskFactors());
        } catch (Exception ignored) {}

        // 6. Persistence into PostgreSQL
        TransactionEntity entity = TransactionEntity.builder()
                .transactionId(request.getTransactionId())
                .userId(request.getUserId())
                .amount(request.getAmount())
                .location(request.getLocation())
                .latitude(request.getLatitude())
                .longitude(request.getLongitude())
                .deviceId(request.getDeviceId())
                .merchantCategory(request.getMerchantCategory())
                .isNewDevice(ruleResult.getIsNewDevice())
                .isNewLocation(ruleResult.getIsNewLocation())
                .geoVelocityKmh(geoVelocityKmh)
                .ruleScore(assessment.getRuleScore())
                .mlScore(assessment.getMlScore())
                .finalRiskScore(assessment.getFinalRiskScore())
                .riskLevel(assessment.getRiskLevel())
                .decision(assessment.getDecision())
                .explanationJson(explanationJson)
                .processingLatencyMs(elapsedMs)
                .createdAt(LocalDateTime.now())
                .build();

        transactionRepository.save(entity);

        // 7. Update User Profile State in Redis & DB
        redisStateService.updateLastKnownState(
                request.getUserId(),
                request.getLatitude(),
                request.getLongitude(),
                request.getDeviceId(),
                request.getLocation(),
                LocalDateTime.now()
        );

        // 8. Build Response
        TransactionResponse response = TransactionResponse.builder()
                .transactionId(request.getTransactionId())
                .userId(request.getUserId())
                .amount(request.getAmount())
                .location(request.getLocation())
                .deviceId(request.getDeviceId())
                .merchantCategory(request.getMerchantCategory())
                .ruleScore(assessment.getRuleScore())
                .mlScore(assessment.getMlScore())
                .finalRiskScore(assessment.getFinalRiskScore())
                .riskLevel(assessment.getRiskLevel())
                .decision(assessment.getDecision())
                .isNewDevice(ruleResult.getIsNewDevice())
                .isNewLocation(ruleResult.getIsNewLocation())
                .geoVelocityKmh(geoVelocityKmh)
                .riskFactors(assessment.getTopRiskFactors())
                .processingLatencyMs(elapsedMs)
                .timestamp(entity.getCreatedAt())
                .build();

        // 9. Real-time WebSocket Broadcast
        webSocketBroadcastService.broadcastTransaction(response);

        log.info("Txn {} [User {}] Amount ₹{} -> Risk: {} | Decision: {} (Latency: {}ms)",
                request.getTransactionId(), request.getUserId(), request.getAmount(),
                assessment.getFinalRiskScore(), assessment.getDecision(), elapsedMs);

        return response;
    }
}
