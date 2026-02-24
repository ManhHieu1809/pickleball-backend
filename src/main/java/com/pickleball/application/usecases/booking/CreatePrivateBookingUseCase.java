package com.pickleball.application.usecases.booking;

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
    private final PaymentService paymentService;

    // phí nền tảng 20%
    private static final BigDecimal PLATFORM_FEE_PERCENTAGE = new BigDecimal("0.20");

    public CreatePrivateBookingUseCase(
            BookingRepository bookingRepository,
            CourtRepository courtRepository,
            CourtPricingRepository courtPricingRepository,
            PriceCalculationService priceCalculationService,
            PaymentService paymentService) {
        this.bookingRepository = bookingRepository;
        this.courtRepository = courtRepository;
        this.courtPricingRepository = courtPricingRepository;
        this.priceCalculationService = priceCalculationService;
        this.paymentService = paymentService;
    }


    public BookingWithPayment execute(Long courtId, LocalDateTime startTime, LocalDateTime endTime, Long hostUserId) {
        // 1. kiểm tra court tồn tại
        courtRepository.findById(courtId)
                .orElseThrow(() -> new IllegalArgumentException("Court not found"));

        // 2. kiem tra xung đột lịch
        List<Booking> conflictingBookings = bookingRepository.findConflictingBookings(courtId, startTime, endTime);
        if (!conflictingBookings.isEmpty()) {
            throw new IllegalArgumentException("Time slot is already booked");
        }

        // 3. Tính phí sân
        Money venueFee = calculateVenueFee(courtId, startTime, endTime);

        // 4. Tạo booking ở trạng thái PENDING
        Booking booking = Booking.builder()
                .courtId(courtId)
                .startTime(startTime)
                .endTime(endTime)
                .bookingType(BookingType.PRIVATE)
                .status(BookingStatus.PENDING)
                .createdAt(LocalDateTime.now())
                .createdByPlayerId(hostUserId)
                .build();

        // Tính toán chi phí
        booking.calculateCosts(venueFee, null, PLATFORM_FEE_PERCENTAGE);
        booking = bookingRepository.save(booking);

        // 5. Thêm host participant
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

        // 6. Tiến hành thanh toán (User thanh toán 100% phí thuê địa điểm)
        String orderId = "BOOKING_" + booking.getId();
        String description = "Private booking - Court #" + courtId + " - " + startTime.toLocalDate();

        PaymentResult paymentResult = paymentService.createPayment(orderId, venueFee, description, hostUserId);

        // 7. Cập nhật trạng thái đặt chỗ dựa trên kết quả thanh toán
        if (paymentResult.success()) {
            if ("SUCCESS".equals(paymentResult.status())) {
                booking.confirm();
                host.setJoinStatus(JoinStatus.PAID);
            }
        } else {
            booking.cancel();
        }
        booking = bookingRepository.save(booking);

        return new BookingWithPayment(booking, paymentResult);
    }

//   Tính phí thuê địa điểm dựa trên quy tắc của CourtPricing
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

        // Calculate total
        BigDecimal totalAmount = pricePerHour.getAmount().multiply(BigDecimal.valueOf(hours));
        return new Money(totalAmount, pricePerHour.getCurrency());
    }

    // trả về kết quả booking kèm theo thông tin thanh toán
    public record BookingWithPayment(
            Booking booking,
            PaymentResult paymentResult
    ) {}
}
