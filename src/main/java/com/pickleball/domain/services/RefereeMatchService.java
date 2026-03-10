package com.pickleball.domain.services;

import com.pickleball.domain.entities.Referee;

import java.math.BigDecimal;

/**
 * Domain service for referee match-related business logic.
 * Handles trust score calculation, eligibility checks, and penalty application.
 */
public class RefereeMatchService {

    private static final BigDecimal TRUST_PENALTY_OVERTURN = new BigDecimal("15.00");
    private static final BigDecimal TRUST_PENALTY_MISSED_EVIDENCE = new BigDecimal("25.00");
    private static final BigDecimal TRUST_BONUS_MATCH_COMPLETED = new BigDecimal("1.00");
    private static final BigDecimal MIN_TRUST_FOR_ACTIVE = new BigDecimal("30.00");

    /**
     * Apply penalty when admin overturns referee's result (referee was wrong)
     */
    public void applyOverturnPenalty(Referee referee) {
        referee.applyPenalty(TRUST_PENALTY_OVERTURN);
    }

    /**
     * Apply penalty when referee misses evidence deadline
     */
    public void applyMissedEvidencePenalty(Referee referee) {
        referee.applyPenalty(TRUST_PENALTY_MISSED_EVIDENCE);
    }

    /**
     * Apply bonus after successful match completion
     */
    public void applyMatchCompletionBonus(Referee referee) {
        BigDecimal newTrust = referee.getTrustScore().add(TRUST_BONUS_MATCH_COMPLETED);
        if (newTrust.compareTo(new BigDecimal("100.00")) > 0) {
            newTrust = new BigDecimal("100.00");
        }
        referee.setTrustScore(newTrust);
        referee.incrementMatchCount();
    }

    /**
     * Check if referee is eligible to be assigned to a match
     */
    public boolean isEligibleForMatch(Referee referee) {
        return referee.isEligibleForMatch();
    }

    /**
     * Check if referee should be auto-banned due to low trust score
     */
    public boolean shouldBeBanned(Referee referee) {
        return referee.getTrustScore().compareTo(MIN_TRUST_FOR_ACTIVE) < 0;
    }
}
