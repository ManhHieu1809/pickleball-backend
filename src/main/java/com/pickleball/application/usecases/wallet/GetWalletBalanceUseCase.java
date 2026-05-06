package com.pickleball.application.usecases.wallet;

import com.pickleball.domain.entities.Wallet;
import com.pickleball.domain.repositories.WalletRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Component
@RequiredArgsConstructor
public class GetWalletBalanceUseCase {

    private final WalletRepository walletRepository;

    public Wallet execute(Long userId) {
        return walletRepository.findByUserId(userId)
                .orElseGet(() -> {
                    // Tự động tạo ví mới nếu chưa có
                    Wallet newWallet = Wallet.builder()
                            .userId(userId)
                            .balance(BigDecimal.ZERO)
                            .updatedAt(LocalDateTime.now())
                            .build();
                    return walletRepository.save(newWallet);
                });
    }
}
