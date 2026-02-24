package com.pickleball.domain.repositories;

import com.pickleball.domain.entities.Court;
import java.util.List;
import java.util.Optional;

public interface CourtRepository {
    Court save(Court court);
    Optional<Court> findById(Long id);
    List<Court> findByVenueId(Long venueId);
    List<Court> findByVenueIdAndActive(Long venueId);
    List<Court> findByIsActive(boolean isActive);
    List<Court> findAll();
}