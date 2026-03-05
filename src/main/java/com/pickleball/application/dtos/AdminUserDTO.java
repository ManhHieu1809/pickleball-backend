package com.pickleball.application.dtos;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
public class AdminUserDTO {
    private Long id;
    private String email;
    private String fullName;
    private String phoneNumber;
    private String profilePictureUrl;
    private LocalDateTime createdAt;
    private List<String> roles;

    // Player-specific
    private Integer currentElo;
    private Integer loyaltyPoints;
    private String loyaltyTier;

    // Owner-specific
    private String taxCode;
    private String bankAccountNumber;
    private String bankName;
    private Integer venueCount;

    // Referee-specific
    private String refereeType;
    private Boolean testPassed;
    private Boolean refereeActive;
}
