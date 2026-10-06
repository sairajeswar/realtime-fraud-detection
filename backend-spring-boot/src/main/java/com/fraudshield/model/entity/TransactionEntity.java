package com.fraudshield.model.entity;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "transactions")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TransactionEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "transaction_id", unique = true, nullable = false, length = 64)
    private String transactionId;

    @Column(name = "user_id", nullable = false, length = 64)
    private String userId;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal amount;

    @Column(nullable = false, length = 128)
    private String location;

    @Column(precision = 9, scale = 6)
    private Double latitude;

    @Column(precision = 9, scale = 6)
    private Double longitude;

    @Column(name = "device_id", nullable = false, length = 64)
    private String deviceId;

    @Column(name = "merchant_category", nullable = false, length = 64)
    private String merchantCategory;

    @Column(name = "is_new_device")
    private Boolean isNewDevice;

    @Column(name = "is_new_location")
    private Boolean isNewLocation;

    @Column(name = "geo_velocity_kmh", precision = 8, scale = 2)
    private Double geoVelocityKmh;

    @Column(name = "rule_score", precision = 5, scale = 4, nullable = false)
    private Double ruleScore;

    @Column(name = "ml_score", precision = 5, scale = 4, nullable = false)
    private Double mlScore;

    @Column(name = "final_risk_score", precision = 5, scale = 4, nullable = false)
    private Double finalRiskScore;

    @Column(name = "risk_level", nullable = false, length = 20)
    private String riskLevel; // LOW, MEDIUM, HIGH, CRITICAL

    @Column(nullable = false, length = 20)
    private String decision; // APPROVE, REVIEW, BLOCK

    @Column(name = "explanation_json", columnDefinition = "TEXT")
    private String explanationJson;

    @Column(name = "processing_latency_ms", precision = 6, scale = 2)
    private Double processingLatencyMs;

    @Column(name = "created_at")
    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();
}
