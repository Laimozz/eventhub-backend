-- Additive only: retain existing bookings, payments, tickets and their statuses.
ALTER TABLE payments ADD COLUMN gateway_transaction_id VARCHAR(255);
ALTER TABLE payments ADD COLUMN response_code VARCHAR(2);

CREATE UNIQUE INDEX uk_payments_gateway_transaction
    ON payments (payment_method, gateway_transaction_id)
    WHERE gateway_transaction_id IS NOT NULL;

CREATE INDEX idx_bookings_customer_history ON bookings (users_id, created_at DESC, id DESC);
CREATE INDEX idx_bookings_pending_expiry ON bookings (events_id, expired_at) WHERE status = 'PENDING';
