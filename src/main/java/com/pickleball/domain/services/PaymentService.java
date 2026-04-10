package com.pickleball.domain.services;

import com.pickleball.domain.valueobjects.Money;

public interface PaymentService {


    PaymentResult createPayment(String orderId, Money amount, String description, Long userId);

    PaymentResult verifyPayment(String transactionId);

    PaymentResult refund(String transactionId, Money amount, String reason);

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
