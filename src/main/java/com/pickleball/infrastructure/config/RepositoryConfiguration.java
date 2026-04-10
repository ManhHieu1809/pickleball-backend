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

    @Bean
    public RankedMatchRepository rankedMatchRepository(RankedMatchRepositoryAdapter adapter) {
        return adapter;
    }

    @Bean
    public RefereeRepository refereeRepository(RefereeRepositoryAdapter adapter) {
        return adapter;
    }

    @Bean
    public MatchDisputeRepository matchDisputeRepository(MatchDisputeRepositoryAdapter adapter) {
        return adapter;
    }

    @Bean
    public EloHistoryRepository eloHistoryRepository(EloHistoryRepositoryAdapter adapter) {
        return adapter;
    }

    @Bean
    public SkillRatingHistoryRepository skillRatingHistoryRepository(SkillRatingHistoryRepositoryAdapter adapter) {
        return adapter;
    }

    @Bean
    public RoleRequestRepository roleRequestRepository(RoleRequestRepositoryAdapter adapter) {
        return adapter;
    }

    @Bean
    public TestQuestionRepository testQuestionRepository(TestQuestionRepositoryAdapter adapter) {
        return adapter;
    }

    @Bean
    public TestAttemptRepository testAttemptRepository(TestAttemptRepositoryAdapter adapter) {
        return adapter;
    }

    @Bean
    public CheckInRepository checkInRepository(CheckInRepositoryAdapter adapter) {
        return adapter;
    }


    @Bean
    public VenueOwnerRepository venueOwnerRepository(VenueOwnerRepositoryAdapter adapter) {
        return adapter;
    }

    @Bean
    public WalletRepository walletRepository(WalletRepositoryAdapter adapter) {
        return adapter;
    }

    @Bean
    public TransactionRepository transactionRepository(TransactionRepositoryAdapter adapter) {
        return adapter;
    }
}