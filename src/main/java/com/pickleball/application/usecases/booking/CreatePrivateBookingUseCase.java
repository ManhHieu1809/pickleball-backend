package com.pickleball.application.usecases.booking;

import com.pickleball.application.usecases.wallet.PayWithWalletUseCase;
import com.pickleball.domain.entities.Booking;
import com.pickleball.domain.entities.BookingParticipant;
import com.pickleball.domain.entities.CourtPricing;
import com.pickleball.domain.enums.BookingStatus;
import com.pickleball.domain.enums.BookingType;
import com.pickleball.domain.enums.JoinStatus;
import com.pickleball.domain.enums.ParticipantRole;
import com.pickleball.domain.repositories.BookingRepository;
import com.pickleball.domain.repositories.CourtPricingRepository;
import com.pickleball.domain.repositories.CourtRepository;
import com.pickleball.domain.services.PaymentService;
import com.pickleball.domain.services.PaymentService.PaymentResult;
import com.pickleball.domain.services.PriceCalculationService;
import com.pickleball.domain.valueobjects.Money;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

public class CreatePrivateBookingUseCase {

    private final BookingRepository bookingRepository;
    private final CourtRepository courtRepository;
    private final CourtPricingRepository courtPricingRepository;
    private final PriceCalculationService priceCalculationService;
    private final PayWithWalletUseCase payWithWalletUseCase;

    private static final BigDecimal PLATFORM_FEE_PERCENTAGE = new BigDecimal("0.20");

    public CreatePrivateBookingUseCase(
            BookingRepository bookingRepository,
            CourtRepository courtRepository,
            CourtPricingRepository courtPricingRepository,
            PriceCalculationService priceCalculationService,
            PayWithWalletUseCase payWithWalletUseCase) {
        this.bookingRepository = bookingRepository;
        this.courtRepository = courtRepository;
        this.courtPricingRepository = courtPricingRepository;
        this.priceCalculationService = priceCalculationService;
        this.payWithWalletUseCase = payWithWalletUseCase;
    }


    public BookingWithPayment execute(Long courtId, LocalDateTime startTime, LocalDateTime endTime, Long hostUserId) {
        courtRepository.findById(courtId)
                .orElseThrow(() -> new IllegalArgumentException("Court not found"));

        List<Booking> conflictingBookings = bookingRepository.findConflictingBookings(courtId, startTime, endTime);
        if (!conflictingBookings.isEmpty()) {
            throw new IllegalArgumentException("Time slot is already booked");
        }

        Money venueFee = calculateVenueFee(courtId, startTime, endTime);

        Booking booking = Booking.builder()
                .courtId(courtId)
                .startTime(startTime)
                .endTime(endTime)
                .bookingType(BookingType.PRIVATE)
                .status(BookingStatus.PENDING)
                .createdAt(LocalDateTime.now())
                .createdByPlayerId(hostUserId)
                .build();

        booking.calculateCosts(venueFee, null, PLATFORM_FEE_PERCENTAGE);
        booking = bookingRepository.save(booking);

        BookingParticipant host = BookingParticipant.builder()
                .bookingId(booking.getId())
                .userId(hostUserId)
                .role(ParticipantRole.HOST)
                .joinStatus(JoinStatus.PENDING)
                .isMatchHost(true)
                .depositAmount(venueFee) // Host pays 100%
                .refundAmount(new Money(BigDecimal.ZERO, "VND"))
                .build();
        booking.addParticipant(host);

        String description = "Private booking - Court #" + courtId + " - " + startTime.toLocalDate();

        // Thanh toán bằng ví nội bộ
        String transactionId;
        PaymentResult paymentResult;
        try {
            transactionId = payWithWalletUseCase.execute(
                    hostUserId,
                    venueFee.getAmount(),
                    booking.getId(),
                    description
            );
            paymentResult = PaymentResult.success(transactionId, venueFee);
            booking.confirm();
            host.setJoinStatus(JoinStatus.PAID);
        } catch (Exception e) {
            paymentResult = PaymentResult.failed("Thanh toán thất bại: " + e.getMessage());
            booking.cancel();
        }

        booking = bookingRepository.save(booking);
        return new BookingWithPayment(booking, paymentResult);
    }

    private Money calculateVenueFee(Long courtId, LocalDateTime startTime, LocalDateTime endTime) {
        List<CourtPricing> pricings = courtPricingRepository.findByCourtId(courtId);

        if (pricings.isEmpty()) {
            return new Money(new BigDecimal("200000"), "VND");
        }

        long hours = Duration.between(startTime, endTime).toHours();
        if (hours <= 0) hours = 1;
        Money pricePerHour = priceCalculationService.calculateSlotPrice(
                pricings,
                startTime.toLocalTime(),
                startTime.getDayOfWeek()
        );

        BigDecimal totalAmount = pricePerHour.getAmount().multiply(BigDecimal.valueOf(hours));
        return new Money(totalAmount, pricePerHour.getCurrency());
    }

    public record BookingWithPayment(
            Booking booking,
            PaymentResult paymentResult
    ) {}
}
