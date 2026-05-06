package com.pickleball.infrastructure.persistence.mappers;

import com.pickleball.domain.entities.MatchmakingTicket;
import com.pickleball.infrastructure.persistence.entities.MatchmakingTicketEntity;
import org.springframework.stereotype.Component;

@Component
public class MatchmakingTicketMapper {

    public MatchmakingTicket toDomain(MatchmakingTicketEntity entity) {
        if (entity == null) {
            return null;
        }

        return MatchmakingTicket.builder()
                .id(entity.getId())
                .userId(entity.getUserId())
                .role(entity.getRole())
                .latitude(entity.getLatitude())
                .longitude(entity.getLongitude())
                .elo(entity.getElo())
                .joinedAt(entity.getJoinedAt())
                .isActive(entity.getIsActive())
                .build();
    }

    public MatchmakingTicketEntity toEntity(MatchmakingTicket domain) {
        if (domain == null) {
            return null;
        }

        return MatchmakingTicketEntity.builder()
                .id(domain.getId())
                .userId(domain.getUserId())
                .role(domain.getRole())
                .latitude(domain.getLatitude())
                .longitude(domain.getLongitude())
                .elo(domain.getElo())
                .joinedAt(domain.getJoinedAt())
                .isActive(domain.getIsActive())
                .build();
    }
}

