package com.pickleball.application.usecases.matchmaking;

import com.pickleball.application.usecases.wallet.PayWithWalletUseCase;
import com.pickleball.domain.entities.*;
import com.pickleball.domain.enums.BookingStatus;
import com.pickleball.domain.enums.BookingType;
import com.pickleball.domain.enums.JoinStatus;
import com.pickleball.domain.enums.MatchStatus;
import com.pickleball.domain.enums.ParticipantRole;
import com.pickleball.domain.repositories.*;
import com.pickleball.domain.services.MatchmakingService;
import com.pickleball.domain.services.PriceCalculationService;
import com.pickleball.domain.valueobjects.Money;
import com.pickleball.infrastructure.persistence.repositories.RankedPartyJpaRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

public class ProcessMatchmakingQueueUseCase {

    private static final Logger log = LoggerFactory.getLogger(ProcessMatchmakingQueueUseCase.class);
    private static final BigDecimal DEPOSIT_PERCENTAGE = new BigDecimal("0.25");

    private final MatchmakingTicketRepository ticketRepository;
    private final BookingRepository bookingRepository;
    private final CourtRepository courtRepository;
    private final VenueRepository venueRepository;
    private final RankedMatchRepository rankedMatchRepository;
    private final MatchmakingService matchmakingService;
    private final CourtPricingRepository courtPricingRepository;
    private final PriceCalculationService priceCalculationService;
    private final RefereeRepository refereeRepository;
    private final PayWithWalletUseCase payWithWalletUseCase;
    private final RankedPartyJpaRepository rankedPartyRepository;

    public ProcessMatchmakingQueueUseCase(
            MatchmakingTicketRepository ticketRepository,
            BookingRepository bookingRepository,
            CourtRepository courtRepository,
            VenueRepository venueRepository,
            RankedMatchRepository rankedMatchRepository,
            MatchmakingService matchmakingService,
            CourtPricingRepository courtPricingRepository,
            PriceCalculationService priceCalculationService,
            RefereeRepository refereeRepository,
            PayWithWalletUseCase payWithWalletUseCase,
            RankedPartyJpaRepository rankedPartyRepository) {
        this.ticketRepository = ticketRepository;
        this.bookingRepository = bookingRepository;
        this.courtRepository = courtRepository;
        this.venueRepository = venueRepository;
        this.rankedMatchRepository = rankedMatchRepository;
        this.matchmakingService = matchmakingService;
        this.courtPricingRepository = courtPricingRepository;
        this.priceCalculationService = priceCalculationService;
        this.refereeRepository = refereeRepository;
        this.payWithWalletUseCase = payWithWalletUseCase;
        this.rankedPartyRepository = rankedPartyRepository;
    }

