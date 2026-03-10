package com.pickleball.infrastructure.persistence.entities;

import com.pickleball.domain.enums.LoyaltyTier;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Entity
@Table(name = "players")
public class PlayerEntity {
    @Id
    @Column(name = "user_id")
    private Long userId;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", insertable = false, updatable = false)
    private UserEntity user;

    @Column(name = "current_elo")
    private Integer currentElo;

    @Column(name = "rating_mu")
    private Double ratingMu;

    @Column(name = "rating_sigma")
    private Double ratingSigma;

    @Column(name = "loyalty_points")
    private Integer loyaltyPoints;

    @Enumerated(EnumType.STRING)
    @Column(name = "loyalty_tier")
    private LoyaltyTier loyaltyTier;

    @Column(name = "last_latitude")
    private Double lastLatitude;

    @Column(name = "last_longitude")
    private Double lastLongitude;

    @Column(name = "location_updated_at")
    private LocalDateTime locationUpdatedAt;
}