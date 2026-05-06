package com.pickleball.application.dtos.finance;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CourtPerformanceDTO {
    private Long courtId;
    private String courtName;
    private Integer totalBookings;
    private BigDecimal revenue;
    private Double occupancyRate; // 0.0 to 100.0
}
