package com.pickleball.application.services;

import com.pickleball.application.dtos.AdminVenueDTO;
import com.pickleball.application.dtos.AdminVenueStatsDTO;
import com.pickleball.infrastructure.persistence.entities.CourtEntity;
import com.pickleball.infrastructure.persistence.entities.UserEntity;
import com.pickleball.infrastructure.persistence.entities.VenueEntity;
import com.pickleball.infrastructure.persistence.entities.VenueOwnerEntity;
import com.pickleball.infrastructure.persistence.repositories.CourtJpaRepository;
import com.pickleball.infrastructure.persistence.repositories.UserJpaRepository;
import com.pickleball.infrastructure.persistence.repositories.VenueJpaRepository;
import com.pickleball.infrastructure.persistence.repositories.VenueOwnerJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class VenueManagementService {

    private final VenueJpaRepository venueJpaRepository;
    private final VenueOwnerJpaRepository venueOwnerJpaRepository;
    private final UserJpaRepository userJpaRepository;
    private final CourtJpaRepository courtJpaRepository;

    public Page<AdminVenueDTO> getAllVenues(int page, int size, String search, String status) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());

        if (search != null && search.trim().isEmpty()) {
            search = null;
        }

        if (status != null && (status.trim().isEmpty() || status.equalsIgnoreCase("ALL"))) {
            status = null;
        } else if (status != null) {
            status = status.toUpperCase();
        }

        Page<VenueEntity> entities = venueJpaRepository.searchVenues(search, status, pageable);
        return entities.map(this::mapToDTO);
    }

    public AdminVenueStatsDTO getVenueStats() {
        long total = venueJpaRepository.count();
        long active = venueJpaRepository.countActiveVenues();
        long pending = venueJpaRepository.countPendingVenues();
        long locked = venueJpaRepository.countLockedVenues();

        return AdminVenueStatsDTO.builder()
                .totalVenues(total)
                .activeVenues(active)
                .pendingVenues(pending)
                .lockedVenues(locked)
                .build();
    }

    public AdminVenueDTO getVenueById(Long id) {
        VenueEntity venue = venueJpaRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Venue not found: " + id));
        return mapToDTO(venue);
    }

    private AdminVenueDTO mapToDTO(VenueEntity venue) {
        String status = "PENDING";
        if (Boolean.TRUE.equals(venue.getIsActive()) && venue.getApprovedByAdminId() != null) {
            status = "ACTIVE";
        } else if (Boolean.FALSE.equals(venue.getIsActive()) && venue.getDeactivatedByAdminId() != null) {
            status = "LOCKED";
        }

        String ownerName = "Unknown";
        String ownerEmail = "Unknown";
        String ownerPhone = "Unknown";

        if (venue.getOwnerId() != null) {
            try {
                VenueOwnerEntity owner = venueOwnerJpaRepository.findById(venue.getOwnerId()).orElse(null);
                if (owner != null && owner.getUserId() != null) {
                    UserEntity user = userJpaRepository.findById(owner.getUserId()).orElse(null);
                    if (user != null) {
                        ownerName = user.getFullName() != null ? user.getFullName() : "User #" + user.getId();
                        ownerEmail = user.getEmail() != null ? user.getEmail() : "Unknown";
                        ownerPhone = user.getPhoneNumber() != null ? user.getPhoneNumber() : "Unknown";
                    }
                }
            } catch (Exception ignored) {
            }
        }

        int totalCourts = 0;
        try {
            Long count = courtJpaRepository.countByVenueId(venue.getId());
            totalCourts = count != null ? count.intValue() : 0;
        } catch (Exception ignored) {
        }

        return AdminVenueDTO.builder()
                .id(venue.getId())
                .name(venue.getName())
                .address(venue.getAddress())
                .description(venue.getDescription())
                .latitude(venue.getLatitude() != null ? venue.getLatitude().doubleValue() : null)
                .longitude(venue.getLongitude() != null ? venue.getLongitude().doubleValue() : null)
                .status(status)
                .isActive(venue.getIsActive())
                .approvedByAdminId(venue.getApprovedByAdminId())
                .deactivatedByAdminId(venue.getDeactivatedByAdminId())
                .approvedAt(venue.getApprovedAt())
                .ownerId(venue.getOwnerId())
                .ownerName(ownerName)
                .ownerEmail(ownerEmail)
                .ownerPhone(ownerPhone)
                .totalCourts(totalCourts)
                .createdAt(venue.getCreatedAt())
                .build();
    }
}
