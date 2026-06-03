package com.pickleball.application.services;

import com.pickleball.application.dtos.TransactionDTO;
import com.pickleball.domain.entities.Transaction;
import com.pickleball.domain.entities.Wallet;
import com.pickleball.domain.repositories.TransactionRepository;
import com.pickleball.domain.repositories.WalletRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class WithdrawalManagementService {
    private final TransactionRepository transactionRepository;
    private final WalletRepository walletRepository;

    @Transactional(readOnly = true)
    public List<TransactionDTO> getPendingWithdrawals() {
        return transactionRepository.findByTypeAndStatus("WITHDRAWAL", "PENDING").stream()
                .map(this::toDto)
                .toList();
    }

    @Transactional
    public TransactionDTO approveWithdrawal(Long transactionId, Long adminId) {
        Transaction transaction = getPendingWithdrawal(transactionId);
        transaction.setStatus("SUCCESS");
        transaction.setMetadata(buildAdminMetadata("APPROVED", adminId, null));
        transaction = transactionRepository.save(transaction);
        return toDto(transaction);
    }

    @Transactional
    public TransactionDTO rejectWithdrawal(Long transactionId, Long adminId, String reason) {
        Transaction transaction = getPendingWithdrawal(transactionId);

        Long userId = transaction.getUserId();
        Wallet wallet = walletRepository.findByUserId(userId)
                .orElseGet(() -> walletRepository.save(Wallet.builder()
                        .userId(userId)
                        .balance(BigDecimal.ZERO)
                        .updatedAt(LocalDateTime.now())
                        .build()));
        wallet.credit(transaction.getAmount());
        walletRepository.save(wallet);

        transaction.setStatus("FAILED");
        transaction.setMetadata(buildAdminMetadata("REJECTED", adminId, reason));
        transaction = transactionRepository.save(transaction);
        return toDto(transaction);
    }

    private Transaction getPendingWithdrawal(Long transactionId) {
        Transaction transaction = transactionRepository.findById(transactionId)
                .orElseThrow(() -> new IllegalArgumentException("Withdrawal transaction not found"));
        if (!"WITHDRAWAL".equals(transaction.getType())) {
            throw new IllegalArgumentException("Transaction is not a withdrawal");
        }
        if (!"PENDING".equals(transaction.getStatus())) {
            throw new IllegalArgumentException("Withdrawal is not pending");
        }
        return transaction;
    }

    private String buildAdminMetadata(String decision, Long adminId, String reason) {
        String safeReason = reason == null ? "" : reason.replace("\\", "\\\\").replace("\"", "\\\"");
        return "{\"decision\":\"" + decision + "\",\"adminId\":" + adminId
                + ",\"reason\":\"" + safeReason + "\"}";
    }

    private TransactionDTO toDto(Transaction transaction) {
        return TransactionDTO.builder()
                .id(transaction.getId())
                .userId(transaction.getUserId())
                .bookingId(transaction.getBookingId())
                .amount(transaction.getAmount())
                .type(transaction.getType())
                .status(transaction.getStatus())
                .paymentMethod(transaction.getPaymentMethod())
                .transactionCode(transaction.getTransactionCode())
                .description(transaction.getDescription())
                .metadata(transaction.getMetadata())
                .createdAt(transaction.getCreatedAt())
                .build();
    }
}
