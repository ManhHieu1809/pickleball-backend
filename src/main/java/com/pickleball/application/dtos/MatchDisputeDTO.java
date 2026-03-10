package com.pickleball.application.dtos;

import com.pickleball.domain.enums.DisputeDecision;
import com.pickleball.domain.enums.DisputeStatus;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class MatchDisputeDTO {
    private Long id;
    private Long rankedMatchId;
    private Long reportingPlayerId;
    private String reason;
    private String evidence;
    private String refereeEvidenceUrl;
    private String refereeResponse;
    private DisputeStatus status;
    private LocalDateTime evidenceDeadline;
    private Long resolvedByAdminId;
    private String adminDecision;
    private DisputeDecision decisionType;
    private LocalDateTime resolvedAt;
}
