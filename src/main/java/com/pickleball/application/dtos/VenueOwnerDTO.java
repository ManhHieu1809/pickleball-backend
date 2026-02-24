package com.pickleball.application.dtos;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class VenueOwnerDTO {
    private Long userId;
    private String taxCode;
    private String bankAccountNumber;
    private String bankName;
}

