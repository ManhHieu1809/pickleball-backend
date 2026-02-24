package com.pickleball.domain.services;

import com.pickleball.domain.valueobjects.Money;

/**
 * Payment Service Interface - Domain Layer
 * Implementations: MockPaymentService (dev), ZaloPayService (prod), VNPayService (prod)
 */
public interface PaymentService {

    /**
     * Create a payment request
     * @param orderId Unique order ID (booking ID)
     * @param amount Amount to charge
     * @param description Payment description
     * @param userId User who is paying
     * @return PaymentResult with transaction details
     */
    PaymentResult createPayment(String orderId, Money amount, String description, Long userId);

    /**
     * Verify payment status
     * @param transactionId Transaction ID from payment gateway
     * @return PaymentResult with current status
     */
    PaymentResult verifyPayment(String transactionId);

    /**
     * Process refund
     * @param transactionId Original transaction ID
     * @param amount Amount to refund
     * @param reason Refund reason
     * @return PaymentResult with refund details
     */
    PaymentResult refund(String transactionId, Money amount, String reason);

    /**
     * Payment Result - contains payment transaction details
     */
    record PaymentResult(
            boolean success,
            String transactionId,
            String status,          // PENDING, SUCCESS, FAILED, REFUNDED
            String message,
            String paymentUrl,      // URL for user to complete payment (if applicable)
            Money amount
    ) {
        public static PaymentResult success(String transactionId, Money amount) {
            return new PaymentResult(true, transactionId, "SUCCESS", "Payment successful", null, amount);
        }

        public static PaymentResult pending(String transactionId, String paymentUrl, Money amount) {
            return new PaymentResult(true, transactionId, "PENDING", "Waiting for payment", paymentUrl, amount);
        }

        public static PaymentResult failed(String message) {
            return new PaymentResult(false, null, "FAILED", message, null, null);
        }

        public static PaymentResult refunded(String transactionId, Money amount) {
            return new PaymentResult(true, transactionId, "REFUNDED", "Refund successful", null, amount);
        }
    }
}
