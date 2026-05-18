package com.pickleball.infrastructure.persistence.entities;
import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "referee_trust_score_history")
public class TrustScoreHistoryEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(name = "referee_id", nullable = false)
    private Long refereeId;
    @Column(name = "old_score", precision = 5, scale = 2, nullable = false)
    private BigDecimal oldScore;
    @Column(name = "new_score", precision = 5, scale = 2, nullable = false)
    private BigDecimal newScore;
    @Column(nullable = false)
    private String reason;
    @Column(name = "changed_at", nullable = false)
    private LocalDateTime changedAt;
    @Column(name = "associated_match_id")
    private Long associatedMatchId;
}
