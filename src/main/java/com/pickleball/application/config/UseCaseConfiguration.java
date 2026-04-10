package com.pickleball.application.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.pickleball.application.services.SettlementService;
import com.pickleball.application.usecases.booking.CreateBookingUseCase;
import com.pickleball.application.usecases.booking.CreateCasualMatchUseCase;
import com.pickleball.application.usecases.booking.CreatePrivateBookingUseCase;
import com.pickleball.application.usecases.booking.CreateRankedMatchUseCase;
import com.pickleball.application.usecases.booking.CreateWalkInBookingUseCase;
import com.pickleball.application.usecases.booking.CheckInUseCase;
import com.pickleball.application.usecases.booking.JoinBookingUseCase;
import com.pickleball.application.usecases.booking.ConfirmMatchResultUseCase;
import com.pickleball.application.usecases.booking.ResolveDisputeUseCase;
import com.pickleball.application.usecases.booking.SubmitDisputeUseCase;
import com.pickleball.application.usecases.booking.SubmitMatchResultUseCase;
import com.pickleball.application.usecases.booking.UpdateEloUseCase;
import com.pickleball.application.usecases.referee.*;
import com.pickleball.application.usecases.timeslot.*;
import com.pickleball.application.usecases.user.*;
import com.pickleball.application.usecases.venue.ApproveVenueUseCase;
import com.pickleball.application.usecases.venue.CreateCourtUseCase;
import com.pickleball.application.usecases.venue.CreateVenueUseCase;
import com.pickleball.application.usecases.venue.GetActiveCourtsUseCase;
import com.pickleball.application.usecases.venue.GetCourtByIdUseCase;
import com.pickleball.application.usecases.venue.GetPendingVenuesUseCase;
import com.pickleball.application.usecases.venue.GetVenueCourtsUseCase;
import com.pickleball.application.usecases.venue.RejectVenueUseCase;
import com.pickleball.application.usecases.venue.SearchVenuesUseCase;
import com.pickleball.application.usecases.venue.ToggleCourtStatusUseCase;
import com.pickleball.application.usecases.venue.ToggleVenueStatusUseCase;
import com.pickleball.application.usecases.venue.UpdateCourtUseCase;
import com.pickleball.application.usecases.venue.UpdateVenueUseCase;
import com.pickleball.domain.repositories.*;
import com.pickleball.domain.services.MatchmakingService;
import com.pickleball.domain.services.PaymentService;
import com.pickleball.domain.services.PriceCalculationService;
import com.pickleball.domain.services.RefereeMatchService;
import com.pickleball.domain.services.TeamBalancingService;
import com.pickleball.domain.services.EloCalculationService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
public class UseCaseConfiguration {

    @Bean
    public RegisterUserUseCase registerUserUseCase(UserRepository userRepository,
                                                   PlayerRepository playerRepository,
                                                   PasswordEncoder passwordEncoder) {
        return new RegisterUserUseCase(userRepository, playerRepository, passwordEncoder);
    }

    @Bean
    public LoginUserUseCase loginUserUseCase(UserRepository userRepository,
                                             PasswordEncoder passwordEncoder) {
        return new LoginUserUseCase(userRepository, passwordEncoder);
    }

    @Bean
    public GetUserProfileUseCase getUserProfileUseCase(UserRepository userRepository) {
        return new GetUserProfileUseCase(userRepository);
    }

    @Bean
    public CreateVenueUseCase createVenueUseCase(VenueRepository venueRepository) {
        return new CreateVenueUseCase(venueRepository);
    }

    @Bean
    public UpdateVenueUseCase updateVenueUseCase(VenueRepository venueRepository) {
        return new UpdateVenueUseCase(venueRepository);
    }

    @Bean
    public ApproveVenueUseCase approveVenueUseCase(VenueRepository venueRepository) {
        return new ApproveVenueUseCase(venueRepository);
    }

    @Bean
    public RejectVenueUseCase rejectVenueUseCase(VenueRepository venueRepository) {
        return new RejectVenueUseCase(venueRepository);
    }

