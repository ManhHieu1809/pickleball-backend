package com.pickleball.application.services;

import com.pickleball.application.dtos.AdminBookingDTO;
import com.pickleball.application.dtos.AdminBookingStatsDTO;
import com.pickleball.domain.enums.BookingStatus;
import com.pickleball.domain.enums.BookingType;
import com.pickleball.infrastructure.persistence.entities.BookingEntity;
import com.pickleball.infrastructure.persistence.entities.CourtEntity;
import com.pickleball.infrastructure.persistence.entities.PlayerEntity;
import com.pickleball.infrastructure.persistence.entities.UserEntity;
import com.pickleball.infrastructure.persistence.entities.VenueEntity;
import com.pickleball.infrastructure.persistence.repositories.BookingJpaRepository;
import com.pickleball.infrastructure.persistence.repositories.CourtJpaRepository;
import com.pickleball.infrastructure.persistence.repositories.PlayerJpaRepository;
import com.pickleball.infrastructure.persistence.repositories.UserJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class BookingManagementService {

    private final BookingJpaRepository bookingJpaRepository;
    private final CourtJpaRepository courtJpaRepository;
    private final PlayerJpaRepository playerJpaRepository;
    private final UserJpaRepository userJpaRepository;

    public Page<AdminBookingDTO> getAllBookings(int page, int size, String search, String statusStr, String typeStr) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());

        BookingStatus status = null;
        if (statusStr != null && !statusStr.isEmpty() && !statusStr.equals("ALL")) {
            try {
                status = BookingStatus.valueOf(statusStr.toUpperCase());
            } catch (Exception ignored) {
            }
        }

        BookingType type = null;
        if (typeStr != null && !typeStr.isEmpty() && !typeStr.equals("ALL")) {
            try {
                type = BookingType.valueOf(typeStr.toUpperCase());
            } catch (Exception ignored) {
            }
        }

        if (search != null && search.trim().isEmpty()) {
            search = null;
        }

        Page<BookingEntity> entities = bookingJpaRepository.searchBookings(search, status, type, pageable);
        return entities.map(this::mapToDTO);
    }

    public AdminBookingStatsDTO getBookingStats() {
        long total = bookingJpaRepository.count();

        LocalDateTime startOfToday = LocalDate.now().atStartOfDay();
        LocalDateTime endOfToday = startOfToday.plusDays(1);
        long today = bookingJpaRepository.countByStartTimeBetween(startOfToday, endOfToday);

        long active = bookingJpaRepository.countByStatus(BookingStatus.CONFIRMED) +
                bookingJpaRepository.countByStatus(BookingStatus.PENDING);

        long cancelled = bookingJpaRepository.countByStatus(BookingStatus.CANCELLED);

        return AdminBookingStatsDTO.builder()
                .totalBookings(total)
                .todayBookings(today)
                .activeBookings(active)
                .cancelledBookings(cancelled)
                .build();
    }

    public AdminBookingDTO getBookingById(Long id) {
        BookingEntity booking = bookingJpaRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Booking not found: " + id));
        return mapToDTO(booking);
    }

    @Transactional
    public AdminBookingDTO cancelBooking(Long id) {
        BookingEntity booking = bookingJpaRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Booking not found: " + id));

        booking.setStatus(BookingStatus.CANCELLED);
        BookingEntity saved = bookingJpaRepository.save(booking);
        return mapToDTO(saved);
    }

    private AdminBookingDTO mapToDTO(BookingEntity booking) {
        String courtName = "Court #" + booking.getCourtId();
        String venueName = "Unknown Venue";
        Long venueId = null;

        if (booking.getCourt() != null) {
            courtName = booking.getCourt().getCourtName() != null ? booking.getCourt().getCourtName() : courtName;
            if (booking.getCourt().getVenue() != null) {
                venueName = booking.getCourt().getVenue().getName();
                venueId = booking.getCourt().getVenue().getId();
            }
        } else {
            try {
                CourtEntity court = courtJpaRepository.findById(booking.getCourtId()).orElse(null);
                if (court != null) {
                    courtName = court.getCourtName() != null ? court.getCourtName() : courtName;
                    if (court.getVenue() != null) {
                        venueName = court.getVenue().getName();
                        venueId = court.getVenue().getId();
                    }
                }
            } catch (Exception ignored) {
            }
        }

        String creatorName = "Unknown";
        if (booking.getCreatedByPlayerId() != null) {
            try {
                PlayerEntity player = playerJpaRepository.findById(booking.getCreatedByPlayerId()).orElse(null);
                if (player != null && player.getUserId() != null) {
                    UserEntity user = userJpaRepository.findById(player.getUserId()).orElse(null);
                    if (user != null) {
                        creatorName = user.getFullName() != null ? user.getFullName() : "User #" + user.getId();
                    }
                }
            } catch (Exception ignored) {
            }
        }

        if (creatorName.equals("Unknown") && booking.getBookingType() == BookingType.WALK_IN
                && booking.getNotes() != null) {
            String notes = booking.getNotes();
            try {
                if (notes.contains("Khách: ")) {
                    int start = notes.indexOf("Khách: ") + 7;
                    int end = notes.indexOf(" | ", start);
                    if (end == -1)
                        end = notes.length();
                    creatorName = notes.substring(start, end).trim() + " (Walk-in)";
                }
            } catch (Exception ignored) {
            }
        }

        return AdminBookingDTO.builder()
                .id(booking.getId())
                .courtId(booking.getCourtId())
                .courtName(courtName)
                .venueId(venueId)
                .venueName(venueName)
                .bookingType(booking.getBookingType() != null ? booking.getBookingType().name() : "N/A")
                .status(booking.getStatus() != null ? booking.getStatus().name() : "N/A")
                .totalCost(booking.getTotalCost())
                .startTime(booking.getStartTime())
                .endTime(booking.getEndTime())
                .createdAt(booking.getCreatedAt())
                .createdByPlayerId(booking.getCreatedByPlayerId())
                .creatorName(creatorName)
                .venueFee(booking.getVenueFee())
                .refereeFee(booking.getRefereeFee())
                .platformFee(booking.getPlatformFee())
                .paymentStatus(booking.getStatus() == BookingStatus.CONFIRMED ? "PAID" : "PENDING")
                .paymentMethod("ONLINE")
                .build();
    }
}
