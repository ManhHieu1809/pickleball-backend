package com.pickleball.application.dtos.requests;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class SubmitDisputeRequest {
    @NotNull(message = "Ranked match ID is required")
    private Long rankedMatchId;

    @NotNull(message = "Reporting player ID is required")
    private Long reportingPlayerId;

    @NotBlank(message = "Reason is required")
    private String reason;

    private String evidence;
}
