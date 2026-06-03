package com.pickleball.application.services;

import com.pickleball.domain.enums.BookingStatus;
import com.pickleball.infrastructure.persistence.entities.BookingEntity;
import com.pickleball.infrastructure.persistence.entities.RankedPartyEntity;
import com.pickleball.infrastructure.persistence.repositories.BookingJpaRepository;
import com.pickleball.infrastructure.persistence.repositories.RankedPartyJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class RankedPartyMatchLifecycleService {

    private static final String STATUS_OPEN = "OPEN";

    private final RankedPartyJpaRepository rankedPartyRepository;
    private final BookingJpaRepository bookingRepository;

    @Transactional
    public void reopenMatchedPartiesForCancelledBooking(Long bookingId) {
        if (bookingId == null) {
            return;
        }

        List<RankedPartyEntity> parties = rankedPartyRepository.findByMatchedBookingId(bookingId);
        for (RankedPartyEntity party : parties) {
            party.setStatus(STATUS_OPEN);
            party.setMatchedBookingId(null);
            party.setQueuedAt(null);
            rankedPartyRepository.save(party);
        }
    }

    @Transactional
    public RankedPartyEntity reopenIfMatchedBookingCancelled(RankedPartyEntity party) {
        if (party == null || party.getMatchedBookingId() == null) {
            return party;
        }

        BookingEntity booking = bookingRepository.findById(party.getMatchedBookingId()).orElse(null);
        if (booking == null || booking.getStatus() == BookingStatus.CANCELLED) {
            party.setStatus(STATUS_OPEN);
            party.setMatchedBookingId(null);
            party.setQueuedAt(null);
            return rankedPartyRepository.save(party);
        }

        return party;
    }
}
