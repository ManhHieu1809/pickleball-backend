package com.pickleball.application.dtos;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AdminVenueDTO {
    private Long id;
    private String name;
    private String address;
    private String description;
    private Double latitude;
    private Double longitude;

    // Status
    private String status; // PENDING, ACTIVE, LOCKED
    private Boolean isActive;
    private Long approvedByAdminId;
    private Long deactivatedByAdminId;
    private LocalDateTime approvedAt;

    // Owner details
    private Long ownerId;
    private String ownerName;
    private String ownerEmail;
    private String ownerPhone;

    // Courts info
    private Integer totalCourts;

    private LocalDateTime createdAt;
}
