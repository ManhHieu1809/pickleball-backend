package com.pickleball.infrastructure.persistence.repositories;

import com.pickleball.domain.enums.RequestStatus;
import com.pickleball.domain.enums.RequestType;
import com.pickleball.infrastructure.persistence.entities.RoleRequestEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface RoleRequestJpaRepository extends JpaRepository<RoleRequestEntity, Long> {
    List<RoleRequestEntity> findByStatus(RequestStatus status);
    List<RoleRequestEntity> findByUserId(Long userId);
    List<RoleRequestEntity> findByUserIdAndRequestType(Long userId, RequestType requestType);
    Optional<RoleRequestEntity> findByUserIdAndRequestTypeAndStatus(Long userId, RequestType requestType, RequestStatus status);
}

