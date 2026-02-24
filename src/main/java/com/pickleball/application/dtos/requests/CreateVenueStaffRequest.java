package com.pickleball.application.dtos.requests;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.Set;

@Data
public class CreateVenueStaffRequest {
    @NotNull(message = "Venue ID không được để trống")
    private Long venueId;

    @NotBlank(message = "Username không được để trống")
    private String username;

    @NotBlank(message = "Mật khẩu không được để trống")
    private String password;

    private String fullName;

    private Set<String> permissions;
}
