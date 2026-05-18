package com.pickleball.domain.services;

import com.pickleball.domain.entities.Player;
import lombok.Builder;
import lombok.Getter;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class EloCalculationService {

    private static final double BASE_K = 24.0;
    private static final double DEFAULT_MU = 25.0;
    private static final double DEFAULT_SIGMA = 8.333;
    private static final double MIN_SIGMA = 1.5;
    private static final double ELO_SCALE = 20.0;
    private static final int DEFAULT_ELO = 1000;
    private static final int PROVISIONAL_MATCH_LIMIT = 10;

    @Getter
    @Builder
    public static class EloChangeResult {
        private int eloBefore;
        private int eloChange;
        private int eloAfter;
        private double muBefore;
        private double muAfter;
        private double sigmaBefore;
        private double sigmaAfter;
        private double expectedScore;
        private double effectiveK;
    }

    public Map<Long, EloChangeResult> calculateEloChanges(
            List<Player> teamAPlayers,
            List<Player> teamBPlayers,
            String winningTeam) {
        return calculateEloChanges(
                teamAPlayers,
                teamBPlayers,
                winningTeam,
                Collections.emptyMap(),
                Collections.emptyMap());
    }

    public Map<Long, EloChangeResult> calculateEloChanges(
            List<Player> teamAPlayers,
            List<Player> teamBPlayers,
            String winningTeam,
            Map<Long, Integer> rankedMatchesPlayed,
            Map<Long, Integer> recentRepeatCounts) {

        TeamRating teamA = calculateTeamRating(teamAPlayers);
        TeamRating teamB = calculateTeamRating(teamBPlayers);

        double expectedScoreA = calculateExpectedScore(teamA, teamB);
        double expectedScoreB = 1.0 - expectedScoreA;

        double actualScoreA = "A".equals(winningTeam) ? 1.0 : 0.0;
        double actualScoreB = "B".equals(winningTeam) ? 1.0 : 0.0;
        double teamRatingDiff = Math.abs(teamA.conservativeElo() - teamB.conservativeElo());

        Map<Long, EloChangeResult> results = new HashMap<>();

        applyTeamChanges(teamAPlayers, actualScoreA, expectedScoreA, teamRatingDiff,
                rankedMatchesPlayed, recentRepeatCounts, results);
        applyTeamChanges(teamBPlayers, actualScoreB, expectedScoreB, teamRatingDiff,
                rankedMatchesPlayed, recentRepeatCounts, results);

        return results;
    }

    public int toConservativeElo(Player player) {
        double conservativeSkill = safeMu(player) - (3.0 * safeSigma(player));
        return Math.max(0, (int) Math.round(DEFAULT_ELO + conservativeSkill * ELO_SCALE));
    }

    private TeamRating calculateTeamRating(List<Player> players) {
        double averageConservativeElo = players.stream()
                .mapToInt(this::toConservativeElo)
                .average()
                .orElse(DEFAULT_ELO);

        double sigmaSumSquares = players.stream()
                .mapToDouble(p -> safeSigma(p) * safeSigma(p))
                .sum();

        return new TeamRating(averageConservativeElo, Math.sqrt(sigmaSumSquares));
    }

    private double calculateExpectedScore(TeamRating teamA, TeamRating teamB) {
        double uncertaintyScale = (teamA.totalSigma() + teamB.totalSigma()) * ELO_SCALE / 2.0;
        double denominator = 400.0 + uncertaintyScale;
        return 1.0 / (1.0 + Math.pow(10.0,
                (teamB.conservativeElo() - teamA.conservativeElo()) / denominator));
    }

    private void applyTeamChanges(
            List<Player> players,
            double actualScore,
            double expectedScore,
            double teamRatingDiff,
            Map<Long, Integer> rankedMatchesPlayed,
            Map<Long, Integer> recentRepeatCounts,
            Map<Long, EloChangeResult> results) {

        for (Player player : players) {
            int matchesPlayed = rankedMatchesPlayed.getOrDefault(player.getUserId(), 0);
            int repeatCount = recentRepeatCounts.getOrDefault(player.getUserId(), 0);
            double effectiveK = calculateEffectiveK(player, matchesPlayed, teamRatingDiff, repeatCount);
            double rawChange = effectiveK * (actualScore - expectedScore);
            int eloChange = normalizeEloChange(rawChange, matchesPlayed, repeatCount);

            double sigmaBefore = safeSigma(player);
            double sigmaAfter = calculateSigmaAfter(sigmaBefore, teamRatingDiff, repeatCount);
            double muBefore = safeMu(player);
            double muAfter = muBefore + (eloChange / ELO_SCALE);

            int eloBefore = safeElo(player);
            int eloAfter = Math.max(0, eloBefore + eloChange);

            results.put(player.getUserId(), EloChangeResult.builder()
                    .eloBefore(eloBefore)
                    .eloChange(eloAfter - eloBefore)
                    .eloAfter(eloAfter)
                    .muBefore(muBefore)
                    .muAfter(muAfter)
                    .sigmaBefore(sigmaBefore)
                    .sigmaAfter(sigmaAfter)
                    .expectedScore(expectedScore)
                    .effectiveK(effectiveK)
                    .build());
        }
    }

    private double calculateEffectiveK(Player player, int matchesPlayed, double teamRatingDiff, int repeatCount) {
        return BASE_K
                * experienceFactor(matchesPlayed)
                * uncertaintyFactor(safeSigma(player))
                * matchQualityFactor(teamRatingDiff)
                * antiFarmFactor(repeatCount);
    }

    private double experienceFactor(int matchesPlayed) {
        if (matchesPlayed <= 10) {
            return 1.50;
        }
        if (matchesPlayed <= 30) {
            return 1.20;
        }
        if (matchesPlayed <= 100) {
            return 1.00;
        }
        return 0.75;
    }

    private double uncertaintyFactor(double sigma) {
        return clamp(sigma / DEFAULT_SIGMA, 0.75, 1.50);
    }

    private double matchQualityFactor(double teamRatingDiff) {
        if (teamRatingDiff <= 150.0) {
            return 1.00;
        }
        if (teamRatingDiff <= 300.0) {
            return 0.75;
        }
        if (teamRatingDiff <= 500.0) {
            return 0.50;
        }
        return 0.25;
    }

    private double antiFarmFactor(int repeatCount) {
        if (repeatCount >= 3) {
            return 0.00;
        }
        if (repeatCount == 2) {
            return 0.50;
        }
        if (repeatCount == 1) {
            return 0.75;
        }
        return 1.00;
    }

    private int normalizeEloChange(double rawChange, int matchesPlayed, int repeatCount) {
        int limit = matchesPlayed <= PROVISIONAL_MATCH_LIMIT ? 40 : 32;
        int change = (int) Math.round(clamp(rawChange, -limit, limit));
        if (repeatCount >= 3) {
            return 0;
        }
        if (change == 0 && rawChange > 0) {
            return 1;
        }
        if (change == 0 && rawChange < 0) {
            return -1;
        }
        return change;
    }

    private double calculateSigmaAfter(double sigmaBefore, double teamRatingDiff, int repeatCount) {
        double confidenceGain = 0.035
                * matchQualityFactor(teamRatingDiff)
                * uncertaintyFactor(sigmaBefore)
                * antiFarmFactor(repeatCount);
        return Math.max(MIN_SIGMA, sigmaBefore * (1.0 - confidenceGain));
    }

    private double safeMu(Player player) {
        return player.getRatingMu() != null ? player.getRatingMu() : DEFAULT_MU;
    }

    private double safeSigma(Player player) {
        return player.getRatingSigma() != null ? player.getRatingSigma() : DEFAULT_SIGMA;
    }

    private int safeElo(Player player) {
        return player.getCurrentElo() != null ? player.getCurrentElo() : DEFAULT_ELO;
    }

    private double clamp(double value, double min, double max) {
        return Math.max(min, Math.min(max, value));
    }

    private record TeamRating(double conservativeElo, double totalSigma) {
    }
}
