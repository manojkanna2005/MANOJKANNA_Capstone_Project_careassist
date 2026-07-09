-- CareAssist optional-insurance/direct-payment migration
-- Run this only if the earlier insurance flow migration was already applied.
-- Back up the database before changing constraints.

-- Direct patient payments can exist without an insurance claim.
ALTER TABLE invoice_payments
    MODIFY claim_id INT NULL;
