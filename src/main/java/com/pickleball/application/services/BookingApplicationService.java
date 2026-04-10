package com.pickleball.application.services;

import com.pickleball.application.dtos.BookingDTO;
import com.pickleball.application.dtos.CasualMatchDTO;
import com.pickleball.application.dtos.PaymentDTO;
import com.pickleball.application.dtos.PlayerMatchDTO;
import com.pickleball.application.dtos.RankedMatchDTO;
import com.pickleball.application.dtos.RefereeMatchDTO;
import com.pickleball.application.dtos.MatchDisputeDTO;
import com.pickleball.application.dtos.requests.CreateBookingRequest;
import com.pickleball.application.dtos.requests.CheckInRequest;
import com.pickleball.application.dtos.requests.ConfirmResultRequest;
import com.pickleball.application.dtos.requests.JoinBookingRequest;
import com.pickleball.application.dtos.requests.SubmitResultRequest;
import com.pickleball.application.dtos.requests.SubmitDisputeRequest;
import com.pickleball.application.usecases.booking.CheckInUseCase;
import com.pickleball.application.usecases.booking.ConfirmMatchResultUseCase;
import com.pickleball.application.usecases.booking.CreateBookingUseCase;
import com.pickleball.application.usecases.booking.CreateCasualMatchUseCase;
import com.pickleball.application.usecases.booking.CreateCasualMatchUseCase.CasualMatchResult;
import com.pickleball.application.usecases.booking.CreatePrivateBookingUseCase;
import com.pickleball.application.usecases.booking.CreatePrivateBookingUseCase.BookingWithPayment;
import com.pickleball.application.usecases.booking.CreateRankedMatchUseCase;
import com.pickleball.application.usecases.booking.CreateRankedMatchUseCase.RankedMatchResult;
import com.pickleball.application.usecases.booking.JoinBookingUseCase;
import com.pickleball.application.usecases.booking.JoinBookingUseCase.JoinResult;
import com.pickleball.application.usecases.booking.SubmitMatchResultUseCase;
import com.pickleball.application.usecases.booking.SubmitDisputeUseCase;
import com.pickleball.application.usecases.booking.UpdateEloUseCase;
import com.pickleball.domain.entities.Booking;
import com.pickleball.domain.entities.MatchDispute;
import com.pickleball.domain.entities.Player;
import com.pickleball.domain.entities.Referee;
import com.pickleball.domain.enums.BookingStatus;
import com.pickleball.domain.enums.BookingType;
import com.pickleball.domain.enums.JoinStatus;
import com.pickleball.domain.repositories.BookingRepository;
import com.pickleball.domain.repositories.CourtRepository;
import com.pickleball.domain.repositories.PlayerRepository;
import com.pickleball.domain.repositories.RefereeRepository;
import com.pickleball.domain.repositories.VenueRepository;
import com.pickleball.domain.services.PaymentService;
import com.pickleball.domain.services.PaymentService.PaymentResult;
import com.pickleball.domain.valueobjects.Money;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class BookingApplicationService {

    private final CreateBookingUseCase createBookingUseCase;
    private final CreatePrivateBookingUseCase createPrivateBookingUseCase;
    private final CreateCasualMatchUseCase createCasualMatchUseCase;
    private final CreateRankedMatchUseCase createRankedMatchUseCase;
    private final JoinBookingUseCase joinBookingUseCase;
    private final CheckInUseCase checkInUseCase;
    private final SubmitMatchResultUseCase submitMatchResultUseCase;
    private final ConfirmMatchResultUseCase confirmMatchResultUseCase;
    private final SubmitDisputeUseCase submitDisputeUseCase;
    private final UpdateEloUseCase updateEloUseCase;
    private final CourtRepository courtRepository;
    private final BookingRepository bookingRepository;
    private final PlayerRepository playerRepository;
    private final RefereeRepository refereeRepository;
    private final VenueRepository venueRepository;
    private final PaymentService paymentService;

    public BookingDTO createBooking(CreateBookingRequest request) {
        courtRepository.findById(request.getCourtId())
                .orElseThrow(() -> new IllegalArgumentException("Court not found"));

        if (request.getBookingType() == BookingType.PRIVATE) {
            return createPrivateBooking(request);
        }

        if (request.getBookingType() == BookingType.CASUAL) {
            // Return basic BookingDTO (use createCasualMatch for full response with
            // candidates)
            CasualMatchDTO casualMatch = createCasualMatch(request);
            return casualMatch.getBooking();
        }

        if (request.getBookingType() == BookingType.RANKED) {
            RankedMatchDTO rankedMatch = createRankedMatch(request);
            return rankedMatch.getBooking();
        }

        // For other booking types (RANKED, WALK_IN) - use existing logic
        // TODO: Implement specific use cases for each type
        Booking booking = createBookingUseCase.execute(
                request.getCourtId(),
                request.getStartTime(),
                request.getEndTime(),
                request.getBookingType(),
                request.getCreatorUserId(),
                request.isPlayer());

        Booking savedBooking = bookingRepository.save(booking);
        return convertToDTO(savedBooking);
    }

    /**
     * Create Casual Match with candidates (WORKFLOW §II.2)
     */
    public CasualMatchDTO createCasualMatch(CreateBookingRequest request) {
        CasualMatchResult result = createCasualMatchUseCase.execute(
                request.getCourtId(),
                request.getStartTime(),
                request.getEndTime(),
                request.getCreatorUserId(),
                request.getNotes());

        BookingDTO bookingDTO = convertToDTO(result.booking());
        PaymentDTO paymentDTO = convertPaymentToDTO(result.paymentResult());

        long paidCount = result.booking().getParticipants().stream()
                .filter(p -> p.getJoinStatus() == JoinStatus.PAID || p.getJoinStatus() == JoinStatus.PENDING)
                .count();

        List<PlayerMatchDTO> candidateDTOs = result.booking().getParticipants().stream()
                .filter(p -> p.getJoinStatus() == JoinStatus.PAID || p.getJoinStatus() == JoinStatus.PENDING)
                .map(p -> playerRepository.findByUserId(p.getUserId())
                        .map(this::convertPlayerToMatchDTO)
                        .orElse(null))
                .filter(p -> p != null)
                .collect(Collectors.toList());

        return CasualMatchDTO.builder()
                .booking(bookingDTO)
                .payment(paymentDTO)
                .depositPerPlayer(result.depositPerPlayer().getAmount())
                .depositCurrency(result.depositPerPlayer().getCurrency())
                .currentPlayerCount((int) paidCount)
                .requiredPlayerCount(4)
                .candidates(candidateDTOs)
                .build();
    }

    /**
     * Get candidates for an existing casual match
     */
    public List<PlayerMatchDTO> getCasualMatchCandidates(Long bookingId) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new IllegalArgumentException("Booking not found"));

        if (booking.getBookingType() != BookingType.CASUAL) {
            throw new IllegalArgumentException("Booking is not a casual match");
        }
        if (booking.getStatus() != BookingStatus.PENDING) {
            throw new IllegalArgumentException("Casual match is no longer accepting players");
        }

        // Return actual participants instead of randomly suggested candidates
        return booking.getParticipants().stream()
                .filter(p -> p.getJoinStatus() == JoinStatus.PAID || p.getJoinStatus() == JoinStatus.PENDING)
                .map(p -> playerRepository.findByUserId(p.getUserId())
                        .map(this::convertPlayerToMatchDTO)
                        .orElse(null))
                .filter(p -> p != null)
                .collect(Collectors.toList());
    }

    // ==================== Ranked Match Methods ====================

    /**
     * Create Ranked Match with candidates (WORKFLOW §II.3)
     */
    public RankedMatchDTO createRankedMatch(CreateBookingRequest request) {
        RankedMatchResult result = createRankedMatchUseCase.execute(
                request.getCourtId(),
                request.getStartTime(),
                request.getEndTime(),
                request.getCreatorUserId(),
                request.getNotes());

        BookingDTO bookingDTO = convertToDTO(result.booking());
        PaymentDTO paymentDTO = convertPaymentToDTO(result.paymentResult());

        long paidCount = result.booking().getParticipants().stream()
                .filter(p -> p.getJoinStatus() == JoinStatus.PAID)
                .filter(p -> p.getRole() != com.pickleball.domain.enums.ParticipantRole.REFEREE)
                .count();

        List<PlayerMatchDTO> playerCandidateDTOs = result.playerCandidates().stream()
                .map(this::convertPlayerToMatchDTO)
                .collect(Collectors.toList());

        // --- DEVELOPMENT / TEST MODE: GENERATE BOTS IF ALONE ---
        // If there are less than 3 candidates, simulate them!
        int botsNeeded = 3 - playerCandidateDTOs.size();
        for (int i = 0; i < botsNeeded; i++) {
            PlayerMatchDTO bot = PlayerMatchDTO.builder()
                .userId((long) (9000 + i)) // Fake ID
                .fullName("Autobot_" + (i + 1))
                .currentElo(1250 + (i * 15)) // Arbitrary Elo
                .loyaltyTier("SILVER")
                .build();
            playerCandidateDTOs.add(bot);
        }
        // --------------------------------------------------------

        List<RefereeMatchDTO> refereeCandidateDTOs = result.refereeCandidates().stream()
                .map(this::convertRefereeToMatchDTO)
                .collect(Collectors.toList());

        // --- DEVELOPMENT / TEST MODE: GENERATE REFEREE BOT IF ALONE ---
        if (refereeCandidateDTOs.isEmpty()) {
            RefereeMatchDTO botRef = RefereeMatchDTO.builder()
                .userId(8888L) // Fake ID
                .fullName("RefBot_Supreme")
                .trustScore(new java.math.BigDecimal("9.8"))
                .totalMatchesRefereed(150)
                .isEligible(true)
                .build();
            refereeCandidateDTOs.add(botRef);
        }
        // ---------------------------------------------------------------

        return RankedMatchDTO.builder()
                .booking(bookingDTO)
                .payment(paymentDTO)
                .depositPerPlayer(result.depositPerPlayer().getAmount())
                .depositCurrency(result.depositPerPlayer().getCurrency())
                .venueFee(result.booking().getVenueFee() != null ? result.booking().getVenueFee().getAmount() : null)
                .refereeFee(result.booking().getRefereeFee() != null ? result.booking().getRefereeFee().getAmount() : null)
                .platformFee(result.booking().getPlatformFee() != null ? result.booking().getPlatformFee().getAmount() : null)
                .totalCost(result.booking().getTotalCost() != null ? result.booking().getTotalCost().getAmount() : null)
                .currentPlayerCount((int) paidCount)
                .requiredPlayerCount(4)
                .playerCandidates(playerCandidateDTOs)
                .refereeAssigned(false)
                .refereeCandidates(refereeCandidateDTOs)
                .rankedMatchId(result.rankedMatch() != null ? result.rankedMatch().getId() : null)
                .matchStatus(result.rankedMatch() != null ? result.rankedMatch().getStatus().name() : null)
                .build();
    }

    /**
     * Get available ranked matches that are PENDING (for players to browse & join)
     */
    public List<RankedMatchDTO> getAvailableRankedMatches() {
        List<Booking> pendingRanked = bookingRepository.findByBookingTypeAndStatus(
                BookingType.RANKED, BookingStatus.PENDING);

        return pendingRanked.stream()
                .map(booking -> {
                    long paidCount = booking.getParticipants().stream()
                            .filter(p -> p.getJoinStatus() == JoinStatus.PAID)
                            .filter(p -> p.getRole() != com.pickleball.domain.enums.ParticipantRole.REFEREE)
                            .count();

                    BigDecimal depositPerPlayer = booking.getTotalCost() != null
                            ? booking.getTotalCost().getAmount().multiply(new BigDecimal("0.25"))
                            : new BigDecimal("50000");

                    boolean hasReferee = booking.getParticipants().stream()
                            .anyMatch(p -> p.getRole() == com.pickleball.domain.enums.ParticipantRole.REFEREE
                                    && p.hasPaid());

                    return RankedMatchDTO.builder()
                            .booking(convertToDTO(booking))
                            .depositPerPlayer(depositPerPlayer)
                            .depositCurrency("VND")
                            .venueFee(booking.getVenueFee() != null ? booking.getVenueFee().getAmount() : null)
                            .refereeFee(booking.getRefereeFee() != null ? booking.getRefereeFee().getAmount() : null)
                            .totalCost(booking.getTotalCost() != null ? booking.getTotalCost().getAmount() : null)
                            .currentPlayerCount((int) paidCount)
                            .requiredPlayerCount(4)
                            .refereeAssigned(hasReferee)
                            .build();
                })
                .collect(Collectors.toList());
    }

    /**
     * Get player & referee candidates for an existing ranked match
     */
    public RankedMatchDTO getRankedMatchCandidates(Long bookingId) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new IllegalArgumentException("Booking not found"));

        if (booking.getBookingType() != BookingType.RANKED) {
            throw new IllegalArgumentException("Booking is not a ranked match");
        }
        if (booking.getStatus() != BookingStatus.PENDING) {
            throw new IllegalArgumentException("Ranked match is no longer accepting players");
        }

        Player hostPlayer = playerRepository.findByUserId(booking.getCreatedByPlayerId())
                .orElseThrow(() -> new IllegalArgumentException("Host player not found"));

        List<Player> playerCandidates = createRankedMatchUseCase.findPlayerCandidates(hostPlayer, bookingId);

        // Get eligible referees directly from repository
        List<Referee> allEligible = refereeRepository.findEligibleReferees();
        List<Referee> eligibleReferees = createRankedMatchUseCase.findRefereeCandidates(
                allEligible, hostPlayer.getUserId());

        List<PlayerMatchDTO> playerDTOs = playerCandidates.stream()
                .map(this::convertPlayerToMatchDTO)
                .collect(Collectors.toList());

        // --- DEVELOPMENT / TEST MODE: GENERATE BOTS IF ALONE ---
        // If there are less than 3 candidates, simulate them!
        int botsNeeded = 3 - playerDTOs.size();
        for (int i = 0; i < botsNeeded; i++) {
            PlayerMatchDTO bot = PlayerMatchDTO.builder()
                .userId((long) (9000 + i)) // Fake ID
                .fullName("Autobot_" + (i + 1))
                .currentElo(1250 + (i * 15)) // Arbitrary Elo
                .loyaltyTier("SILVER")
                .build();
            playerDTOs.add(bot);
        }
        // --------------------------------------------------------

        List<RefereeMatchDTO> refereeDTOs = eligibleReferees.stream()
                .map(this::convertRefereeToMatchDTO)
                .collect(Collectors.toList());

        // --- DEVELOPMENT / TEST MODE: GENERATE REFEREE BOT IF ALONE ---
        if (refereeDTOs.isEmpty()) {
            RefereeMatchDTO botRef = RefereeMatchDTO.builder()
                .userId(8888L) // Fake ID
                .fullName("RefBot_Supreme")
                .trustScore(new java.math.BigDecimal("9.8"))
                .totalMatchesRefereed(150)
                .isEligible(true)
                .build();
            refereeDTOs.add(botRef);
        }
        // ---------------------------------------------------------------

        return RankedMatchDTO.builder()
                .booking(convertToDTO(booking))
                .playerCandidates(playerDTOs)
                .refereeCandidates(refereeDTOs)
                .build();
    }

    /**
     * Get available casual matches that are PENDING (for players to browse & join)
     */
    public List<CasualMatchDTO> getAvailableCasualMatches() {
        List<Booking> pendingCasual = bookingRepository.findByBookingTypeAndStatus(
                BookingType.CASUAL, BookingStatus.PENDING);

        return pendingCasual.stream()
                .map(booking -> {
                    long paidCount = booking.getParticipants().stream()
                            .filter(p -> p.getJoinStatus() == JoinStatus.PAID || p.getJoinStatus() == JoinStatus.PENDING)
                            .count();

                    BigDecimal depositPerPlayer = booking.getVenueFee() != null
                            ? booking.getVenueFee().getAmount().multiply(new BigDecimal("0.25"))
                            : new BigDecimal("50000");

                    return CasualMatchDTO.builder()
                            .booking(convertToDTO(booking))
                            .depositPerPlayer(depositPerPlayer)
                            .depositCurrency("VND")
                            .currentPlayerCount((int) paidCount)
                            .requiredPlayerCount(4)
                            .build();
                })
                .collect(Collectors.toList());
    }

    /**
     * Create Private Booking with Payment (WORKFLOW §II.1)
     */
    private BookingDTO createPrivateBooking(CreateBookingRequest request) {
        BookingWithPayment result = createPrivateBookingUseCase.execute(
                request.getCourtId(),
                request.getStartTime(),
                request.getEndTime(),
                request.getCreatorUserId());

        BookingDTO dto = convertToDTO(result.booking());
        dto.setPayment(convertPaymentToDTO(result.paymentResult()));
        return dto;
    }

    public BookingDTO joinBooking(Long bookingId, JoinBookingRequest request) {
        boolean asReferee = request.getAsReferee() != null && request.getAsReferee();
        JoinResult result = joinBookingUseCase.execute(bookingId, request.getUserId(), asReferee);

        BookingDTO dto = convertToDTO(result.booking());
        dto.setPayment(convertPaymentToDTO(result.paymentResult()));
        return dto;
    }

    public void checkIn(Long bookingId, CheckInRequest request) {
        checkInUseCase.execute(bookingId, request);
    }

    public BookingDTO cancelBooking(Long bookingId, Long userId) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new IllegalArgumentException("Booking not found"));

        if (!booking.getCreatorId().equals(userId)) {
            throw new IllegalArgumentException("Only the creator can cancel this booking");
        }
        if (!booking.canBeCancelled()) {
            throw new IllegalArgumentException("Booking cannot be cancelled (less than 24 hours before start)");
        }

        Money refundAmount = booking.calculateRefundAmount();
        booking.cancel();
        Booking cancelledBooking = bookingRepository.save(booking);

        BookingDTO dto = convertToDTO(cancelledBooking);
        if (refundAmount.getAmount().compareTo(BigDecimal.ZERO) > 0) {
            String originalTransactionId = "BOOKING_" + bookingId;
            PaymentResult refundResult = paymentService.refund(originalTransactionId, refundAmount,
                    "Booking cancelled by user");
            dto.setPayment(convertPaymentToDTO(refundResult));
        }

        return dto;
    }

    public BookingDTO getBooking(Long bookingId) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new IllegalArgumentException("Booking not found"));

        return convertToDTO(booking);
    }

    public List<BookingDTO> getMyBookings(Long userId) {
        List<Booking> bookings = bookingRepository.findByPlayerId(userId);
        return bookings.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    public void submitMatchResult(Long bookingId, SubmitResultRequest request) {
        submitMatchResultUseCase.execute(bookingId, request.getRefereeUserId(),
                request.getTeamAScore(), request.getTeamBScore());
    }

    public void confirmMatchResult(Long bookingId, ConfirmResultRequest request) {
        confirmMatchResultUseCase.execute(bookingId, request.getPlayerUserId(), request.getAccepted());
        
        // Check if all players have confirmed and update Elo if so
        // Note: Ideally confirmMatchResultUseCase should return if match was CONFIRMED
        // Or we re-fetch match status here or inside UpdateEloUseCase (UpdateEloUseCase validates status)
        
        // For simplicity and decoupling, we can try to update Elo. 
        // If match is not CONFIRMED, UpdateEloUseCase will throw exception or should simply return.
        // Let's modify UpdateEloUseCase to be idempotent and safe to call.
        // Or better: check match status here before calling.
        
        try {
            updateEloUseCase.execute(bookingId);
        } catch (IllegalStateException e) {
            // Match not confirmed yet or already updated, ignore
        } catch (Exception e) {
            // Log error but don't fail the confirmation request
            e.printStackTrace();
        }
    }
    
    public void triggerEloUpdate(Long bookingId) {
         updateEloUseCase.execute(bookingId);
    }

    public MatchDisputeDTO submitDispute(SubmitDisputeRequest request) {
        MatchDispute dispute = submitDisputeUseCase.execute(
                request.getRankedMatchId(),
                request.getReportingPlayerId(),
                request.getReason(),
                request.getEvidence());
        
        return MatchDisputeDTO.builder()
                .id(dispute.getId())
                .rankedMatchId(dispute.getRankedMatchId())
                .reportingPlayerId(dispute.getReportingPlayerId())
                .reason(dispute.getReason())
                .status(dispute.getStatus())
                .build();
    }

    private PlayerMatchDTO convertPlayerToMatchDTO(Player player) {
        // We might want to fetch UserEntity for full name. For now, use "Player {id}" if full name is null
        return PlayerMatchDTO.builder()
                .userId(player.getUserId())
                .fullName("Player " + player.getUserId()) // Simulated placeholder as player entity might lack name
                .currentElo(player.getCurrentElo())
                .loyaltyTier(player.getLoyaltyTier() != null ? player.getLoyaltyTier().name() : null)
                .build();
    }

    private RefereeMatchDTO convertRefereeToMatchDTO(Referee referee) {
        return RefereeMatchDTO.builder()
                .userId(referee.getUserId())
                .fullName("Referee " + referee.getUserId()) // Simulated placeholder
                .trustScore(referee.getTrustScore())
                .totalMatchesRefereed(referee.getTotalMatchesRefereed())
                .isEligible(referee.isEligibleForMatch())
                .build();
    }

    private PaymentDTO convertPaymentToDTO(PaymentResult paymentResult) {
        if (paymentResult == null)
            return null;

        return PaymentDTO.builder()
                .transactionId(paymentResult.transactionId())
                .status(paymentResult.status())
                .amount(paymentResult.amount() != null ? paymentResult.amount().getAmount() : null)
                .currency(paymentResult.amount() != null ? paymentResult.amount().getCurrency() : null)
                .paymentUrl(paymentResult.paymentUrl())
                .message(paymentResult.message())
                .build();
    }

    private BookingDTO convertToDTO(Booking booking) {
        BookingDTO dto = new BookingDTO();
        dto.setId(booking.getId());
        dto.setCourtId(booking.getCourtId());
        dto.setStartTime(booking.getStartTime());
        dto.setEndTime(booking.getEndTime());
        dto.setBookingType(booking.getBookingType());
        dto.setStatus(booking.getStatus());
        dto.setCreatedByPlayerId(booking.getCreatedByPlayerId());
        dto.setCreatedByStaffId(booking.getCreatedByStaffId());
        dto.setNotes(booking.getNotes());

        if (booking.getCourtId() != null) {
            courtRepository.findById(booking.getCourtId()).ifPresent(court -> {
                dto.setCourtName(court.getCourtName());
                if (court.getVenueId() != null) {
                    dto.setVenueId(court.getVenueId());
                    venueRepository.findById(court.getVenueId()).ifPresent(venue -> {
                        dto.setVenueName(venue.getName());
                    });
                }
            });
        }

        if (booking.getVenueFee() != null) {
            dto.setVenueFee(booking.getVenueFee().getAmount());
        }
        if (booking.getRefereeFee() != null) {
            dto.setRefereeFee(booking.getRefereeFee().getAmount());
        }
        if (booking.getPlatformFee() != null) {
            dto.setPlatformFee(booking.getPlatformFee().getAmount());
        }
        if (booking.getTotalCost() != null) {
            dto.setTotalCost(booking.getTotalCost().getAmount());
        }

        dto.setCreatedAt(booking.getCreatedAt());

        return dto;
    }
}
