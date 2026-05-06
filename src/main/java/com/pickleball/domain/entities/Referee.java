package com.pickleball.domain.entities;

import com.pickleball.domain.enums.RefereeType;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Referee {
    private Long userId;
    @Builder.Default
    private Boolean testPassed = false;
    private BigDecimal testScore;
    private RefereeType refereeType;
    private Long worksAtVenueId;
    private Long approvedByAdminId;
    private LocalDateTime approvedAt;
    @Builder.Default
    private Boolean isActive = true;
    @Builder.Default
    private Boolean isReady = false;
    @Builder.Default
    private BigDecimal trustScore = new BigDecimal("100.00");
    @Builder.Default
    private Integer totalMatchesRefereed = 0;

    public void activate(Long adminId) {
        this.isActive = true;
        this.approvedByAdminId = adminId;
        this.approvedAt = LocalDateTime.now();
    }

    public void deactivate() {
        this.isActive = false;
        this.isReady = false;
    }

    public void toggleReady(boolean ready) {
        if (ready && !isActive) {
            throw new IllegalStateException("Active flags must be true to toggle ready");
        }
        this.isReady = ready;
    }

    public boolean isEligibleForMatch() {
        return this.isActive && this.trustScore.compareTo(new BigDecimal("30.00")) >= 0;
    }

    public void incrementMatchCount() {
        this.totalMatchesRefereed++;
    }

    public void incrementTrustScore() {
        this.trustScore = this.trustScore.add(BigDecimal.ONE);
        if (this.trustScore.compareTo(new BigDecimal("100.00")) > 0) {
            this.trustScore = new BigDecimal("100.00");
        }
    }

    public void decrementTrustScore(BigDecimal penalty) {
        this.trustScore = this.trustScore.subtract(penalty);
        if (this.trustScore.compareTo(BigDecimal.ZERO) < 0) {
            this.trustScore = BigDecimal.ZERO;
        }
        if (this.trustScore.compareTo(new BigDecimal("30.00")) < 0) {
            this.isActive = false;
        }
    }

    public void applyPenalty(BigDecimal penaltyAmount) {
        decrementTrustScore(penaltyAmount);
    }
}
