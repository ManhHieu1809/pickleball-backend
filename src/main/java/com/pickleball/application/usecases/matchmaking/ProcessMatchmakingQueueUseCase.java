package com.pickleball.application.usecases.matchmaking;

import com.pickleball.domain.entities.*;
import com.pickleball.domain.enums.BookingStatus;
import com.pickleball.domain.enums.BookingType;
import com.pickleball.domain.enums.JoinStatus;
import com.pickleball.domain.enums.MatchStatus;
import com.pickleball.domain.enums.ParticipantRole;
import com.pickleball.domain.repositories.*;
import com.pickleball.domain.services.MatchmakingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@RequiredArgsConstructor
public class ProcessMatchmakingQueueUseCase {

    private final MatchmakingTicketRepository ticketRepository;
    private final BookingRepository bookingRepository;
    private final CourtRepository courtRepository;
    private final VenueRepository venueRepository;
    private final RankedMatchRepository rankedMatchRepository;
    private final MatchmakingService matchmakingService;

    @Transactional
    public void execute() {
        List<MatchmakingTicket> playerTickets = ticketRepository.findActiveTicketsByRoleOrderByJoinedAtAsc(ParticipantRole.PLAYER);
        List<MatchmakingTicket> refereeTickets = ticketRepository.findActiveTicketsByRoleOrderByJoinedAtAsc(ParticipantRole.REFEREE);

        if (playerTickets.size() < 4 || refereeTickets.isEmpty()) {
            log.debug("Not enough players ({}) or referees ({}) in queue to form a match.", playerTickets.size(), refereeTickets.size());
            return;
        }

        // Simple greedy matching: Take the first player (longest waiting)
        while (playerTickets.size() >= 4 && !refereeTickets.isEmpty()) {
            MatchmakingTicket hostTicket = playerTickets.get(0);
            
            // Find 3 other players matching elo and distance
            List<MatchmakingTicket> matchedPlayers = findMatchingPlayers(hostTicket, playerTickets);
            
            if (matchedPlayers.size() == 4) {
                // Find a referee
                MatchmakingTicket assignedReferee = refereeTickets.get(0);
                
                // Calculate central location
                double centerLat = matchedPlayers.stream().mapToDouble(MatchmakingTicket::getLatitude).average().orElse(0);
                double centerLng = matchedPlayers.stream().mapToDouble(MatchmakingTicket::getLongitude).average().orElse(0);

                // Find time slot (>= 20 mins from now, rounded up to next hour)
                LocalDateTime now = LocalDateTime.now();
                LocalDateTime targetStartTime = now.plusMinutes(20).truncatedTo(ChronoUnit.HOURS).plusHours(1);
                // If 8:58 + 20 = 9:18 -> truncated to 9:00 -> +1 hr = 10:00. This is safe. If it was 8:10 + 20 = 8:30 -> truncated 8:00 + 1 hr = 9:00 (which is >= 20 mins). Perfect.
                if (targetStartTime.isBefore(now.plusMinutes(20))) {
                    targetStartTime = targetStartTime.plusHours(1);
                }
                LocalDateTime targetEndTime = targetStartTime.plusHours(1);

                // Find nearest available court
                Court selectedCourt = findNearestAvailableCourt(centerLat, centerLng, targetStartTime, targetEndTime);

                if (selectedCourt != null) {
                    createMatch(selectedCourt, targetStartTime, targetEndTime, matchedPlayers, assignedReferee);
                    
                    // Remove from queue
                    List<Long> matchedUserIds = matchedPlayers.stream().map(MatchmakingTicket::getUserId).collect(Collectors.toList());
                    matchedUserIds.add(assignedReferee.getUserId());
                    ticketRepository.deactivateTickets(matchedUserIds);
                    
                    // Remove from local lists so we can continue the loop
                    playerTickets.removeAll(matchedPlayers);
                    refereeTickets.remove(assignedReferee);
                    log.info("Successfully grouped 4 players and 1 referee at court {} for time {}", selectedCourt.getId(), targetStartTime);
                } else {
                    log.warn("Could not find an available court for the matched group. Will try again later.");
                    // Move the host to the end or just break to try again later
                    break;
                }
            } else {
                // The longest waiting player cannot find a match right now. Move to next or wait.
                // For simplicity, we break or remove from temporary list to try others.
                playerTickets.remove(0);
            }
        }
    }

