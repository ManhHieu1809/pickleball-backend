package com.pickleball.application.usecases.user;

import com.pickleball.domain.entities.RoleRequest;
import com.pickleball.domain.entities.VenueOwner;
import com.pickleball.domain.repositories.RoleRequestRepository;
import com.pickleball.domain.repositories.VenueOwnerRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;

public class ApproveVenueOwnerRequestUseCase {

    private final RoleRequestRepository roleRequestRepository;
    private final VenueOwnerRepository venueOwnerRepository;
    private final ObjectMapper objectMapper;

    public ApproveVenueOwnerRequestUseCase(
            RoleRequestRepository roleRequestRepository,
            VenueOwnerRepository venueOwnerRepository,
            ObjectMapper objectMapper) {
        this.roleRequestRepository = roleRequestRepository;
        this.venueOwnerRepository = venueOwnerRepository;
        this.objectMapper = objectMapper;
    }

    @Transactional(rollbackFor = Exception.class)
    public RoleRequest execute(Long requestId, Long adminId) {
        RoleRequest request = roleRequestRepository.findById(requestId)
                .orElseThrow(() -> new IllegalArgumentException("Role request not found with id: " + requestId));

        if (!request.isPending()) {
            throw new IllegalStateException("Request has already been processed");
        }

        if (venueOwnerRepository.existsByUserId(request.getUserId())) {
            throw new IllegalStateException("User is already a venue owner");
        }

        try {
            Map<String, String> legalInfo = objectMapper.readValue(
                    request.getLegalInfo(),
                    Map.class
            );

            VenueOwner venueOwner = VenueOwner.builder()
                    .userId(request.getUserId())
                    .taxCode(legalInfo.get("taxCode"))
                    .bankAccountNumber(legalInfo.get("bankAccountNumber"))
                    .bankName(legalInfo.get("bankName"))
                    .build();

            venueOwnerRepository.save(venueOwner);

            request.approve(adminId);
            return roleRequestRepository.save(request);

        } catch (Exception e) {
            throw new RuntimeException("Failed to parse legal information: " + e.getMessage(), e);
        }
    }
}

