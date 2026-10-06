package com.fraudshield.service;

import com.fraudshield.model.dto.FactorImpactDto;
import com.fraudshield.model.dto.TransactionRequest;
import com.fraudshield.model.entity.UserProfileEntity;
import lombok.Builder;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
public class RuleEngineService {

    @Data
    @Builder
    public static class RuleEvaluationResult {
        private Double ruleScore; // 0.0 to 1.0
        private Boolean isNewDevice;
        private Boolean isNewLocation;
        private Double geoVelocityKmh;
        private List<FactorImpactDto> triggeredFactors;
    }

    public RuleEvaluationResult evaluateRules(TransactionRequest request, UserProfileEntity profile, int txns5Min, double geoVelocityKmh) {
        List<FactorImpactDto> factors = new ArrayList<>();
        double accumulatedRisk = 0.05; // Base baseline risk

        boolean isNewDevice = !request.getDeviceId().equalsIgnoreCase(profile.getPrimaryDeviceId());
        boolean isNewLocation = !request.getLocation().equalsIgnoreCase(profile.getPrimaryLocation());

        // Rule 1: High Absolute Amount (Amount > ₹100,000)
        BigDecimal highAmountThreshold = BigDecimal.valueOf(100000.00);
        if (request.getAmount().compareTo(highAmountThreshold) > 0) {
            accumulatedRisk += 0.28;
            factors.add(FactorImpactDto.builder()
                    .feature("HIGH_TRANSACTION_AMOUNT")
                    .impactPercentage(28.0)
                    .description("Transaction amount ₹" + request.getAmount() + " exceeds the high-risk ₹1,00,000 threshold")
                    .rawValue(request.getAmount())
                    .build());
        }

        // Rule 2: Unrecognized Device
        if (isNewDevice) {
            accumulatedRisk += 0.22;
            factors.add(FactorImpactDto.builder()
                    .feature("NEW_DEVICE_DETECTED")
                    .impactPercentage(22.0)
                    .description("Originating device (" + request.getDeviceId() + ") differs from user primary device (" + profile.getPrimaryDeviceId() + ")")
                    .rawValue(request.getDeviceId())
                    .build());
        }

        // Rule 3: Unrecognized Location
        if (isNewLocation) {
            accumulatedRisk += 0.18;
            factors.add(FactorImpactDto.builder()
                    .feature("NEW_LOCATION_DETECTED")
                    .impactPercentage(18.0)
                    .description("Transaction location (" + request.getLocation() + ") is outside typical zone (" + profile.getPrimaryLocation() + ")")
                    .rawValue(request.getLocation())
                    .build());
        }

        // Rule 4: Midnight / Odd Hour Window (12 AM - 5 AM)
        int hour = LocalTime.now().getHour();
        if (request.getTimestamp() != null && request.getTimestamp().contains(":")) {
            try {
                String[] parts = request.getTimestamp().split("[:T]");
                hour = Integer.parseInt(parts[0].length() > 2 ? parts[1] : parts[0]);
            } catch (Exception ignored) {}
        }
        if (hour >= 0 && hour <= 5) {
            accumulatedRisk += 0.16;
            factors.add(FactorImpactDto.builder()
                    .feature("MIDNIGHT_ANOMALY")
                    .impactPercentage(16.0)
                    .description("Transaction occurred during early morning hours (" + hour + ":00 hrs) with elevated fraud frequency")
                    .rawValue(hour + ":00")
                    .build());
        }

        // Rule 5: User Behavioral Spending Ratio Deviation
        BigDecimal avg = profile.getAvgAmount() != null && profile.getAvgAmount().compareTo(BigDecimal.ZERO) > 0 
                ? profile.getAvgAmount() : BigDecimal.valueOf(2500.00);
        double ratio = request.getAmount().doubleValue() / avg.doubleValue();
        if (ratio >= 4.0) {
            double impact = Math.min(35.0, ratio * 3.5);
            accumulatedRisk += 0.32;
            factors.add(FactorImpactDto.builder()
                    .feature("USER_SPENDING_DEVIATION")
                    .impactPercentage(impact)
                    .description("Amount is " + String.format("%.1f", ratio) + "x higher than user's normal average (₹" + avg + ")")
                    .rawValue(String.format("%.1fx", ratio))
                    .build());
        }

        // Advanced Rule 6: Impossible Travel / Geo-Velocity
        if (geoVelocityKmh > 600.0) {
            accumulatedRisk += 0.40;
            factors.add(FactorImpactDto.builder()
                    .feature("IMPOSSIBLE_TRAVEL_VELOCITY")
                    .impactPercentage(40.0)
                    .description("Calculated velocity between transactions (" + String.format("%.0f", geoVelocityKmh) + " km/h) violates physical travel limits")
                    .rawValue(String.format("%.0f km/h", geoVelocityKmh))
                    .build());
        }

        // Advanced Rule 7: Burst Velocity (Rapid sequential requests)
        if (txns5Min >= 4) {
            accumulatedRisk += 0.25;
            factors.add(FactorImpactDto.builder()
                    .feature("CARD_TESTING_BURST_VELOCITY")
                    .impactPercentage(25.0)
                    .description(txns5Min + " transactions recorded within 5 minutes (potential automated bot attack)")
                    .rawValue(txns5Min)
                    .build());
        }

        double finalRuleScore = Math.min(1.0, Math.max(0.02, accumulatedRisk));

        return RuleEvaluationResult.builder()
                .ruleScore(finalRuleScore)
                .isNewDevice(isNewDevice)
                .isNewLocation(isNewLocation)
                .geoVelocityKmh(geoVelocityKmh)
                .triggeredFactors(factors)
                .build();
    }
}
