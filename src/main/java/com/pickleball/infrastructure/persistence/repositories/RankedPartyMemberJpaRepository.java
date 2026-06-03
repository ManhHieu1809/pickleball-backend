package com.pickleball.infrastructure.persistence.repositories;

import com.pickleball.infrastructure.persistence.entities.RankedPartyMemberEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface RankedPartyMemberJpaRepository extends JpaRepository<RankedPartyMemberEntity, Long> {
    List<RankedPartyMemberEntity> findByPartyIdOrderByCreatedAtAsc(Long partyId);
    List<RankedPartyMemberEntity> findByUserIdAndStatusOrderByCreatedAtDesc(Long userId, String status);
    List<RankedPartyMemberEntity> findByUserIdAndStatusInOrderByCreatedAtDesc(Long userId, List<String> statuses);
    Optional<RankedPartyMemberEntity> findByPartyIdAndUserId(Long partyId, Long userId);
    long countByUserIdAndStatus(Long userId, String status);
}
