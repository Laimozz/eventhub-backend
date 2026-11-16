CREATE TABLE payment_confirmation_emails (
    booking_id INTEGER PRIMARY KEY REFERENCES bookings (id) ON DELETE CASCADE,
    recipient VARCHAR(255) NOT NULL,
    event_name VARCHAR(255) NOT NULL,
    amount NUMERIC(19, 2) NOT NULL,
    gateway_transaction_id VARCHAR(255) NOT NULL,
    paid_at TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    next_attempt_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    attempts INTEGER NOT NULL DEFAULT 0,
    sent_at TIMESTAMPTZ
);

CREATE INDEX ix_payment_confirmation_emails_due
    ON payment_confirmation_emails (next_attempt_at, booking_id)
    WHERE sent_at IS NULL AND attempts < 5;
