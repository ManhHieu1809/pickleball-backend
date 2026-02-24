package com.pickleball.infrastructure.persistence.mappers;

import com.pickleball.domain.entities.Player;
import com.pickleball.infrastructure.persistence.entities.PlayerEntity;
import org.springframework.stereotype.Component;

@Component
public class PlayerMapper {
    public PlayerEntity toEntity(Player domainPlayer){
        if(domainPlayer == null){
            return null;
        }

        PlayerEntity entity = new PlayerEntity();
        entity.setUserId(domainPlayer.getUserId());
        entity.setCurrentElo(domainPlayer.getCurrentElo());
        entity.setRatingMu(domainPlayer.getRatingMu());
        entity.setRatingSigma(domainPlayer.getRatingSigma());
        entity.setLoyaltyPoints(domainPlayer.getLoyaltyPoints());
        entity.setLoyaltyTier(domainPlayer.getLoyaltyTier());
        return entity;
    }

    public Player toDomain(PlayerEntity entity){
        if(entity == null){
            return null;
        }

        return Player.builder()
                .userId(entity.getUserId())
                .currentElo(entity.getCurrentElo())
                .ratingMu(entity.getRatingMu())
                .ratingSigma(entity.getRatingSigma())
                .loyaltyPoints(entity.getLoyaltyPoints())
                .loyaltyTier(entity.getLoyaltyTier())
                .build();
    }
}
