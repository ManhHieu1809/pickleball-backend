package com.pickleball.infrastructure.persistence.entities;

import java.io.Serializable;
import java.util.Objects;

public class VenueStaffPermissionId implements Serializable {
    private Long staffId;
    private String permissionKey;

    public VenueStaffPermissionId() {}

    public VenueStaffPermissionId(Long staffId, String permissionKey) {
        this.staffId = staffId;
        this.permissionKey = permissionKey;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        VenueStaffPermissionId that = (VenueStaffPermissionId) o;
        return Objects.equals(staffId, that.staffId) &&
               Objects.equals(permissionKey, that.permissionKey);
    }

    @Override
    public int hashCode() {
        return Objects.hash(staffId, permissionKey);
    }
}
