package com.pickleball.domain.services;

import com.pickleball.domain.entities.Player;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class EloCalculationServiceTest {

    private final EloCalculationService service = new EloCalculationService();

    @Test
    void provisionalHighUncertaintyPlayerGetsHigherEffectiveK() {
        Player newPlayer = player(1L, 1000, 25.0, 8.333);
        Player stablePlayer = player(2L, 1000, 25.0, 2.0);
        Player opponentA = player(3L, 1000, 25.0, 8.333);
        Player opponentB = player(4L, 1000, 25.0, 8.333);

        Map<Long, EloCalculationService.EloChangeResult> result = service.calculateEloChanges(
                List.of(newPlayer, stablePlayer),
                List.of(opponentA, opponentB),
                "A",
                Map.of(1L, 0, 2L, 120, 3L, 0, 4L, 0),
                Map.of());

        assertThat(result.get(1L).getEffectiveK()).isGreaterThan(result.get(2L).getEffectiveK());
        assertThat(result.get(1L).getEloChange()).isGreaterThan(result.get(2L).getEloChange());
    }

    @Test
    void favoriteBeatingMuchWeakerTeamGetsSmallGain() {
        Player favoriteA = player(1L, 1600, 45.0, 3.0);
        Player favoriteB = player(2L, 1600, 45.0, 3.0);
        Player weakerA = player(3L, 1000, 25.0, 3.0);
        Player weakerB = player(4L, 1000, 25.0, 3.0);

        Map<Long, EloCalculationService.EloChangeResult> result = service.calculateEloChanges(
                List.of(favoriteA, favoriteB),
                List.of(weakerA, weakerB),
                "A",
                Map.of(1L, 50, 2L, 50, 3L, 50, 4L, 50),
                Map.of());

        assertThat(result.get(1L).getEloChange()).isBetween(1, 4);
        assertThat(result.get(2L).getEloChange()).isBetween(1, 4);
    }

    @Test
    void repeatedRecentGroupCanBeExcludedFromRatingChange() {
        Player playerA = player(1L, 1000, 25.0, 8.333);
        Player playerB = player(2L, 1000, 25.0, 8.333);
        Player playerC = player(3L, 1000, 25.0, 8.333);
        Player playerD = player(4L, 1000, 25.0, 8.333);

        Map<Long, EloCalculationService.EloChangeResult> result = service.calculateEloChanges(
                List.of(playerA, playerB),
                List.of(playerC, playerD),
                "A",
                Map.of(1L, 0, 2L, 0, 3L, 0, 4L, 0),
                Map.of(1L, 3, 2L, 3, 3L, 3, 4L, 3));

        assertThat(result.values()).allMatch(change -> change.getEloChange() == 0);
    }

    private Player player(Long userId, int elo, double mu, double sigma) {
        return Player.builder()
                .userId(userId)
                .currentElo(elo)
                .ratingMu(mu)
                .ratingSigma(sigma)
                .build();
    }
}
