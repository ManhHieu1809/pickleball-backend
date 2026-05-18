package com.pickleball.application.dtos.requests;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SubmitResultRequest {
    @NotNull(message = "Referee ID is required")
    private Long refereeUserId;

    @NotNull(message = "Team A score is required")
    @PositiveOrZero(message = "Score must be non-negative")
    private Integer teamAScore;

    @NotNull(message = "Team B score is required")
    @PositiveOrZero(message = "Score must be non-negative")
    private Integer teamBScore;

    private String evidenceUrl;
}

