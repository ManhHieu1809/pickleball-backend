package com.pickleball.application.dtos;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class PlayerCareerStatsDTO {
    private Long userId;
    private int totalMvp;
    private String highestRank;
    private int totalTournaments;
    private int currentWinStreak;
    private int totalWins;
    private int totalMatches;
}
