package com.pickleball.application.dtos.requests;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class InviteRankedPartyMemberRequest {
    @NotNull
    private Long inviterUserId;

    @NotNull
    private Long inviteeUserId;

    private String message;
}
