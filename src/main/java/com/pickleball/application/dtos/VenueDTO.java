package com.pickleball.application.dtos;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class VenueDTO {
    private Long id;
    private Long ownerId;
    private String name;
    private String address;
    private BigDecimal latitude;
    private BigDecimal longitude;
    private String description;
    private List<String> amenities;
    private Boolean isActive;
    private Long approvedByAdminId;
    private LocalDateTime approvedAt;
    private LocalDateTime createdAt;
    private Long deactivatedByAdminId;

}