package com.fraudshield.model.dto;

import lombok.*;
import java.math.BigDecimal;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DashboardStatsDto {
    private Long totalTransactions;
    private Long approvedCount;
    private Long reviewCount;
    private Long blockedCount;
    private Double fraudRatePercentage;
    private BigDecimal totalVolume;
    private BigDecimal blockedAmount;
    private Double avgLatencyMs;
    private Map<String, Long> decisionBreakdown;
    private Map<String, Long> locationFraudStats;
    private Map<String, Long> categoryRiskStats;
}
