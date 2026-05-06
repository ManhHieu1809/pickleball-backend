package com.pickleball.application.config;

import com.pickleball.application.services.VenueApplicationService;
import com.pickleball.domain.repositories.*;
import com.pickleball.application.usecases.venue.CreateCourtUseCase;
import com.pickleball.application.usecases.venue.CreateVenueUseCase;
import com.pickleball.application.usecases.venue.GetActiveCourtsUseCase;
import com.pickleball.application.usecases.venue.GetCourtByIdUseCase;
import com.pickleball.application.usecases.venue.GetVenueCourtsUseCase;
import com.pickleball.application.usecases.venue.SearchVenuesUseCase;
import com.pickleball.application.usecases.venue.ToggleCourtStatusUseCase;
import com.pickleball.application.usecases.venue.ToggleVenueStatusUseCase;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ServiceConfiguration {

    @Bean
    public VenueApplicationService venueApplicationService(
            CreateVenueUseCase createVenueUseCase,
            SearchVenuesUseCase searchVenuesUseCase,
            CreateCourtUseCase createCourtUseCase,
            GetVenueCourtsUseCase getVenueCourtsUseCase,
            GetCourtByIdUseCase getCourtByIdUseCase,
            ToggleCourtStatusUseCase toggleCourtStatusUseCase,
            GetActiveCourtsUseCase getActiveCourtsUseCase,
            ToggleVenueStatusUseCase toggleVenueStatusUseCase,
            VenueRepository venueRepository,
            VenueOwnerRepository venueOwnerRepository,
            CourtPricingRepository courtPricingRepository) {
        return new VenueApplicationService(
                createVenueUseCase,
                searchVenuesUseCase,
                createCourtUseCase,
                getVenueCourtsUseCase,
                getCourtByIdUseCase,
                toggleCourtStatusUseCase,
                getActiveCourtsUseCase,
                toggleVenueStatusUseCase,
                venueRepository,
                venueOwnerRepository,
                courtPricingRepository
        );
    }
}