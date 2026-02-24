package com.pickleball.application.usecases.user;

import com.pickleball.domain.entities.RoleRequest;
import com.pickleball.domain.enums.RequestStatus;
import com.pickleball.domain.repositories.RoleRequestRepository;

import java.util.List;

public class GetPendingRoleRequestsUseCase {

    private final RoleRequestRepository roleRequestRepository;

    public GetPendingRoleRequestsUseCase(RoleRequestRepository roleRequestRepository) {
        this.roleRequestRepository = roleRequestRepository;
    }

    public List<RoleRequest> execute() {
        return roleRequestRepository.findByStatus(RequestStatus.PENDING);
    }
}

