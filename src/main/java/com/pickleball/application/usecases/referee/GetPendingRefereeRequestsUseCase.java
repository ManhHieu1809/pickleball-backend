package com.pickleball.application.usecases.referee;

import com.pickleball.domain.entities.RoleRequest;
import com.pickleball.domain.enums.RequestStatus;
import com.pickleball.domain.enums.RequestType;
import com.pickleball.domain.repositories.RoleRequestRepository;

import java.util.List;
import java.util.stream.Collectors;

public class GetPendingRefereeRequestsUseCase {
    private final RoleRequestRepository roleRequestRepository;

    public GetPendingRefereeRequestsUseCase(RoleRequestRepository roleRequestRepository) {
        this.roleRequestRepository = roleRequestRepository;
    }

    public List<RoleRequest> execute() {
        return roleRequestRepository.findByStatus(RequestStatus.PENDING).stream()
                .filter(req -> RequestType.PLATFORM_REFEREE.equals(req.getRequestType()) 
                        || RequestType.VENUE_REFEREE.equals(req.getRequestType()))
                .collect(Collectors.toList());
    }
}

