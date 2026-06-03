package com.pickleball.application.dtos;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class PendingInviteCountDTO {
    private long pendingCount;
}
