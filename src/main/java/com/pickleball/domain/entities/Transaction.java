package com.pickleball.domain.entities;

import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Transaction {
    private Long id;
    private Long userId;
    private Long bookingId;
    private BigDecimal amount;
    private String type; // DEPOSIT, BOOKING_PAYMENT, TOP_UP, WITHDRAWAL, REFUND, PENALTY, PAYOUT
    private String status; // PENDING, SUCCESS, FAILED
    private String description;
    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();
}

