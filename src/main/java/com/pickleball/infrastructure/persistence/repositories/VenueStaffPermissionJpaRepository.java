package com.pickleball.infrastructure.persistence.repositories;

import com.pickleball.infrastructure.persistence.entities.VenueStaffPermissionEntity;
import com.pickleball.infrastructure.persistence.entities.VenueStaffPermissionId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface VenueStaffPermissionJpaRepository extends JpaRepository<VenueStaffPermissionEntity, VenueStaffPermissionId> {
    List<VenueStaffPermissionEntity> findByStaffId(Long staffId);
    void deleteByStaffId(Long staffId);
}
