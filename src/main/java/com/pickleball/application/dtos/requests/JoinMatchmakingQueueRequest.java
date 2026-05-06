package com.pickleball.application.dtos.requests;

import com.pickleball.domain.enums.ParticipantRole;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class JoinMatchmakingQueueRequest {
    @NotNull(message = "User ID is required")
    private Long userId;

    @NotNull(message = "Role is required (PLAYER or REFEREE)")
    private ParticipantRole role;

    @NotNull(message = "Latitude is required")
    private Double latitude;

    @NotNull(message = "Longitude is required")
    private Double longitude;
}

