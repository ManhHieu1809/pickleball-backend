package com.pickleball.domain.repositories;

import com.pickleball.domain.entities.TestAttempt;

import java.util.List;
import java.util.Optional;

public interface TestAttemptRepository {
    TestAttempt save(TestAttempt attempt);
    List<TestAttempt> findByUserId(Long userId);
    Optional<TestAttempt> findLatestByUserId(Long userId);
    int countByUserId(Long userId);
}
