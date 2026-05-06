package com.pickleball.domain.repositories;

import com.pickleball.domain.entities.MatchDispute;
import com.pickleball.domain.enums.DisputeStatus;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface MatchDisputeRepository {
    MatchDispute save(MatchDispute dispute);
    Optional<MatchDispute> findById(Long id);
    List<MatchDispute> findByRankedMatchId(Long rankedMatchId);
    List<MatchDispute> findByStatus(DisputeStatus status);
    List<MatchDispute> findByReportingPlayerId(Long playerId);
    List<MatchDispute> findExpiredAwaitingEvidence(LocalDateTime now);
    List<MatchDispute> findAll();
    List<MatchDispute> findByRefereeId(Long refereeId);
}
