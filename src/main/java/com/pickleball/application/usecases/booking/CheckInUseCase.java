package com.pickleball.application.usecases.booking;

import com.pickleball.application.dtos.requests.CheckInRequest;
import com.pickleball.domain.entities.Booking;
import com.pickleball.domain.entities.BookingParticipant;
import com.pickleball.domain.entities.CheckIn;
import com.pickleball.domain.entities.Court;
import com.pickleball.domain.entities.Venue;
import com.pickleball.domain.enums.BookingStatus;
import com.pickleball.domain.enums.CheckInMethod;
import com.pickleball.domain.enums.JoinStatus;
import com.pickleball.domain.repositories.BookingRepository;
import com.pickleball.domain.repositories.CheckInRepository;
import com.pickleball.domain.repositories.CourtRepository;
import com.pickleball.domain.repositories.VenueRepository;
import com.pickleball.domain.services.MatchmakingService;

import java.time.LocalDateTime;

public class CheckInUseCase {
    private final BookingRepository bookingRepository;
    private final CheckInRepository checkInRepository;
    private final CourtRepository courtRepository;
    private final VenueRepository venueRepository;
    private final MatchmakingService matchmakingService;

    // Configuration for MAX distance to check-in (200 meters = 0.2 km)
    private static final double MAX_CHECK_IN_DISTANCE_KM = 0.2;

    public CheckInUseCase(BookingRepository bookingRepository,
                          CheckInRepository checkInRepository,
                          CourtRepository courtRepository,
                          VenueRepository venueRepository,
                          MatchmakingService matchmakingService) {
        this.bookingRepository = bookingRepository;
        this.checkInRepository = checkInRepository;
        this.courtRepository = courtRepository;
        this.venueRepository = venueRepository;
        this.matchmakingService = matchmakingService;
    }

    public CheckIn execute(Long bookingId, CheckInRequest request) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new IllegalArgumentException("Booking not found."));

        if (booking.getStatus() != BookingStatus.CONFIRMED && booking.getStatus() != BookingStatus.COMPLETED) {
            throw new IllegalArgumentException("Match is not confirmed. Cannot check-in.");
        }

        // Get the participant
        BookingParticipant participant = booking.getParticipants().stream()
                .filter(p -> p.getUserId().equals(request.getUserId()))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Player/Referee is not part of this match."));

        if (participant.getJoinStatus() == JoinStatus.CHECKED_IN) {
            throw new IllegalArgumentException("Participant has already checked in.");
        }

        if (participant.getJoinStatus() != JoinStatus.PAID) {
            throw new IllegalArgumentException("Participant must be PAID to check-in.");
        }

        // Validate GPS distance if method is GPS
        if (request.getCheckInMethod() == CheckInMethod.GPS) {
            Court court = courtRepository.findById(booking.getCourtId())
                    .orElseThrow(() -> new IllegalStateException("Court not found"));
            Venue venue = venueRepository.findById(court.getVenueId())
                    .orElseThrow(() -> new IllegalStateException("Venue not found"));

            if (venue.getLocation() == null || venue.getLocation().getLatitude() == null || venue.getLocation().getLongitude() == null) {
                // If venue location is not populated correctly in Venue entity
                throw new IllegalStateException("Venue GPS location is not set. Cannot perform GPS check-in.");
            }

            double dist = matchmakingService.haversine(
                    venue.getLocation().getLatitude().doubleValue(),
                    venue.getLocation().getLongitude().doubleValue(),
                    request.getLatitude().doubleValue(),
                    request.getLongitude().doubleValue()
            );

            if (dist > MAX_CHECK_IN_DISTANCE_KM) {
                throw new IllegalArgumentException(String.format(
                        "You are too far from the venue. Distance: %.3f km, Max allowed: %.3f km",
                        dist, MAX_CHECK_IN_DISTANCE_KM));
            }
        }

        // Save CheckIn
        CheckIn checkIn = CheckIn.builder()
                .bookingId(bookingId)
                .userId(request.getUserId())
                .checkInMethod(request.getCheckInMethod())
                .checkInTime(LocalDateTime.now())
                .latitude(request.getLatitude())
                .longitude(request.getLongitude())
                .build();
        checkIn = checkInRepository.save(checkIn);

        // Update participant status
        participant.setJoinStatus(JoinStatus.CHECKED_IN);
        bookingRepository.save(booking);

        return checkIn;
    }
}
