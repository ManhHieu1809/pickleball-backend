package com.pickleball.infrastructure.persistence.entities;

import com.pickleball.domain.enums.RefereeType;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Entity
@Table(name = "referees")
public class RefereeEntity {
    @Id
    private Long userId;

    @OneToOne(fetch = FetchType.LAZY)
    @MapsId
    @JoinColumn(name = "user_id")
    private UserEntity user;

    @Column(name = "test_passed")
    private Boolean testPassed;

    @Column(name = "test_score", precision = 5, scale = 2)
    private BigDecimal testScore;

    @Enumerated(EnumType.STRING)
    @Column(name = "referee_type", nullable = false)
    private RefereeType refereeType;

    @Column(name = "works_at_venue_id")
    private Long worksAtVenueId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "works_at_venue_id", insertable = false, updatable = false)
    private VenueEntity worksAtVenue;

    @Column(name = "approved_by_admin_id")
    private Long approvedByAdminId;

    @Column(name = "approved_at")
    private LocalDateTime approvedAt;

    @Column(name = "is_active")
    private Boolean isActive;

    @Column(name = "trust_score", precision = 5, scale = 2)
    @Builder.Default
    private BigDecimal trustScore = new BigDecimal("100.00");

    @Column(name = "total_matches_refereed")
    @Builder.Default
    private Integer totalMatchesRefereed = 0;
}