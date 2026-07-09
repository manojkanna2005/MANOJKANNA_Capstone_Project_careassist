import { useEffect, useMemo, useState } from "react";
import Layout from "../common/Layout.jsx";
import Message from "../common/Message.jsx";
import ClaimDocumentInput from "../common/ClaimDocumentInput.jsx";
import { submitClaimWithDocuments } from "../../services/claimService.js";
import { getPatientByUserId } from "../../services/patientService.js";
import { getMyInvoices } from "../../services/invoiceService.js";
import { getActiveInsurancesByPatientId } from "../../services/patientInsuranceService.js";
import { getUserId } from "../../utils/auth.js";
import { money, today } from "../../utils/date.js";

const empty = {
  patientId: "",
  invoiceId: "",
  enrollmentId: "",
  companyId: "",
  diagnosis: "",
  treatment: "",
  dateOfService: today(),
  claimAmount: "",
};

const normalize = (value) => String(value || "").trim().toUpperCase();
const hasAtMostTwoDecimals = (value) => /^\d+(?:\.\d{1,2})?$/.test(String(value));

const earliestDate = (...values) =>
  values.filter(Boolean).sort()[0] || undefined;

const isClaimableInvoice = (invoice) => {
  const invoiceStatus = normalize(invoice.status);
  const claimStatus = normalize(invoice.claimStatus);
  return (
    ["PENDING", "UNPAID", "OVERDUE"].includes(invoiceStatus) &&
    !invoice.paymentId &&
    !["PENDING", "SUBMITTED", "UNDER_REVIEW", "APPROVED"].includes(claimStatus) &&
    Number(invoice.remainingAmount ?? invoice.totalAmount ?? 0) > 0
  );
};