    @Transactional
    public void execute() {
        List<MatchmakingTicket> playerTickets = ticketRepository.findActiveTicketsByRoleOrderByJoinedAtAsc(ParticipantRole.PLAYER);
        
        List<Referee> readyReferees = refereeRepository.findEligibleReferees().stream()
                .filter(Referee::getIsReady)
                .collect(Collectors.toList());

        if (playerTickets.size() < 4 || readyReferees.isEmpty()) {
            log.debug("Not enough players ({}) or ready referees ({}) in queue to form a match.", playerTickets.size(), readyReferees.size());
            return;
        }

        // Simple greedy matching: Take the first player (longest waiting)
        while (playerTickets.size() >= 4 && !readyReferees.isEmpty()) {
            MatchmakingTicket hostTicket = playerTickets.get(0);
            
            // Find 3 other players matching elo and distance
            List<MatchmakingTicket> matchedPlayers = findMatchingPlayers(hostTicket, playerTickets);
            
            if (matchedPlayers.size() == 4) {
                // Calculate central location
                double centerLat = matchedPlayers.stream().mapToDouble(MatchmakingTicket::getLatitude).average().orElse(0);
                double centerLng = matchedPlayers.stream().mapToDouble(MatchmakingTicket::getLongitude).average().orElse(0);

                LocalDateTime targetStartTime = resolveStartTime(matchedPlayers);
                LocalDateTime targetEndTime = resolveEndTime(matchedPlayers, targetStartTime);

                // Find nearest available court
                Court selectedCourt = findNearestAvailableCourt(centerLat, centerLng, targetStartTime, targetEndTime);

                if (selectedCourt != null) {
                    boolean matchCreated = false;

                    // Calculate expected total cost and deposit for the referee
                    List<CourtPricing> pricings = courtPricingRepository.findByCourtId(selectedCourt.getId());
                    Money venueFee = new Money(new BigDecimal("200000"), "VND");
                    if (!pricings.isEmpty()) {
                        long hours = ChronoUnit.HOURS.between(targetStartTime, targetEndTime);
                        if (hours <= 0) hours = 1;
                        Money pricePerHour = priceCalculationService.calculateSlotPrice(
                                pricings, targetStartTime.toLocalTime(), targetStartTime.getDayOfWeek());
                        BigDecimal totalAmount = pricePerHour.getAmount().multiply(BigDecimal.valueOf(hours));
                        venueFee = new Money(totalAmount, pricePerHour.getCurrency());
                    }

                    Money refereeFee = new Money(new BigDecimal("100000"), "VND");
                    BigDecimal totalCostAmount = venueFee.getAmount().add(refereeFee.getAmount())
                            .add(venueFee.getAmount().multiply(new BigDecimal("0.20")));
                    BigDecimal requiredDeposit = totalCostAmount.multiply(DEPOSIT_PERCENTAGE).setScale(0, RoundingMode.HALF_UP);

                    // Actually, a better approach: create the match, then try assigning referees until one pays successfully.
                    Referee selectedReferee = null;
                    for (int i = 0; i < readyReferees.size(); i++) {
                        Referee ref = readyReferees.get(i);
                        try {
                            Booking booking = createMatchWithRefereePayment(selectedCourt, targetStartTime, targetEndTime, matchedPlayers, ref);
                            markMatchedParties(matchedPlayers, booking.getId());
                            selectedReferee = ref;
                            matchCreated = true;
                            break;
                        } catch (Exception e) {
                            log.debug("Referee {} failed to pay deposit, trying next. Reason: {}", ref.getUserId(), e.getMessage());
                        }
                    }

                    if (matchCreated) {
                        // Remove from queue
                        List<Long> matchedUserIds = matchedPlayers.stream().map(MatchmakingTicket::getUserId).collect(Collectors.toList());
                        ticketRepository.deactivateTickets(matchedUserIds);
                        
                        // Remove from local lists so we can continue the loop
                        playerTickets.removeAll(matchedPlayers);
                        readyReferees.remove(selectedReferee);
                        log.info("Successfully grouped 4 players and 1 referee at court {} for time {}", selectedCourt.getId(), targetStartTime);
                    } else {
                        log.warn("Could not find a referee with sufficient balance for the matched group.");
                        // Can't form match right now due to no referee with money, break or just remove players to retry later?
                        break;
                    }

                } else {
                    log.warn("Could not find an available court for the matched group. Will try again later.");
                    break;
                }
            } else {
                playerTickets.remove(0);
            }
        }
    }

