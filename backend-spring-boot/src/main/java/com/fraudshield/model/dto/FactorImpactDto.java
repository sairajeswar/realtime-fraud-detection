package com.fraudshield.model.dto;

import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FactorImpactDto {
    private String feature;
    private Double impactPercentage;
    private String description;
    private Object rawValue;
}
