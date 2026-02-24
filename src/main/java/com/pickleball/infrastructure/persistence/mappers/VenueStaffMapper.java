package com.pickleball.infrastructure.persistence.mappers;

import com.pickleball.domain.entities.VenueStaff;
import com.pickleball.infrastructure.persistence.entities.VenueStaffEntity;
import com.pickleball.infrastructure.persistence.entities.VenueStaffPermissionEntity;
import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Component
public class VenueStaffMapper {

    public VenueStaff toDomain(VenueStaffEntity entity, List<VenueStaffPermissionEntity> permissions) {
        if (entity == null) return null;

        Set<String> permissionSet = permissions != null
            ? permissions.stream()
                .map(VenueStaffPermissionEntity::getPermissionKey)
                .collect(Collectors.toSet())
            : new HashSet<>();

        return VenueStaff.builder()
                .id(entity.getId())
                .venueId(entity.getVenueId())
                .username(entity.getUsername())
                .passwordHash(entity.getPasswordHash())
                .fullName(entity.getFullName())
                .isActive(entity.getIsActive() != null && entity.getIsActive())
                .createdAt(entity.getCreatedAt())
                .permissions(permissionSet)
                .build();
    }

    public VenueStaffEntity toEntity(VenueStaff domain) {
        if (domain == null) return null;

        return VenueStaffEntity.builder()
                .id(domain.getId())
                .venueId(domain.getVenueId())
                .username(domain.getUsername())
                .passwordHash(domain.getPasswordHash())
                .fullName(domain.getFullName())
                .isActive(domain.isActive())
                .createdAt(domain.getCreatedAt())
                .build();
    }

    public List<VenueStaffPermissionEntity> toPermissionEntities(Long staffId, Set<String> permissions) {
        if (permissions == null || permissions.isEmpty()) {
            return List.of();
        }

        return permissions.stream()
                .map(perm -> VenueStaffPermissionEntity.builder()
                        .staffId(staffId)
                        .permissionKey(perm)
                        .build())
                .collect(Collectors.toList());
    }
}
