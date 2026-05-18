package com.pickleball.infrastructure.persistence.adapters;
import com.pickleball.domain.entities.TrustScoreHistory;
import com.pickleball.domain.repositories.TrustScoreHistoryRepository;
import com.pickleball.infrastructure.persistence.entities.TrustScoreHistoryEntity;
import com.pickleball.infrastructure.persistence.mappers.TrustScoreHistoryMapper;
import com.pickleball.infrastructure.persistence.repositories.TrustScoreHistoryJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import java.util.List;
import java.util.stream.Collectors;
@Component
@RequiredArgsConstructor
public class TrustScoreHistoryRepositoryAdapter implements TrustScoreHistoryRepository {
    private final TrustScoreHistoryJpaRepository jpaRepository;
    @Override
    public TrustScoreHistory save(TrustScoreHistory history) {
        TrustScoreHistoryEntity entity = TrustScoreHistoryMapper.toEntity(history);
        return TrustScoreHistoryMapper.toDomain(jpaRepository.save(entity));
    }
    @Override
    public List<TrustScoreHistory> findByRefereeId(Long refereeId) {
        return jpaRepository.findByRefereeIdOrderByChangedAtDesc(refereeId).stream()
                .map(TrustScoreHistoryMapper::toDomain)
                .collect(Collectors.toList());
    }
}
