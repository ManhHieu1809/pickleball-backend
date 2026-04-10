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
@Table(name = "elo_history")
public class EloHistoryEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id")
    private Long userId;

    @Column(name = "ranked_match_id")
    private Long rankedMatchId;

    @Column(name = "season_id")
    private Long seasonId;

    @Column(name = "elo_before")
    private Integer eloBefore;

    @Column(name = "elo_change")
    private Integer eloChange;

    @Column(name = "elo_after")
    private Integer eloAfter;

    @Column(name = "created_at", insertable = false, updatable = false)
    private LocalDateTime createdAt;
}

