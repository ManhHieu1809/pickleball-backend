package com.pickleball.domain.entities;

import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class SkillRatingHistory {
    private Long id;
    private Long playerId;
    private Long matchId;
    private Long seasonId;
    private Double muBefore;
    private Double sigmaBefore;
    private Double muAfter;
    private Double sigmaAfter;
    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();
}

