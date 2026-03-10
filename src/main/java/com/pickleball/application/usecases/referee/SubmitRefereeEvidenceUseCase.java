package com.pickleball.application.usecases.referee;

import com.pickleball.domain.entities.MatchDispute;
import com.pickleball.domain.entities.RankedMatch;
import com.pickleball.domain.repositories.MatchDisputeRepository;
import com.pickleball.domain.repositories.RankedMatchRepository;
import org.springframework.transaction.annotation.Transactional;

/**
 * Referee submits evidence for a dispute (within 24h deadline).
 */
public class SubmitRefereeEvidenceUseCase {

    private final MatchDisputeRepository matchDisputeRepository;
    private final RankedMatchRepository rankedMatchRepository;

    public SubmitRefereeEvidenceUseCase(
            MatchDisputeRepository matchDisputeRepository,
            RankedMatchRepository rankedMatchRepository) {
        this.matchDisputeRepository = matchDisputeRepository;
        this.rankedMatchRepository = rankedMatchRepository;
    }

    @Transactional(rollbackFor = Exception.class)
    public MatchDispute execute(Long disputeId, Long refereeUserId, String evidenceUrl, String response) {
        MatchDispute dispute = matchDisputeRepository.findById(disputeId)
                .orElseThrow(() -> new IllegalArgumentException("Dispute not found with id: " + disputeId));

        // Verify the referee is the one assigned to the match
        RankedMatch match = rankedMatchRepository.findById(dispute.getRankedMatchId())
                .orElseThrow(() -> new IllegalStateException("Ranked match not found"));

        if (!match.getRefereeId().equals(refereeUserId)) {
            throw new IllegalStateException("Only the assigned referee can submit evidence for this dispute");
        }

        // Submit evidence (validates deadline and status internally)
        dispute.submitRefereeEvidence(evidenceUrl, response);

        return matchDisputeRepository.save(dispute);
    }
}
