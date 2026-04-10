package com.pickleball.domain.services;

import com.pickleball.domain.entities.Player;
import lombok.Builder;
import lombok.Getter;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class EloCalculationService {

    private static final int K_FACTOR = 32;

    @Getter
    @Builder
    public static class EloChangeResult {
        private int eloBefore;
        private int eloChange;
        private int eloAfter;
    }

    /**
     * Calculates Elo changes for a 2v2 match.
     */
    public Map<Long, EloChangeResult> calculateEloChanges(
            List<Player> teamAPlayers,
            List<Player> teamBPlayers,
            String winningTeam) {

        double teamAAvgElo = teamAPlayers.stream()
                .mapToInt(Player::getCurrentElo)
                .average()
                .orElse(1000.0);

        double teamBAvgElo = teamBPlayers.stream()
                .mapToInt(Player::getCurrentElo)
                .average()
                .orElse(1000.0);

        // Expected_A = 1 / (1 + 10^((Rating_B - Rating_A) / 400))
        double expectedScoreA = 1.0 / (1.0 + Math.pow(10.0, (teamBAvgElo - teamAAvgElo) / 400.0));
        double expectedScoreB = 1.0 - expectedScoreA;

        double actualScoreA = "A".equals(winningTeam) ? 1.0 : 0.0;
        double actualScoreB = "B".equals(winningTeam) ? 1.0 : 0.0;

        Map<Long, EloChangeResult> results = new HashMap<>();

        // Calculate change for Team A players
        for (Player p : teamAPlayers) {
            int change = (int) Math.round(K_FACTOR * (actualScoreA - expectedScoreA));
            results.put(p.getUserId(), EloChangeResult.builder()
                    .eloBefore(p.getCurrentElo())
                    .eloChange(change)
                    .eloAfter(p.getCurrentElo() + change)
                    .build());
        }

        // Calculate change for Team B players
        for (Player p : teamBPlayers) {
            int change = (int) Math.round(K_FACTOR * (actualScoreB - expectedScoreB));
            results.put(p.getUserId(), EloChangeResult.builder()
                    .eloBefore(p.getCurrentElo())
                    .eloChange(change)
                    .eloAfter(p.getCurrentElo() + change)
                    .build());
        }

        return results;
    }
}
