package com.pickleball.application.usecases.referee;

import com.pickleball.domain.entities.MatchDispute;
import com.pickleball.domain.entities.RankedMatch;
import com.pickleball.domain.entities.Referee;
import com.pickleball.domain.enums.DisputeDecision;
import com.pickleball.domain.repositories.MatchDisputeRepository;
import com.pickleball.domain.repositories.RankedMatchRepository;
import com.pickleball.domain.repositories.RefereeRepository;
import com.pickleball.domain.services.RefereeMatchService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Scheduled task to auto-resolve disputes when referee misses evidence deadline (24h).
 * Auto-OVERTURN: referee didn't submit evidence → considered wrong.
 */
@Component
public class DisputeDeadlineScheduler {

    private static final Logger log = LoggerFactory.getLogger(DisputeDeadlineScheduler.class);

    private final MatchDisputeRepository matchDisputeRepository;
    private final RankedMatchRepository rankedMatchRepository;
    private final RefereeRepository refereeRepository;
    private final RefereeMatchService refereeMatchService;

    public DisputeDeadlineScheduler(
            MatchDisputeRepository matchDisputeRepository,
            RankedMatchRepository rankedMatchRepository,
            RefereeRepository refereeRepository,
            RefereeMatchService refereeMatchService) {
        this.matchDisputeRepository = matchDisputeRepository;
        this.rankedMatchRepository = rankedMatchRepository;
        this.refereeRepository = refereeRepository;
        this.refereeMatchService = refereeMatchService;
    }

    /**
     * Run every 10 minutes to check for expired evidence deadlines.
     */
    @Scheduled(fixedRate = 600000) // 10 minutes
    @Transactional
    public void checkExpiredDisputes() {
        List<MatchDispute> expiredDisputes = matchDisputeRepository.findExpiredAwaitingEvidence(LocalDateTime.now());

        for (MatchDispute dispute : expiredDisputes) {
            try {
                // Auto-OVERTURN since referee didn't submit evidence
                dispute.resolve(null, "Auto-resolved: Referee failed to submit evidence within 24h deadline",
                        DisputeDecision.OVERTURN);
                matchDisputeRepository.save(dispute);

                // Apply missed evidence penalty to referee
                RankedMatch match = rankedMatchRepository.findById(dispute.getRankedMatchId()).orElse(null);
                if (match != null) {
                    Referee referee = refereeRepository.findByUserId(match.getRefereeId()).orElse(null);
                    if (referee != null) {
                        refereeMatchService.applyMissedEvidencePenalty(referee);
                        refereeRepository.save(referee);
                    }

                    match.resolve();
                    rankedMatchRepository.save(match);
                }

                log.info("Auto-resolved expired dispute id={}, refereeId={}",
                        dispute.getId(), match != null ? match.getRefereeId() : "unknown");

            } catch (Exception e) {
                log.error("Failed to auto-resolve dispute id={}: {}", dispute.getId(), e.getMessage());
            }
        }

        if (!expiredDisputes.isEmpty()) {
            log.info("Processed {} expired disputes", expiredDisputes.size());
        }
    }
}
