package com.pickleball.application.dtos;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PlayerRankedStatsDTO {
    private int totalRankedMatches;
    private int wins;
    private int losses;
    private double winRate;
    private double lossRate;
}

