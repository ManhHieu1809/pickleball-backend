package com.pickleball.application.usecases.user;

import com.pickleball.domain.entities.RoleRequest;
import com.pickleball.domain.repositories.RoleRequestRepository;

public class RejectVenueOwnerRequestUseCase {

    private final RoleRequestRepository roleRequestRepository;

    public RejectVenueOwnerRequestUseCase(RoleRequestRepository roleRequestRepository) {
        this.roleRequestRepository = roleRequestRepository;
    }

    public RoleRequest execute(Long requestId, Long adminId, String notes) {
        RoleRequest request = roleRequestRepository.findById(requestId)
                .orElseThrow(() -> new IllegalArgumentException("Role request not found with id: " + requestId));

        if (!request.isPending()) {
            throw new IllegalStateException("Request has already been processed");
        }

        request.reject(adminId, notes);
        return roleRequestRepository.save(request);
    }
}

