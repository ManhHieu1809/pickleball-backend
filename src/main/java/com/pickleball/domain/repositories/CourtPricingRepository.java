package com.pickleball.domain.repositories;

import com.pickleball.domain.entities.CourtPricing;
import java.util.List;

public interface CourtPricingRepository {
    CourtPricing save(CourtPricing courtPricing);
    List<CourtPricing> findByCourtId(Long courtId);
    List<CourtPricing> findByCourtIdAndDayOfWeek(Long courtId, Integer dayOfWeek);
}