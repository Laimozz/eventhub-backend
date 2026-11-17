-- Older automatic jobs that have never contacted VNPAY must wait for Admin approval.
-- Preserve all attempted/in-flight refunds for reconciliation; never reissue them.
UPDATE refunds r
SET status = 'WAITING', method = NULL, last_error = NULL,
    updated_at = (CURRENT_TIMESTAMP AT TIME ZONE 'UTC')
WHERE r.status = 'QUEUED' AND r.requested_by IS NULL
  AND NOT EXISTS (SELECT 1 FROM refund_attempts a WHERE a.refunds_id = r.id);
