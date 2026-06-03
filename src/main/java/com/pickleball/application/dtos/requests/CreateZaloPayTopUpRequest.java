package com.pickleball.application.dtos.requests;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class CreateZaloPayTopUpRequest {
    @NotNull(message = "Amount is required")
    @DecimalMin(value = "10000", message = "Minimum top-up amount is 10,000 VND")
    private BigDecimal amount;
    private String description;
}
