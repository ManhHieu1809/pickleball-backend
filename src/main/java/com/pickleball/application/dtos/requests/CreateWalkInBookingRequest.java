package com.pickleball.application.dtos.requests;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class CreateWalkInBookingRequest {
    @NotNull(message = "Court ID không được để trống")
    private Long courtId;

    @NotNull(message = "Thời gian bắt đầu không được để trống")
    private LocalDateTime startTime;

    @NotNull(message = "Thời gian kết thúc không được để trống")
    private LocalDateTime endTime;

    private String customerName;

    private String customerPhone;

    private String paymentMethod;  // CASH, BANK_TRANSFER, MOMO, ZALOPAY

    private String notes;
}
