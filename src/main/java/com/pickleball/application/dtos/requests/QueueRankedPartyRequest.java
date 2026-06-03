package com.pickleball.application.dtos.requests;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class QueueRankedPartyRequest {
    @NotNull
    private Long userId;

    @NotNull
    private Double latitude;

    @NotNull
    private Double longitude;

    @NotNull
    private LocalDateTime startTime;

    @NotNull
    private LocalDateTime endTime;
}
