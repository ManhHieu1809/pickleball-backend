package com.pickleball.application.dtos.requests;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class JoinBookingRequest {
    @NotNull
    private Long userId;
}