package com.pickleball.application.usecases.wallet;

import com.pickleball.domain.entities.Transaction;
import com.pickleball.domain.entities.Wallet;
import com.pickleball.domain.repositories.TransactionRepository;
import com.pickleball.domain.repositories.WalletRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Component
@RequiredArgsConstructor
public class WithdrawWalletUseCase {

    private final WalletRepository walletRepository;
    private final TransactionRepository transactionRepository;

    @Transactional
    public Wallet execute(Long userId, BigDecimal amount, String description) {
        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Số tiền rút phải lớn hơn 0");
        }

        Wallet wallet = walletRepository.findByUserId(userId)
                .orElseThrow(() -> new IllegalArgumentException("Ví không tồn tại"));

        if (wallet.getBalance().compareTo(amount) < 0) {
            throw new IllegalArgumentException("Số dư không đủ để rút tiền");
        }

        wallet.debit(amount);
        Wallet updatedWallet = walletRepository.save(wallet);

        Transaction transaction = Transaction.builder()
                .userId(userId)
                .bookingId(null)
                .amount(amount)
                .type("WITHDRAWAL")
                .status("SUCCESS")
                .description(description != null ? description : "Rút tiền từ ví")
                .createdAt(LocalDateTime.now())
                .build();
        transactionRepository.save(transaction);

        return updatedWallet;
    }
}

