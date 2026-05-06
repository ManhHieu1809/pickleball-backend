package com.pickleball.application.usecases.wallet;

import com.pickleball.domain.entities.Transaction;
import com.pickleball.domain.repositories.TransactionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class GetWalletTransactionsUseCase {

    private final TransactionRepository transactionRepository;

    public List<Transaction> execute(Long userId) {
        return transactionRepository.findByUserId(userId);
    }
}
