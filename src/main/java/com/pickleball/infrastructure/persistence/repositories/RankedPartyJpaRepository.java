package com.pickleball.infrastructure.persistence.repositories;

import com.pickleball.infrastructure.persistence.entities.RankedPartyEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface RankedPartyJpaRepository extends JpaRepository<RankedPartyEntity, Long> {
    List<RankedPartyEntity> findByHostUserIdAndStatusInOrderByCreatedAtDesc(Long hostUserId, List<String> statuses);
    List<RankedPartyEntity> findByMatchedBookingId(Long matchedBookingId);
}
