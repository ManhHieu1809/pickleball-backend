package com.pickleball.application.dtos;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TransactionDTO {
    private Long id;
    private Long userId;
    private Long bookingId;
    private BigDecimal amount;
    private String type; // DEPOSIT, BOOKING_PAYMENT, TOP_UP, WITHDRAWAL, REFUND, PENALTY, PAYOUT
    private String status; // PENDING, SUCCESS, FAILED
    private String description;
    private LocalDateTime createdAt;
}
