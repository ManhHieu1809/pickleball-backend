package com.pickleball.domain.entities;

import com.pickleball.domain.enums.RequestStatus;
import com.pickleball.domain.enums.RequestType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Objects;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class RoleRequest {
    private Long id;
    private Long userId;
    private RequestType requestType;
    private Long venueId;
    private String legalInfo;
    private BigDecimal testScore;
    private RequestStatus status;
    private LocalDateTime submittedAt;
    private Long processedByAdminId;
    private LocalDateTime processedAt;
    private String notes;

    public void approve(Long adminId) {
        this.status = RequestStatus.APPROVED;
        this.processedByAdminId = adminId;
        this.processedAt = LocalDateTime.now();
    }

    public void reject(Long adminId, String notes) {
        this.status = RequestStatus.REJECTED;
        this.processedByAdminId = adminId;
        this.processedAt = LocalDateTime.now();
        this.notes = notes;
    }

    public boolean isPending() {
        return RequestStatus.PENDING.equals(this.status);
    }

    public boolean isApproved() {
        return RequestStatus.APPROVED.equals(this.status);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        RoleRequest that = (RoleRequest) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}