    private List<MatchmakingTicket> findMatchingPlayers(MatchmakingTicket host, List<MatchmakingTicket> pool) {
        List<List<MatchmakingTicket>> groups = groupPlayerTickets(pool);
        List<MatchmakingTicket> hostGroup = groups.stream()
                .filter(group -> group.stream().anyMatch(ticket -> ticket.getId().equals(host.getId())))
                .findFirst()
                .orElse(List.of(host));

        if (hostGroup.size() > 2) {
            return List.of();
        }

        List<MatchmakingTicket> matched = new ArrayList<>(hostGroup);
        int hostElo = averageElo(hostGroup);
        double hostLat = averageLatitude(hostGroup);
        double hostLng = averageLongitude(hostGroup);
        int[] eloRange = matchmakingService.calculateEloRange(hostElo);

        for (List<MatchmakingTicket> candidateGroup : groups) {
            if (candidateGroup == hostGroup || candidateGroup.stream().anyMatch(ticket -> ticket.getId().equals(host.getId()))) {
                continue;
            }
            if (candidateGroup.size() > 2 || matched.size() + candidateGroup.size() > 4) {
                continue;
            }
            if (!hasSameRequestedTime(hostGroup, candidateGroup)) {
                continue;
            }

            int candidateElo = averageElo(candidateGroup);
            if (candidateElo < eloRange[0] || candidateElo > eloRange[1]) {
                continue;
            }

            double distance = matchmakingService.haversine(
                    hostLat, hostLng,
                    averageLatitude(candidateGroup), averageLongitude(candidateGroup)
            );

            if (distance <= 15.0) {
                matched.addAll(candidateGroup);
                if (matched.size() == 4) break;
            }
        }
        return matched;
    }

    private List<List<MatchmakingTicket>> groupPlayerTickets(List<MatchmakingTicket> tickets) {
        Map<String, List<MatchmakingTicket>> grouped = new LinkedHashMap<>();
        for (MatchmakingTicket ticket : tickets) {
            String key = ticket.getPartyId() != null ? "party-" + ticket.getPartyId() : "solo-" + ticket.getId();
            grouped.computeIfAbsent(key, ignored -> new ArrayList<>()).add(ticket);
        }

        return grouped.values().stream()
                .sorted(Comparator.comparing(group -> group.stream()
                        .map(MatchmakingTicket::getJoinedAt)
                        .min(LocalDateTime::compareTo)
                        .orElse(LocalDateTime.MAX)))
                .collect(Collectors.toList());
    }

    private int averageElo(List<MatchmakingTicket> group) {
        return (int) Math.round(group.stream().mapToInt(MatchmakingTicket::getElo).average().orElse(0));
    }

    private double averageLatitude(List<MatchmakingTicket> group) {
        return group.stream().mapToDouble(MatchmakingTicket::getLatitude).average().orElse(0);
    }

    private double averageLongitude(List<MatchmakingTicket> group) {
        return group.stream().mapToDouble(MatchmakingTicket::getLongitude).average().orElse(0);
    }

    private boolean hasSameRequestedTime(List<MatchmakingTicket> firstGroup, List<MatchmakingTicket> secondGroup) {
        return Objects.equals(requestedStartTime(firstGroup), requestedStartTime(secondGroup))
                && Objects.equals(requestedEndTime(firstGroup), requestedEndTime(secondGroup));
    }

    private LocalDateTime requestedStartTime(List<MatchmakingTicket> group) {
        return group.stream()
                .map(MatchmakingTicket::getRequestedStartTime)
                .filter(Objects::nonNull)
                .findFirst()
                .orElse(null);
    }

    private LocalDateTime requestedEndTime(List<MatchmakingTicket> group) {
        return group.stream()
                .map(MatchmakingTicket::getRequestedEndTime)
                .filter(Objects::nonNull)
                .findFirst()
                .orElse(null);
    }

    private LocalDateTime resolveStartTime(List<MatchmakingTicket> matchedPlayers) {
        LocalDateTime requestedStart = requestedStartTime(matchedPlayers);
        if (requestedStart != null) {
            return requestedStart;
        }

        LocalDateTime now = LocalDateTime.now();
        LocalDateTime targetStartTime = now.plusMinutes(20).truncatedTo(ChronoUnit.HOURS).plusHours(1);
        if (targetStartTime.isBefore(now.plusMinutes(20))) {
            targetStartTime = targetStartTime.plusHours(1);
        }
        return targetStartTime;
    }

