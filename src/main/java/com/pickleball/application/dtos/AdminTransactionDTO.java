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
    private String description;
    private String createdAt;
}

