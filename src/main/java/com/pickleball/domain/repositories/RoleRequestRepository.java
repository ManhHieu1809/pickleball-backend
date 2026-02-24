package com.pickleball.domain.repositories;

import com.pickleball.domain.entities.RoleRequest;
import com.pickleball.domain.enums.RequestStatus;
import com.pickleball.domain.enums.RequestType;

import java.util.List;
import java.util.Optional;

public interface RoleRequestRepository {
    RoleRequest save(RoleRequest roleRequest);
    Optional<RoleRequest> findById(Long id);
    List<RoleRequest> findByStatus(RequestStatus status);
    List<RoleRequest> findByUserId(Long userId);
    List<RoleRequest> findByUserIdAndRequestType(Long userId, RequestType requestType);
    Optional<RoleRequest> findByUserIdAndRequestTypeAndStatus(Long userId, RequestType requestType, RequestStatus status);
}

