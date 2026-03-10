package com.pickleball.application.dtos;

import com.pickleball.domain.enums.RefereeType;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class RefereeDTO {
    private Long userId;
    private Boolean testPassed;
    private BigDecimal testScore;
    private RefereeType refereeType;
    private Long worksAtVenueId;
    private Boolean isActive;
    private BigDecimal trustScore;
    private Integer totalMatchesRefereed;
    private LocalDateTime approvedAt;
}
