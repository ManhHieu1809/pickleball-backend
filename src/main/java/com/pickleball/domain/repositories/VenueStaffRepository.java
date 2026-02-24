package com.pickleball.domain.repositories;

import com.pickleball.domain.entities.VenueStaff;

import java.util.List;
import java.util.Optional;

public interface VenueStaffRepository {
    VenueStaff save(VenueStaff staff);
    Optional<VenueStaff> findById(Long id);
    Optional<VenueStaff> findByUsername(String username);
    List<VenueStaff> findByVenueId(Long venueId);
    List<VenueStaff> findActiveByVenueId(Long venueId);
    boolean existsByUsername(String username);
    void deleteById(Long id);
}
