package com.pickleball.application.dtos;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class CasualMatchDTO {
    private BookingDTO booking;
    private PaymentDTO payment;
    private BigDecimal depositPerPlayer;
    private String depositCurrency;
    private Integer currentPlayerCount;
    private Integer requiredPlayerCount;
    private List<PlayerMatchDTO> candidates;
}
