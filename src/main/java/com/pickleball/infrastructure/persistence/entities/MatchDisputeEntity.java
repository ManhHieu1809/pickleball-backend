package com.pickleball.infrastructure.persistence.entities;

import com.pickleball.domain.enums.DisputeDecision;
import com.pickleball.domain.enums.DisputeStatus;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Entity
@Table(name = "match_disputes")
public class MatchDisputeEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "ranked_match_id", nullable = false)
    private Long rankedMatchId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ranked_match_id", insertable = false, updatable = false)
    private RankedMatchEntity rankedMatch;

    @Column(name = "reporting_player_id", nullable = false)
    private Long reportingPlayerId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reporting_player_id", insertable = false, updatable = false)
    private UserEntity reportingPlayer;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String reason;

    @Column(columnDefinition = "JSON")
    private String evidence;

    @Column(name = "referee_evidence_url")
    private String refereeEvidenceUrl;

    @Column(name = "referee_response", columnDefinition = "TEXT")
    private String refereeResponse;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private DisputeStatus status = DisputeStatus.OPEN;

    @Column(name = "evidence_deadline")
    private LocalDateTime evidenceDeadline;

    @Column(name = "resolved_by_admin_id")
    private Long resolvedByAdminId;

    @Column(name = "admin_decision", columnDefinition = "TEXT")
    private String adminDecision;

    @Enumerated(EnumType.STRING)
    @Column(name = "decision_type")
    private DisputeDecision decisionType;

    @Column(name = "resolved_at")
    private LocalDateTime resolvedAt;
}
