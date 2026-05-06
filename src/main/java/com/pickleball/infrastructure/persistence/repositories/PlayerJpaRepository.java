package com.pickleball.infrastructure.persistence.repositories;

import com.pickleball.infrastructure.persistence.entities.PlayerEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface PlayerJpaRepository extends JpaRepository<PlayerEntity, Long> {
    Optional<PlayerEntity> findByUserId(Long userId);
    boolean existsByUserId(Long userId);

    @Query("SELECT p FROM PlayerEntity p WHERE p.currentElo BETWEEN :minElo AND :maxElo")
    List<PlayerEntity> findByCurrentEloBetween(@Param("minElo") int minElo, @Param("maxElo") int maxElo);

    List<PlayerEntity> findByUserIdIn(List<Long> userIds);

    @Modifying
    @Query("UPDATE PlayerEntity p SET p.lastLatitude = :lat, p.lastLongitude = :lng, p.locationUpdatedAt = :now WHERE p.userId = :userId")
    void updateLocation(@Param("userId") Long userId, @Param("lat") Double lat, @Param("lng") Double lng, @Param("now") LocalDateTime now);        

    @EntityGraph(attributePaths = {"user"})
    Page<PlayerEntity> findAllByOrderByCurrentEloDesc(Pageable pageable);
}
