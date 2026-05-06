package com.pickleball.infrastructure.payment;

import com.pickleball.domain.services.PaymentService;
import com.pickleball.domain.valueobjects.Money;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Service
@Slf4j
public class MockPaymentService implements PaymentService {

    private final Map<String, MockTransaction> transactions = new ConcurrentHashMap<>();

    private boolean shouldFail = false;
    private boolean shouldPending = false;

    @Override
    public PaymentResult createPayment(String orderId, Money amount, String description, Long userId) {
        log.info("[MOCK PAYMENT] Creating payment - Order: {}, Amount: {}, User: {}",
                orderId, amount.getAmount(), userId);

        if (shouldFail) {
            log.warn("[MOCK PAYMENT] Simulated payment failure");
            return PaymentResult.failed("Simulated payment failure");
        }

        String transactionId = "MOCK_TXN_" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();

        MockTransaction transaction = new MockTransaction(
                transactionId,
                orderId,
                userId,
                amount,
                description,
                shouldPending ? "PENDING" : "SUCCESS"
        );
        transactions.put(transactionId, transaction);

        log.info("[MOCK PAYMENT] Payment created - Transaction: {}, Status: {}",
                transactionId, transaction.status());

        if (shouldPending) {
            String mockPaymentUrl = "https://mock-payment.local/pay/" + transactionId;
            return PaymentResult.pending(transactionId, mockPaymentUrl, amount);
        }

        return PaymentResult.success(transactionId, amount);
    }

    @Override
    public PaymentResult verifyPayment(String transactionId) {
        log.info("[MOCK PAYMENT] Verifying payment - Transaction: {}", transactionId);

        MockTransaction transaction = transactions.get(transactionId);
        if (transaction == null) {
            return PaymentResult.failed("Transaction not found: " + transactionId);
        }

        return new PaymentResult(
                true,
                transactionId,
                transaction.status(),
                "Transaction status: " + transaction.status(),
                null,
                transaction.amount()
        );
    }

    @Override
    public PaymentResult refund(String transactionId, Money amount, String reason) {
        log.info("[MOCK PAYMENT] Processing refund - Transaction: {}, Amount: {}, Reason: {}",
                transactionId, amount.getAmount(), reason);

        MockTransaction transaction = transactions.get(transactionId);
        if (transaction == null) {
            return PaymentResult.failed("Original transaction not found: " + transactionId);
        }

        if (!"SUCCESS".equals(transaction.status())) {
            return PaymentResult.failed("Cannot refund non-successful transaction");
        }

        MockTransaction refundedTransaction = new MockTransaction(
                transaction.transactionId(),
                transaction.orderId(),
                transaction.userId(),
                transaction.amount(),
                transaction.description(),
                "REFUNDED"
        );
        transactions.put(transactionId, refundedTransaction);

        String refundTransactionId = "MOCK_REFUND_" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        log.info("[MOCK PAYMENT] Refund processed - Refund Transaction: {}", refundTransactionId);

        return PaymentResult.refunded(refundTransactionId, amount);
    }

    public void setSimulateFailure(boolean fail) {
        this.shouldFail = fail;
        log.info("[MOCK PAYMENT] Failure simulation: {}", fail);
    }

    public void setSimulatePending(boolean pending) {
        this.shouldPending = pending;
        log.info("[MOCK PAYMENT] Pending simulation: {}", pending);
    }

    public void completePayment(String transactionId) {
        MockTransaction transaction = transactions.get(transactionId);
        if (transaction != null && "PENDING".equals(transaction.status())) {
            transactions.put(transactionId, new MockTransaction(
                    transaction.transactionId(),
                    transaction.orderId(),
                    transaction.userId(),
                    transaction.amount(),
                    transaction.description(),
                    "SUCCESS"
            ));
            log.info("[MOCK PAYMENT] Payment completed manually - Transaction: {}", transactionId);
        }
    }

    public MockTransaction getTransaction(String transactionId) {
        return transactions.get(transactionId);
    }

    public void clearTransactions() {
        transactions.clear();
        log.info("[MOCK PAYMENT] All transactions cleared");
    }

    public record MockTransaction(
            String transactionId,
            String orderId,
            Long userId,
            Money amount,
            String description,
            String status
    ) {}
}
