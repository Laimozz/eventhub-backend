-- NULL finance snapshots identify legacy payments that still require reconciliation.
ALTER TABLE payments
    ADD COLUMN gross_amount NUMERIC(19,2),
    ADD COLUMN platform_fee NUMERIC(19,2),
    ADD COLUMN organizer_amount NUMERIC(19,2),
    ADD COLUMN finance_recorded_at TIMESTAMP(6) WITHOUT TIME ZONE,
    ADD COLUMN finance_credited BOOLEAN NOT NULL DEFAULT FALSE,
    ADD COLUMN finance_settled BOOLEAN NOT NULL DEFAULT FALSE,
    ADD COLUMN finance_reversed BOOLEAN NOT NULL DEFAULT FALSE,
    ADD CONSTRAINT ck_payments_finance_snapshot CHECK (
        (finance_recorded_at IS NULL AND gross_amount IS NULL AND platform_fee IS NULL
            AND organizer_amount IS NULL AND NOT finance_credited AND NOT finance_settled AND NOT finance_reversed)
        OR (finance_recorded_at IS NOT NULL AND gross_amount IS NOT NULL AND platform_fee IS NOT NULL
            AND organizer_amount IS NOT NULL AND gross_amount >= 0 AND platform_fee >= 0
            AND organizer_amount >= 0 AND gross_amount = platform_fee + organizer_amount)),
    ADD CONSTRAINT ck_payments_finance_settled CHECK (NOT finance_settled OR finance_credited);

ALTER TABLE refunds
    ADD COLUMN active_request_id VARCHAR(32) UNIQUE,
    ADD COLUMN active_command VARCHAR(20),
    ADD COLUMN attempt_started_at TIMESTAMP(6) WITHOUT TIME ZONE,
    ADD COLUMN attempt_history JSONB NOT NULL DEFAULT '[]'::jsonb CHECK (jsonb_typeof(attempt_history) = 'array'),
    ADD COLUMN account_history JSONB NOT NULL DEFAULT '[]'::jsonb CHECK (jsonb_typeof(account_history) = 'array'),
    ADD COLUMN manual_transaction_code VARCHAR(255) UNIQUE,
    ADD COLUMN evidence_content_type VARCHAR(50),
    ADD COLUMN evidence_content BYTEA CHECK (octet_length(evidence_content) BETWEEN 1 AND 5242880),
    ADD COLUMN evidence_recorded_by INTEGER REFERENCES users(id),
    ADD COLUMN evidence_recorded_at TIMESTAMP(6) WITHOUT TIME ZONE,
    ADD CONSTRAINT ck_refunds_evidence CHECK (
        (evidence_content IS NULL AND evidence_content_type IS NULL AND manual_transaction_code IS NULL
            AND evidence_recorded_by IS NULL AND evidence_recorded_at IS NULL)
        OR (evidence_content IS NOT NULL AND evidence_content_type IS NOT NULL
            AND evidence_content_type IN ('application/pdf','image/png','image/jpeg')
            AND manual_transaction_code IS NOT NULL AND evidence_recorded_by IS NOT NULL AND evidence_recorded_at IS NOT NULL));

-- Preserve the V8 snapshots and audit records before retiring the old tables.
UPDATE payments p SET gross_amount=f.gross_amount, platform_fee=f.platform_fee,
    organizer_amount=f.organizer_amount, finance_recorded_at=f.created_at,
    finance_credited=f.credited, finance_settled=f.settled, finance_reversed=f.reversed
FROM payment_finances f WHERE f.payments_id=p.id;

UPDATE refunds r SET attempt_history=a.history
FROM (SELECT refunds_id, jsonb_agg(jsonb_build_object(
    'id',id,'requestId',request_id,'command',command,'outcome',outcome,
    'responseCode',response_code,'transactionStatus',transaction_status,
    'createdAt',created_at,'completedAt',completed_at) ORDER BY id) AS history
    FROM refund_attempts GROUP BY refunds_id) a WHERE a.refunds_id=r.id;

UPDATE refunds r SET active_request_id=a.request_id, active_command=a.command,
    attempt_started_at=a.created_at
FROM (SELECT DISTINCT ON (refunds_id) * FROM refund_attempts
    ORDER BY refunds_id,id DESC) a
WHERE a.refunds_id=r.id AND r.status='PROCESSING' AND a.completed_at IS NULL;

UPDATE refunds r SET manual_transaction_code=e.transaction_code,
    evidence_content_type=e.content_type, evidence_content=e.content,
    evidence_recorded_by=e.recorded_by, evidence_recorded_at=e.recorded_at
FROM refund_evidence e WHERE e.refunds_id=r.id;

ALTER TABLE refund_bank_accounts
    ADD COLUMN provided_by INTEGER REFERENCES users(id),
    ADD COLUMN provided_at TIMESTAMP(6) WITHOUT TIME ZONE;
-- Legacy accounts were supplied by the verifying administrator.
UPDATE refund_bank_accounts SET provided_by=verified_by, provided_at=created_at;
ALTER TABLE refund_bank_accounts
    ALTER COLUMN provided_by SET NOT NULL,
    ALTER COLUMN provided_at SET NOT NULL,
    ALTER COLUMN verified_by DROP NOT NULL,
    ALTER COLUMN verified_at DROP NOT NULL,
    ADD CHECK (length(trim(bank_code)) > 0) NOT VALID,
    ADD CHECK (account_number ~ '^[0-9A-Za-z]{4,50}$') NOT VALID,
    ADD CHECK (length(trim(account_holder_name)) > 0) NOT VALID,
    ADD CHECK ((verified_by IS NULL) = (verified_at IS NULL));

DROP TABLE refund_evidence;
DROP TABLE refund_attempts;
DROP TABLE payment_finances;
