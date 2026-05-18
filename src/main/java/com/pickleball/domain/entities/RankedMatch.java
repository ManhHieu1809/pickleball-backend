package com.pickleball.domain.entities;

import com.pickleball.domain.enums.MatchStatus;
import lombok.*;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

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
    private String winningTeam;
    private String evidenceUrl;
    private LocalDateTime submittedAt;
    private LocalDateTime confirmedAt;

    @Builder.Default
    private Set<Long> confirmedPlayerIds = new HashSet<>();

    public void assignReferee(Long refereeId) {
        if (this.refereeId != null) {
            throw new IllegalStateException("Referee already assigned to this match");
        }
        this.refereeId = refereeId;
    }

    public boolean hasReferee() {
        return this.refereeId != null;
    }

    public void submitResult(Long refereeId, int teamAScore, int teamBScore, String winningTeam, String evidenceUrl) {
        if (this.refereeId == null || !this.refereeId.equals(refereeId)) {
            throw new IllegalStateException("Only the assigned referee can submit results");
        }
        if (this.status != MatchStatus.PENDING) {
            throw new IllegalStateException("Match result can only be submitted when status is PENDING");
        }
        this.teamAScore = teamAScore;
        this.teamBScore = teamBScore;
        this.winningTeam = winningTeam;
        this.evidenceUrl = evidenceUrl;
        this.status = MatchStatus.SUBMITTED;
        this.submittedAt = LocalDateTime.now();
        this.confirmedPlayerIds.clear();
    }

    public void confirm(Long playerId) {
        if (this.status != MatchStatus.SUBMITTED) {
            throw new IllegalStateException("Match can only be confirmed when status is SUBMITTED");
        }
        this.confirmedPlayerIds.add(playerId);

        if (this.confirmedPlayerIds.size() >= 4) {
            this.status = MatchStatus.CONFIRMED;
            this.confirmedAt = LocalDateTime.now();
        }
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