    private LocalDateTime resolveEndTime(List<MatchmakingTicket> matchedPlayers, LocalDateTime startTime) {
        LocalDateTime requestedEnd = requestedEndTime(matchedPlayers);
        return requestedEnd != null ? requestedEnd : startTime.plusHours(1);
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

    private Booking createMatchWithRefereePayment(Court court, LocalDateTime start, LocalDateTime end, List<MatchmakingTicket> players, Referee referee) {
        
        List<CourtPricing> pricings = courtPricingRepository.findByCourtId(court.getId());
        
        Money venueFee = new Money(new BigDecimal("200000"), "VND");
        if (!pricings.isEmpty()) {
            long hours = ChronoUnit.HOURS.between(start, end);
            if (hours <= 0) hours = 1;
            Money pricePerHour = priceCalculationService.calculateSlotPrice(
                    pricings, start.toLocalTime(), start.getDayOfWeek());
            BigDecimal totalAmount = pricePerHour.getAmount().multiply(BigDecimal.valueOf(hours));
            venueFee = new Money(totalAmount, pricePerHour.getCurrency());
        }

        Money refereeFee = new Money(new BigDecimal("100000"), "VND");

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
                
        booking.calculateCosts(venueFee, refereeFee, new BigDecimal("0.20"));
        booking = bookingRepository.save(booking);

        // Attempt to charge the referee deposit immediately
        BigDecimal depositAmount = booking.getTotalCost().getAmount()
                .multiply(DEPOSIT_PERCENTAGE)
                .setScale(0, RoundingMode.HALF_UP);
        Money requiredDeposit = new Money(depositAmount, "VND");

        String description = "Referee deposit - Auto Assigned Booking #" + booking.getId();
        payWithWalletUseCase.execute(
                referee.getUserId(),
                depositAmount,
                booking.getId(),
                description
        );
        
        RankedMatch rankedMatch = RankedMatch.builder()
                .bookingId(booking.getId())
                .status(MatchStatus.PENDING)
                .refereeId(referee.getUserId())
                .build();
        rankedMatchRepository.save(rankedMatch);
        
        // Insert participants as PENDING (Players still need to pay/deposit)
        // If Payment fails in 2 mins, a separate job will CANCEL this booking and re-queue Paid members.
        for (MatchmakingTicket p : players) {
            BookingParticipant bp = BookingParticipant.builder()
                    .bookingId(booking.getId())
                    .userId(p.getUserId())
                    .partyId(p.getPartyId())
                    .role(ParticipantRole.PLAYER)
                    .joinStatus(JoinStatus.PENDING)
                    .depositAmount(requiredDeposit)
                    .refundAmount(new Money(BigDecimal.ZERO, "VND"))
                    .isMatchHost(p.getId().equals(players.get(0).getId()))
                    .build();
            booking.addParticipant(bp);
        }
        
        BookingParticipant refBp = BookingParticipant.builder()
                .bookingId(booking.getId())
                .userId(referee.getUserId())
                .role(ParticipantRole.REFEREE)
                .joinStatus(JoinStatus.PAID)
                .depositAmount(requiredDeposit)
                .refundAmount(new Money(BigDecimal.ZERO, "VND"))
                .isMatchHost(false)
                .build();
        booking.addParticipant(refBp);
        
        return bookingRepository.save(booking);
    }

    private void markMatchedParties(List<MatchmakingTicket> players, Long bookingId) {
        List<Long> partyIds = players.stream()
                .map(MatchmakingTicket::getPartyId)
                .filter(Objects::nonNull)
                .distinct()
                .collect(Collectors.toList());

        for (Long partyId : partyIds) {
            rankedPartyRepository.findById(partyId).ifPresent(party -> {
                party.setStatus("MATCHED");
                party.setMatchedBookingId(bookingId);
                rankedPartyRepository.save(party);
            });
        }
    }
}
