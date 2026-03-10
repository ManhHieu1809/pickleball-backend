package com.pickleball.domain.entities;

import com.pickleball.domain.enums.MatchStatus;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class RankedMatch {
    private Long id;
    private Long bookingId;
    private Long refereeId;
    private Long seasonId;
    @Builder.Default
    private MatchStatus status = MatchStatus.PENDING;
    private Integer teamAScore;
    private Integer teamBScore;
    private String winningTeam; // 'A' or 'B'
    private LocalDateTime submittedAt;
    private LocalDateTime confirmedAt;

    public void submitResult(Long refereeId, int teamAScore, int teamBScore, String winningTeam) {
        if (!this.refereeId.equals(refereeId)) {
            throw new IllegalStateException("Only the assigned referee can submit results");
        }
        if (this.status != MatchStatus.PENDING) {
            throw new IllegalStateException("Match result can only be submitted when status is PENDING");
        }
        this.teamAScore = teamAScore;
        this.teamBScore = teamBScore;
        this.winningTeam = winningTeam;
        this.status = MatchStatus.SUBMITTED;
        this.submittedAt = LocalDateTime.now();
    }

    public void confirm() {
        if (this.status != MatchStatus.SUBMITTED) {
            throw new IllegalStateException("Match can only be confirmed when status is SUBMITTED");
        }
        this.status = MatchStatus.CONFIRMED;
        this.confirmedAt = LocalDateTime.now();
    }

    public void dispute() {
        if (this.status != MatchStatus.SUBMITTED) {
            throw new IllegalStateException("Match can only be disputed when status is SUBMITTED");
        }
        this.status = MatchStatus.IN_DISPUTE;
    }

    public void resolve() {
        this.status = MatchStatus.RESOLVED;
    }

    public boolean isResultSubmitted() {
        return status == MatchStatus.SUBMITTED || status == MatchStatus.CONFIRMED
                || status == MatchStatus.IN_DISPUTE || status == MatchStatus.RESOLVED;
    }
}
