package com.fraudshield.service;

import com.fraudshield.model.dto.FactorImpactDto;
import com.fraudshield.model.dto.RiskAssessment;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
public class RiskScoreAggregatorService {

    @Value("${fraud.thresholds.weights.rule-weight:0.40}")
    private double ruleWeight;

    @Value("${fraud.thresholds.weights.ml-weight:0.60}")
    private double mlWeight;

    @Value("${fraud.thresholds.approve-max:0.30}")
    private double approveMax;

    @Value("${fraud.thresholds.review-max:0.70}")
    private double reviewMax;

    public RiskAssessment aggregate(RuleEngineService.RuleEvaluationResult ruleResult,
                                    MlClientService.MlPredictionResult mlResult,
                                    double totalLatencyMs) {

        // Composite weighted risk formula
        double compositeScore = (ruleResult.getRuleScore() * ruleWeight) + (mlResult.getMlScore() * mlWeight);
        compositeScore = Math.round(compositeScore * 10000.0) / 10000.0; // Round to 4 decimal places

        String riskLevel;
        String decision;

        if (compositeScore < approveMax) {
            riskLevel = "LOW";
            decision = "APPROVE";
        } else if (compositeScore < reviewMax) {
            riskLevel = "MEDIUM";
            decision = "REVIEW";
        } else if (compositeScore < 0.90) {
            riskLevel = "HIGH";
            decision = "BLOCK";
        } else {
            riskLevel = "CRITICAL";
            decision = "BLOCK";
        }

        // Deduplicate and combine explainability factors
        Map<String, FactorImpactDto> combinedMap = new LinkedHashMap<>();
        if (ruleResult.getTriggeredFactors() != null) {
            for (FactorImpactDto f : ruleResult.getTriggeredFactors()) {
                combinedMap.put(f.getFeature(), f);
            }
        }
        if (mlResult.getFactors() != null) {
            for (FactorImpactDto f : mlResult.getFactors()) {
                combinedMap.putIfAbsent(f.getFeature(), f);
            }
        }

        List<FactorImpactDto> factors = new ArrayList<>(combinedMap.values());
        factors.sort((a, b) -> Double.compare(b.getImpactPercentage(), a.getImpactPercentage()));

        return RiskAssessment.builder()
                .ruleScore(ruleResult.getRuleScore())
                .mlScore(mlResult.getMlScore())
                .finalRiskScore(compositeScore)
                .riskLevel(riskLevel)
                .decision(decision)
                .isNewDevice(ruleResult.getIsNewDevice())
                .isNewLocation(ruleResult.getIsNewLocation())
                .geoVelocityKmh(ruleResult.getGeoVelocityKmh())
                .topRiskFactors(factors)
                .latencyMs(totalLatencyMs)
                .build();
    }
}
