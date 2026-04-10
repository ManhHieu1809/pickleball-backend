package com.pickleball.application.dtos.requests;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ConfirmResultRequest {
    @NotNull(message = "Player ID is required")
    private Long playerUserId;

    @NotNull(message = "Decision is required")
    private Boolean accepted;
}