    @Bean
    public GetPendingVenuesUseCase getPendingVenuesUseCase(VenueRepository venueRepository) {
        return new GetPendingVenuesUseCase(venueRepository);
    }

    @Bean
    public SearchVenuesUseCase searchVenuesUseCase(VenueRepository venueRepository) {
        return new SearchVenuesUseCase(venueRepository);
    }

    @Bean
    public CreateBookingUseCase createBookingUseCase(BookingRepository bookingRepository,
                                                     CourtRepository courtRepository) {
        return new CreateBookingUseCase(bookingRepository, courtRepository);
    }

    @Bean
    public CreatePrivateBookingUseCase createPrivateBookingUseCase(
            BookingRepository bookingRepository,
            CourtRepository courtRepository,
            CourtPricingRepository courtPricingRepository,
            PriceCalculationService priceCalculationService,
            PaymentService paymentService) {
        return new CreatePrivateBookingUseCase(
                bookingRepository,
                courtRepository,
                courtPricingRepository,
                priceCalculationService,
                paymentService);
    }

    @Bean
    public CreateWalkInBookingUseCase createWalkInBookingUseCase(
            BookingRepository bookingRepository,
            CourtRepository courtRepository,
            CourtPricingRepository courtPricingRepository,
            VenueStaffRepository venueStaffRepository,
            PriceCalculationService priceCalculationService) {
        return new CreateWalkInBookingUseCase(
                bookingRepository,
                courtRepository,
                courtPricingRepository,
                venueStaffRepository,
                priceCalculationService);
    }

    @Bean
    public MatchmakingService matchmakingService() {
        return new MatchmakingService();
    }

    @Bean
    public CreateCasualMatchUseCase createCasualMatchUseCase(
            BookingRepository bookingRepository,
            CourtRepository courtRepository,
            CourtPricingRepository courtPricingRepository,
            PlayerRepository playerRepository,
            VenueRepository venueRepository,
            PriceCalculationService priceCalculationService,
            PaymentService paymentService,
            MatchmakingService matchmakingService) {
        return new CreateCasualMatchUseCase(
                bookingRepository,
                courtRepository,
                courtPricingRepository,
                playerRepository,
                venueRepository,
                priceCalculationService,
                paymentService,
                matchmakingService);
    }

    @Bean
    public TeamBalancingService teamBalancingService(PlayerRepository playerRepository) {
        return new TeamBalancingService(playerRepository);
    }

    @Bean
    public EloCalculationService eloCalculationService() {
        return new EloCalculationService();
    }

    @Bean
    public UpdateEloUseCase updateEloUseCase(
            RankedMatchRepository rankedMatchRepository,
            BookingRepository bookingRepository,
            PlayerRepository playerRepository,
            EloHistoryRepository eloHistoryRepository,
            SkillRatingHistoryRepository skillRatingHistoryRepository,
            EloCalculationService eloCalculationService) {
        return new UpdateEloUseCase(
                rankedMatchRepository,
                bookingRepository,
                playerRepository,
                eloHistoryRepository,
                skillRatingHistoryRepository,
                eloCalculationService);
    }

    @Bean
    public JoinBookingUseCase joinBookingUseCase(
            BookingRepository bookingRepository,
            PlayerRepository playerRepository,
            RefereeRepository refereeRepository,
            RankedMatchRepository rankedMatchRepository,
            PaymentService paymentService,
            MatchmakingService matchmakingService,
            TeamBalancingService teamBalancingService) {
        return new JoinBookingUseCase(bookingRepository, playerRepository, refereeRepository,
                rankedMatchRepository, paymentService, matchmakingService, teamBalancingService);
    }

    @Bean
    public CheckInUseCase checkInUseCase(
            BookingRepository bookingRepository,
            CheckInRepository checkInRepository,
            CourtRepository courtRepository,
            VenueRepository venueRepository,
            MatchmakingService matchmakingService) {
        return new CheckInUseCase(bookingRepository, checkInRepository, courtRepository, venueRepository, matchmakingService);
    }

