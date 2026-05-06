package com.pickleball.domain.entities;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class VenueStaff {
    private Long id;
    private Long venueId;
    private String username;
    private String passwordHash;
    private String fullName;
    private boolean isActive;
    private LocalDateTime createdAt;

    @Builder.Default
    private Set<String> permissions = new HashSet<>();

    public static final String PERM_CREATE_BOOKING = "CAN_CREATE_BOOKING";
    public static final String PERM_CHECK_IN = "CAN_CHECK_IN";
    public static final String PERM_VIEW_REVENUE = "CAN_VIEW_REVENUE";
    public static final String PERM_CANCEL_BOOKING = "CAN_CANCEL_BOOKING";

    public boolean hasPermission(String permission) {
        return permissions.contains(permission);
    }

    public boolean canCreateBooking() {
        return isActive && hasPermission(PERM_CREATE_BOOKING);
    }

    public boolean canCheckIn() {
        return isActive && hasPermission(PERM_CHECK_IN);
    }

    public boolean canViewRevenue() {
        return isActive && hasPermission(PERM_VIEW_REVENUE);
    }

    public boolean canCancelBooking() {
        return isActive && hasPermission(PERM_CANCEL_BOOKING);
    }

    public void addPermission(String permission) {
        permissions.add(permission);
    }

    public void removePermission(String permission) {
        permissions.remove(permission);
    }
}
