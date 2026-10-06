package com.fraudshield.model.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TransactionRequest {

    private String transactionId;

    @NotBlank(message = "userId is required")
    private String userId;

    @NotNull(message = "amount is required")
    @DecimalMin(value = "0.01", message = "Amount must be greater than zero")
    private BigDecimal amount;

    @NotBlank(message = "location is required")
    private String location;

    private Double latitude;
    private Double longitude;

    @NotBlank(message = "deviceId is required")
    private String deviceId;

    @NotBlank(message = "merchantCategory is required")
    private String merchantCategory;

    private String timestamp; // ISO-8601 or HH:mm
}
