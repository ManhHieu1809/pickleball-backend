package com.pickleball.infrastructure.persistence.repositories;

import com.pickleball.domain.enums.ParticipantRole;
import com.pickleball.infrastructure.persistence.entities.MatchmakingTicketEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface MatchmakingTicketJpaRepository extends JpaRepository<MatchmakingTicketEntity, Long> {

    Optional<MatchmakingTicketEntity> findByUserIdAndIsActiveTrue(Long userId);

    List<MatchmakingTicketEntity> findByRoleAndIsActiveTrueOrderByJoinedAtAsc(ParticipantRole role);

    @Modifying
    @Query("UPDATE MatchmakingTicketEntity m SET m.isActive = false WHERE m.userId = :userId")
    void deactivateByUserId(@Param("userId") Long userId);

    @Modifying
    @Query("UPDATE MatchmakingTicketEntity m SET m.isActive = false WHERE m.userId IN :userIds")
    void deactivateByUserIds(@Param("userIds") List<Long> userIds);
}

