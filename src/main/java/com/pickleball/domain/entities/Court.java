package com.pickleball.domain.entities;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Court {
    private Long id;
    private Long venueId;
    private String courtName;
    private boolean isActive;
    private Long deactivatedByAdminId;

    @Builder.Default
    private List<CourtPricing> pricing = new ArrayList<>();



    public void deactivate(Long deactivatorId, boolean isAdmin) {
        this.isActive = false;
        if (isAdmin) {
            this.deactivatedByAdminId = deactivatorId;
        }
    }

    public void activate() {
        if (this.deactivatedByAdminId != null) {
            throw new IllegalStateException("Court đã bị admin khóa, chỉ admin mới có thể mở lại");
        }
        this.isActive = true;
    }

    public void activateByAdmin() {
        this.isActive = true;
        this.deactivatedByAdminId = null;
    }

    public void addPricing(CourtPricing courtPricing) {
        if (courtPricing != null && !pricing.contains(courtPricing)) {
            pricing.add(courtPricing);
        }
    }

    public boolean isAvailable() {
        return isActive;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Court court = (Court) o;
        return Objects.equals(id, court.id) && Objects.equals(venueId, court.venueId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, venueId);
    }
}