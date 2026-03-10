package com.pickleball.application.usecases.referee;

import com.pickleball.domain.entities.MatchDispute;
import com.pickleball.domain.entities.RankedMatch;
import com.pickleball.domain.entities.Referee;
import com.pickleball.domain.enums.DisputeDecision;
import com.pickleball.domain.repositories.MatchDisputeRepository;
import com.pickleball.domain.repositories.RankedMatchRepository;
import com.pickleball.domain.repositories.RefereeRepository;
import com.pickleball.domain.services.RefereeMatchService;
import org.springframework.transaction.annotation.Transactional;

/**
 * Admin resolves a dispute: UPHOLD (referee was right) or OVERTURN (referee was wrong).
 * If OVERTURN: referee gets trust score penalty, may be auto-banned.
 */
public class ResolveDisputeUseCase {

    private final MatchDisputeRepository matchDisputeRepository;
    private final RankedMatchRepository rankedMatchRepository;
    private final RefereeRepository refereeRepository;
    private final RefereeMatchService refereeMatchService;

    public ResolveDisputeUseCase(
            MatchDisputeRepository matchDisputeRepository,
            RankedMatchRepository rankedMatchRepository,
            RefereeRepository refereeRepository,
            RefereeMatchService refereeMatchService) {
        this.matchDisputeRepository = matchDisputeRepository;
        this.rankedMatchRepository = rankedMatchRepository;
        this.refereeRepository = refereeRepository;
        this.refereeMatchService = refereeMatchService;
    }

    @Transactional(rollbackFor = Exception.class)
    public MatchDispute execute(Long disputeId, Long adminId, String decision, DisputeDecision decisionType) {
        MatchDispute dispute = matchDisputeRepository.findById(disputeId)
                .orElseThrow(() -> new IllegalArgumentException("Dispute not found with id: " + disputeId));

        // Resolve dispute
        dispute.resolve(adminId, decision, decisionType);
        MatchDispute savedDispute = matchDisputeRepository.save(dispute);

        // Get the ranked match
        RankedMatch match = rankedMatchRepository.findById(dispute.getRankedMatchId())
                .orElseThrow(() -> new IllegalStateException("Ranked match not found"));

        // Get the referee
        Referee referee = refereeRepository.findByUserId(match.getRefereeId())
                .orElseThrow(() -> new IllegalStateException("Referee not found"));

        if (decisionType == DisputeDecision.OVERTURN) {
            // Referee was wrong - apply penalty
            refereeMatchService.applyOverturnPenalty(referee);
            refereeRepository.save(referee);

            // TODO: Reverse match result, recalculate Elo, etc.
        } else {
            // UPHOLD - referee was correct, match result stands
            refereeMatchService.applyMatchCompletionBonus(referee);
            refereeRepository.save(referee);
        }

        // Resolve the ranked match
        match.resolve();
        rankedMatchRepository.save(match);

        return savedDispute;
    }
}
