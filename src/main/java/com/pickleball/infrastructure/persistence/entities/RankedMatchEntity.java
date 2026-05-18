package com.pickleball.infrastructure.persistence.entities;

import com.pickleball.domain.enums.MatchStatus;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Entity
@Table(name = "ranked_matches")
public class RankedMatchEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "booking_id", nullable = false, unique = true)
    private Long bookingId;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "booking_id", insertable = false, updatable = false)
    private BookingEntity booking;

    @Column(name = "referee_id")
    private Long refereeId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "referee_id", insertable = false, updatable = false)
    private RefereeEntity referee;

    @Column(name = "season_id")
    private Long seasonId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "season_id", insertable = false, updatable = false)
    private SeasonEntity season;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private MatchStatus status;

    @Column(name = "team_a_score")
    private Integer teamAScore;

    @Column(name = "team_b_score")
    private Integer teamBScore;

    @Column(name = "winning_team")
    private String winningTeam;

    @Column(name = "evidence_url")
    private String evidenceUrl;

    @Column(name = "submitted_at")
    private LocalDateTime submittedAt;

    @Column(name = "confirmed_at")
    private LocalDateTime confirmedAt;

    @ElementCollection
    @CollectionTable(
        name = "match_confirmations",
        joinColumns = @JoinColumn(name = "ranked_match_id")
    )
    @Column(name = "player_id")
    @Builder.Default
    private Set<Long> confirmedPlayerIds = new HashSet<>();
}