    private List<MatchmakingTicket> findMatchingPlayers(MatchmakingTicket host, List<MatchmakingTicket> pool) {
        List<MatchmakingTicket> matched = new ArrayList<>();
        matched.add(host);
        
        int[] eloRange = matchmakingService.calculateEloRange(host.getElo());
        
        for (MatchmakingTicket candidate : pool) {
            if (candidate.getId().equals(host.getId())) continue;
            
            // Check Elo
            if (candidate.getElo() >= eloRange[0] && candidate.getElo() <= eloRange[1]) {
                // Check distance (approximate Haversine)
                double distance = matchmakingService.haversine(
                        host.getLatitude(), host.getLongitude(),
                        candidate.getLatitude(), candidate.getLongitude()
                );
                
                if (distance <= 15.0) { // 15km radius
                    matched.add(candidate);
                    if (matched.size() == 4) break;
                }
            }
        }
        return matched;
    }

    private Court findNearestAvailableCourt(double lat, double lng, LocalDateTime start, LocalDateTime end) {
        List<Venue> venues = venueRepository.findActiveVenues();
        
        // Sort venues by distance to center
        venues.sort((v1, v2) -> {
            double d1 = (v1.getLocation() != null && v1.getLocation().getLatitude() != null) 
                    ? matchmakingService.haversine(lat, lng, v1.getLocation().getLatitude().doubleValue(), v1.getLocation().getLongitude().doubleValue()) 
                    : Double.MAX_VALUE;
            double d2 = (v2.getLocation() != null && v2.getLocation().getLatitude() != null) 
                    ? matchmakingService.haversine(lat, lng, v2.getLocation().getLatitude().doubleValue(), v2.getLocation().getLongitude().doubleValue()) 
                    : Double.MAX_VALUE;
            return Double.compare(d1, d2);
        });

        for (Venue venue : venues) {
            List<Court> courts = courtRepository.findByVenueId(venue.getId());
            for (Court court : courts) {
                if (!court.isActive()) continue;
                List<Booking> conflicts = bookingRepository.findConflictingBookings(court.getId(), start, end);
                if (conflicts.isEmpty()) {
                    return court; // First available court in nearest venue
                }
            }
        }
        return null;
    }

    private void createMatch(Court court, LocalDateTime start, LocalDateTime end, List<MatchmakingTicket> players, MatchmakingTicket referee) {
        // Find venue fee (mocked simple or fetch proper pricing)
        // Here we create a PENDING booking. Users will need to call joining/deposit API or a specific "acceptMatch" API.
        
        Booking booking = Booking.builder()
                .courtId(court.getId())
                .startTime(start)
                .endTime(end)
                .bookingType(BookingType.RANKED)
                .status(BookingStatus.PENDING)
                .notes("Auto-matchmaking generated")
                .createdAt(LocalDateTime.now())
                .createdByPlayerId(players.get(0).getUserId()) // Make the first player the host nominally
                .build();
                
        // In a full implementation, we'd calculate costs here using PriceCalculationService.
        // For now, we save it directly to establish the match.
        booking = bookingRepository.save(booking);
        
        RankedMatch rankedMatch = RankedMatch.builder()
                .bookingId(booking.getId())
                .status(MatchStatus.PENDING)
                .build();
        rankedMatchRepository.save(rankedMatch);
        
        // Insert participants as PENDING (They still need to pay/deposit)
        // If Payment fails in 2 mins, a separate job will CANCEL this booking and re-queue Paid members.
        for (MatchmakingTicket p : players) {
            BookingParticipant bp = BookingParticipant.builder()
                    .bookingId(booking.getId())
                    .userId(p.getUserId())
                    .role(ParticipantRole.PLAYER)
                    .joinStatus(JoinStatus.PENDING)
                    .isMatchHost(p.getId().equals(players.get(0).getId()))
                    .build();
            booking.addParticipant(bp);
        }
        
        BookingParticipant refBp = BookingParticipant.builder()
                .bookingId(booking.getId())
                .userId(referee.getUserId())
                .role(ParticipantRole.REFEREE)
                .joinStatus(JoinStatus.PENDING)
                .isMatchHost(false)
                .build();
        booking.addParticipant(refBp);
        
        bookingRepository.save(booking);
    }
}
