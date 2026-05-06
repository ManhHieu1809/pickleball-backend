package com.pickleball.application.dtos.requests;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WithdrawRequest {
    @NotNull(message = "Số tiền không được để trống")
    @Min(value = 1000, message = "Số tiền rút tối thiểu là 1,000")
    private BigDecimal amount;

    private String description;
}

