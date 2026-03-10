package com.pickleball.infrastructure.persistence.adapters;

import com.pickleball.domain.entities.TestAttempt;
import com.pickleball.domain.repositories.TestAttemptRepository;
import com.pickleball.infrastructure.persistence.mappers.TestAttemptMapper;
import com.pickleball.infrastructure.persistence.repositories.TestAttemptJpaRepository;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Component
public class TestAttemptRepositoryAdapter implements TestAttemptRepository {

    private final TestAttemptJpaRepository jpaRepository;

    public TestAttemptRepositoryAdapter(TestAttemptJpaRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public TestAttempt save(TestAttempt attempt) {
        var entity = TestAttemptMapper.toEntity(attempt);
        var saved = jpaRepository.save(entity);
        return TestAttemptMapper.toDomain(saved);
    }

    @Override
    public List<TestAttempt> findByUserId(Long userId) {
        return jpaRepository.findByUserIdOrderByAttemptedAtDesc(userId).stream()
                .map(TestAttemptMapper::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public Optional<TestAttempt> findLatestByUserId(Long userId) {
        return jpaRepository.findFirstByUserIdOrderByAttemptedAtDesc(userId)
                .map(TestAttemptMapper::toDomain);
    }

    @Override
    public int countByUserId(Long userId) {
        return jpaRepository.countByUserId(userId);
    }
}
