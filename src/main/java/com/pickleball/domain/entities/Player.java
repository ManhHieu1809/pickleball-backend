package com.pickleball.domain.entities;

import com.pickleball.domain.enums.LoyaltyTier;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

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

    private Double lastLatitude;
    private Double lastLongitude;
    private LocalDateTime locationUpdatedAt;



    public void updateElo(Integer newElo) {
        if (newElo != null && newElo >= 0) {
            this.currentElo = newElo;
        }
    }

    public void updateLocation(Double latitude, Double longitude) {
        if (latitude != null && longitude != null) {
            if (latitude < -90 || latitude > 90) {
                throw new IllegalArgumentException("Latitude must be between -90 and 90");
            }
            if (longitude < -180 || longitude > 180) {
                throw new IllegalArgumentException("Longitude must be between -180 and 180");
            }
            this.lastLatitude = latitude;
            this.lastLongitude = longitude;
            this.locationUpdatedAt = LocalDateTime.now();
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