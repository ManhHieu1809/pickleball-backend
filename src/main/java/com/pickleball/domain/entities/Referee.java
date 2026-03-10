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
    }

    public void incrementMatchCount() {
        this.totalMatchesRefereed++;
    }

    public void applyPenalty(BigDecimal penaltyAmount) {
        this.trustScore = this.trustScore.subtract(penaltyAmount);
        if (this.trustScore.compareTo(BigDecimal.ZERO) < 0) {
            this.trustScore = BigDecimal.ZERO;
        }
        // Auto-ban if trust score drops below threshold
        if (this.trustScore.compareTo(new BigDecimal("30.00")) < 0) {
            this.deactivate();
        }
    }

    public boolean isEligibleForMatch() {
        return Boolean.TRUE.equals(testPassed)
                && Boolean.TRUE.equals(isActive)
                && trustScore.compareTo(new BigDecimal("30.00")) >= 0;
    }
}
