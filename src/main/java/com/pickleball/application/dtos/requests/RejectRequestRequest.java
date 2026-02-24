package com.pickleball.application.dtos.requests;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class RejectRequestRequest {
    @NotBlank(message = "Notes are required for rejection")
    private String notes;
}

