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
    private String type;
    private String status;
    private String paymentMethod;
    private String transactionCode;
    private String description;
    private String metadata;
    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();
}

