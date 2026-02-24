package com.pickleball.application.dtos.requests;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import java.math.BigDecimal;

@Data
public class CreateVenueRequest {
    @NotNull
    private Long ownerId;

    @NotBlank
    private String name;

    @NotBlank
    private String address;

    @NotNull
    private BigDecimal latitude;

    @NotNull
    private BigDecimal longitude;

    private String description;
}