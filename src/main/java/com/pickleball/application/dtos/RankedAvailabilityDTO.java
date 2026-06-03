package com.pickleball.application.dtos;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class RankedAvailabilityDTO {
    private boolean available;
    private int availableCourtCount;
    private Long nearestVenueId;
    private String nearestVenueName;
    private Long nearestCourtId;
    private String nearestCourtName;
    private Double nearestDistanceKm;
}
