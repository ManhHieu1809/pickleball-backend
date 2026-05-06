package com.pickleball.application.usecases.referee;

import com.pickleball.domain.entities.Referee;
import com.pickleball.domain.entities.RoleRequest;
import com.pickleball.domain.enums.RefereeType;
import com.pickleball.domain.enums.RequestType;
import com.pickleball.domain.repositories.RefereeRepository;
import com.pickleball.domain.repositories.RoleRequestRepository;
import org.springframework.transaction.annotation.Transactional;

public class ApproveRefereeRequestUseCase {

    private final RoleRequestRepository roleRequestRepository;
    private final RefereeRepository refereeRepository;

    public ApproveRefereeRequestUseCase(
            RoleRequestRepository roleRequestRepository,
            RefereeRepository refereeRepository) {
        this.roleRequestRepository = roleRequestRepository;
        this.refereeRepository = refereeRepository;
    }

    @Transactional(rollbackFor = Exception.class)
    public RoleRequest execute(Long requestId, Long adminId) {
        RoleRequest request = roleRequestRepository.findById(requestId)
                .orElseThrow(() -> new IllegalArgumentException("Role request not found with id: " + requestId));

        if (!request.isPending()) {
            throw new IllegalStateException("Request has already been processed");
        }

        if (request.getRequestType() != RequestType.PLATFORM_REFEREE
                && request.getRequestType() != RequestType.VENUE_REFEREE) {
            throw new IllegalStateException("This request is not a referee request");
        }

        if (refereeRepository.existsByUserId(request.getUserId())) {
            throw new IllegalStateException("User is already a referee");
        }

        RefereeType refereeType = request.getRequestType() == RequestType.VENUE_REFEREE
                ? RefereeType.VENUE
                : RefereeType.PLATFORM;

        Referee referee = Referee.builder()
                .userId(request.getUserId())
                .testPassed(true)
                .testScore(request.getTestScore())
                .refereeType(refereeType)
                .worksAtVenueId(request.getVenueId())
                .isActive(true)
                .build();
        referee.activate(adminId);

        refereeRepository.save(referee);

        request.approve(adminId);
        return roleRequestRepository.save(request);
    }
}
