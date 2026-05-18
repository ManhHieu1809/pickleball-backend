package com.pickleball.application.usecases.booking;

import com.pickleball.domain.entities.RankedMatch;
import com.pickleball.domain.repositories.RankedMatchRepository;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class SubmitMatchResultUseCase {
    private final RankedMatchRepository rankedMatchRepository;

    public void execute(Long bookingId, Long refereeUserId, Integer teamAScore, Integer teamBScore, String evidenceUrl) {
        RankedMatch match = rankedMatchRepository.findByBookingId(bookingId)
                .orElseThrow(() -> new RuntimeException("Ranked match not found for booking ID: " + bookingId));

        processSubmission(match, refereeUserId, teamAScore, teamBScore, evidenceUrl);
    }

    public void executeByMatchId(Long matchId, Long refereeUserId, Integer teamAScore, Integer teamBScore, String evidenceUrl) {
        RankedMatch match = rankedMatchRepository.findById(matchId)
                .orElseThrow(() -> new RuntimeException("Ranked match not found with ID: " + matchId));

        processSubmission(match, refereeUserId, teamAScore, teamBScore, evidenceUrl);
    }

    private void processSubmission(RankedMatch match, Long refereeUserId, Integer teamAScore, Integer teamBScore, String evidenceUrl) {
        if (teamAScore == null || teamBScore == null || teamAScore < 0 || teamBScore < 0) {
            throw new IllegalArgumentException("Invalid scores provided");
        }

        if (teamAScore.equals(teamBScore)) {
            throw new IllegalArgumentException("Draws are not allowed in ranked matches");
        }

        String winningTeam = teamAScore > teamBScore ? "A" : "B";
        match.submitResult(refereeUserId, teamAScore, teamBScore, winningTeam, evidenceUrl);
        rankedMatchRepository.save(match);
    }
}
