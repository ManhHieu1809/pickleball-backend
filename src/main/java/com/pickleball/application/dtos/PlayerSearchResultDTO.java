package com.pickleball.application.dtos;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class PlayerSearchResultDTO {
    private Long userId;
    private String fullName;
    private String username;
    private Integer currentElo;
    private String loyaltyTier;
    private String avatarUrl;
}
