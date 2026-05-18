package com.pickleball.domain.repositories;
import com.pickleball.domain.entities.TrustScoreHistory;
import java.util.List;
public interface TrustScoreHistoryRepository {
    TrustScoreHistory save(TrustScoreHistory history);
    List<TrustScoreHistory> findByRefereeId(Long refereeId);
}
