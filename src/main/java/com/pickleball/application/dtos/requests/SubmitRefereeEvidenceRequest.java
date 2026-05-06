package com.pickleball.application.dtos.requests;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class SubmitRefereeEvidenceRequest {
    @NotNull(message = "Referee user ID is required")
    private Long refereeUserId;

    @NotBlank(message = "Evidence URL is required")
    private String evidenceUrl;

    private String response;
}
