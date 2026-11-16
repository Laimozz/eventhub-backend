package com.eventhub.backend.repository;

import com.eventhub.backend.entity.Booking;
import com.eventhub.backend.entity.Payment;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class PaymentEmailRepository {
    private final JdbcTemplate jdbc;

    public void enqueue(Booking booking, Payment payment) {
        jdbc.update("""
                INSERT INTO payment_confirmation_emails
                    (booking_id, recipient, event_name, amount, gateway_transaction_id, paid_at)
                VALUES (?, ?, ?, ?, ?, ?)
                ON CONFLICT (booking_id) DO NOTHING
                """, booking.getId(), booking.getUser().getEmail(), booking.getEvent().getName(),
                payment.getAmount(), payment.getGatewayTransactionId(), payment.getPaidAt());
    }

    public Optional<PendingEmail> lockNextDue() {
        return jdbc.query("""
                SELECT booking_id, recipient, event_name, amount, gateway_transaction_id, paid_at, attempts
                FROM payment_confirmation_emails
                WHERE sent_at IS NULL AND attempts < 5 AND next_attempt_at <= CURRENT_TIMESTAMP
                ORDER BY next_attempt_at, booking_id
                LIMIT 1 FOR UPDATE SKIP LOCKED
                """, (rs, row) -> new PendingEmail(rs.getInt("booking_id"), rs.getString("recipient"),
                rs.getString("event_name"), rs.getBigDecimal("amount"), rs.getString("gateway_transaction_id"),
                rs.getObject("paid_at", LocalDateTime.class), rs.getInt("attempts"))).stream().findFirst();
    }

    public void markSent(int bookingId) {
        jdbc.update("""
                UPDATE payment_confirmation_emails SET sent_at = CURRENT_TIMESTAMP
                WHERE booking_id = ?
                """, bookingId);
    }

    public void recordFailure(int bookingId) {
        jdbc.update("""
                UPDATE payment_confirmation_emails
                SET attempts = attempts + 1, next_attempt_at = CURRENT_TIMESTAMP + INTERVAL '1 minute'
                WHERE booking_id = ?
                """, bookingId);
    }

    public record PendingEmail(int bookingId, String recipient, String eventName, BigDecimal amount,
                               String transactionId, LocalDateTime paidAt, int attempts) {}
}
