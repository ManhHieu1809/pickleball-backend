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
    List<VenueEntity> findByIsActiveTrue();
    List<VenueEntity> findByIsActiveFalseAndApprovedByAdminIdIsNull();

    @Query("SELECT v FROM VenueEntity v WHERE v.isActive = true AND " +
            "(6371 * acos(cos(radians(:lat)) * cos(radians(v.latitude)) * " +
            "cos(radians(v.longitude) - radians(:lng)) + sin(radians(:lat)) * " +
            "sin(radians(v.latitude)))) < :radius")
    List<VenueEntity> findNearbyVenues(@Param("lat") Double latitude,
                                       @Param("lng") Double longitude,
                                       @Param("radius") Double radiusInKm);
}