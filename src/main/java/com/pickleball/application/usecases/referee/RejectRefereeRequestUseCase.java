package com.pickleball.application.usecases.referee;

import com.pickleball.domain.entities.RoleRequest;
import com.pickleball.domain.enums.RequestType;
import com.pickleball.domain.repositories.RoleRequestRepository;
import org.springframework.transaction.annotation.Transactional;

/**
 * Admin rejects a referee registration request.
 */
public class RejectRefereeRequestUseCase {

    private final RoleRequestRepository roleRequestRepository;

    public RejectRefereeRequestUseCase(RoleRequestRepository roleRequestRepository) {
        this.roleRequestRepository = roleRequestRepository;
    }

    @Transactional(rollbackFor = Exception.class)
    public RoleRequest execute(Long requestId, Long adminId, String notes) {
        RoleRequest request = roleRequestRepository.findById(requestId)
                .orElseThrow(() -> new IllegalArgumentException("Role request not found with id: " + requestId));

        if (!request.isPending()) {
            throw new IllegalStateException("Request has already been processed");
        }

        if (request.getRequestType() != RequestType.PLATFORM_REFEREE
                && request.getRequestType() != RequestType.VENUE_REFEREE) {
            throw new IllegalStateException("This request is not a referee request");
        }

        request.reject(adminId, notes);
        return roleRequestRepository.save(request);
    }
}
