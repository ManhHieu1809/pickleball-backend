package com.pickleball.application.services;

import com.pickleball.application.dtos.DashboardStatsDTO;
import com.pickleball.domain.enums.BookingType;
import com.pickleball.domain.enums.RequestStatus;
import com.pickleball.infrastructure.persistence.entities.BookingEntity;
import com.pickleball.infrastructure.persistence.entities.CourtEntity;
import com.pickleball.infrastructure.persistence.entities.RoleRequestEntity;
import com.pickleball.infrastructure.persistence.entities.VenueEntity;
import com.pickleball.infrastructure.persistence.repositories.*;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class DashboardService {

    private final BookingJpaRepository bookingJpaRepository;
    private final UserJpaRepository userJpaRepository;
    private final VenueJpaRepository venueJpaRepository;
    private final RoleRequestJpaRepository roleRequestJpaRepository;
    private final CourtJpaRepository courtJpaRepository;

    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("MMM dd, yyyy HH:mm");

    public DashboardStatsDTO getDashboardStats() {
        // ===== Summary cards =====
        long totalBookings = bookingJpaRepository.count();

        LocalDateTime startOfToday = LocalDate.now().atStartOfDay();
        LocalDateTime endOfToday = startOfToday.plusDays(1);
        long todayBookings = bookingJpaRepository.countByStartTimeBetween(startOfToday, endOfToday);

        LocalDateTime startOfMonth = LocalDate.now().withDayOfMonth(1).atStartOfDay();
        long newUsersThisMonth = userJpaRepository.countByCreatedAtAfter(startOfMonth);

        long activeVenues = venueJpaRepository.findByIsActiveTrue().size();

        // ===== Pending actions count =====
        List<RoleRequestEntity> pendingRequests = roleRequestJpaRepository.findByStatus(RequestStatus.PENDING);
        long pendingOwnerRequests = pendingRequests.size();

        List<VenueEntity> pendingVenuesList = venueJpaRepository.findByIsActiveFalseAndApprovedByAdminIdIsNull();
        long pendingVenues = pendingVenuesList.size();

        // ===== Booking type distribution =====
        Map<String, Long> bookingTypeDistribution = new LinkedHashMap<>();
        for (BookingType type : BookingType.values()) {
            long count = bookingJpaRepository.countByBookingType(type);
            bookingTypeDistribution.put(type.name(), count);
        }

        // ===== Recent bookings (last 5) =====
        List<BookingEntity> recentBookingEntities = bookingJpaRepository.findRecentBookings(PageRequest.of(0, 5));
        List<DashboardStatsDTO.RecentBookingDTO> recentBookings = recentBookingEntities.stream()
                .map(this::mapToRecentBooking)
                .collect(Collectors.toList());

        // ===== Pending actions list =====
        List<DashboardStatsDTO.PendingActionDTO> pendingActions = new ArrayList<>();

        // Add pending role requests
        for (RoleRequestEntity req : pendingRequests) {
            pendingActions.add(DashboardStatsDTO.PendingActionDTO.builder()
                    .id(req.getId())
                    .type("OWNER_REQUEST")
                    .title("New Owner Registration")
                    .description("User #" + req.getUserId() + " - " + req.getRequestType().name())
                    .submittedAt(req.getSubmittedAt() != null ? req.getSubmittedAt().format(DATE_FMT) : "")
                    .build());
        }

        // Add pending venues
        for (VenueEntity venue : pendingVenuesList) {
            pendingActions.add(DashboardStatsDTO.PendingActionDTO.builder()
                    .id(venue.getId())
                    .type("VENUE_PENDING")
                    .title("Venue Approval")
                    .description(venue.getName() != null ? venue.getName() : "Venue #" + venue.getId())
                    .submittedAt("")
                    .build());
        }

        return DashboardStatsDTO.builder()
                .totalBookings(totalBookings)
                .todayBookings(todayBookings)
                .newUsersThisMonth(newUsersThisMonth)
                .activeVenues(activeVenues)
                .pendingOwnerRequests(pendingOwnerRequests)
                .pendingVenues(pendingVenues)
                .bookingTypeDistribution(bookingTypeDistribution)
                .recentBookings(recentBookings)
                .pendingActions(pendingActions)
                .build();
    }

    private DashboardStatsDTO.RecentBookingDTO mapToRecentBooking(BookingEntity booking) {
        String courtName = "Court #" + booking.getCourtId();
        try {
            Optional<CourtEntity> courtOpt = courtJpaRepository.findById(booking.getCourtId());
            if (courtOpt.isPresent()) {
                courtName = courtOpt.get().getCourtName() != null
                        ? courtOpt.get().getCourtName()
                        : "Court #" + booking.getCourtId();
            }
        } catch (Exception ignored) {
        }

        return DashboardStatsDTO.RecentBookingDTO.builder()
                .id(booking.getId())
                .courtId(booking.getCourtId())
                .courtName(courtName)
                .bookingType(booking.getBookingType() != null ? booking.getBookingType().name() : "N/A")
                .status(booking.getStatus() != null ? booking.getStatus().name() : "N/A")
                .totalCost(booking.getTotalCost())
                .startTime(booking.getStartTime() != null ? booking.getStartTime().format(DATE_FMT) : "")
                .build();
    }
}
