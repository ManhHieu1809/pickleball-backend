package com.pickleball.domain.entities;

import com.pickleball.domain.enums.DisputeDecision;
import com.pickleball.domain.enums.DisputeStatus;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class MatchDispute {
    private Long id;
    private Long rankedMatchId;
    private Long reportingPlayerId;
    private String reason;
    private String evidence;
    private String refereeEvidenceUrl;
    private String refereeResponse;
    @Builder.Default
    private DisputeStatus status = DisputeStatus.OPEN;
    private LocalDateTime evidenceDeadline;
    private Long resolvedByAdminId;
    private String adminDecision;
    private DisputeDecision decisionType;
    private LocalDateTime resolvedAt;

    private static final int EVIDENCE_DEADLINE_HOURS = 24;

    public void open() {
        this.status = DisputeStatus.AWAITING_EVIDENCE;
        this.evidenceDeadline = LocalDateTime.now().plusHours(EVIDENCE_DEADLINE_HOURS);
    }

    public void submitRefereeEvidence(String evidenceUrl, String response) {
        if (this.status != DisputeStatus.AWAITING_EVIDENCE) {
            throw new IllegalStateException("Dispute is not awaiting evidence");
        }
        if (LocalDateTime.now().isAfter(this.evidenceDeadline)) {
            throw new IllegalStateException("Evidence submission deadline has passed");
        }
        this.refereeEvidenceUrl = evidenceUrl;
        this.refereeResponse = response;
        this.status = DisputeStatus.IN_REVIEW;
    }

    public void resolve(Long adminId, String decision, DisputeDecision decisionType) {
        if (this.status != DisputeStatus.IN_REVIEW && this.status != DisputeStatus.AWAITING_EVIDENCE) {
            throw new IllegalStateException("Dispute cannot be resolved in current status: " + this.status);
        }
        this.resolvedByAdminId = adminId;
        this.adminDecision = decision;
        this.decisionType = decisionType;
        this.status = DisputeStatus.RESOLVED;
        this.resolvedAt = LocalDateTime.now();
    }

    public boolean isEvidenceDeadlineExpired() {
        return evidenceDeadline != null && LocalDateTime.now().isAfter(evidenceDeadline);
    }

    public boolean isUphold() {
        return DisputeDecision.UPHOLD.equals(decisionType);
    }

    public boolean isOverturn() {
        return DisputeDecision.OVERTURN.equals(decisionType);
    }
}
