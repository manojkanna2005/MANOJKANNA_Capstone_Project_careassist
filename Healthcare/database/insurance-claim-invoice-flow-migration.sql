-- CareAssist insurance -> invoice -> patient payment migration
-- Run once on an existing MySQL database before starting the updated application.
-- Back up the database first. Hibernate ddl-auto=update can add the column,
-- but this script also documents and applies the required financial constraints.

-- 1) Add the insurer-approved amount to each claim.
ALTER TABLE claims
    ADD COLUMN approved_amount DECIMAL(12,2) NULL AFTER claim_amount;

-- Preserve the previous meaning of already-approved claims.
UPDATE claims
SET approved_amount = claim_amount
WHERE status = 'APPROVED' AND approved_amount IS NULL;

-- 2) Check for duplicate claim payments before adding the one-payment-per-claim rule.
SELECT claim_id, COUNT(*) AS payment_count
FROM claim_payments
GROUP BY claim_id
HAVING COUNT(*) > 1;

-- Resolve any rows returned above before running the next ALTER statement.
ALTER TABLE claim_payments
    MODIFY claim_id INT NOT NULL,
    MODIFY payment_amount DECIMAL(12,2) NOT NULL,
    MODIFY payment_mode VARCHAR(30) NOT NULL,
    MODIFY transaction_reference VARCHAR(60) NOT NULL,
    ADD CONSTRAINT uk_claim_payment_claim UNIQUE (claim_id),
    ADD CONSTRAINT uk_claim_payment_reference UNIQUE (transaction_reference);

-- 3) Claims must be connected to the exact patient insurance enrollment.
-- First inspect legacy rows that do not have an enrollment.
SELECT claim_id, patient_id, invoice_id, company_id
FROM claims
WHERE enrollment_id IS NULL;

-- Fill legacy enrollment_id values manually if the query above returns rows,
-- then enforce the relationship.
ALTER TABLE claims
    MODIFY enrollment_id INT NOT NULL;

-- Useful verification: approved totals must remain within each enrollment's plan coverage.
SELECT c.enrollment_id,
       SUM(CASE WHEN c.status = 'APPROVED' THEN c.approved_amount ELSE 0 END) AS approved_used,
       ip.coverage_amount
FROM claims c
JOIN patient_insurance pi ON pi.enrollment_id = c.enrollment_id
JOIN insurance_plans ip ON ip.plan_id = pi.plan_id
GROUP BY c.enrollment_id, ip.coverage_amount
HAVING approved_used > ip.coverage_amount;


-- 4) Insurance is optional for patient invoice payment.
-- Direct full invoice payments do not have an associated claim, so claim_id must be nullable.
ALTER TABLE invoice_payments
    MODIFY claim_id INT NULL;
