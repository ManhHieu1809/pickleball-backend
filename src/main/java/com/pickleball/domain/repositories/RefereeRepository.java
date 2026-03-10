package com.pickleball.domain.repositories;

import com.pickleball.domain.entities.Referee;

import java.util.List;
import java.util.Optional;

public interface RefereeRepository {
    Referee save(Referee referee);
    Optional<Referee> findByUserId(Long userId);
    List<Referee> findActiveReferees();
    List<Referee> findEligibleReferees();
    boolean existsByUserId(Long userId);
}
