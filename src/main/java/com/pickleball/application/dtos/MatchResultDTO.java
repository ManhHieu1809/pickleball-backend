package com.pickleball.application.dtos;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class MatchResultDTO {
    private Long rankedMatchId;
    private Integer teamAScore;
    private Integer teamBScore;
    private String winningTeam;
    private String matchStatus;

    private List<PlayerEloChangeDTO> eloChanges;

    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class PlayerEloChangeDTO {
        private Long userId;
        private String fullName;
        private String team;
        private Integer eloBefore;
        private Integer eloChange;
        private Integer eloAfter;
    }
}
