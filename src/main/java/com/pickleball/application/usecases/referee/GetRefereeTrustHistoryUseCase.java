package com.pickleball.application.usecases.referee;
import com.pickleball.domain.entities.TrustScoreHistory;
import com.pickleball.domain.repositories.TrustScoreHistoryRepository;
import lombok.RequiredArgsConstructor;
import java.util.List;
@RequiredArgsConstructor
public class GetRefereeTrustHistoryUseCase {
    private final TrustScoreHistoryRepository repository;
    public List<TrustScoreHistory> execute(Long refereeId) {
        return repository.findByRefereeId(refereeId);
    }
}
