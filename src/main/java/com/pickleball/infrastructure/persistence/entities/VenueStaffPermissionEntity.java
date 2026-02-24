package com.pickleball.infrastructure.persistence.entities;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "venue_staff_permissions")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@IdClass(VenueStaffPermissionId.class)
public class VenueStaffPermissionEntity {

    @Id
    @Column(name = "staff_id")
    private Long staffId;

    @Id
    @Column(name = "permission_key", length = 50)
    private String permissionKey;
}
