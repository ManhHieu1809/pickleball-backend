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
public class TopUpWalletUseCase {

    private final WalletRepository walletRepository;
    private final TransactionRepository transactionRepository;

    @Transactional
    public Wallet execute(Long userId, BigDecimal amount, String description) {
        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Số tiền nạp phải lớn hơn 0");
        }

        // Lấy hoặc tạo ví
        Wallet wallet = walletRepository.findByUserId(userId)
                .orElseGet(() -> {
                    Wallet newWallet = Wallet.builder()
                            .userId(userId)
                            .balance(BigDecimal.ZERO)
                            .updatedAt(LocalDateTime.now())
                            .build();
                    return walletRepository.save(newWallet);
                });

        // Nạp tiền vào ví
        wallet.credit(amount);
        Wallet updatedWallet = walletRepository.save(wallet);

        // Tạo transaction log
        Transaction transaction = Transaction.builder()
                .userId(userId)
                .bookingId(null)
                .amount(amount)
                .type("TOP_UP")
                .status("SUCCESS")
                .description(description != null ? description : "Nạp tiền vào ví")
                .createdAt(LocalDateTime.now())
                .build();
        transactionRepository.save(transaction);

        return updatedWallet;
    }
}