function SubmitClaim() {
  const [form, setForm] = useState(empty);
  const [invoices, setInvoices] = useState([]);
  const [policies, setPolicies] = useState([]);
  const [documents, setDocuments] = useState([]);
  const [documentKey, setDocumentKey] = useState(0);
  const [loading, setLoading] = useState(true);
  const [submitting, setSubmitting] = useState(false);
  const [message, setMessage] = useState("");
  const [error, setError] = useState("");

  useEffect(() => {
    async function load() {
      setLoading(true);
      setError("");
      try {
        const patient = await getPatientByUserId(getUserId());
        const [patientInvoices, activePolicies] = await Promise.all([
          getMyInvoices(),
          getActiveInsurancesByPatientId(patient.patientId),
        ]);

        setInvoices((patientInvoices || []).filter(isClaimableInvoice));
        setPolicies(
          (activePolicies || []).filter(
            (policy) => Number(policy.remainingCoverage || 0) > 0,
          ),
        );
        setForm((previous) => ({ ...previous, patientId: patient.patientId }));
      } catch (loadError) {
        setError(
          loadError.userMessage ||
            "Complete your patient profile and select an active insurance plan before submitting a claim.",
        );
      } finally {
        setLoading(false);
      }
    }

    load();
  }, []);

  const selectedInvoice = useMemo(
    () =>
      invoices.find(
        (invoice) => String(invoice.invoiceId) === String(form.invoiceId),
      ),
    [invoices, form.invoiceId],
  );

  const selectedPolicy = useMemo(
    () =>
      policies.find(
        (policy) => String(policy.enrollmentId) === String(form.enrollmentId),
      ),
    [policies, form.enrollmentId],
  );

  const maximumServiceDate = useMemo(
    () =>
      earliestDate(
        today(),
        selectedPolicy?.expiryDate,
        selectedInvoice?.invoiceDate,
      ),
    [selectedInvoice, selectedPolicy],
  );

  const handleChange = (event) => {
    setForm((previous) => ({
      ...previous,
      [event.target.name]: event.target.value,
    }));
  };

  const handleInvoice = (event) => {
    const invoiceId = event.target.value;
    const invoice = invoices.find(
      (item) => String(item.invoiceId) === String(invoiceId),
    );
    setForm((previous) => ({
      ...previous,
      invoiceId,
      claimAmount: invoice?.totalAmount ?? "",
    }));
  };

  const handlePolicy = (event) => {
    const enrollmentId = event.target.value;
    const policy = policies.find(
      (item) => String(item.enrollmentId) === String(enrollmentId),
    );
    setForm((previous) => ({
      ...previous,
      enrollmentId,
      companyId: policy?.companyId || "",
    }));
  };

  const handleSubmit = async (event) => {
    event.preventDefault();
    setMessage("");
    setError("");

    if (!selectedInvoice || !selectedPolicy) {
      setError("Select a valid unpaid invoice and active insurance policy.");
      return;
    }

    const diagnosis = form.diagnosis.trim();
    const treatment = form.treatment.trim();
    const claimAmountText = String(form.claimAmount).trim();
    const claimAmount = Number(claimAmountText);

    if (diagnosis.length < 3 || diagnosis.length > 100) {
      setError("Diagnosis must be between 3 and 100 characters.");
      return;
    }
    if (treatment.length < 3 || treatment.length > 100) {
      setError("Treatment must be between 3 and 100 characters.");
      return;
    }
    if (!claimAmountText || !Number.isFinite(claimAmount) || claimAmount <= 0) {
      setError("Claim amount must be greater than zero.");
      return;
    }
    if (!hasAtMostTwoDecimals(claimAmountText)) {
      setError("Claim amount can have at most 2 decimal places.");
      return;
    }
    if (claimAmount > Number(selectedInvoice.totalAmount || 0)) {
      setError("Claim amount cannot exceed the selected invoice total.");
      return;
    }
    if (claimAmount > 9999999999.99) {
      setError("Claim amount is too large.");
      return;
    }
    if (
      !form.dateOfService ||
      form.dateOfService < selectedPolicy.enrollmentDate ||
      form.dateOfService > maximumServiceDate
    ) {
      setError(
        "Date of service must be within the policy period, not in the future, and not after the invoice date.",
      );
      return;
    }
    if (documents.length === 0) {
      setError("At least one valid medical document is required.");
      return;
    }

    setSubmitting(true);
    try {
      await submitClaimWithDocuments(
        {
          patientId: Number(form.patientId),
          invoiceId: Number(form.invoiceId),
          enrollmentId: Number(form.enrollmentId),
          companyId: Number(form.companyId),
          diagnosis,
          treatment,
          dateOfService: form.dateOfService,
          claimAmount,
        },
        documents,
      );

      setMessage(
        "Claim submitted. The insurance company will choose an approved amount within your remaining policy coverage.",
      );
      setInvoices((items) =>
        items.filter((invoice) => invoice.invoiceId !== Number(form.invoiceId)),
      );
      setForm({ ...empty, patientId: form.patientId });
      setDocuments([]);
      setDocumentKey((value) => value + 1);
    } catch (submitError) {
      setError(submitError.userMessage || "Unable to submit claim.");
    } finally {
      setSubmitting(false);
    }
  };

  return (
    <Layout
      title="Submit Claim"
      subtitle="Insurance is optional. Submit a claim only for an unpaid invoice you want covered by one of your active policies."
    >
      <div className="card page-card p-4">
        <Message type="success">{message}</Message>
        <Message type="danger">{error}</Message>

        {!loading && policies.length === 0 && (
          <div className="alert alert-warning">
            No active policy with remaining coverage is available.
          </div>
        )}
        {!loading && invoices.length === 0 && (
          <div className="alert alert-info">
            No unpaid, unclaimed invoice is available. Paid invoices do not need an insurance claim.
          </div>
        )}

        <form onSubmit={handleSubmit}>
          <div className="row">
            <div className="col-md-4 mb-3">
              <label className="form-label">Patient ID</label>
              <input className="form-control" name="patientId" value={form.patientId} readOnly />
            </div>

            <div className="col-md-4 mb-3">
              <label className="form-label">Invoice</label>
              <select
                className="form-select"
                name="invoiceId"
                value={form.invoiceId}
                onChange={handleInvoice}
                required
              >
                <option value="">Select invoice</option>
                {invoices.map((invoice) => (
                  <option key={invoice.invoiceId} value={invoice.invoiceId}>
                    {invoice.invoiceNumber || `Invoice #${invoice.invoiceId}`} · {money(invoice.totalAmount)}
                  </option>
                ))}
              </select>
            </div>

            <div className="col-md-4 mb-3">
              <label className="form-label">Active Insurance Policy</label>
              <select
                className="form-select"
                name="enrollmentId"
                value={form.enrollmentId}
                onChange={handlePolicy}
                required
              >
                <option value="">Select active policy</option>
                {policies.map((policy) => (
                  <option key={policy.enrollmentId} value={policy.enrollmentId}>
                    {policy.planName} · {policy.companyName} · {money(policy.remainingCoverage)} remaining
                  </option>
                ))}
              </select>
            </div>
          </div>

          {selectedPolicy && (
            <div className="alert alert-light border">
              <div className="row g-2">
                <div className="col-md-4"><strong>Plan limit:</strong> {money(selectedPolicy.coverageAmount)}</div>
                <div className="col-md-4"><strong>Already approved:</strong> {money(selectedPolicy.approvedCoverageUsed)}</div>
                <div className="col-md-4"><strong>Remaining coverage:</strong> {money(selectedPolicy.remainingCoverage)}</div>
              </div>
            </div>
          )}

          <div className="row">
            <div className="col-md-6 mb-3">
              <label className="form-label">Diagnosis</label>
              <input
                className="form-control"
                name="diagnosis"
                value={form.diagnosis}
                onChange={handleChange}
                minLength="3"
                maxLength="100"
                required
              />
            </div>
            <div className="col-md-6 mb-3">
              <label className="form-label">Treatment</label>
              <input
                className="form-control"
                name="treatment"
                value={form.treatment}
                onChange={handleChange}
                minLength="3"
                maxLength="100"
                required
              />
            </div>
          </div>

          <div className="row">
            <div className="col-md-6 mb-3">
              <label className="form-label">Date of Service</label>
              <input
                className="form-control"
                type="date"
                name="dateOfService"
                value={form.dateOfService}
                onChange={handleChange}
                min={selectedPolicy?.enrollmentDate || undefined}
                max={maximumServiceDate}
                required
              />
            </div>
            <div className="col-md-6 mb-3">
              <label className="form-label">Requested Claim Amount</label>
              <input
                className="form-control"
                type="number"
                name="claimAmount"
                value={form.claimAmount}
                onChange={handleChange}
                min="0.01"
                max={selectedInvoice?.totalAmount || 9999999999.99}
                step="0.01"
                inputMode="decimal"
                required
              />
              {selectedInvoice && selectedPolicy && (
                <div className="form-text">
                  You may request up to the invoice total of {money(selectedInvoice.totalAmount)}.
                  The insurer can approve at most {money(
                    Math.min(
                      Number(form.claimAmount || selectedInvoice.totalAmount || 0),
                      Number(selectedPolicy.remainingCoverage || 0),
                    ),
                  )} based on the current plan limit.
                </div>
              )}
            </div>
          </div>

          <ClaimDocumentInput
            key={documentKey}
            files={documents}
            onChange={setDocuments}
          />

          <button
            className="btn btn-primary"
            disabled={
              loading ||
              submitting ||
              !form.patientId ||
              !form.invoiceId ||
              !form.enrollmentId ||
              documents.length === 0 ||
              policies.length === 0
            }
          >
            {submitting ? "Submitting…" : "Submit Claim"}
          </button>
        </form>
      </div>
    </Layout>
  );
}

export default SubmitClaim;
