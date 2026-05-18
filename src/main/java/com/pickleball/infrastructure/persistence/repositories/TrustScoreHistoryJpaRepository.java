package com.pickleball.infrastructure.persistence.repositories;
import com.pickleball.infrastructure.persistence.entities.TrustScoreHistoryEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
@Repository
public interface TrustScoreHistoryJpaRepository extends JpaRepository<TrustScoreHistoryEntity, Long> {
    List<TrustScoreHistoryEntity> findByRefereeIdOrderByChangedAtDesc(Long refereeId);
}
