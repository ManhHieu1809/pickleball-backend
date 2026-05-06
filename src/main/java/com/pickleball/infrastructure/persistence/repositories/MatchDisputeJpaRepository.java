package com.pickleball.infrastructure.persistence.repositories;

import com.pickleball.domain.enums.DisputeStatus;
import com.pickleball.infrastructure.persistence.entities.MatchDisputeEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface MatchDisputeJpaRepository extends JpaRepository<MatchDisputeEntity, Long> {
    List<MatchDisputeEntity> findByRankedMatchId(Long rankedMatchId);

    List<MatchDisputeEntity> findByStatus(DisputeStatus status);

    List<MatchDisputeEntity> findByReportingPlayerId(Long playerId);

    @Query("SELECT d FROM MatchDisputeEntity d JOIN RankedMatchEntity r ON d.rankedMatchId = r.id WHERE r.refereeId = :refereeId")
    List<MatchDisputeEntity> findByRefereeId(@Param("refereeId") Long refereeId);

    @Query("SELECT d FROM MatchDisputeEntity d WHERE d.status = 'AWAITING_EVIDENCE' AND d.evidenceDeadline < :now")
    List<MatchDisputeEntity> findExpiredAwaitingEvidence(@Param("now") LocalDateTime now);
}
