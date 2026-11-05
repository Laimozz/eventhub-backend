-- V7 follows booking checkout; neither migration has been applied to the user's DB.
-- Prevent two concurrent Admin requests from recording the same bank transfer.
-- Existing duplicates must be reconciled explicitly; no existing records are changed.
CREATE UNIQUE INDEX uk_payout_transactions_transfer_reference
    ON payout_transactions (transaction_code)
    WHERE transaction_code IS NOT NULL;
