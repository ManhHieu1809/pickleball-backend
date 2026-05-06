package com.pickleball.application.dtos;

import lombok.Builder;
import lombok.Data;
import java.math.BigDecimal;

@Data
@Builder
public class AdminFinanceStatsDTO {
    private BigDecimal totalGMV;
    private BigDecimal totalRevenue;
    private BigDecimal totalRefunds;
    private Long successfulBookingsCount; 
}

