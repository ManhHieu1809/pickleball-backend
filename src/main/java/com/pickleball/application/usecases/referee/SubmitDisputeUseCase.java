package com.pickleball.application.usecases.referee;

import com.pickleball.domain.entities.MatchDispute;
import com.pickleball.domain.entities.RankedMatch;
import com.pickleball.domain.enums.MatchStatus;
import com.pickleball.domain.repositories.BookingRepository;
import com.pickleball.domain.repositories.MatchDisputeRepository;
import com.pickleball.domain.repositories.RankedMatchRepository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Player submits a dispute against a referee's reported result.
 * Sets evidence deadline for referee (24h).
 */
public class SubmitDisputeUseCase {

    private final RankedMatchRepository rankedMatchRepository;
    private final MatchDisputeRepository matchDisputeRepository;
    private final BookingRepository bookingRepository;

    public SubmitDisputeUseCase(
            RankedMatchRepository rankedMatchRepository,
            MatchDisputeRepository matchDisputeRepository,
            BookingRepository bookingRepository) {
        this.rankedMatchRepository = rankedMatchRepository;
        this.matchDisputeRepository = matchDisputeRepository;
        this.bookingRepository = bookingRepository;
    }

    @Transactional(rollbackFor = Exception.class)
    public MatchDispute execute(Long rankedMatchId, Long reportingPlayerId, String reason, String evidence) {
        RankedMatch match = rankedMatchRepository.findById(rankedMatchId)
                .orElseThrow(() -> new IllegalArgumentException("Ranked match not found with id: " + rankedMatchId));

        if (match.getStatus() != MatchStatus.SUBMITTED) {
            throw new IllegalStateException("Can only dispute a match with SUBMITTED status. Current: " + match.getStatus());
        }

        // Verify the player is a participant of this match
        List<Long> participantIds = bookingRepository.findParticipantUserIdsByBookingId(match.getBookingId());
        if (!participantIds.contains(reportingPlayerId)) {
            throw new IllegalStateException("Only match participants can submit disputes");
        }

        // Check if player already disputed this match
        List<MatchDispute> existingDisputes = matchDisputeRepository.findByRankedMatchId(rankedMatchId);
        boolean alreadyDisputed = existingDisputes.stream()
                .anyMatch(d -> d.getReportingPlayerId().equals(reportingPlayerId));
        if (alreadyDisputed) {
            throw new IllegalStateException("You have already submitted a dispute for this match");
        }

        // Create dispute
        MatchDispute dispute = MatchDispute.builder()
                .rankedMatchId(rankedMatchId)
                .reportingPlayerId(reportingPlayerId)
                .reason(reason)
                .evidence(evidence)
                .build();
        dispute.open(); // Sets AWAITING_EVIDENCE + deadline 24h

        MatchDispute savedDispute = matchDisputeRepository.save(dispute);

        // Update match status to IN_DISPUTE
        match.dispute();
        rankedMatchRepository.save(match);

        return savedDispute;
    }
}
