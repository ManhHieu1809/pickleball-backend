package com.pickleball.infrastructure.persistence.repositories;

import com.pickleball.infrastructure.persistence.entities.TestAttemptEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TestAttemptJpaRepository extends JpaRepository<TestAttemptEntity, Long> {
    List<TestAttemptEntity> findByUserIdOrderByAttemptedAtDesc(Long userId);

    Optional<TestAttemptEntity> findFirstByUserIdOrderByAttemptedAtDesc(Long userId);

    int countByUserId(Long userId);
}
