export function generateTransactionReference(claimId) {
  const timestamp = new Date()
    .toISOString()
    .replace(/[-:T.]/g, "")
    .slice(0, 14);
  const safeClaimId = String(claimId ?? "0000").padStart(4, "0");
  return `TRX-${timestamp}-${safeClaimId}`;
}
