package com.pickleball.infrastructure.persistence.entities;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Entity
@Table(name = "skill_rating_history")
public class SkillRatingHistoryEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "player_id")
    private Long playerId;

    @Column(name = "match_id")
    private Long matchId;

    @Column(name = "season_id")
    private Long seasonId;

    @Column(name = "mu_before")
    private Double muBefore;

    @Column(name = "sigma_before")
    private Double sigmaBefore;

    @Column(name = "mu_after")
    private Double muAfter;

    @Column(name = "sigma_after")
    private Double sigmaAfter;

    @Column(name = "created_at", insertable = false, updatable = false)
    private LocalDateTime createdAt;
}

