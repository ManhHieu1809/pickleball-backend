package com.pickleball.domain.entities;

import com.pickleball.domain.enums.LoyaltyTier;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Player {
    private Long userId;
    @Builder.Default
    private Integer currentElo = 1000;
    @Builder.Default
    private Double ratingMu = 25.0;
    @Builder.Default
    private Double ratingSigma = 8.333;
    @Builder.Default
    private Integer loyaltyPoints = 0;
    @Builder.Default
    private LoyaltyTier loyaltyTier = LoyaltyTier.BRONZE;
    private User user;



    public void updateElo(Integer newElo) {
        if (newElo != null && newElo >= 0) {
            this.currentElo = newElo;
        }
    }

    public void addLoyaltyPoints(Integer points) {
        if (points != null && points > 0) {
            this.loyaltyPoints += points;
            updateLoyaltyTier();
        }
    }

    private void updateLoyaltyTier() {
        if (loyaltyPoints >= 5000) {
            loyaltyTier = LoyaltyTier.PLATINUM;
        } else if (loyaltyPoints >= 2000) {
            loyaltyTier = LoyaltyTier.GOLD;
        } else if (loyaltyPoints >= 500) {
            loyaltyTier = LoyaltyTier.SILVER;
        } else {
            loyaltyTier = LoyaltyTier.BRONZE;
        }
    }


}