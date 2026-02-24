package com.pickleball.application.dtos.requests;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateCourtRequest {
    @NotNull(message = "Venue ID không được để trống")
    private Long venueId;

    @NotBlank(message = "Tên sân không được để trống")
    private String courtName;

    private List<CreateCourtPricingRequest> pricings;
}

