package com.pickleball.application.dtos;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
public class AdminBookingDTO {
    private Long id;
    private Long courtId;
    private String courtName;
    private Long venueId;
    private String venueName;
    private String bookingType;
    private String status;
    private BigDecimal totalCost;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private LocalDateTime createdAt;
    private Long createdByPlayerId;
    private String creatorName;

    private BigDecimal venueFee;
    private BigDecimal refereeFee;
    private BigDecimal platformFee;

    private String paymentStatus;
    private String paymentMethod;
}
