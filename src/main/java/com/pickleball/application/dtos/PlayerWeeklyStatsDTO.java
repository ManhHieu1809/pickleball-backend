package com.pickleball.application.dtos;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Map;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PlayerWeeklyStatsDTO {
    private int totalMatchesThisWeek;
    private double percentageChange;
    private Map<String, Integer> matchesPerDay;
}

