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
public class TopCustomerDTO {
    private Long userId;
    private String userName;
    private String avatarUrl; // Assuming we have some initials or logic in frontend
    private Integer totalBookings;
    private BigDecimal totalSpent;
}
