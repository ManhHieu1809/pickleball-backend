package com.pickleball.application.dtos.requests;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class CreateRankedPartyRequest {
    @NotNull
    private Long hostUserId;

    @NotNull
    private Double latitude;

    @NotNull
    private Double longitude;

    private LocalDateTime startTime;

    private LocalDateTime endTime;
}
