package com.pickleball.application.services;

import com.pickleball.application.dtos.BookingDTO;
import com.pickleball.application.dtos.BookingParticipantDTO;
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
import com.pickleball.application.usecases.booking.AcceptMatchUseCase;
import com.pickleball.application.usecases.booking.SubmitMatchResultUseCase;
import com.pickleball.application.usecases.booking.SubmitDisputeUseCase;
import com.pickleball.application.usecases.booking.UpdateEloUseCase;
import com.pickleball.domain.entities.Booking;
import com.pickleball.domain.entities.MatchDispute;
import com.pickleball.domain.entities.Player;
import com.pickleball.domain.entities.RankedMatch;
import com.pickleball.domain.entities.Referee;
import com.pickleball.domain.entities.User;
import com.pickleball.domain.enums.BookingStatus;
import com.pickleball.domain.enums.BookingType;
import com.pickleball.domain.enums.JoinStatus;
import com.pickleball.domain.repositories.BookingRepository;
import com.pickleball.domain.repositories.CourtRepository;
import com.pickleball.domain.repositories.EloHistoryRepository;
import com.pickleball.domain.repositories.PlayerRepository;
import com.pickleball.domain.repositories.RankedMatchRepository;
import com.pickleball.domain.repositories.RefereeRepository;
import com.pickleball.domain.repositories.UserRepository;
import com.pickleball.domain.repositories.VenueRepository;
import com.pickleball.domain.services.EloCalculationService;
import com.pickleball.domain.services.PaymentService;
import com.pickleball.domain.services.PaymentService.PaymentResult;
import com.pickleball.domain.valueobjects.Money;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.pickleball.application.dtos.MatchResultDTO;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class BookingApplicationService {

    private final CreateBookingUseCase createBookingUseCase;
    private final CreateCasualMatchUseCase createCasualMatchUseCase;
    private final CreatePrivateBookingUseCase createPrivateBookingUseCase;
    private final CreateRankedMatchUseCase createRankedMatchUseCase;
    private final JoinBookingUseCase joinBookingUseCase;
    private final AcceptMatchUseCase acceptMatchUseCase;
    private final CheckInUseCase checkInUseCase;
    private final SubmitMatchResultUseCase submitMatchResultUseCase;
    private final ConfirmMatchResultUseCase confirmMatchResultUseCase;
    private final SubmitDisputeUseCase submitDisputeUseCase;
    private final UpdateEloUseCase updateEloUseCase;
    private final BookingRepository bookingRepository;
    private final PlayerRepository playerRepository;
    private final RefereeRepository refereeRepository;
    private final UserRepository userRepository;
    private final RankedMatchRepository rankedMatchRepository;
    private final EloHistoryRepository eloHistoryRepository;
    private final PaymentService paymentService;
    private final CourtRepository courtRepository;
    private final VenueRepository venueRepository;

    public BookingDTO createBooking(CreateBookingRequest request) {
        courtRepository.findById(request.getCourtId())
                .orElseThrow(() -> new IllegalArgumentException("Court not found"));

        if (request.getBookingType() == BookingType.PRIVATE) {
            return createPrivateBooking(request);
        }

        if (request.getBookingType() == BookingType.CASUAL) {
            CasualMatchDTO casualMatch = createCasualMatch(request);
            return casualMatch.getBooking();
        }

        if (request.getBookingType() == BookingType.RANKED) {
            RankedMatchDTO rankedMatch = createRankedMatch(request);
            return rankedMatch.getBooking();
        }

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

    public List<PlayerMatchDTO> getCasualMatchCandidates(Long bookingId) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new IllegalArgumentException("Booking not found"));

        if (booking.getBookingType() != BookingType.CASUAL) {
            throw new IllegalArgumentException("Booking is not a casual match");
        }
        if (booking.getStatus() != BookingStatus.PENDING) {
            throw new IllegalArgumentException("Casual match is no longer accepting players");
        }

        return booking.getParticipants().stream()
                .filter(p -> p.getJoinStatus() == JoinStatus.PAID || p.getJoinStatus() == JoinStatus.PENDING)
                .map(p -> playerRepository.findByUserId(p.getUserId())
                        .map(this::convertPlayerToMatchDTO)
                        .orElse(null))
                .filter(p -> p != null)
                .collect(Collectors.toList());
    }

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

        List<PlayerMatchDTO> teamA = new ArrayList<>();
        List<PlayerMatchDTO> teamB = new ArrayList<>();
        for (int i = 0; i < playerCandidateDTOs.size(); i++) {
            if (i % 2 == 0) {
                teamA.add(playerCandidateDTOs.get(i));
            } else {
                teamB.add(playerCandidateDTOs.get(i));
            }
        }

        List<RefereeMatchDTO> refereeCandidateDTOs = result.refereeCandidates().stream()
                .map(this::convertRefereeToMatchDTO)
                .collect(Collectors.toList());

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
                .teamACandidates(teamA)
                .teamBCandidates(teamB)
                .refereeAssigned(false)
                .refereeCandidates(refereeCandidateDTOs)
                .rankedMatchId(result.rankedMatch() != null ? result.rankedMatch().getId() : null)
                .matchStatus(result.rankedMatch() != null ? result.rankedMatch().getStatus().name() : null)
                .build();
    }

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

    public RankedMatchDTO getRankedMatchCandidates(Long bookingId) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new IllegalArgumentException("Booking not found"));

        if (booking.getBookingType() != BookingType.RANKED) {
            throw new IllegalArgumentException("Booking is not a ranked match");
        }
        if (booking.getStatus() != BookingStatus.PENDING && booking.getStatus() != BookingStatus.CONFIRMED) {
            throw new IllegalArgumentException("Ranked match is no longer accepting players");
        }

        List<PlayerMatchDTO> teamA = new ArrayList<>();
        List<PlayerMatchDTO> teamB = new ArrayList<>();
        List<RefereeMatchDTO> refereeDTOs = new ArrayList<>();

        if (booking.getParticipants() != null && booking.getParticipants().size() > 1) {
            // Auto-matchmaking or already has participants stored
            for (com.pickleball.domain.entities.BookingParticipant p : booking.getParticipants()) {
                if (p.getRole() == com.pickleball.domain.enums.ParticipantRole.REFEREE) {
                    refereeRepository.findByUserId(p.getUserId()).ifPresent(ref -> {
                        refereeDTOs.add(convertRefereeToMatchDTO(ref));
                    });
                } else if (p.getRole() == com.pickleball.domain.enums.ParticipantRole.PLAYER) {
                    playerRepository.findByUserId(p.getUserId()).ifPresent(player -> {
                        PlayerMatchDTO dto = convertPlayerToMatchDTO(player);
                        if ("A".equals(p.getTeam())) {
                            teamA.add(dto);
                        } else if ("B".equals(p.getTeam())) {
                            teamB.add(dto);
                        } else {
                            // Default to A if team is not set
                            if (teamA.size() <= teamB.size()) teamA.add(dto);
                            else teamB.add(dto);
                        }
                    });
                }
            }
        } else {
            // Manual matchmaking scenario
            Player hostPlayer = playerRepository.findByUserId(booking.getCreatedByPlayerId())
                    .orElseThrow(() -> new IllegalArgumentException("Host player not found"));

            List<Player> playerCandidates = createRankedMatchUseCase.findPlayerCandidates(hostPlayer, bookingId);

            List<Referee> allEligible = refereeRepository.findEligibleReferees();
            List<Referee> eligibleReferees = createRankedMatchUseCase.findRefereeCandidates(
                    allEligible, hostPlayer.getUserId());

            for (int i = 0; i < playerCandidates.size(); i++) {
                PlayerMatchDTO dto = convertPlayerToMatchDTO(playerCandidates.get(i));
                if (i % 2 == 0) {
                    teamA.add(dto);
                } else {
                    teamB.add(dto);
                }
            }

            refereeDTOs.addAll(eligibleReferees.stream()
                    .map(this::convertRefereeToMatchDTO)
                    .collect(Collectors.toList()));
        }

        boolean hasAssignedRef = booking.getParticipants() != null && booking.getParticipants().stream()
                .anyMatch(p -> p.getRole() == com.pickleball.domain.enums.ParticipantRole.REFEREE);
        RefereeMatchDTO assignedRef = hasAssignedRef && !refereeDTOs.isEmpty() ? refereeDTOs.get(0) : null;

        return RankedMatchDTO.builder()
                .booking(convertToDTO(booking))
                .teamACandidates(teamA)
                .teamBCandidates(teamB)
                .refereeAssigned(hasAssignedRef)
                .assignedReferee(assignedRef)
                .refereeCandidates(refereeDTOs)
                .build();
    }

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

    public BookingDTO acceptMatch(Long bookingId, Long userId) {
        PaymentResult paymentResult = acceptMatchUseCase.execute(bookingId, userId);
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new IllegalArgumentException("Booking not found"));
        
        BookingDTO dto = convertToDTO(booking);
        dto.setPayment(convertPaymentToDTO(paymentResult));
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

    public List<BookingDTO> getOwnerBookings(Long ownerId) {
        List<Booking> bookings = bookingRepository.findByOwnerId(ownerId);
        return convertBookingsToDTOs(bookings);
    }

    @Transactional(readOnly = true)
    public List<BookingDTO> getVenueBookings(Long venueId) {
        List<Booking> bookings = bookingRepository.findByVenueId(venueId);
        return convertBookingsToDTOs(bookings);
    }

    @Transactional(readOnly = true)
    public List<BookingDTO> getStaffBookings(Long staffId) {
        List<Booking> bookings = bookingRepository.findByStaffId(staffId);
        return convertBookingsToDTOs(bookings);
    }

    public void submitMatchResult(Long bookingId, SubmitResultRequest request) {
        submitMatchResultUseCase.execute(bookingId, request.getRefereeUserId(),
                request.getTeamAScore(), request.getTeamBScore(), request.getEvidenceUrl());
    }

    public void confirmMatchResult(Long bookingId, ConfirmResultRequest request) {
        confirmMatchResultUseCase.execute(bookingId, request.getPlayerUserId(), request.getAccepted());
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

    public MatchResultDTO getMatchResultInfo(Long bookingId) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new IllegalArgumentException("Booking not found"));
        
        if (booking.getBookingType() != BookingType.RANKED) {
            throw new IllegalArgumentException("Booking is not a ranked match");
        }

        RankedMatch rankedMatch = rankedMatchRepository.findByBookingId(bookingId)
                .orElseThrow(() -> new IllegalArgumentException("Ranked match not found"));

        List<MatchResultDTO.PlayerEloChangeDTO> eloChanges = new ArrayList<>();

        if (rankedMatch.getStatus() == com.pickleball.domain.enums.MatchStatus.CONFIRMED ||
            rankedMatch.getStatus() == com.pickleball.domain.enums.MatchStatus.RESOLVED) {

            List<com.pickleball.domain.entities.EloHistory> histories = eloHistoryRepository.findByRankedMatchId(rankedMatch.getId());

            for (com.pickleball.domain.entities.EloHistory history : histories) {
                for (com.pickleball.domain.entities.BookingParticipant p : booking.getParticipants()) {
                    if (p.getRole() == com.pickleball.domain.enums.ParticipantRole.PLAYER && p.getUserId().equals(history.getUserId())) {
                        String fullName = userRepository.findById(p.getUserId())
                            .map(User::getFullName)
                            .orElse("Player " + p.getUserId());

                        eloChanges.add(MatchResultDTO.PlayerEloChangeDTO.builder()
                                .userId(p.getUserId())
                                .fullName(fullName)
                                .team(p.getTeam())
                                .eloBefore(history.getEloBefore())
                                .eloChange(history.getEloChange())
                                .eloAfter(history.getEloAfter())
                                .build());
                    }
                }
            }
        }

        return MatchResultDTO.builder()
                .rankedMatchId(rankedMatch.getId())
                .teamAScore(rankedMatch.getTeamAScore())
                .teamBScore(rankedMatch.getTeamBScore())
                .winningTeam(rankedMatch.getWinningTeam())
                .matchStatus(rankedMatch.getStatus().name())
                .eloChanges(eloChanges)
                .build();
    }

    private PlayerMatchDTO convertPlayerToMatchDTO(Player player) {
        String fullName = userRepository.findById(player.getUserId())
                .map(com.pickleball.domain.entities.User::getFullName)
                .orElse("Player " + player.getUserId());

        return PlayerMatchDTO.builder()
                .userId(player.getUserId())
                .fullName(fullName)
                .currentElo(player.getCurrentElo())
                .loyaltyTier(player.getLoyaltyTier() != null ? player.getLoyaltyTier().name() : null)
                .build();
    }

    private RefereeMatchDTO convertRefereeToMatchDTO(Referee referee) {
        String fullName = userRepository.findById(referee.getUserId())
                .map(com.pickleball.domain.entities.User::getFullName)
                .orElse("Referee " + referee.getUserId());

        return RefereeMatchDTO.builder()
                .userId(referee.getUserId())
                .fullName(fullName)
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

    private List<BookingDTO> convertBookingsToDTOs(List<Booking> bookings) {
        return bookings.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    private BookingDTO convertToDTO(Booking booking) {
        BookingDTO dto = new BookingDTO();
        dto.setId(booking.getId());
        dto.setCourtId(booking.getCourtId());

        dto.setStartTime(booking.getStartTime());
        dto.setEndTime(booking.getEndTime());
        dto.setBookingType(booking.getBookingType());
        dto.setStatus(booking.getStatus());
        dto.setNotes(booking.getNotes());

        dto.setCreatedByPlayerId(booking.getCreatedByPlayerId());
        dto.setCreatedByStaffId(booking.getCreatedByStaffId());

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
            if (booking.getBookingType() == BookingType.RANKED) {
                dto.setRequiredDeposit(booking.getTotalCost().getAmount().multiply(new BigDecimal("0.25")));
            }
        }
        if (booking.getBookingType() == BookingType.CASUAL && booking.getVenueFee() != null) {
            dto.setRequiredDeposit(booking.getVenueFee().getAmount().multiply(new BigDecimal("0.25")));
        }

        dto.setCreatedAt(booking.getCreatedAt());

        if (booking.getParticipants() != null && !booking.getParticipants().isEmpty()) {
            List<BookingParticipantDTO> participantDTOs = booking.getParticipants().stream().map(p -> {
                return BookingParticipantDTO.builder()
                        .id(p.getId())
                        .userId(p.getUserId())
                        .role(p.getRole())
                        .team(p.getTeam())
                        .joinStatus(p.getJoinStatus())
                        .matchHost(p.isMatchHost())
                        .depositAmount(p.getDepositAmount() != null ? p.getDepositAmount().getAmount() : null)
                        .actualPaymentAmount(p.getActualPaymentAmount() != null ? p.getActualPaymentAmount().getAmount() : null)
                        .refundAmount(p.getRefundAmount() != null ? p.getRefundAmount().getAmount() : null)
                        .build();
            }).collect(Collectors.toList());
            dto.setParticipants(participantDTOs);
        }

        courtRepository.findById(booking.getCourtId()).ifPresent(court -> {
            dto.setCourtName(court.getCourtName());
            if (court.getVenueId() != null) {
                dto.setVenueId(court.getVenueId());
                venueRepository.findById(court.getVenueId()).ifPresent(venue -> {
                    dto.setVenueName(venue.getName());
                });
            }
        });

        return dto;
    }
}
