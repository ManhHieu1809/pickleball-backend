package com.pickleball.application.services;

import com.pickleball.application.dtos.RankedPartyDTO;
import com.pickleball.application.dtos.PendingInviteCountDTO;
import com.pickleball.application.dtos.RankedAvailabilityDTO;
import com.pickleball.application.dtos.requests.CheckRankedAvailabilityRequest;
import com.pickleball.application.dtos.requests.CreateRankedPartyRequest;
import com.pickleball.application.dtos.requests.InviteRankedPartyMemberRequest;
import com.pickleball.application.dtos.requests.QueueRankedPartyRequest;
import com.pickleball.domain.entities.Booking;
import com.pickleball.domain.entities.Court;
import com.pickleball.domain.entities.MatchmakingTicket;
import com.pickleball.domain.entities.Player;
import com.pickleball.domain.entities.User;
import com.pickleball.domain.entities.Venue;
import com.pickleball.domain.enums.ParticipantRole;
import com.pickleball.domain.repositories.BookingRepository;
import com.pickleball.domain.repositories.CourtRepository;
import com.pickleball.domain.repositories.MatchmakingTicketRepository;
import com.pickleball.domain.repositories.PlayerRepository;
import com.pickleball.domain.repositories.UserRepository;
import com.pickleball.domain.repositories.VenueRepository;
import com.pickleball.domain.services.MatchmakingService;
import com.pickleball.infrastructure.persistence.entities.RankedPartyEntity;
import com.pickleball.infrastructure.persistence.entities.RankedPartyMemberEntity;
import com.pickleball.infrastructure.persistence.repositories.RankedPartyJpaRepository;
import com.pickleball.infrastructure.persistence.repositories.RankedPartyMemberJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class RankedPartyApplicationService {

    private static final int MAX_PARTY_SIZE = 2;
    private static final String STATUS_OPEN = "OPEN";
    private static final String STATUS_QUEUED = "QUEUED";
    private static final String STATUS_MATCHED = "MATCHED";
    private static final String MEMBER_INVITED = "INVITED";
    private static final String MEMBER_JOINED = "JOINED";
    private static final String MEMBER_REJECTED = "REJECTED";
    private static final String MEMBER_LEFT = "LEFT";

    private final RankedPartyJpaRepository partyRepository;
    private final RankedPartyMemberJpaRepository memberRepository;
    private final MatchmakingTicketRepository matchmakingTicketRepository;
    private final PlayerRepository playerRepository;
    private final UserRepository userRepository;
    private final BookingRepository bookingRepository;
    private final VenueRepository venueRepository;
    private final CourtRepository courtRepository;
    private final MatchmakingService matchmakingService;
    private final RankedPartyMatchLifecycleService rankedPartyMatchLifecycleService;

    @Transactional
    public RankedPartyDTO createParty(CreateRankedPartyRequest request) {
        requirePlayer(request.getHostUserId());
        if (request.getStartTime() != null || request.getEndTime() != null) {
            if (request.getStartTime() == null || request.getEndTime() == null) {
                throw new IllegalArgumentException("Both startTime and endTime are required when creating a time-specific ranked party");
            }
            validateRequestedTime(request.getStartTime(), request.getEndTime());
            ensureNoActiveRankedOverlap(request.getHostUserId(), request.getStartTime(), request.getEndTime());
        }

        if (request.getStartTime() == null) {
            List<RankedPartyEntity> activeParties = partyRepository.findByHostUserIdAndStatusInOrderByCreatedAtDesc(
                    request.getHostUserId(), Arrays.asList(STATUS_OPEN, STATUS_QUEUED));
            if (!activeParties.isEmpty()) {
                return toDTO(activeParties.get(0));
            }
        }

        RankedPartyEntity party = RankedPartyEntity.builder()
                .hostUserId(request.getHostUserId())
                .status(STATUS_OPEN)
                .latitude(request.getLatitude())
                .longitude(request.getLongitude())
                .requestedStartTime(request.getStartTime())
                .requestedEndTime(request.getEndTime())
                .build();
        party = partyRepository.save(party);

        RankedPartyMemberEntity hostMember = RankedPartyMemberEntity.builder()
                .partyId(party.getId())
                .userId(request.getHostUserId())
                .status(MEMBER_JOINED)
                .isHost(true)
                .joinedAt(LocalDateTime.now())
                .build();
        memberRepository.save(hostMember);

        return toDTO(party);
    }

    @Transactional
    public RankedPartyDTO getParty(Long partyId) {
        RankedPartyEntity party = getPartyOrThrow(partyId);
        return toDTO(party);
    }

    @Transactional(readOnly = true)
    public List<RankedPartyDTO> getMyInvites(Long userId) {
        return memberRepository.findByUserIdAndStatusOrderByCreatedAtDesc(userId, MEMBER_INVITED).stream()
                .map(member -> partyRepository.findById(member.getPartyId()).orElse(null))
                .filter(Objects::nonNull)
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public PendingInviteCountDTO getPendingInviteCount(Long userId) {
        return PendingInviteCountDTO.builder()
                .pendingCount(memberRepository.countByUserIdAndStatus(userId, MEMBER_INVITED))
                .build();
    }

    @Transactional
    public RankedPartyDTO getMyActiveParty(Long userId) {
        List<RankedPartyDTO> parties = getMyActiveParties(userId);
        return parties.isEmpty() ? null : parties.get(0);
    }

    @Transactional
    public List<RankedPartyDTO> getMyActiveParties(Long userId) {
        return memberRepository.findByUserIdAndStatusInOrderByCreatedAtDesc(userId, Arrays.asList(MEMBER_JOINED, MEMBER_INVITED)).stream()
                .map(member -> partyRepository.findById(member.getPartyId()).orElse(null))
                .filter(Objects::nonNull)
                .map(rankedPartyMatchLifecycleService::reopenIfMatchedBookingCancelled)
                .filter(party -> STATUS_OPEN.equals(party.getStatus())
                        || STATUS_QUEUED.equals(party.getStatus())
                        || STATUS_MATCHED.equals(party.getStatus()))
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public RankedAvailabilityDTO checkAvailability(CheckRankedAvailabilityRequest request) {
        validateRequestedTime(request.getStartTime(), request.getEndTime());
        return findAvailability(request.getLatitude(), request.getLongitude(), request.getStartTime(), request.getEndTime());
    }

    @Transactional
    public RankedPartyDTO inviteMember(Long partyId, InviteRankedPartyMemberRequest request) {
        RankedPartyEntity party = getPartyOrThrow(partyId);
        ensureOpen(party);
        requirePlayer(request.getInviteeUserId());

        RankedPartyMemberEntity inviter = memberRepository.findByPartyIdAndUserId(partyId, request.getInviterUserId())
                .orElseThrow(() -> new IllegalArgumentException("Inviter is not in this ranked party"));
        if (!MEMBER_JOINED.equals(inviter.getStatus())) {
            throw new IllegalArgumentException("Inviter must be a joined party member");
        }

        RankedPartyMemberEntity existingInvitee = memberRepository.findByPartyIdAndUserId(partyId, request.getInviteeUserId())
                .orElse(null);
        if (existingInvitee != null
                && (MEMBER_JOINED.equals(existingInvitee.getStatus()) || MEMBER_INVITED.equals(existingInvitee.getStatus()))) {
            throw new IllegalArgumentException("User is already in this ranked party");
        }

        long activeMemberCount = memberRepository.findByPartyIdOrderByCreatedAtAsc(partyId).stream()
                .filter(member -> MEMBER_JOINED.equals(member.getStatus()) || MEMBER_INVITED.equals(member.getStatus()))
                .count();
        if (activeMemberCount >= MAX_PARTY_SIZE) {
            throw new IllegalArgumentException("Ranked party is full");
        }

        if (existingInvitee != null) {
            existingInvitee.setInvitedByUserId(request.getInviterUserId());
            existingInvitee.setStatus(MEMBER_INVITED);
            existingInvitee.setIsHost(false);
            existingInvitee.setMessage(request.getMessage());
            existingInvitee.setJoinedAt(null);
            existingInvitee.setRespondedAt(null);
            memberRepository.save(existingInvitee);
        } else {
            RankedPartyMemberEntity invite = RankedPartyMemberEntity.builder()
                    .partyId(partyId)
                    .userId(request.getInviteeUserId())
                    .invitedByUserId(request.getInviterUserId())
                    .status(MEMBER_INVITED)
                    .isHost(false)
                    .message(request.getMessage())
                    .build();
            memberRepository.save(invite);
        }

        return toDTO(party);
    }

    @Transactional
    public RankedPartyDTO acceptInvite(Long partyId, Long userId) {
        RankedPartyEntity party = getPartyOrThrow(partyId);
        ensureOpen(party);

        RankedPartyMemberEntity member = memberRepository.findByPartyIdAndUserId(partyId, userId)
                .orElseThrow(() -> new IllegalArgumentException("Invite not found"));
        if (!MEMBER_INVITED.equals(member.getStatus())) {
            throw new IllegalArgumentException("Invite is not pending");
        }

        long joinedCount = memberRepository.findByPartyIdOrderByCreatedAtAsc(partyId).stream()
                .filter(existing -> MEMBER_JOINED.equals(existing.getStatus()))
                .count();
        if (joinedCount >= MAX_PARTY_SIZE) {
            throw new IllegalArgumentException("Ranked party is full");
        }

        member.setStatus(MEMBER_JOINED);
        member.setJoinedAt(LocalDateTime.now());
        member.setRespondedAt(LocalDateTime.now());
        memberRepository.save(member);

        return toDTO(party);
    }

    @Transactional
    public RankedPartyDTO rejectInvite(Long partyId, Long userId) {
        RankedPartyEntity party = getPartyOrThrow(partyId);
        RankedPartyMemberEntity member = memberRepository.findByPartyIdAndUserId(partyId, userId)
                .orElseThrow(() -> new IllegalArgumentException("Invite not found"));
        if (!MEMBER_INVITED.equals(member.getStatus())) {
            throw new IllegalArgumentException("Invite is not pending");
        }

        member.setStatus(MEMBER_REJECTED);
        member.setRespondedAt(LocalDateTime.now());
        memberRepository.save(member);

        return toDTO(party);
    }

    @Transactional
    public RankedPartyDTO leaveParty(Long partyId, Long userId) {
        RankedPartyEntity party = getPartyOrThrow(partyId);
        ensureOpen(party);

        RankedPartyMemberEntity member = memberRepository.findByPartyIdAndUserId(partyId, userId)
                .orElseThrow(() -> new IllegalArgumentException("Party member not found"));
        if (Boolean.TRUE.equals(member.getIsHost())) {
            party.setStatus("CANCELLED");
            partyRepository.save(party);
        } else {
            member.setStatus(MEMBER_LEFT);
            member.setRespondedAt(LocalDateTime.now());
            memberRepository.save(member);
        }

        return toDTO(party);
    }

    @Transactional
    public RankedPartyDTO removeMember(Long partyId, Long hostUserId, Long memberUserId) {
        RankedPartyEntity party = getPartyOrThrow(partyId);
        ensureOpen(party);
        if (!party.getHostUserId().equals(hostUserId)) {
            throw new IllegalArgumentException("Only the party host can remove members");
        }
        if (party.getHostUserId().equals(memberUserId)) {
            throw new IllegalArgumentException("Host cannot remove themselves");
        }

        RankedPartyMemberEntity member = memberRepository.findByPartyIdAndUserId(partyId, memberUserId)
                .orElseThrow(() -> new IllegalArgumentException("Party member not found"));
        if (!MEMBER_JOINED.equals(member.getStatus()) && !MEMBER_INVITED.equals(member.getStatus())) {
            throw new IllegalArgumentException("Party member is not active");
        }

        member.setStatus(MEMBER_LEFT);
        member.setRespondedAt(LocalDateTime.now());
        memberRepository.save(member);

        return toDTO(party);
    }

    @Transactional
    public RankedPartyDTO queueParty(Long partyId, QueueRankedPartyRequest request) {
        RankedPartyEntity party = getPartyOrThrow(partyId);
        ensureOpen(party);
        if (!party.getHostUserId().equals(request.getUserId())) {
            throw new IllegalArgumentException("Only the party host can join matchmaking");
        }
        validateRequestedTime(request.getStartTime(), request.getEndTime());
        if (party.getRequestedStartTime() != null && party.getRequestedEndTime() != null
                && (!party.getRequestedStartTime().equals(request.getStartTime())
                || !party.getRequestedEndTime().equals(request.getEndTime()))) {
            throw new IllegalArgumentException("Queue time must match the ranked party time");
        }

        RankedAvailabilityDTO availability = findAvailability(
                request.getLatitude(), request.getLongitude(), request.getStartTime(), request.getEndTime());
        if (!availability.isAvailable()) {
            throw new IllegalArgumentException("No available court found near this location for the requested time range");
        }

        List<RankedPartyMemberEntity> joinedMembers = memberRepository.findByPartyIdOrderByCreatedAtAsc(partyId).stream()
                .filter(member -> MEMBER_JOINED.equals(member.getStatus()))
                .collect(Collectors.toList());
        if (joinedMembers.isEmpty() || joinedMembers.size() > MAX_PARTY_SIZE) {
            throw new IllegalArgumentException("Ranked party must have 1 or 2 joined players");
        }

        for (RankedPartyMemberEntity member : joinedMembers) {
            ensureNoActiveRankedOverlap(member.getUserId(), request.getStartTime(), request.getEndTime());
        }

        LocalDateTime now = LocalDateTime.now();
        for (RankedPartyMemberEntity member : joinedMembers) {
            Player player = requirePlayer(member.getUserId());
            matchmakingTicketRepository.save(MatchmakingTicket.builder()
                    .userId(member.getUserId())
                    .partyId(partyId)
                    .role(ParticipantRole.PLAYER)
                    .latitude(request.getLatitude())
                    .longitude(request.getLongitude())
                    .elo(player.getCurrentElo())
                    .requestedStartTime(request.getStartTime())
                    .requestedEndTime(request.getEndTime())
                    .joinedAt(now)
                    .isActive(true)
                    .build());
        }

        party.setLatitude(request.getLatitude());
        party.setLongitude(request.getLongitude());
        party.setQueuedAt(now);
        party.setRequestedStartTime(request.getStartTime());
        party.setRequestedEndTime(request.getEndTime());
        party.setStatus(STATUS_QUEUED);
        party = partyRepository.save(party);

        return toDTO(party);
    }

    @Transactional
    public RankedPartyDTO cancelQueue(Long partyId, Long userId) {
        RankedPartyEntity party = getPartyOrThrow(partyId);
        if (!party.getHostUserId().equals(userId)) {
            throw new IllegalArgumentException("Only the party host can cancel matchmaking");
        }
        if (!STATUS_QUEUED.equals(party.getStatus())) {
            throw new IllegalArgumentException("Ranked party is not queued");
        }

        matchmakingTicketRepository.deactivateTicketsByPartyId(partyId);

        party.setStatus(STATUS_OPEN);
        party.setQueuedAt(null);
        party.setMatchedBookingId(null);
        party = partyRepository.save(party);

        return toDTO(party);
    }

    private RankedPartyEntity getPartyOrThrow(Long partyId) {
        RankedPartyEntity party = partyRepository.findById(partyId)
                .orElseThrow(() -> new IllegalArgumentException("Ranked party not found"));
        return rankedPartyMatchLifecycleService.reopenIfMatchedBookingCancelled(party);
    }

    private void ensureOpen(RankedPartyEntity party) {
        if (!STATUS_OPEN.equals(party.getStatus())) {
            throw new IllegalArgumentException("Ranked party is not open");
        }
    }

    private Player requirePlayer(Long userId) {
        return playerRepository.findByUserId(userId)
                .orElseThrow(() -> new IllegalArgumentException("Player profile not found for user: " + userId));
    }

    private void ensureNoActiveRankedOverlap(Long userId, LocalDateTime startTime, LocalDateTime endTime) {
        if (!matchmakingTicketRepository.findActiveTicketsByUserIdOverlapping(userId, startTime, endTime).isEmpty()) {
            throw new IllegalArgumentException("Party member is already in matchmaking queue for an overlapping time range: " + userId);
        }
        if (!bookingRepository.findActiveRankedMatchesByUserIdOverlapping(userId, startTime, endTime).isEmpty()) {
            throw new IllegalArgumentException("Party member already has an active ranked match in this time range: " + userId);
        }
    }

    private void validateRequestedTime(LocalDateTime startTime, LocalDateTime endTime) {
        if (!endTime.isAfter(startTime)) {
            throw new IllegalArgumentException("End time must be after start time");
        }
        if (!startTime.isAfter(LocalDateTime.now())) {
            throw new IllegalArgumentException("Start time must be in the future");
        }
    }

    private RankedAvailabilityDTO findAvailability(Double latitude, Double longitude, LocalDateTime startTime, LocalDateTime endTime) {
        int availableCourtCount = 0;
        AvailableCourt nearest = null;

        for (Venue venue : venueRepository.findActiveVenues()) {
            if (venue.getLocation() == null || venue.getLocation().getLatitude() == null || venue.getLocation().getLongitude() == null) {
                continue;
            }

            double distanceKm = matchmakingService.haversine(
                    latitude,
                    longitude,
                    venue.getLocation().getLatitude().doubleValue(),
                    venue.getLocation().getLongitude().doubleValue());
            if (distanceKm > 15.0) {
                continue;
            }

            for (Court court : courtRepository.findByVenueId(venue.getId())) {
                if (!court.isActive()) {
                    continue;
                }

                List<Booking> conflicts = bookingRepository.findConflictingBookings(court.getId(), startTime, endTime);
                if (!conflicts.isEmpty()) {
                    continue;
                }

                availableCourtCount++;
                if (nearest == null || distanceKm < nearest.distanceKm()) {
                    nearest = new AvailableCourt(venue, court, distanceKm);
                }
            }
        }

        return RankedAvailabilityDTO.builder()
                .available(availableCourtCount > 0)
                .availableCourtCount(availableCourtCount)
                .nearestVenueId(nearest != null ? nearest.venue().getId() : null)
                .nearestVenueName(nearest != null ? nearest.venue().getName() : null)
                .nearestCourtId(nearest != null ? nearest.court().getId() : null)
                .nearestCourtName(nearest != null ? nearest.court().getCourtName() : null)
                .nearestDistanceKm(nearest != null ? Math.round(nearest.distanceKm() * 100.0) / 100.0 : null)
                .build();
    }

    private RankedPartyDTO toDTO(RankedPartyEntity party) {
        List<RankedPartyMemberEntity> members = memberRepository.findByPartyIdOrderByCreatedAtAsc(party.getId());
        Map<Long, Player> players = playerRepository.findByUserIdIn(
                        members.stream().map(RankedPartyMemberEntity::getUserId).collect(Collectors.toList()))
                .stream()
                .collect(Collectors.toMap(Player::getUserId, player -> player));

        return RankedPartyDTO.builder()
                .id(party.getId())
                .hostUserId(party.getHostUserId())
                .status(party.getStatus())
                .latitude(party.getLatitude())
                .longitude(party.getLongitude())
                .createdAt(party.getCreatedAt())
                .queuedAt(party.getQueuedAt())
                .requestedStartTime(party.getRequestedStartTime())
                .requestedEndTime(party.getRequestedEndTime())
                .matchedBookingId(party.getMatchedBookingId())
                .members(members.stream().map(member -> toMemberDTO(member, players.get(member.getUserId()))).collect(Collectors.toList()))
                .build();
    }

    private RankedPartyDTO.MemberDTO toMemberDTO(RankedPartyMemberEntity member, Player player) {
        User user = userRepository.findById(member.getUserId()).orElse(null);
        return RankedPartyDTO.MemberDTO.builder()
                .id(member.getId())
                .partyId(member.getPartyId())
                .userId(member.getUserId())
                .fullName(user != null ? user.getFullName() : null)
                .currentElo(player != null ? player.getCurrentElo() : null)
                .invitedByUserId(member.getInvitedByUserId())
                .status(member.getStatus())
                .isHost(member.getIsHost())
                .message(member.getMessage())
                .joinedAt(member.getJoinedAt())
                .respondedAt(member.getRespondedAt())
                .createdAt(member.getCreatedAt())
                .build();
    }

    private record AvailableCourt(Venue venue, Court court, double distanceKm) {}
}
