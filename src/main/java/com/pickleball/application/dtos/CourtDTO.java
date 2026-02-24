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
public class CourtDTO {
    private Long id;
    private Long venueId;
    private String courtName;
    private Boolean isActive;
    private Long deactivatedByAdminId; // Who locked it
    private List<CourtPricingDTO> pricings;
}

