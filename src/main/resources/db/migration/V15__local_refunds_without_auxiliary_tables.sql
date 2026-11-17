-- Refunds now run locally and synchronously. Preserve refund and finance records.
UPDATE refunds SET status = 'WAITING', last_error = NULL,
    response_code = NULL, transaction_status = NULL,
    updated_at = (CURRENT_TIMESTAMP AT TIME ZONE 'UTC')
WHERE status IN ('QUEUED', 'PROCESSING', 'FAILED', 'RECONCILIATION');

DROP TABLE IF EXISTS refund_attempts;
DROP TABLE IF EXISTS payment_confirmation_emails;
