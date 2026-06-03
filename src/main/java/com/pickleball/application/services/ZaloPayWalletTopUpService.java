package com.pickleball.application.services;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.pickleball.application.dtos.ZaloPayPaymentDTO;
import com.pickleball.domain.entities.Transaction;
import com.pickleball.domain.entities.Wallet;
import com.pickleball.domain.repositories.TransactionRepository;
import com.pickleball.domain.repositories.WalletRepository;
import com.pickleball.domain.valueobjects.Money;
import com.pickleball.infrastructure.payment.zalopay.ZaloPayCreateOrderResult;
import com.pickleball.infrastructure.payment.zalopay.ZaloPayPaymentService;
import com.pickleball.infrastructure.payment.zalopay.ZaloPayQueryResult;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.LinkedHashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class ZaloPayWalletTopUpService {
    private static final ZoneId VIETNAM_ZONE = ZoneId.of("Asia/Ho_Chi_Minh");
    private static final DateTimeFormatter APP_TRANS_DATE = DateTimeFormatter.ofPattern("yyMMdd");

    private final ZaloPayPaymentService zaloPayPaymentService;
    private final TransactionRepository transactionRepository;
    private final WalletRepository walletRepository;
    private final ObjectMapper objectMapper;

    @Transactional
    public ZaloPayPaymentDTO createTopUp(Long userId, BigDecimal amount, String description) {
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Amount must be greater than zero");
        }

        String appTransId = generateAppTransId();
        String normalizedDescription = description != null && !description.isBlank()
                ? description
                : "ZaloPay top-up for wallet";

        Transaction transaction = Transaction.builder()
                .userId(userId)
                .bookingId(null)
                .amount(amount)
                .type("TOP_UP")
                .status("PENDING")
                .paymentMethod("ZALOPAY")
                .transactionCode(appTransId)
                .description(normalizedDescription)
                .metadata("{}")
                .createdAt(LocalDateTime.now())
                .build();
        transaction = transactionRepository.save(transaction);

        ZaloPayCreateOrderResult result = zaloPayPaymentService.createTopUpOrder(
                appTransId,
                userId,
                new Money(amount, "VND"),
                normalizedDescription);

        transaction.setMetadata(result.getRawResponse());
        if (!result.isAccepted()) {
            transaction.setStatus("FAILED");
            transactionRepository.save(transaction);
            throw new IllegalStateException("ZaloPay rejected order: " + result.getReturnMessage());
        }

        transaction = transactionRepository.save(transaction);

        return ZaloPayPaymentDTO.builder()
                .appTransId(appTransId)
                .status(transaction.getStatus())
                .amount(amount)
                .paymentUrl(result.getOrderUrl())
                .orderToken(result.getOrderToken())
                .zpTransToken(result.getZpTransToken())
                .qrCode(result.getQrCode())
                .message(result.getReturnMessage())
                .transactionId(transaction.getId())
                .userId(userId)
                .createdAt(transaction.getCreatedAt())
                .build();
    }

    @Transactional
    public void completeFromCallback(String data, String requestMac) {
        if (!zaloPayPaymentService.isValidCallback(data, requestMac)) {
            throw new SecurityException("Invalid ZaloPay callback mac");
        }

        Map<String, Object> callbackData = zaloPayPaymentService.parseCallbackData(data);
        String appTransId = String.valueOf(callbackData.get("app_trans_id"));
        Transaction transaction = transactionRepository.findByTransactionCode(appTransId)
                .orElseThrow(() -> new IllegalArgumentException("Transaction not found: " + appTransId));

        if ("SUCCESS".equals(transaction.getStatus())) {
            return;
        }

        if (!"PENDING".equals(transaction.getStatus())) {
            throw new IllegalStateException("Transaction cannot be completed from status: " + transaction.getStatus());
        }

        Object paidAmount = callbackData.get("amount");
        if (paidAmount instanceof Number number
                && transaction.getAmount().compareTo(BigDecimal.valueOf(number.longValue())) != 0) {
            throw new IllegalStateException("Callback amount does not match transaction amount");
        }

        creditWallet(transaction.getUserId(), transaction.getAmount());
        transaction.setStatus("SUCCESS");
        transaction.setMetadata(toJson(callbackData));
        Object zpTransId = callbackData.get("zp_trans_id");
        if (zpTransId != null) {
            transaction.setDescription(transaction.getDescription() + " | zp_trans_id=" + zpTransId);
        }
        transactionRepository.save(transaction);
    }

    @Transactional
    public ZaloPayPaymentDTO syncStatus(String appTransId) {
        Transaction transaction = transactionRepository.findByTransactionCode(appTransId)
                .orElseThrow(() -> new IllegalArgumentException("Transaction not found: " + appTransId));

        if ("SUCCESS".equals(transaction.getStatus())) {
            return toDto(transaction, "Payment already completed");
        }

        ZaloPayQueryResult queryResult = zaloPayPaymentService.queryOrder(appTransId);
        transaction.setMetadata(queryResult.getRawResponse());

        if (queryResult.isPaid() && "PENDING".equals(transaction.getStatus())) {
            creditWallet(transaction.getUserId(), transaction.getAmount());
            transaction.setStatus("SUCCESS");
        } else if (!queryResult.isProcessing() && !queryResult.isPaid()) {
            transaction.setStatus("FAILED");
        }

        transaction = transactionRepository.save(transaction);
        return toDto(transaction, queryResult.getReturnMessage());
    }

    @Transactional(readOnly = true)
    public ZaloPayPaymentDTO getLocalStatus(String appTransId) {
        Transaction transaction = transactionRepository.findByTransactionCode(appTransId)
                .orElseThrow(() -> new IllegalArgumentException("Transaction not found: " + appTransId));
        return toDto(transaction, "Local payment status");
    }

    private void creditWallet(Long userId, BigDecimal amount) {
        Wallet wallet = walletRepository.findByUserId(userId)
                .orElseGet(() -> walletRepository.save(Wallet.builder()
                        .userId(userId)
                        .balance(BigDecimal.ZERO)
                        .updatedAt(LocalDateTime.now())
                        .build()));

        wallet.credit(amount);
        walletRepository.save(wallet);
    }

    private ZaloPayPaymentDTO toDto(Transaction transaction, String message) {
        return ZaloPayPaymentDTO.builder()
                .appTransId(transaction.getTransactionCode())
                .status(transaction.getStatus())
                .amount(transaction.getAmount())
                .message(message)
                .transactionId(transaction.getId())
                .userId(transaction.getUserId())
                .createdAt(transaction.getCreatedAt())
                .build();
    }

    private String generateAppTransId() {
        String datePrefix = APP_TRANS_DATE.format(LocalDateTime.now(VIETNAM_ZONE));
        String suffix = String.valueOf(System.currentTimeMillis());
        return datePrefix + "_" + suffix;
    }

    private String toJson(Object value) {
        try {
            return objectMapper.writeValueAsString(value);
        } catch (Exception e) {
            Map<String, String> fallback = new LinkedHashMap<>();
            fallback.put("serialization_error", e.getMessage());
            try {
                return objectMapper.writeValueAsString(fallback);
            } catch (Exception ignored) {
                return "{}";
            }
        }
    }
}
