package com.pickleball.application.dtos;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DetailedCourtDTO {
    private Long id;
    private Long venueId;
    private String courtName;
    private Boolean isActive;
    private Long deactivatedByAdminId;

    private VenueDTO venueInfo;
    private List<CourtPricingDTO> pricings;

    private Integer slotDurationMinutes;
}

