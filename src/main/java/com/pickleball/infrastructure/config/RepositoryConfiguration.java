package com.pickleball.infrastructure.config;

import com.pickleball.domain.repositories.*;
import com.pickleball.infrastructure.persistence.adapters.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RepositoryConfiguration {

    @Bean
    public UserRepository userRepository(UserRepositoryAdapter adapter) {
        return adapter;
    }

    @Bean
    public PlayerRepository playerRepository(PlayerRepositoryAdapter adapter) {
        return adapter;
    }

    @Bean
    public VenueRepository venueRepository(VenueRepositoryAdapter adapter) {
        return adapter;
    }

    @Bean
    public BookingRepository bookingRepository(BookingRepositoryAdapter adapter) {
        return adapter;
    }

    @Bean
    public CourtRepository courtRepository(CourtRepositoryAdapter adapter) {
        return adapter;
    }

    @Bean
    public CourtPricingRepository courtPricingRepository(CourtPricingRepositoryAdapter adapter) {
        return adapter;
    }

    @Bean
    public VenueStaffRepository venueStaffRepository(VenueStaffRepositoryAdapter adapter) {
        return adapter;
    }
}