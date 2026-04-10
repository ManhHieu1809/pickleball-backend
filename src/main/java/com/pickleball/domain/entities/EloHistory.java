package com.pickleball.domain.entities;

import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class EloHistory {
    private Long id;
    private Long userId;
    private Long rankedMatchId;
    private Long seasonId;
    private Integer eloBefore;
    private Integer eloChange;
    private Integer eloAfter;
    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();
}

