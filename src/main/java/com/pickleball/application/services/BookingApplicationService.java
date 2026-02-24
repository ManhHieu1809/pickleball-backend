package com.pickleball.application.services;

import com.pickleball.application.dtos.BookingDTO;
import com.pickleball.application.dtos.PaymentDTO;
import com.pickleball.application.dtos.requests.CreateBookingRequest;
import com.pickleball.application.dtos.requests.JoinBookingRequest;
import com.pickleball.application.usecases.booking.CreateBookingUseCase;
import com.pickleball.application.usecases.booking.CreatePrivateBookingUseCase;
import com.pickleball.application.usecases.booking.CreatePrivateBookingUseCase.BookingWithPayment;
import com.pickleball.application.usecases.booking.JoinBookingUseCase;
import com.pickleball.domain.entities.Booking;
import com.pickleball.domain.enums.BookingType;
import com.pickleball.domain.repositories.BookingRepository;
import com.pickleball.domain.repositories.CourtRepository;
import com.pickleball.domain.services.PaymentService;
import com.pickleball.domain.services.PaymentService.PaymentResult;
import com.pickleball.domain.valueobjects.Money;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
@Transactional
public class BookingApplicationService {

    private final CreateBookingUseCase createBookingUseCase;
    private final CreatePrivateBookingUseCase createPrivateBookingUseCase;
    private final JoinBookingUseCase joinBookingUseCase;
    private final CourtRepository courtRepository;
    private final BookingRepository bookingRepository;
    private final PaymentService paymentService;

    public BookingDTO createBooking(CreateBookingRequest request) {
        courtRepository.findById(request.getCourtId())
                .orElseThrow(() -> new IllegalArgumentException("Court not found"));

        if (request.getBookingType() == BookingType.PRIVATE) {
            return createPrivateBooking(request);
        }

        // For other booking types (CASUAL, RANKED, WALK_IN) - use existing logic for now
        // TODO: Implement specific use cases for each type
        Booking booking = createBookingUseCase.execute(
                request.getCourtId(),
                request.getStartTime(),
                request.getEndTime(),
                request.getBookingType(),
                request.getCreatorUserId(),
                request.isPlayer()
        );

        Booking savedBooking = bookingRepository.save(booking);
        return convertToDTO(savedBooking);
    }

    /**
     * Create Private Booking with Payment (WORKFLOW §II.1)
     */
    private BookingDTO createPrivateBooking(CreateBookingRequest request) {
        BookingWithPayment result = createPrivateBookingUseCase.execute(
                request.getCourtId(),
                request.getStartTime(),
                request.getEndTime(),
                request.getCreatorUserId()
        );

        BookingDTO dto = convertToDTO(result.booking());
        dto.setPayment(convertPaymentToDTO(result.paymentResult()));
        return dto;
    }

    public BookingDTO joinBooking(Long bookingId, JoinBookingRequest request) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new IllegalArgumentException("Booking not found"));


        Booking updatedBooking = joinBookingUseCase.execute(bookingId, request.getUserId());
        Booking savedBooking = bookingRepository.save(updatedBooking);

        return convertToDTO(savedBooking);
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

        // Process refund via payment service
        BookingDTO dto = convertToDTO(cancelledBooking);
        if (refundAmount.getAmount().compareTo(BigDecimal.ZERO) > 0) {
            // Find original transaction ID (would be stored in a payment transaction table in production)
            String originalTransactionId = "BOOKING_" + bookingId;
            PaymentResult refundResult = paymentService.refund(originalTransactionId, refundAmount, "Booking cancelled by user");
            dto.setPayment(convertPaymentToDTO(refundResult));
        }

        return dto;
    }

    public BookingDTO getBooking(Long bookingId) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new IllegalArgumentException("Booking not found"));

        return convertToDTO(booking);
    }

    private PaymentDTO convertPaymentToDTO(PaymentResult paymentResult) {
        if (paymentResult == null) return null;

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