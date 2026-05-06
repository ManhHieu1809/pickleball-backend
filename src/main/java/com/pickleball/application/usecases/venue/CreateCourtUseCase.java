package com.pickleball.application.usecases.venue;

import com.pickleball.domain.entities.Court;
import com.pickleball.domain.entities.CourtPricing;
import com.pickleball.domain.entities.Venue;
import com.pickleball.domain.repositories.CourtPricingRepository;
import com.pickleball.domain.repositories.CourtRepository;
import com.pickleball.domain.repositories.VenueRepository;
import com.pickleball.domain.valueobjects.Money;
import lombok.RequiredArgsConstructor;

import java.math.BigDecimal;
import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

@RequiredArgsConstructor
public class CreateCourtUseCase {
    private final CourtRepository courtRepository;
    private final VenueRepository venueRepository;
    private final CourtPricingRepository courtPricingRepository;

    public Court execute(Long venueId, String courtName, List<CourtPricingRequest> pricingRequests) {
        Venue venue = venueRepository.findById(venueId)
                .orElseThrow(() -> new IllegalArgumentException("Venue không tồn tại"));

        if (!venue.isActive()) {
            throw new IllegalArgumentException("Venue chưa được duyệt hoặc không hoạt động");
        }

        Court court = Court.builder()
                .venueId(venueId)
                .courtName(courtName)
                .isActive(true)
                .pricing(new ArrayList<>())
                .build();

        Court savedCourt = courtRepository.save(court);

        if (pricingRequests != null && !pricingRequests.isEmpty()) {
            for (CourtPricingRequest pricingRequest : pricingRequests) {
                CourtPricing pricing = CourtPricing.builder()
                        .courtId(savedCourt.getId())
                        .startTime(pricingRequest.getStartTime())
                        .endTime(pricingRequest.getEndTime())
                        .pricePerHour(new Money(pricingRequest.getPricePerHour(), "VND"))
                        .dayOfWeek(pricingRequest.getDayOfWeek() != null ?
                                DayOfWeek.of(pricingRequest.getDayOfWeek()) : null)
                        .build();

                CourtPricing savedPricing = courtPricingRepository.save(pricing);
                savedCourt.addPricing(savedPricing);
            }
        }

        return savedCourt;
    }

    public static class CourtPricingRequest {
        private LocalTime startTime;
        private LocalTime endTime;
        private BigDecimal pricePerHour;
        private Integer dayOfWeek;

        public CourtPricingRequest(LocalTime startTime, LocalTime endTime,
                                   BigDecimal pricePerHour, Integer dayOfWeek) {
            this.startTime = startTime;
            this.endTime = endTime;
            this.pricePerHour = pricePerHour;
            this.dayOfWeek = dayOfWeek;
        }

        public LocalTime getStartTime() { return startTime; }
        public LocalTime getEndTime() { return endTime; }
        public BigDecimal getPricePerHour() { return pricePerHour; }
        public Integer getDayOfWeek() { return dayOfWeek; }
    }
}

