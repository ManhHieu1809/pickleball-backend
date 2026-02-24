package com.pickleball.application.dtos.requests;

import com.pickleball.domain.enums.BookingType;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import java.time.LocalDateTime;

@Data
public class CreateBookingRequest {
    @NotNull
    private Long courtId;

    @NotNull
    @Future
    private LocalDateTime startTime;

    @NotNull
    @Future
    private LocalDateTime endTime;

    @NotNull
    private BookingType bookingType;

    @NotNull
    private Long creatorUserId;


    private Boolean isPlayer = true;

    public boolean isPlayer() {
        return isPlayer != null ? isPlayer : true;
    }
}