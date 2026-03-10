package com.pickleball.application.dtos;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class PlayerMatchDTO {
    private Long userId;
    private String fullName;
    private Integer currentElo;
    private String loyaltyTier;
}
