-- Manual bank transfers are no longer part of the refund workflow.
-- Refuse to discard accounts or evidence that still need reconciliation.
DO $$
BEGIN
    IF EXISTS (SELECT 1 FROM refund_bank_accounts)
        OR EXISTS (SELECT 1 FROM refund_evidence)
        OR EXISTS (SELECT 1 FROM refunds WHERE status = 'MANUAL_PENDING') THEN
        RAISE EXCEPTION 'Manual refunds must be reconciled before removing their tables';
    END IF;
END;
$$;

DROP TABLE refund_evidence;
DROP TABLE refund_bank_accounts;
