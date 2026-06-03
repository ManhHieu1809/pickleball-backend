package com.pickleball.infrastructure.persistence.repositories;

import com.pickleball.domain.enums.ParticipantRole;
import com.pickleball.infrastructure.persistence.entities.MatchmakingTicketEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface MatchmakingTicketJpaRepository extends JpaRepository<MatchmakingTicketEntity, Long> {

    Optional<MatchmakingTicketEntity> findByUserIdAndIsActiveTrue(Long userId);
    List<MatchmakingTicketEntity> findByUserIdAndIsActiveTrueOrderByJoinedAtDesc(Long userId);

    List<MatchmakingTicketEntity> findByRoleAndIsActiveTrueOrderByJoinedAtAsc(ParticipantRole role);
    List<MatchmakingTicketEntity> findByPartyIdAndIsActiveTrue(Long partyId);

    @Query("SELECT m FROM MatchmakingTicketEntity m WHERE m.userId = :userId " +
            "AND m.isActive = true " +
            "AND m.requestedStartTime IS NOT NULL " +
            "AND m.requestedEndTime IS NOT NULL " +
            "AND m.requestedStartTime < :endTime " +
            "AND m.requestedEndTime > :startTime")
    List<MatchmakingTicketEntity> findActiveByUserIdOverlapping(
            @Param("userId") Long userId,
            @Param("startTime") LocalDateTime startTime,
            @Param("endTime") LocalDateTime endTime);

    @Modifying
    @Query("UPDATE MatchmakingTicketEntity m SET m.isActive = false WHERE m.userId = :userId")
    void deactivateByUserId(@Param("userId") Long userId);

    @Modifying
    @Query("UPDATE MatchmakingTicketEntity m SET m.isActive = false WHERE m.userId IN :userIds")
    void deactivateByUserIds(@Param("userIds") List<Long> userIds);

    @Modifying
    @Query("UPDATE MatchmakingTicketEntity m SET m.isActive = false WHERE m.partyId = :partyId")
    void deactivateByPartyId(@Param("partyId") Long partyId);
}

