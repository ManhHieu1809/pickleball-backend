package com.pickleball.domain.repositories;

import com.pickleball.domain.entities.RankedMatch;
import com.pickleball.domain.enums.MatchStatus;

import java.util.List;
import java.util.Optional;

public interface RankedMatchRepository {
    RankedMatch save(RankedMatch rankedMatch);
    Optional<RankedMatch> findById(Long id);
    Optional<RankedMatch> findByBookingId(Long bookingId);
    List<RankedMatch> findByRefereeId(Long refereeId);
    List<RankedMatch> findByStatus(MatchStatus status);
}
