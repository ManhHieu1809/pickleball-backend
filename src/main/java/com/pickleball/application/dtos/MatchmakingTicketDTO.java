package com.pickleball.application.dtos;

import com.pickleball.domain.enums.ParticipantRole;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class MatchmakingTicketDTO {
    private Long id;
    private Long userId;
    private ParticipantRole role;
    private Double latitude;
    private Double longitude;
    private Integer elo;
    private LocalDateTime joinedAt;
    private Boolean isActive;
    private String matchStatus;
    private Long matchedBookingId;
}

