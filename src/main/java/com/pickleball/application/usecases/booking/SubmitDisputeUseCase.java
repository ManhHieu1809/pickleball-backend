package com.pickleball.application.usecases.booking;

import com.pickleball.domain.entities.MatchDispute;
import com.pickleball.domain.entities.RankedMatch;
import com.pickleball.domain.enums.DisputeStatus;
import com.pickleball.domain.enums.MatchStatus;
import com.pickleball.domain.repositories.MatchDisputeRepository;
import com.pickleball.domain.repositories.RankedMatchRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
public class SubmitDisputeUseCase {

    private final RankedMatchRepository rankedMatchRepository;
    private final MatchDisputeRepository matchDisputeRepository;

    @Transactional
    public MatchDispute execute(Long rankedMatchId, Long reportingPlayerId, String reason, String evidence) {
        RankedMatch match = rankedMatchRepository.findById(rankedMatchId)
                .orElseThrow(() -> new IllegalArgumentException("Ranked match not found: " + rankedMatchId));

        if (match.getStatus() != MatchStatus.SUBMITTED && match.getStatus() != MatchStatus.IN_DISPUTE) {
            // Allow dispute only if result is submitted or already in dispute (another player disputed)
            // Actually, if it's already confirmed, it might be too late? 
            // The requirement says "Player creates dispute". Usually before confirmation.
            // If match is CONFIRMED, usually dispute is harder. Let's assume SUBMITTED or IN_DISPUTE.
            throw new IllegalStateException("Cannot dispute match with status: " + match.getStatus());
        }

        match.setStatus(MatchStatus.IN_DISPUTE);
        rankedMatchRepository.save(match);

        MatchDispute dispute = MatchDispute.builder()
                .rankedMatchId(rankedMatchId)
                .reportingPlayerId(reportingPlayerId)
                .reason(reason)
                .evidence(evidence)
                .status(DisputeStatus.OPEN)
                .build();
        
        return matchDisputeRepository.save(dispute);
    }
}

