package com.pickleball.domain.services;

import com.pickleball.domain.entities.Referee;

import java.math.BigDecimal;

public class RefereeMatchService {

    private static final BigDecimal TRUST_PENALTY_OVERTURN = new BigDecimal("15.00");
    private static final BigDecimal TRUST_PENALTY_MISSED_EVIDENCE = new BigDecimal("25.00");
    private static final BigDecimal TRUST_BONUS_MATCH_COMPLETED = new BigDecimal("1.00");
    private static final BigDecimal MIN_TRUST_FOR_ACTIVE = new BigDecimal("30.00");

    public void applyOverturnPenalty(Referee referee) {
        referee.applyPenalty(TRUST_PENALTY_OVERTURN);
    }

    public void applyMissedEvidencePenalty(Referee referee) {
        referee.applyPenalty(TRUST_PENALTY_MISSED_EVIDENCE);
    }

    public void applyMatchCompletionBonus(Referee referee) {
        BigDecimal newTrust = referee.getTrustScore().add(TRUST_BONUS_MATCH_COMPLETED);
        if (newTrust.compareTo(new BigDecimal("100.00")) > 0) {
            newTrust = new BigDecimal("100.00");
        }
        referee.setTrustScore(newTrust);
        referee.incrementMatchCount();
    }

    public boolean isEligibleForMatch(Referee referee) {
        return referee.isEligibleForMatch();
    }

    public boolean shouldBeBanned(Referee referee) {
        return referee.getTrustScore().compareTo(MIN_TRUST_FOR_ACTIVE) < 0;
    }
}
