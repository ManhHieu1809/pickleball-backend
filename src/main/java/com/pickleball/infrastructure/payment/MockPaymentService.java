package com.pickleball.infrastructure.payment;

import com.pickleball.domain.services.PaymentService;
import com.pickleball.domain.valueobjects.Money;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Mock Payment Service for Development/Testing
 *
 * Simulates payment gateway behavior:
 * - Always succeeds (can be configured to fail for testing)
 * - Stores transactions in memory
 * - No real money involved
 *
 * Replace with ZaloPayService/VNPayService in production
 */
@Service
@Slf4j
public class MockPaymentService implements PaymentService {

    // In-memory transaction storage (for testing)
    private final Map<String, MockTransaction> transactions = new ConcurrentHashMap<>();

    // Configuration flags for testing different scenarios
    private boolean shouldFail = false;
    private boolean shouldPending = false;

    @Override
    public PaymentResult createPayment(String orderId, Money amount, String description, Long userId) {
        log.info("[MOCK PAYMENT] Creating payment - Order: {}, Amount: {}, User: {}",
                orderId, amount.getAmount(), userId);

        // Simulate failure for testing
        if (shouldFail) {
            log.warn("[MOCK PAYMENT] Simulated payment failure");
            return PaymentResult.failed("Simulated payment failure");
        }

        // Generate mock transaction ID
        String transactionId = "MOCK_TXN_" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();

        // Store transaction
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

        // Return pending with mock payment URL (for QR code flow simulation)
        if (shouldPending) {
            String mockPaymentUrl = "https://mock-payment.local/pay/" + transactionId;
            return PaymentResult.pending(transactionId, mockPaymentUrl, amount);
        }

        // Return immediate success (for simple flow)
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

        // Update transaction status
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

    // ============ Testing Helper Methods ============

    /**
     * Set mock to simulate payment failures
     */
    public void setSimulateFailure(boolean fail) {
        this.shouldFail = fail;
        log.info("[MOCK PAYMENT] Failure simulation: {}", fail);
    }

    /**
     * Set mock to return pending status (simulate async payment like QR code)
     */
    public void setSimulatePending(boolean pending) {
        this.shouldPending = pending;
        log.info("[MOCK PAYMENT] Pending simulation: {}", pending);
    }

    /**
     * Manually complete a pending payment (for testing)
     */
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

    /**
     * Get transaction for testing/debugging
     */
    public MockTransaction getTransaction(String transactionId) {
        return transactions.get(transactionId);
    }

    /**
     * Clear all transactions (for testing)
     */
    public void clearTransactions() {
        transactions.clear();
        log.info("[MOCK PAYMENT] All transactions cleared");
    }

    // Inner record for mock transaction
    public record MockTransaction(
            String transactionId,
            String orderId,
            Long userId,
            Money amount,
            String description,
            String status
    ) {}
}
