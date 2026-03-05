package com.pickleball.application.dtos;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AdminVenueStatsDTO {
    private long totalVenues;
    private long activeVenues;
    private long pendingVenues;
    private long lockedVenues;
}
