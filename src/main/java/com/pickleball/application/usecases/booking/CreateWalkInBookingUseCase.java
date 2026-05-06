package com.pickleball.application.usecases.booking;

import com.pickleball.domain.entities.Booking;
import com.pickleball.domain.entities.Court;
import com.pickleball.domain.entities.CourtPricing;
import com.pickleball.domain.entities.VenueStaff;
import com.pickleball.domain.enums.BookingStatus;
import com.pickleball.domain.enums.BookingType;
import com.pickleball.domain.repositories.BookingRepository;
import com.pickleball.domain.repositories.CourtPricingRepository;
import com.pickleball.domain.repositories.CourtRepository;
import com.pickleball.domain.repositories.VenueStaffRepository;
import com.pickleball.domain.services.PriceCalculationService;
import com.pickleball.domain.valueobjects.Money;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

public class CreateWalkInBookingUseCase {

    private final BookingRepository bookingRepository;
    private final CourtRepository courtRepository;
    private final CourtPricingRepository courtPricingRepository;
    private final VenueStaffRepository venueStaffRepository;
    private final PriceCalculationService priceCalculationService;

    private static final BigDecimal PLATFORM_FEE_PERCENTAGE = new BigDecimal("0.20");

    public CreateWalkInBookingUseCase(
            BookingRepository bookingRepository,
            CourtRepository courtRepository,
            CourtPricingRepository courtPricingRepository,
            VenueStaffRepository venueStaffRepository,
            PriceCalculationService priceCalculationService) {
        this.bookingRepository = bookingRepository;
        this.courtRepository = courtRepository;
        this.courtPricingRepository = courtPricingRepository;
        this.venueStaffRepository = venueStaffRepository;
        this.priceCalculationService = priceCalculationService;
    }

    public WalkInBookingResult execute(
            Long staffId,
            Long courtId,
            LocalDateTime startTime,
            LocalDateTime endTime,
            String customerName,
            String customerPhone,
            String paymentMethod,
            String notes) {

        VenueStaff staff = venueStaffRepository.findById(staffId)
                .orElseThrow(() -> new IllegalArgumentException("Nhân viên không tồn tại"));

        if (!staff.canCreateBooking()) {
            throw new IllegalArgumentException("Nhân viên không có quyền tạo booking");
        }

        Court court = courtRepository.findById(courtId)
                .orElseThrow(() -> new IllegalArgumentException("Sân không tồn tại"));

        if (!court.getVenueId().equals(staff.getVenueId())) {
            throw new IllegalArgumentException("Sân không thuộc venue của nhân viên này");
        }

        if (!court.isActive()) {
            throw new IllegalArgumentException("Sân hiện đang không hoạt động");
        }

        List<Booking> conflictingBookings = bookingRepository.findConflictingBookings(courtId, startTime, endTime);
        if (!conflictingBookings.isEmpty()) {
            throw new IllegalArgumentException("Slot này đã được đặt");
        }

        Money venueFee = calculateVenueFee(courtId, startTime, endTime);

        Booking booking = Booking.builder()
                .courtId(courtId)
                .startTime(startTime)
                .endTime(endTime)
                .bookingType(BookingType.WALK_IN)
                .status(BookingStatus.CONFIRMED)
                .createdAt(LocalDateTime.now())
                .createdByStaffId(staffId)
                .notes(buildNotes(customerName, customerPhone, paymentMethod, notes))
                .build();

        booking.calculateCosts(venueFee, null, PLATFORM_FEE_PERCENTAGE);

        Booking savedBooking = bookingRepository.save(booking);

        return new WalkInBookingResult(
                savedBooking,
                customerName,
                customerPhone,
                paymentMethod,
                venueFee
        );
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

    private String buildNotes(String customerName, String customerPhone, String paymentMethod, String additionalNotes) {
        StringBuilder sb = new StringBuilder();
        sb.append("[WALK-IN] ");

        if (customerName != null && !customerName.isBlank()) {
            sb.append("Khách: ").append(customerName).append(" | ");
        }
        if (customerPhone != null && !customerPhone.isBlank()) {
            sb.append("SĐT: ").append(customerPhone).append(" | ");
        }
        if (paymentMethod != null && !paymentMethod.isBlank()) {
            sb.append("Thanh toán: ").append(paymentMethod).append(" | ");
        }
        if (additionalNotes != null && !additionalNotes.isBlank()) {
            sb.append("Ghi chú: ").append(additionalNotes);
        }

        return sb.toString().trim();
    }

    public record WalkInBookingResult(
            Booking booking,
            String customerName,
            String customerPhone,
            String paymentMethod,
            Money amountPaid
    ) {}
}
