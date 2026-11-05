package com.eventhub.backend.service;

import com.eventhub.backend.entity.Payment;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/** Provider input captured while the payment is still attached to its transaction. */
public record RefundPaymentSnapshot(Integer id, String transactionId, String gatewayTransactionId,
                                    BigDecimal amount, LocalDateTime createdAt) {
    public static RefundPaymentSnapshot from(Payment payment) {
        return new RefundPaymentSnapshot(payment.getId(), payment.getTransactionId(),
                payment.getGatewayTransactionId(), payment.getAmount(), payment.getCreatedAt());
    }
}
