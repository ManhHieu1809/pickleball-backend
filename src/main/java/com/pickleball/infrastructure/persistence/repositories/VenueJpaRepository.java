package com.pickleball.infrastructure.persistence.repositories;

import com.pickleball.infrastructure.persistence.entities.VenueEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface VenueJpaRepository extends JpaRepository<VenueEntity, Long> {
        List<VenueEntity> findByOwnerId(Long ownerId);

        long countByOwnerId(Long ownerId);

        List<VenueEntity> findByIsActiveTrue();

        List<VenueEntity> findByIsActiveFalseAndApprovedByAdminIdIsNull();

        @Query("SELECT v FROM VenueEntity v WHERE v.isActive = true AND " +
                        "(6371 * acos(cos(radians(:lat)) * cos(radians(v.latitude)) * " +
                        "cos(radians(v.longitude) - radians(:lng)) + sin(radians(:lat)) * " +
                        "sin(radians(v.latitude)))) < :radius")
        List<VenueEntity> findNearbyVenues(@Param("lat") Double latitude,
                        @Param("lng") Double longitude,
                        @Param("radius") Double radiusInKm);

        @Query("SELECT v FROM VenueEntity v WHERE " +
                        "(:search IS NULL OR v.name LIKE CONCAT('%', :search, '%') OR v.address LIKE CONCAT('%', :search, '%')) AND "
                        +
                        "(:status IS NULL OR " +
                        " (:status = 'ACTIVE' AND v.isActive = true AND v.approvedByAdminId IS NOT NULL) OR " +
                        " (:status = 'PENDING' AND v.isActive = false AND v.approvedByAdminId IS NULL) OR " +
                        " (:status = 'LOCKED' AND v.isActive = false AND v.deactivatedByAdminId IS NOT NULL))")
        org.springframework.data.domain.Page<VenueEntity> searchVenues(@Param("search") String search,
                        @Param("status") String status, org.springframework.data.domain.Pageable pageable);

        @Query("SELECT COUNT(v) FROM VenueEntity v WHERE v.isActive = true AND v.approvedByAdminId IS NOT NULL")
        long countActiveVenues();

        @Query("SELECT COUNT(v) FROM VenueEntity v WHERE v.isActive = false AND v.approvedByAdminId IS NULL")
        long countPendingVenues();

        @Query("SELECT COUNT(v) FROM VenueEntity v WHERE v.isActive = false AND v.deactivatedByAdminId IS NOT NULL")
        long countLockedVenues();
}