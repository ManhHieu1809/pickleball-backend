package com.pickleball.application.dtos;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
public class RankedPartyDTO {
    private Long id;
    private Long hostUserId;
    private String status;
    private Double latitude;
    private Double longitude;
    private LocalDateTime createdAt;
    private LocalDateTime queuedAt;
    private LocalDateTime requestedStartTime;
    private LocalDateTime requestedEndTime;
    private Long matchedBookingId;
    private List<MemberDTO> members;

    @Data
    @Builder
    public static class MemberDTO {
        private Long id;
        private Long partyId;
        private Long userId;
        private String fullName;
        private Integer currentElo;
        private Long invitedByUserId;
        private String status;
        private Boolean isHost;
        private String message;
        private LocalDateTime joinedAt;
        private LocalDateTime respondedAt;
        private LocalDateTime createdAt;
    }
}
