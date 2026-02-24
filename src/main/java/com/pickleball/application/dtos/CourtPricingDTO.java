package com.pickleball.application.dtos;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CourtPricingDTO {
    private Long id;
    private Long courtId;
    private LocalTime startTime;
    private LocalTime endTime;
    private BigDecimal pricePerHour;
    private Integer dayOfWeek;
}

