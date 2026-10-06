package com.fraudshield.model.dto;

import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TransactionResponse {

    private String transactionId;
    private String userId;
    private BigDecimal amount;
    private String location;
    private String deviceId;
    private String merchantCategory;
    
    private Double ruleScore;
    private Double mlScore;
    private Double finalRiskScore;
    private String riskLevel;
    private String decision; // APPROVE, REVIEW, BLOCK
    
    private Boolean isNewDevice;
    private Boolean isNewLocation;
    private Double geoVelocityKmh;
    
    private List<FactorImpactDto> riskFactors;
    private Double processingLatencyMs;
    private LocalDateTime timestamp;
}
