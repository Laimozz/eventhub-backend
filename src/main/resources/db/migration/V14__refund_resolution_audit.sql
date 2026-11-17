-- Keep Admin reconciliation decisions in the existing attempt history.
ALTER TABLE refund_attempts ADD COLUMN recorded_by INTEGER REFERENCES users(id);
ALTER TABLE refund_attempts ADD COLUMN provider_case_reference VARCHAR(255);
ALTER TABLE refund_attempts ADD COLUMN note VARCHAR(1000);
