package com.pickleball.application.dtos;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
public class ZaloPayPaymentDTO {
    private String appTransId;
    private String status;
    private BigDecimal amount;
    private String paymentUrl;
    private String orderToken;
    private String zpTransToken;
    private String qrCode;
    private String message;
    private Long transactionId;
    private Long userId;
    private String providerTransactionId;
    private LocalDateTime createdAt;
}
