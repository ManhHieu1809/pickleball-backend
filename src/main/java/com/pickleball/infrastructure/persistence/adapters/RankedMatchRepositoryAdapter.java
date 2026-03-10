package com.pickleball.infrastructure.persistence.adapters;

import com.pickleball.domain.entities.RankedMatch;
import com.pickleball.domain.enums.MatchStatus;
import com.pickleball.domain.repositories.RankedMatchRepository;
import com.pickleball.infrastructure.persistence.mappers.RankedMatchMapper;
import com.pickleball.infrastructure.persistence.repositories.RankedMatchJpaRepository;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Component
public class RankedMatchRepositoryAdapter implements RankedMatchRepository {

    private final RankedMatchJpaRepository jpaRepository;

    public RankedMatchRepositoryAdapter(RankedMatchJpaRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public RankedMatch save(RankedMatch rankedMatch) {
        var entity = RankedMatchMapper.toEntity(rankedMatch);
        var saved = jpaRepository.save(entity);
        return RankedMatchMapper.toDomain(saved);
    }

    @Override
    public Optional<RankedMatch> findById(Long id) {
        return jpaRepository.findById(id).map(RankedMatchMapper::toDomain);
    }

    @Override
    public Optional<RankedMatch> findByBookingId(Long bookingId) {
        return jpaRepository.findByBookingId(bookingId).map(RankedMatchMapper::toDomain);
    }

    @Override
    public List<RankedMatch> findByRefereeId(Long refereeId) {
        return jpaRepository.findByRefereeId(refereeId).stream()
                .map(RankedMatchMapper::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public List<RankedMatch> findByStatus(MatchStatus status) {
        return jpaRepository.findByStatus(status).stream()
                .map(RankedMatchMapper::toDomain)
                .collect(Collectors.toList());
    }
}
