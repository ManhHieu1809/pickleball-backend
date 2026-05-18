package com.pickleball.application.dtos.requests;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class SubmitMatchResultRequest {
    @NotNull(message = "Referee user ID is required")
    private Long refereeUserId;

    @NotNull(message = "Team A score is required")
    private Integer teamAScore;

    @NotNull(message = "Team B score is required")
    private Integer teamBScore;

    @NotBlank(message = "Winning team is required (A or B)")
    private String winningTeam;

    private String evidenceUrl;
}
