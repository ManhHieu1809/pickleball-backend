package com.pickleball.domain.repositories;

import com.pickleball.domain.entities.EloHistory;
import java.util.List;

public interface EloHistoryRepository {
    EloHistory save(EloHistory eloHistory);
    List<EloHistory> findByUserId(Long userId);
    List<EloHistory> findByRankedMatchId(Long rankedMatchId);
}

