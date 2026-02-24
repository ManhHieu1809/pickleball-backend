package com.pickleball.application.usecases.user;

import com.pickleball.domain.entities.RoleRequest;
import com.pickleball.domain.enums.RequestStatus;
import com.pickleball.domain.enums.RequestType;
import com.pickleball.domain.repositories.RoleRequestRepository;
import com.pickleball.domain.repositories.UserRepository;

import java.time.LocalDateTime;

public class SubmitVenueOwnerRequestUseCase {

    private final RoleRequestRepository roleRequestRepository;
    private final UserRepository userRepository;

    public SubmitVenueOwnerRequestUseCase(RoleRequestRepository roleRequestRepository, UserRepository userRepository) {
        this.roleRequestRepository = roleRequestRepository;
        this.userRepository = userRepository;
    }

    public RoleRequest execute(Long userId, String legalInfoJson) {
        userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found with id: " + userId));

        var existingPendingRequest = roleRequestRepository
                .findByUserIdAndRequestTypeAndStatus(userId, RequestType.VENUE_OWNER, RequestStatus.PENDING);

        if (existingPendingRequest.isPresent()) {
            throw new IllegalStateException("User already has a pending venue owner request");
        }

        var existingApprovedRequest = roleRequestRepository
                .findByUserIdAndRequestTypeAndStatus(userId, RequestType.VENUE_OWNER, RequestStatus.APPROVED);

        if (existingApprovedRequest.isPresent()) {
            throw new IllegalStateException("User is already a venue owner");
        }

        RoleRequest roleRequest = RoleRequest.builder()
                .userId(userId)
                .requestType(RequestType.VENUE_OWNER)
                .legalInfo(legalInfoJson)
                .status(RequestStatus.PENDING)
                .submittedAt(LocalDateTime.now())
                .build();

        return roleRequestRepository.save(roleRequest);
    }
}

