package com.pickleball.infrastructure.persistence.adapters;

import com.pickleball.domain.entities.Referee;
import com.pickleball.domain.repositories.RefereeRepository;
import com.pickleball.infrastructure.persistence.entities.RefereeEntity;
import com.pickleball.infrastructure.persistence.mappers.RefereeMapper;
import com.pickleball.infrastructure.persistence.repositories.RefereeJpaRepository;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Component
public class RefereeRepositoryAdapter implements RefereeRepository {

    private final RefereeJpaRepository jpaRepository;

    public RefereeRepositoryAdapter(RefereeJpaRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public Referee save(Referee referee) {
        RefereeEntity entity = RefereeMapper.toEntity(referee);
        RefereeEntity saved = jpaRepository.save(entity);
        return RefereeMapper.toDomain(saved);
    }

    @Override
    public Optional<Referee> findByUserId(Long userId) {
        return jpaRepository.findByUserId(userId).map(RefereeMapper::toDomain);
    }

    @Override
    public List<Referee> findActiveReferees() {
        return jpaRepository.findByIsActiveTrue().stream()
                .map(RefereeMapper::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public List<Referee> findEligibleReferees() {
        return jpaRepository.findEligibleReferees().stream()
                .map(RefereeMapper::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public boolean existsByUserId(Long userId) {
        return jpaRepository.existsByUserId(userId);
    }
}