    @Bean
    public CreateRankedMatchUseCase createRankedMatchUseCase(
            BookingRepository bookingRepository,
            CourtRepository courtRepository,
            CourtPricingRepository courtPricingRepository,
            PlayerRepository playerRepository,
            VenueRepository venueRepository,
            RefereeRepository refereeRepository,
            RankedMatchRepository rankedMatchRepository,
            PriceCalculationService priceCalculationService,
            PaymentService paymentService,
            MatchmakingService matchmakingService) {
        return new CreateRankedMatchUseCase(
                bookingRepository,
                courtRepository,
                courtPricingRepository,
                playerRepository,
                venueRepository,
                refereeRepository,
                rankedMatchRepository,
                priceCalculationService,
                paymentService,
                matchmakingService);
    }

    @Bean
    public SubmitVenueOwnerRequestUseCase submitVenueOwnerRequestUseCase(
            RoleRequestRepository roleRequestRepository,
            UserRepository userRepository) {
        return new SubmitVenueOwnerRequestUseCase(roleRequestRepository, userRepository);
    }

    @Bean
    public ApproveVenueOwnerRequestUseCase approveVenueOwnerRequestUseCase(
            RoleRequestRepository roleRequestRepository,
            VenueOwnerRepository venueOwnerRepository,
            ObjectMapper objectMapper) {
        return new ApproveVenueOwnerRequestUseCase(roleRequestRepository, venueOwnerRepository, objectMapper);
    }

    @Bean
    public RejectVenueOwnerRequestUseCase rejectVenueOwnerRequestUseCase(
            RoleRequestRepository roleRequestRepository) {
        return new RejectVenueOwnerRequestUseCase(roleRequestRepository);
    }

    @Bean
    public GetPendingRoleRequestsUseCase getPendingRoleRequestsUseCase(
            RoleRequestRepository roleRequestRepository) {
        return new GetPendingRoleRequestsUseCase(roleRequestRepository);
    }

    @Bean
    public CreateCourtUseCase createCourtUseCase(
            CourtRepository courtRepository,
            VenueRepository venueRepository,
            CourtPricingRepository courtPricingRepository) {
        return new CreateCourtUseCase(courtRepository, venueRepository, courtPricingRepository);
    }

    @Bean
    public UpdateCourtUseCase updateCourtUseCase(
            CourtRepository courtRepository,
            VenueRepository venueRepository) {
        return new UpdateCourtUseCase(courtRepository, venueRepository);
    }

    @Bean
    public GetVenueCourtsUseCase getVenueCourtsUseCase(CourtRepository courtRepository) {
        return new GetVenueCourtsUseCase(courtRepository);
    }

    @Bean
    public GetCourtByIdUseCase getCourtByIdUseCase(CourtRepository courtRepository) {
        return new GetCourtByIdUseCase(courtRepository);
    }

    @Bean
    public ToggleCourtStatusUseCase toggleCourtStatusUseCase(
            CourtRepository courtRepository,
            VenueRepository venueRepository) {
        return new ToggleCourtStatusUseCase(courtRepository, venueRepository);
    }

    @Bean
    public GetActiveCourtsUseCase getActiveCourtsUseCase(CourtRepository courtRepository) {
        return new GetActiveCourtsUseCase(courtRepository);
    }

    @Bean
    public ToggleVenueStatusUseCase toggleVenueStatusUseCase(VenueRepository venueRepository) {
        return new ToggleVenueStatusUseCase(venueRepository);
    }

    // TimeSlot use cases (simplified - on-demand generation)
    @Bean
    public PriceCalculationService priceCalculationService() {
        return new PriceCalculationService();
    }

    @Bean
    public GetAvailableSlotsUseCase getAvailableSlotsUseCase(
            BookingRepository bookingRepository,
            CourtPricingRepository courtPricingRepository,
            PriceCalculationService priceCalculationService) {
        return new GetAvailableSlotsUseCase(
                bookingRepository,
                courtPricingRepository,
                priceCalculationService);
    }

