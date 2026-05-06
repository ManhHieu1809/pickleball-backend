package com.pickleball.domain.entities;

import com.pickleball.domain.enums.ParticipantRole;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MatchmakingTicket {
    private Long id;
    private Long userId;
    private ParticipantRole role;
    private Double latitude;
    private Double longitude;
    private Integer elo;
    private LocalDateTime joinedAt;
    private Boolean isActive;
}

