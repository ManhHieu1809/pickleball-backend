package com.pickleball.domain.entities;

import com.pickleball.domain.valueobjects.Location;
import com.pickleball.domain.valueobjects.Money;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Venue {
    private Long id;
    private Long ownerId;
    private String name;
    private String address;
    private Location location;
    private String description;
    private List<String> amenities;
    private boolean isActive;
    private Long approvedByAdminId;
    private LocalDateTime approvedAt;
    private LocalDateTime createdAt;
    private Long deactivatedByAdminId; // Track if admin locked the venue

    @Builder.Default
    private List<Court> courts = new ArrayList<>();



    public void approve(Long adminId) {
        this.isActive = true;
        this.approvedByAdminId = adminId;
        this.approvedAt = LocalDateTime.now();
    }

    public void reject() {
        this.isActive = false;
        this.approvedByAdminId = null;
        this.approvedAt = null;
    }

    public void activate() {
        if (this.approvedByAdminId == null) {
            throw new IllegalStateException("Venue chưa được duyệt, không thể kích hoạt");
        }
        if (this.deactivatedByAdminId != null) {
            throw new IllegalStateException("Venue đã bị admin khóa, chỉ admin mới có thể mở lại");
        }
        this.isActive = true;
    }

    public void deactivate(Long deactivatorId, boolean isAdmin) {
        this.isActive = false;
        if (isAdmin) {
            this.deactivatedByAdminId = deactivatorId;
        }
        // Owner deactivate: không set deactivatedByAdminId
    }

    public void activateByAdmin() {
        if (this.approvedByAdminId == null) {
            throw new IllegalStateException("Venue chưa được duyệt, không thể kích hoạt");
        }
        this.isActive = true;
        this.deactivatedByAdminId = null;
    }

    public void addAmenity(String amenity) {
        if (amenity != null && !amenity.trim().isEmpty() && !amenities.contains(amenity)) {
            amenities.add(amenity);
        }
    }

    public void addCourt(Court court) {
        if (court != null && !courts.contains(court)) {
            courts.add(court);
        }
    }

    public boolean isApproved() {
        return isActive && approvedByAdminId != null;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Venue venue = (Venue) o;
        return Objects.equals(id, venue.id) && Objects.equals(name, venue.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, name);
    }
}