package com.pickleball.application.dtos;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Set;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class VenueStaffDTO {
    private Long id;
    private Long venueId;
    private String venueName;
    private String username;
    private String fullName;
    private boolean isActive;
    private Set<String> permissions;
    private LocalDateTime createdAt;
}
