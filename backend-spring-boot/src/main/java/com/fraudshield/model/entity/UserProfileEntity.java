package com.fraudshield.model.entity;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "user_profiles")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserProfileEntity {

    @Id
    @Column(name = "user_id", length = 64)
    private String userId;

    @Column(name = "avg_amount", precision = 12, scale = 2)
    @Builder.Default
    private BigDecimal avgAmount = BigDecimal.valueOf(2500.00);

    @Column(name = "std_dev_amount", precision = 12, scale = 2)
    @Builder.Default
    private BigDecimal stdDevAmount = BigDecimal.valueOf(1200.00);

    @Column(name = "primary_device_id", length = 64)
    private String primaryDeviceId;

    @Column(name = "primary_location", length = 128)
    private String primaryLocation;

    @Column(name = "primary_latitude", precision = 9, scale = 6)
    private Double primaryLatitude;

    @Column(name = "primary_longitude", precision = 9, scale = 6)
    private Double primaryLongitude;

    @Column(name = "account_age_days")
    @Builder.Default
    private Integer accountAgeDays = 180;

    @Column(name = "total_transactions")
    @Builder.Default
    private Integer totalTransactions = 0;

    @Column(name = "last_txn_timestamp")
    private LocalDateTime lastTxnTimestamp;

    @Column(name = "last_txn_latitude", precision = 9, scale = 6)
    private Double lastTxnLatitude;

    @Column(name = "last_txn_longitude", precision = 9, scale = 6)
    private Double lastTxnLongitude;

    @Column(name = "updated_at")
    @Builder.Default
    private LocalDateTime updatedAt = LocalDateTime.now();
}
