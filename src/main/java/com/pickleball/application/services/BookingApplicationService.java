package com.pickleball.application.services;

import com.pickleball.application.dtos.BookingDTO;
import com.pickleball.application.dtos.CasualMatchDTO;
import com.pickleball.application.dtos.PaymentDTO;
import com.pickleball.application.dtos.PlayerMatchDTO;
import com.pickleball.application.dtos.requests.CreateBookingRequest;
import com.pickleball.application.dtos.requests.JoinBookingRequest;
import com.pickleball.application.usecases.booking.CreateBookingUseCase;
import com.pickleball.application.usecases.booking.CreateCasualMatchUseCase;
import com.pickleball.application.usecases.booking.CreateCasualMatchUseCase.CasualMatchResult;
import com.pickleball.application.usecases.booking.CreatePrivateBookingUseCase;
import com.pickleball.application.usecases.booking.CreatePrivateBookingUseCase.BookingWithPayment;
import com.pickleball.application.usecases.booking.JoinBookingUseCase;
import com.pickleball.application.usecases.booking.JoinBookingUseCase.JoinResult;
import com.pickleball.domain.entities.Booking;
import com.pickleball.domain.entities.Player;
import com.pickleball.domain.enums.BookingStatus;
import com.pickleball.domain.enums.BookingType;
import com.pickleball.domain.enums.JoinStatus;
import com.pickleball.domain.repositories.BookingRepository;
import com.pickleball.domain.repositories.CourtRepository;
import com.pickleball.domain.repositories.PlayerRepository;
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
    private final JoinBookingUseCase joinBookingUseCase;
    private final CourtRepository courtRepository;
    private final BookingRepository bookingRepository;
    private final PlayerRepository playerRepository;
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
                .filter(p -> p.getJoinStatus() == JoinStatus.PAID)
                .count();

        List<PlayerMatchDTO> candidateDTOs = result.candidates().stream()
                .map(this::convertPlayerToMatchDTO)
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

        Player hostPlayer = playerRepository.findByUserId(booking.getCreatedByPlayerId())
                .orElseThrow(() -> new IllegalArgumentException("Host player not found"));

        List<Player> candidates = createCasualMatchUseCase.findCandidates(hostPlayer, bookingId);

        return candidates.stream()
                .map(this::convertPlayerToMatchDTO)
                .collect(Collectors.toList());
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
                            .filter(p -> p.getJoinStatus() == JoinStatus.PAID)
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
        JoinResult result = joinBookingUseCase.execute(bookingId, request.getUserId());

        BookingDTO dto = convertToDTO(result.booking());
        dto.setPayment(convertPaymentToDTO(result.paymentResult()));
        return dto;
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

    private PlayerMatchDTO convertPlayerToMatchDTO(Player player) {
        return PlayerMatchDTO.builder()
                .userId(player.getUserId())
                .currentElo(player.getCurrentElo())
                .loyaltyTier(player.getLoyaltyTier() != null ? player.getLoyaltyTier().name() : null)
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
