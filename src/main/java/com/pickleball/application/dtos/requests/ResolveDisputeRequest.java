package com.pickleball.application.dtos.requests;

import com.pickleball.domain.enums.DisputeDecision;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ResolveDisputeRequest {
    @NotNull(message = "Admin ID is required")
    private Long adminId;

    @NotBlank(message = "Decision text is required")
    private String decision;

    @NotNull(message = "Decision type is required (UPHOLD or OVERTURN)")
    private DisputeDecision decisionType;
}
