package com.pickleball.application.usecases.referee;

import com.pickleball.domain.entities.RankedMatch;
import com.pickleball.domain.enums.MatchStatus;
import com.pickleball.domain.repositories.RankedMatchRepository;
import org.springframework.transaction.annotation.Transactional;

/**
 * Only the assigned referee can submit match results.
 */
public class SubmitMatchResultUseCase {

    private final RankedMatchRepository rankedMatchRepository;

    public SubmitMatchResultUseCase(RankedMatchRepository rankedMatchRepository) {
        this.rankedMatchRepository = rankedMatchRepository;
    }

    @Transactional(rollbackFor = Exception.class)
    public RankedMatch execute(Long matchId, Long refereeUserId, int teamAScore, int teamBScore, String winningTeam) {
        RankedMatch match = rankedMatchRepository.findById(matchId)
                .orElseThrow(() -> new IllegalArgumentException("Ranked match not found with id: " + matchId));

        if (!match.getRefereeId().equals(refereeUserId)) {
            throw new IllegalStateException("Only the assigned referee can submit results for this match");
        }

        if (match.getStatus() != MatchStatus.PENDING) {
            throw new IllegalStateException("Match result has already been submitted");
        }

        // Validate winning team
        if (!"A".equals(winningTeam) && !"B".equals(winningTeam)) {
            throw new IllegalArgumentException("Winning team must be 'A' or 'B'");
        }

        // Validate scores match winning team
        if ("A".equals(winningTeam) && teamAScore <= teamBScore) {
            throw new IllegalArgumentException("Team A score must be higher than Team B if Team A wins");
        }
        if ("B".equals(winningTeam) && teamBScore <= teamAScore) {
            throw new IllegalArgumentException("Team B score must be higher than Team A if Team B wins");
        }

        match.submitResult(refereeUserId, teamAScore, teamBScore, winningTeam);
        return rankedMatchRepository.save(match);
    }
}
