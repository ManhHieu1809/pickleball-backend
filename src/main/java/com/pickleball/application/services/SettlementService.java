package com.pickleball.application.services;

import com.pickleball.domain.entities.Booking;
import com.pickleball.domain.entities.BookingParticipant;
import com.pickleball.domain.entities.Transaction;
import com.pickleball.domain.entities.Venue;
import com.pickleball.domain.entities.VenueOwner;
import com.pickleball.domain.entities.Wallet;
import com.pickleball.domain.entities.Court;
import com.pickleball.domain.enums.BookingStatus;
import com.pickleball.domain.enums.ParticipantRole;
import com.pickleball.domain.repositories.BookingRepository;
import com.pickleball.domain.repositories.TransactionRepository;
import com.pickleball.domain.repositories.VenueOwnerRepository;
import com.pickleball.domain.repositories.VenueRepository;
import com.pickleball.domain.repositories.WalletRepository;
import com.pickleball.domain.repositories.CourtRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@RequiredArgsConstructor
public class SettlementService {

    private final BookingRepository bookingRepository;
    private final WalletRepository walletRepository;
    private final TransactionRepository transactionRepository;
    private final VenueRepository venueRepository;
    private final CourtRepository courtRepository;

    private static final BigDecimal PLATFORM_FEE_PERCENTAGE = new BigDecimal("0.20");

    @Transactional
    public void processSettlement(Long bookingId) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new IllegalArgumentException("Booking not found"));

        if (booking.getStatus() != BookingStatus.COMPLETED) {
            throw new IllegalStateException("Booking must be COMPLETED to process settlement");
        }

        processVenuePayment(booking);
        processRefereePayment(booking);
    }

    private void processVenuePayment(Booking booking) {
        if (booking.getVenueFee() == null || booking.getVenueFee().getAmount().compareTo(BigDecimal.ZERO) <= 0) {
            return;
        }

        Court court = courtRepository.findById(booking.getCourtId())
                .orElseThrow(() -> new IllegalArgumentException("Court not found"));
        
        Venue venue = venueRepository.findById(court.getVenueId())
                .orElseThrow(() -> new IllegalArgumentException("Venue not found"));

        Long ownerId = venue.getOwnerId();
        BigDecimal grossAmount = booking.getVenueFee().getAmount();
        BigDecimal platformFee = grossAmount.multiply(PLATFORM_FEE_PERCENTAGE);
        BigDecimal netAmount = grossAmount.subtract(platformFee);

        Wallet ownerWallet = walletRepository.findByUserId(ownerId)
                .orElseGet(() -> {
                    Wallet newWallet = Wallet.builder()
                            .userId(ownerId)
                            .balance(BigDecimal.ZERO)
                            .updatedAt(LocalDateTime.now())
                            .build();
                    return walletRepository.save(newWallet);
                });
        
        ownerWallet.credit(netAmount);
        walletRepository.save(ownerWallet);

        Transaction transaction = Transaction.builder()
                .userId(ownerId)
                .bookingId(booking.getId())
                .amount(netAmount)
                .type("PAYOUT")
                .status("SUCCESS")
                .description("Payout for booking " + booking.getId() + " at " + venue.getName())
                .createdAt(LocalDateTime.now())
                .build();
        transactionRepository.save(transaction);
    }
    
    private void processRefereePayment(Booking booking) {
        if (booking.getRefereeFee() == null || booking.getRefereeFee().getAmount().compareTo(BigDecimal.ZERO) <= 0) {
            return;
        }

        BookingParticipant referee = booking.getParticipants().stream()
                .filter(p -> p.getRole() == ParticipantRole.REFEREE)
                .findFirst()
                .orElse(null);

        if (referee == null) return;

        BigDecimal amount = booking.getRefereeFee().getAmount();
        Long refereeId = referee.getUserId();
        Wallet refereeWallet = walletRepository.findByUserId(refereeId)
                .orElseGet(() -> {
                    Wallet newWallet = Wallet.builder().userId(refereeId).balance(BigDecimal.ZERO).build();
                    return walletRepository.save(newWallet);
                });

        refereeWallet.credit(amount);
        walletRepository.save(refereeWallet);

        Transaction transaction = Transaction.builder()
                .userId(refereeId)
                .bookingId(booking.getId())
                .amount(amount)
                .type("PAYOUT")
                .status("SUCCESS")
                .description("Referee fee for booking " + booking.getId())
                .createdAt(LocalDateTime.now())
                .build();
        transactionRepository.save(transaction);
    }
}
