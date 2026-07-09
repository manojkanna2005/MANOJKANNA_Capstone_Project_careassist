-- Repair legacy claim-payment rows that point to missing claims.
-- Back up the careassist database before running this script.

-- 1. Inspect orphan rows first.
SELECT cp.payment_id,
       cp.claim_id,
       cp.payment_amount,
       cp.transaction_reference
FROM claim_payments cp
LEFT JOIN claims c ON c.claim_id = cp.claim_id
WHERE c.claim_id IS NULL;

-- 2. Delete only rows whose referenced claim no longer exists.
DELETE cp
FROM claim_payments cp
LEFT JOIN claims c ON c.claim_id = cp.claim_id
WHERE c.claim_id IS NULL;

-- 3. Verify that no orphan rows remain.
SELECT cp.payment_id, cp.claim_id
FROM claim_payments cp
LEFT JOIN claims c ON c.claim_id = cp.claim_id
WHERE c.claim_id IS NULL;

-- 4. Add the foreign key if the table does not already have one.
-- Check first:
SELECT CONSTRAINT_NAME
FROM information_schema.KEY_COLUMN_USAGE
WHERE TABLE_SCHEMA = DATABASE()
  AND TABLE_NAME = 'claim_payments'
  AND COLUMN_NAME = 'claim_id'
  AND REFERENCED_TABLE_NAME = 'claims';

-- Run this only when the query above returns no row.
-- ALTER TABLE claim_payments
--     ADD CONSTRAINT fk_claim_payment_claim
--     FOREIGN KEY (claim_id) REFERENCES claims(claim_id)
--     ON DELETE RESTRICT;
