package com.pickleball.infrastructure.persistence.adapters;

import com.pickleball.domain.entities.CheckIn;
import com.pickleball.domain.repositories.CheckInRepository;
import com.pickleball.infrastructure.persistence.entities.CheckInEntity;
import com.pickleball.infrastructure.persistence.mappers.CheckInMapper;
import com.pickleball.infrastructure.persistence.repositories.CheckInJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
@RequiredArgsConstructor
public class CheckInRepositoryAdapter implements CheckInRepository {

    private final CheckInJpaRepository jpaRepository;

    @Override
    public CheckIn save(CheckIn checkIn) {
        CheckInEntity entity = CheckInMapper.toEntity(checkIn);
        CheckInEntity saved = jpaRepository.save(entity);
        return CheckInMapper.toDomain(saved);
    }

    @Override
    public Optional<CheckIn> findByBookingIdAndUserId(Long bookingId, Long userId) {
        return jpaRepository.findByBookingIdAndUserId(bookingId, userId)
                .map(CheckInMapper::toDomain);
    }
}
