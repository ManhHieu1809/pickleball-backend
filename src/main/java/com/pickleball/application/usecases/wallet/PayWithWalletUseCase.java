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
public class PayWithWalletUseCase {

    private final WalletRepository walletRepository;
    private final TransactionRepository transactionRepository;

    @Transactional
    public String execute(Long userId, BigDecimal amount, Long bookingId, String description) {
        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Số tiền thanh toán phải lớn hơn 0");
        }

        // Lấy ví
        Wallet wallet = walletRepository.findByUserId(userId)
                .orElseThrow(() -> new IllegalArgumentException("Ví không tồn tại. Vui lòng nạp tiền trước"));

        // Kiểm tra số dư
        if (wallet.getBalance().compareTo(amount) < 0) {
            throw new IllegalArgumentException(
                    String.format("Số dư không đủ. Số dư hiện tại: %,.0f VND, cần: %,.0f VND",
                            wallet.getBalance(), amount)
            );
        }

        // Trừ tiền từ ví
        wallet.debit(amount);
        walletRepository.save(wallet);

        // Tạo transaction log
        String transactionId = "WALLET_" + bookingId + "_USER_" + userId + "_" + System.currentTimeMillis();
        Transaction transaction = Transaction.builder()
                .userId(userId)
                .bookingId(bookingId)
                .amount(amount)
                .type("BOOKING_PAYMENT")
                .status("SUCCESS")
                .description(description != null ? description : "Thanh toán booking #" + bookingId)
                .createdAt(LocalDateTime.now())
                .build();
        transactionRepository.save(transaction);

        return transactionId;
    }
}
