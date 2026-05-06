package com.pickleball.application.dtos.finance;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OwnerFinanceOverviewDTO {
    private BigDecimal grossRevenue;
    private BigDecimal platformFee;
    private BigDecimal netRevenue;
    private Long totalBookings;
    private List<CourtPerformanceDTO> courtPerformances;
    private List<TopCustomerDTO> topCustomers;
}
