package com.pickleball.application.dtos.requests;

import com.pickleball.domain.enums.CheckInMethod;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class CheckInRequest {
    @NotNull
    private Long userId;

    @NotNull
    private CheckInMethod checkInMethod;

    @NotNull
    @DecimalMin("-90.0")
    @DecimalMax("90.0")
    private BigDecimal latitude;

    @NotNull
    @DecimalMin("-180.0")
    @DecimalMax("180.0")
    private BigDecimal longitude;
}