    // ==================== Referee Use Cases ====================

    @Bean
    public RefereeMatchService refereeMatchService() {
        return new RefereeMatchService();
    }

    @Bean
    public GenerateRefereeTestUseCase generateRefereeTestUseCase(
            TestQuestionRepository testQuestionRepository) {
        return new GenerateRefereeTestUseCase(testQuestionRepository);
    }

    @Bean
    public SubmitRefereeTestUseCase submitRefereeTestUseCase(
            TestQuestionRepository testQuestionRepository,
            TestAttemptRepository testAttemptRepository,
            RoleRequestRepository roleRequestRepository,
            RefereeRepository refereeRepository,
            UserRepository userRepository) {
        return new SubmitRefereeTestUseCase(
                testQuestionRepository,
                testAttemptRepository,
                roleRequestRepository,
                refereeRepository,
                userRepository);
    }

    @Bean
    public ApproveRefereeRequestUseCase approveRefereeRequestUseCase(
            RoleRequestRepository roleRequestRepository,
            RefereeRepository refereeRepository) {
        return new ApproveRefereeRequestUseCase(roleRequestRepository, refereeRepository);
    }

    @Bean
    public RejectRefereeRequestUseCase rejectRefereeRequestUseCase(
            RoleRequestRepository roleRequestRepository) {
        return new RejectRefereeRequestUseCase(roleRequestRepository);
    }

    @Bean
    public GetRefereeTestHistoryUseCase getRefereeTestHistoryUseCase(
            TestAttemptRepository testAttemptRepository) {
        return new GetRefereeTestHistoryUseCase(testAttemptRepository);
    }

    @Bean
    public SubmitMatchResultUseCase submitMatchResultUseCase(RankedMatchRepository rankedMatchRepository) {
        return new SubmitMatchResultUseCase(rankedMatchRepository);
    }

    @Bean
    public SettlementService settlementService(
            BookingRepository bookingRepository,
            WalletRepository walletRepository,
            TransactionRepository transactionRepository,
            VenueRepository venueRepository,
            CourtRepository courtRepository) {
        return new SettlementService(bookingRepository, walletRepository, transactionRepository, venueRepository, courtRepository);
    }

    @Bean
    public ConfirmMatchResultUseCase confirmMatchResultUseCase(
            RankedMatchRepository rankedMatchRepository,
            BookingRepository bookingRepository,
            UpdateEloUseCase updateEloUseCase,
            SettlementService settlementService) {
        return new ConfirmMatchResultUseCase(rankedMatchRepository, bookingRepository, updateEloUseCase, settlementService);
    }

    @Bean
    public SubmitDisputeUseCase submitDisputeUseCase(
            RankedMatchRepository rankedMatchRepository,
            MatchDisputeRepository matchDisputeRepository) {
        return new SubmitDisputeUseCase(rankedMatchRepository, matchDisputeRepository);
    }

    @Bean
    public ResolveDisputeUseCase resolveDisputeUseCase(
            MatchDisputeRepository matchDisputeRepository,
            RankedMatchRepository rankedMatchRepository,
            BookingRepository bookingRepository,
            RefereeRepository refereeRepository,
            UpdateEloUseCase updateEloUseCase,
            SettlementService settlementService,
            PaymentService paymentService) {
        return new ResolveDisputeUseCase(
                matchDisputeRepository, 
                rankedMatchRepository, 
                bookingRepository,
                refereeRepository, 
                updateEloUseCase, 
                settlementService,
                paymentService);
    }

    @Bean
    public SubmitRefereeEvidenceUseCase submitRefereeEvidenceUseCase(
            MatchDisputeRepository matchDisputeRepository,
            RankedMatchRepository rankedMatchRepository) {
        return new SubmitRefereeEvidenceUseCase(matchDisputeRepository, rankedMatchRepository);
    }
}