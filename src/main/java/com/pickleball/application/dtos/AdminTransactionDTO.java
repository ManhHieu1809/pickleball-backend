package com.pickleball.application.dtos;

import lombok.Builder;
import lombok.Data;
import java.math.BigDecimal;

@Data
@Builder
public class AdminTransactionDTO {
    private Long id;
    private Long userId;
    private String userName;
    private Long bookingId;
    private BigDecimal amount;
    private String type;
    private String status;
    private String paymentMethod;
    private String transactionCode;
    private String description;
    private String metadata;
    private String createdAt;
}

