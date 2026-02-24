package com.pickleball.infrastructure.persistence.adapters;

import com.pickleball.domain.entities.RoleRequest;
import com.pickleball.domain.enums.RequestStatus;
import com.pickleball.domain.enums.RequestType;
import com.pickleball.domain.repositories.RoleRequestRepository;
import com.pickleball.infrastructure.persistence.entities.RoleRequestEntity;
import com.pickleball.infrastructure.persistence.mappers.RoleRequestMapper;
import com.pickleball.infrastructure.persistence.repositories.RoleRequestJpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Repository
public class RoleRequestRepositoryAdapter implements RoleRequestRepository {

    private final RoleRequestJpaRepository jpaRepository;
    private final RoleRequestMapper mapper;

    public RoleRequestRepositoryAdapter(RoleRequestJpaRepository jpaRepository, RoleRequestMapper mapper) {
        this.jpaRepository = jpaRepository;
        this.mapper = mapper;
    }

    @Override
    public RoleRequest save(RoleRequest roleRequest) {
        RoleRequestEntity entity = mapper.toEntity(roleRequest);
        RoleRequestEntity saved = jpaRepository.save(entity);
        return mapper.toDomain(saved);
    }

    @Override
    public Optional<RoleRequest> findById(Long id) {
        return jpaRepository.findById(id)
                .map(mapper::toDomain);
    }

    @Override
    public List<RoleRequest> findByStatus(RequestStatus status) {
        return jpaRepository.findByStatus(status).stream()
                .map(mapper::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public List<RoleRequest> findByUserId(Long userId) {
        return jpaRepository.findByUserId(userId).stream()
                .map(mapper::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public List<RoleRequest> findByUserIdAndRequestType(Long userId, RequestType requestType) {
        return jpaRepository.findByUserIdAndRequestType(userId, requestType).stream()
                .map(mapper::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public Optional<RoleRequest> findByUserIdAndRequestTypeAndStatus(Long userId, RequestType requestType, RequestStatus status) {
        return jpaRepository.findByUserIdAndRequestTypeAndStatus(userId, requestType, status)
                .map(mapper::toDomain);
    }
}

