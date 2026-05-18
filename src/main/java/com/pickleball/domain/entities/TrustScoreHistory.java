package com.pickleball.domain.entities;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class TrustScoreHistory {
    private Long id;
    private Long refereeId;
    private BigDecimal oldScore;
    private BigDecimal newScore;
    private String reason;
    private LocalDateTime changedAt;
    private Long associatedMatchId;
}
