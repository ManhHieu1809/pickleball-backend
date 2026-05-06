package com.pickleball.presentation.controllers;

import com.pickleball.application.dtos.TransactionDTO;
import com.pickleball.application.dtos.WalletDTO;
import com.pickleball.application.dtos.requests.TopUpRequest;
import com.pickleball.application.dtos.requests.WithdrawRequest;
import com.pickleball.application.usecases.wallet.GetWalletBalanceUseCase;
import com.pickleball.application.usecases.wallet.GetWalletTransactionsUseCase;
import com.pickleball.application.usecases.wallet.TopUpWalletUseCase;
import com.pickleball.application.usecases.wallet.WithdrawWalletUseCase;
import com.pickleball.domain.entities.Transaction;
import com.pickleball.domain.entities.Wallet;
import com.pickleball.infrastructure.security.JwtService;
import com.pickleball.presentation.helpers.ResponseHelper;
import com.pickleball.presentation.responses.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/wallet")
@RequiredArgsConstructor
public class WalletController {

    private final GetWalletBalanceUseCase getWalletBalanceUseCase;
    private final GetWalletTransactionsUseCase getWalletTransactionsUseCase;
    private final TopUpWalletUseCase topUpWalletUseCase;
    private final WithdrawWalletUseCase withdrawWalletUseCase;
    private final JwtService jwtService;

    @GetMapping
    public ResponseEntity<ApiResponse<WalletDTO>> getWalletBalance(
            @RequestHeader(value = "Authorization", required = false) String authHeader) {
        try {
            Long userId = extractUserIdFromToken(authHeader);
            Wallet wallet = getWalletBalanceUseCase.execute(userId);
            return ResponseHelper.ok(convertToDTO(wallet));
        } catch (IllegalArgumentException e) {
            return ResponseHelper.badRequest(e.getMessage());
        } catch (Exception e) {
            return ResponseHelper.internalError("Không thể lấy thông tin ví: " + e.getMessage());
        }
    }

    @GetMapping("/transactions")
    public ResponseEntity<ApiResponse<List<TransactionDTO>>> getTransactions(
            @RequestHeader(value = "Authorization", required = false) String authHeader) {
        try {
            Long userId = extractUserIdFromToken(authHeader);
            List<Transaction> transactions = getWalletTransactionsUseCase.execute(userId);
            List<TransactionDTO> dtos = transactions.stream()
                    .map(this::convertTransactionToDTO)
                    .collect(Collectors.toList());
            return ResponseHelper.ok(dtos);
        } catch (IllegalArgumentException e) {
            return ResponseHelper.badRequest(e.getMessage());
        } catch (Exception e) {
            return ResponseHelper.internalError("Không thể lấy lịch sử giao dịch: " + e.getMessage());
        }
    }

    @PostMapping("/topup")
    public ResponseEntity<ApiResponse<WalletDTO>> topUp(
            @Valid @RequestBody TopUpRequest request,
            @RequestHeader(value = "Authorization", required = false) String authHeader) {
        try {
            Long userId = extractUserIdFromToken(authHeader);
            Wallet wallet = topUpWalletUseCase.execute(userId, request.getAmount(), request.getDescription());
            return ResponseHelper.ok(convertToDTO(wallet));
        } catch (IllegalArgumentException e) {
            return ResponseHelper.badRequest(e.getMessage());
        } catch (Exception e) {
            return ResponseHelper.internalError("Không thể nạp tiền: " + e.getMessage());
        }
    }

    @PostMapping("/withdraw")
    public ResponseEntity<ApiResponse<WalletDTO>> withdraw(
            @Valid @RequestBody WithdrawRequest request,
            @RequestHeader(value = "Authorization", required = false) String authHeader) {
        try {
            Long userId = extractUserIdFromToken(authHeader);
            Wallet wallet = withdrawWalletUseCase.execute(userId, request.getAmount(), request.getDescription());
            return ResponseHelper.ok(convertToDTO(wallet));
        } catch (IllegalArgumentException e) {
            return ResponseHelper.badRequest(e.getMessage());
        } catch (Exception e) {
            return ResponseHelper.internalError("Không thể rút tiền: " + e.getMessage());
        }
    }

    private Long extractUserIdFromToken(String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            throw new IllegalArgumentException("Token không hợp lệ");
        }
        String token = authHeader.substring(7);
        return jwtService.extractUserId(token);
    }

    private WalletDTO convertToDTO(Wallet wallet) {
        return WalletDTO.builder()
                .userId(wallet.getUserId())
                .balance(wallet.getBalance())
                .updatedAt(wallet.getUpdatedAt())
                .build();
    }

    private TransactionDTO convertTransactionToDTO(Transaction transaction) {
        return TransactionDTO.builder()
                .id(transaction.getId())
                .userId(transaction.getUserId())
                .bookingId(transaction.getBookingId())
                .amount(transaction.getAmount())
                .type(transaction.getType())
                .status(transaction.getStatus())
                .description(transaction.getDescription())
                .createdAt(transaction.getCreatedAt())
                .build();
    }
}
