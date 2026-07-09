import { useEffect, useMemo, useState } from "react";
import { useNavigate, useParams } from "react-router-dom";
import Layout from "../common/Layout.jsx";
import Message from "../common/Message.jsx";
import {
  approveClaim,
  getClaimById,
  rejectClaim,
} from "../../services/claimService.js";
import { money } from "../../utils/date.js";

const hasAtMostTwoDecimals = (value) => /^\d+(?:\.\d{1,2})?$/.test(String(value));

function ApproveRejectClaim() {
  const { claimId } = useParams();
  const navigate = useNavigate();
  const [claim, setClaim] = useState(null);
  const [approvedAmount, setApprovedAmount] = useState("");
  const [reason, setReason] = useState("");
  const [approvalError, setApprovalError] = useState("");
  const [rejectionError, setRejectionError] = useState("");
  const [processing, setProcessing] = useState(false);
  const [message, setMessage] = useState("");
  const [error, setError] = useState("");

  useEffect(() => {
    getClaimById(claimId)
      .then((data) => {
        setClaim(data);
        setApprovedAmount(data.maxApprovableAmount ?? data.claimAmount ?? "");
      })
      .catch((loadError) =>
        setError(loadError.userMessage || "Unable to load claim."),
      );
  }, [claimId]);

  const maximumApproval = useMemo(
    () => Number(claim?.maxApprovableAmount ?? 0),
    [claim],
  );

  const isActionable =
    claim?.status === "PENDING" && !claim?.insurancePaymentProcessed;

  const approve = async () => {
    setError("");
    setApprovalError("");
    setRejectionError("");

    if (!isActionable) {
      setApprovalError("Only an unpaid pending claim can be approved.");
      return;
    }

    const amountText = String(approvedAmount).trim();
    const amount = Number(amountText);
    if (!amountText || !Number.isFinite(amount) || amount <= 0) {
      setApprovalError("Approved amount must be greater than zero.");
      return;
    }
    if (!hasAtMostTwoDecimals(amountText)) {
      setApprovalError("Approved amount can have at most 2 decimal places.");
      return;
    }
    if (amount > 9999999999.99) {
      setApprovalError("Approved amount is too large.");
      return;
    }
    if (amount > maximumApproval) {
      setApprovalError(
        `Approved amount cannot exceed ${money(maximumApproval)}, which is the lowest of the claim request, invoice total, and remaining policy coverage.`,
      );
      return;
    }

    setProcessing(true);
    try {
      await approveClaim(claimId, amount);
      setMessage(
        `Claim approved for ${money(amount)}. The amount is now reserved from the policy coverage.`,
      );
      setTimeout(() => navigate("/insurance/incoming-claims"), 800);
    } catch (approveError) {
      setError(approveError.userMessage || "Unable to approve claim.");
    } finally {
      setProcessing(false);
    }
  };

  const reject = async () => {
    setError("");
    setApprovalError("");
    setRejectionError("");

    if (!isActionable) {
      setRejectionError("Only an unpaid pending claim can be rejected.");
      return;
    }

    const normalizedReason = reason.trim();
    if (normalizedReason.length < 5 || normalizedReason.length > 255) {
      setRejectionError(
        "Rejection reason must be between 5 and 255 characters.",
      );
      return;
    }

    setProcessing(true);
    try {
      await rejectClaim(claimId, normalizedReason);
      setMessage("Claim rejected and the patient was notified.");
      setTimeout(() => navigate("/insurance/incoming-claims"), 800);
    } catch (rejectError) {
      setError(rejectError.userMessage || "Unable to reject claim.");
    } finally {
      setProcessing(false);
    }
  };

  return (
    <Layout
      title="Approve / Reject Claim"
      subtitle="Choose an approved amount within the request, invoice total, and remaining policy coverage."
    >
      <div className="card page-card p-4">
        <Message type="success">{message}</Message>
        <Message type="danger">{error}</Message>

        {claim?.insurancePaymentProcessed && (
          <div className="alert alert-info">
            This claim has already been paid and is no longer available for a
            decision.
          </div>
        )}

        {claim && (
          <>
            <div className="row g-3 mb-4">
              <div className="col-md-6"><strong>Claim:</strong> #{claim.claimId}</div>
              <div className="col-md-6"><strong>Patient:</strong> {claim.patientName}</div>
              <div className="col-md-6"><strong>Invoice total:</strong> {money(claim.invoiceAmount)}</div>
              <div className="col-md-6"><strong>Requested amount:</strong> {money(claim.claimAmount)}</div>
              <div className="col-md-6"><strong>Plan coverage limit:</strong> {money(claim.coverageAmount)}</div>
              <div className="col-md-6"><strong>Coverage already reserved:</strong> {money(claim.approvedCoverageUsed)}</div>
              <div className="col-md-6"><strong>Remaining coverage:</strong> {money(claim.remainingCoverage)}</div>
              <div className="col-md-6"><strong>Maximum approvable now:</strong> {money(claim.maxApprovableAmount)}</div>
            </div>

            <div className="mb-3">
              <label className="form-label" htmlFor="approvedAmount">
                Approved Amount
              </label>
              <input
                id="approvedAmount"
                name="approvedAmount"
                className={`form-control ${approvalError ? "is-invalid" : ""}`}
                type="number"
                value={approvedAmount}
                onChange={(event) => {
                  setApprovedAmount(event.target.value);
                  setApprovalError("");
                }}
                min="0.01"
                max={maximumApproval || undefined}
                step="0.01"
                disabled={!isActionable || processing}
                required
              />
              <div className="form-text">
                Approval reserves this amount from the patient's selected plan
                limit.
              </div>
              {approvalError && (
                <div className="invalid-feedback d-block">{approvalError}</div>
              )}
            </div>

            <button
              type="button"
              className="btn btn-success"
              onClick={approve}
              disabled={!isActionable || processing || maximumApproval <= 0}
            >
              {processing ? "Processing…" : "Approve Claim"}
            </button>
          </>
        )}

        <hr />
        <label className="form-label" htmlFor="rejectionReason">
          Rejection Reason
        </label>
        <textarea
          id="rejectionReason"
          name="rejectionReason"
          className={`form-control ${rejectionError ? "is-invalid" : ""}`}
          value={reason}
          onChange={(event) => {
            setReason(event.target.value);
            setRejectionError("");
          }}
          minLength="5"
          maxLength="255"
          rows="4"
          disabled={!isActionable || processing}
        />
        <div className="form-text">{reason.trim().length}/255 characters</div>
        {rejectionError && (
          <div className="invalid-feedback d-block">{rejectionError}</div>
        )}
        <button
          type="button"
          className="btn btn-danger mt-3"
          onClick={reject}
          disabled={!isActionable || processing}
        >
          Reject Claim
        </button>
      </div>
    </Layout>
  );
}

export default ApproveRejectClaim;
