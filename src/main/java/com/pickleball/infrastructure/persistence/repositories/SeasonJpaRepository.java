package com.pickleball.infrastructure.persistence.repositories;

import com.pickleball.infrastructure.persistence.entities.SeasonEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SeasonJpaRepository extends JpaRepository<SeasonEntity, Long> {
    Optional<SeasonEntity> findByIsActiveTrue();
    List<SeasonEntity> findByOrderByStartDateDesc();
}