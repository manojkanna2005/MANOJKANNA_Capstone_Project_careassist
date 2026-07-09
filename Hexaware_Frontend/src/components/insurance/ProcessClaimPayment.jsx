import { useEffect, useMemo, useState } from "react";
import Layout from "../common/Layout.jsx";
import Message from "../common/Message.jsx";
import { processClaimPayment } from "../../services/claimPaymentService.js";
import { getActionableClaimsByInsuranceCompanyId } from "../../services/claimService.js";
import { getCompanyByUserId } from "../../services/insuranceCompanyService.js";
import { getUserId } from "../../utils/auth.js";
import { money } from "../../utils/date.js";
import { generateTransactionReference } from "../../utils/payment.js";

const PAYMENT_MODES = new Set([
  "UPI",
  "NET_BANKING",
  "CHEQUE",
  "CARD",
  "CASH",
]);
const REFERENCE_PATTERN = /^[A-Za-z0-9][A-Za-z0-9/_-]{5,59}$/;

function ProcessClaimPayment() {
  const [claims, setClaims] = useState([]);
  const [form, setForm] = useState({
    claimId: "",
    paymentMode: "UPI",
    transactionReference: "",
  });
  const [loading, setLoading] = useState(true);
  const [processing, setProcessing] = useState(false);
  const [message, setMessage] = useState("");
  const [error, setError] = useState("");
  const [fieldError, setFieldError] = useState("");

  const load = async () => {
    setLoading(true);
    setError("");
    try {
      const company = await getCompanyByUserId(getUserId());
      const companyClaims = await getActionableClaimsByInsuranceCompanyId(
        company.companyId,
      );
      setClaims(
        (companyClaims || []).filter(
          (claim) =>
            claim.status === "APPROVED" &&
            !claim.insurancePaymentProcessed &&
            Number(claim.approvedAmount || 0) > 0,
        ),
      );
    } catch (loadError) {
      setError(
        loadError.userMessage ||
          "Complete your insurance company profile before processing payments.",
      );
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    load();
  }, []);

  const selectedClaim = useMemo(
    () =>
      claims.find((claim) => String(claim.claimId) === String(form.claimId)),
    [claims, form.claimId],
  );

  const handleClaimChange = (event) => {
    const claimId = event.target.value;
    setFieldError("");
    setForm((previous) => ({
      ...previous,
      claimId,
      transactionReference: claimId
        ? generateTransactionReference(claimId)
        : "",
    }));
  };

  const validate = () => {
    if (!selectedClaim) return "Select an approved claim.";
    if (selectedClaim.insurancePaymentProcessed) {
      return "This claim payment has already been processed.";
    }
    if (selectedClaim.status !== "APPROVED") {
      return "Only approved claims can be paid.";
    }
    if (!Number.isFinite(Number(selectedClaim.approvedAmount)) || Number(selectedClaim.approvedAmount) <= 0) {
      return "The claim does not have a valid approved amount.";
    }
    if (!PAYMENT_MODES.has(form.paymentMode)) {
      return "Select a valid payment mode.";
    }
    const reference = form.transactionReference.trim();
    if (reference && !REFERENCE_PATTERN.test(reference)) {
      return "Transaction reference must be 6-60 characters using only letters, numbers, slash, underscore, or hyphen.";
    }
    return "";
  };

  const handleSubmit = async (event) => {
    event.preventDefault();
    setMessage("");
    setError("");

    const validationMessage = validate();
    if (validationMessage) {
      setFieldError(validationMessage);
      return;
    }

    setFieldError("");
    setProcessing(true);
    try {
      const result = await processClaimPayment({
        claimId: Number(form.claimId),
        paymentMode: form.paymentMode,
        transactionReference: form.transactionReference.trim() || undefined,
      });
      setMessage(
        `Insurance payment of ${money(result.paymentAmount)} was recorded. This paid claim has been removed from active insurance work.`,
      );
      setClaims((items) =>
        items.filter((claim) => claim.claimId !== Number(form.claimId)),
      );
      setForm({ claimId: "", paymentMode: "UPI", transactionReference: "" });
    } catch (submitError) {
      setError(submitError.userMessage || "Unable to process insurance payment.");
    } finally {
      setProcessing(false);
    }
  };

  return (
    <Layout
      title="Process Claim Payment"
      subtitle="Pay the exact approved amount once. Completed claim payments are removed from the insurance dashboard and active claims list."
    >
      <div className="card page-card p-4">
        <Message type="success">{message}</Message>
        <Message type="danger">{error}</Message>
        <Message type="warning">{fieldError}</Message>

        {!loading && claims.length === 0 && (
          <div className="alert alert-info mb-0">
            No approved claims are waiting for insurance payment.
          </div>
        )}

        {claims.length > 0 && (
          <form onSubmit={handleSubmit}>
            <div className="row">
              <div className="col-md-7 mb-3">
                <label className="form-label" htmlFor="claimId">
                  Approved Claim
                </label>
                <select
                  id="claimId"
                  name="claimId"
                  className="form-select"
                  value={form.claimId}
                  onChange={handleClaimChange}
                  required
                  disabled={processing}
                >
                  <option value="">Select approved claim</option>
                  {claims.map((claim) => (
                    <option key={claim.claimId} value={claim.claimId}>
                      Claim #{claim.claimId} · {claim.patientName} · approved {money(claim.approvedAmount)}
                    </option>
                  ))}
                </select>
              </div>

              <div className="col-md-5 mb-3">
                <label className="form-label" htmlFor="paymentMode">
                  Payment Mode
                </label>
                <select
                  id="paymentMode"
                  name="paymentMode"
                  className="form-select"
                  value={form.paymentMode}
                  onChange={(event) => {
                    setFieldError("");
                    setForm((previous) => ({
                      ...previous,
                      paymentMode: event.target.value,
                    }));
                  }}
                  required
                  disabled={processing}
                >
                  <option value="UPI">UPI</option>
                  <option value="NET_BANKING">Net banking</option>
                  <option value="CHEQUE">Cheque</option>
                  <option value="CARD">Card</option>
                  <option value="CASH">Cash</option>
                </select>
              </div>
            </div>

            {selectedClaim && (
              <div className="alert alert-light border">
                <div className="row g-2">
                  <div className="col-md-4">
                    <strong>Invoice total:</strong> {money(selectedClaim.invoiceAmount)}
                  </div>
                  <div className="col-md-4">
                    <strong>Approved amount:</strong> {money(selectedClaim.approvedAmount)}
                  </div>
                  <div className="col-md-4">
                    <strong>Patient balance after insurance:</strong>{" "}
                    {money(
                      Math.max(
                        0,
                        Number(selectedClaim.invoiceAmount || 0) -
                          Number(selectedClaim.approvedAmount || 0),
                      ),
                    )}
                  </div>
                </div>
              </div>
            )}

            <div className="mb-3">
              <label className="form-label" htmlFor="transactionReference">
                Transaction Reference
              </label>
              <input
                id="transactionReference"
                name="transactionReference"
                className="form-control"
                value={form.transactionReference}
                onChange={(event) => {
                  setFieldError("");
                  setForm((previous) => ({
                    ...previous,
                    transactionReference: event.target.value,
                  }));
                }}
                minLength="6"
                maxLength="60"
                pattern="[A-Za-z0-9][A-Za-z0-9/_-]{5,59}"
                title="Use 6-60 letters, numbers, slash, underscore, or hyphen."
                placeholder="Leave blank to let the server generate one"
                disabled={processing}
              />
            </div>

            <div className="alert alert-warning">
              The payment amount is fixed by the approved claim amount and cannot
              be edited.
            </div>

            <button
              type="submit"
              className="btn btn-primary"
              disabled={processing || !selectedClaim}
            >
              {processing
                ? "Processing…"
                : `Process ${money(selectedClaim?.approvedAmount)} Insurance Payment`}
            </button>
          </form>
        )}
      </div>
    </Layout>
  );
}

export default ProcessClaimPayment;
