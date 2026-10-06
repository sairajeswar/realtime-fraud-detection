package com.fraudshield.model.dto;

import lombok.*;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RiskAssessment {

    private Double ruleScore;
    private Double mlScore;
    private Double finalRiskScore;
    private String riskLevel;   // LOW, MEDIUM, HIGH, CRITICAL
    private String decision;    // APPROVE, REVIEW, BLOCK
    private Boolean isNewDevice;
    private Boolean isNewLocation;
    private Double geoVelocityKmh;
    private List<FactorImpactDto> topRiskFactors;
    private Double latencyMs;
}
