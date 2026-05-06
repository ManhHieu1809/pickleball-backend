package com.pickleball.application.dtos;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.pickleball.domain.enums.BookingStatus;
import com.pickleball.domain.enums.BookingType;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class BookingDTO {
    private Long id;
    private Long courtId;
    private String courtName;
    private Long venueId;
    private String venueName;

    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private BookingType bookingType;
    private BookingStatus status;

    private Long createdByPlayerId;
    private Long createdByStaffId;

    private String customerName;
    private String customerPhone;
    private String paymentMethod;
    private String notes;

    private BigDecimal venueFee;
    private BigDecimal refereeFee;
    private BigDecimal platformFee;
    private BigDecimal totalCost;

    private LocalDateTime createdAt;

    private PaymentDTO payment;
}