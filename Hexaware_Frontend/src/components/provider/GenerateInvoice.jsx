import { useEffect, useMemo, useState } from "react";
import Layout from "../common/Layout.jsx";
import Message from "../common/Message.jsx";
import { getProviderByUserId } from "../../services/healthcareProviderService.js";
import { getAllPatients } from "../../services/patientService.js";
import { generateInvoice } from "../../services/invoiceService.js";
import { getUserId } from "../../utils/auth.js";
import { nowDateTime, today } from "../../utils/date.js";

const MAX_MONEY = 9999999999.99;
const FEE_FIELDS = [
  ["consultationFee", "Consultation Fee"],
  ["diagnosticTestsFee", "Diagnostic Tests Fee"],
  ["diagnosticScanFee", "Diagnostic Scan Fee"],
  ["medicationsFee", "Medications Fee"],
];

const base = {
  invoiceNumber: "",
  providerId: "",
  patientId: "",
  invoiceDate: today(),
  dueDate: today(),
  consultationFee: "0.00",
  diagnosticTestsFee: "0.00",
  diagnosticScanFee: "0.00",
  medicationsFee: "0.00",
  taxPercentage: "8.00",
  status: "PENDING",
  createdAt: nowDateTime(),
};

const hasAtMostTwoDecimals = (value) => /^\d+(?:\.\d{1,2})?$/.test(String(value));

function GenerateInvoice() {
  const [form, setForm] = useState(base);
  const [patients, setPatients] = useState([]);
  const [processing, setProcessing] = useState(false);
  const [message, setMessage] = useState("");
  const [error, setError] = useState("");

  useEffect(() => {
    async function load() {
      setError("");
      try {
        const [provider, patientList] = await Promise.all([
          getProviderByUserId(getUserId()),
          getAllPatients().catch(() => []),
        ]);

        setPatients(patientList);
        setForm((current) => ({
          ...current,
          providerId: provider.providerId,
          invoiceNumber: `INV-${Date.now()}`,
        }));
      } catch (loadError) {
        setError(
          loadError.userMessage || "Complete your provider profile first.",
        );
      }
    }

    load();
  }, []);

  const billAmount = useMemo(
    () =>
      FEE_FIELDS.reduce(
        (sum, [field]) => sum + Number(form[field] || 0),
        0,
      ),
    [form],
  );
  const taxAmount = useMemo(
    () => (billAmount * Number(form.taxPercentage || 0)) / 100,
    [billAmount, form.taxPercentage],
  );
  const totalAmount = useMemo(
    () => billAmount + taxAmount,
    [billAmount, taxAmount],
  );

  const handleChange = (event) => {
    setMessage("");
    setError("");
    setForm((current) => ({
      ...current,
      [event.target.name]: event.target.value,
    }));
  };

  const validate = () => {
    if (!form.providerId || !form.patientId) {
      return "Select a patient and ensure the provider profile is complete.";
    }
    const invoiceNumber = form.invoiceNumber.trim();
    if (!/^[A-Za-z0-9][A-Za-z0-9/_-]{2,29}$/.test(invoiceNumber)) {
      return "Invoice number must be 3-30 characters using letters, numbers, slash, underscore, or hyphen.";
    }
    if (!form.invoiceDate || form.invoiceDate > today()) {
      return "Invoice date is required and cannot be in the future.";
    }
    if (!form.dueDate || form.dueDate < form.invoiceDate) {
      return "Due date cannot be before the invoice date.";
    }

    for (const [field, label] of FEE_FIELDS) {
      const text = String(form[field]).trim();
      const value = Number(text);
      if (!text || !Number.isFinite(value) || value < 0) {
        return `${label} cannot be negative.`;
      }
      if (!hasAtMostTwoDecimals(text)) {
        return `${label} can have at most 2 decimal places.`;
      }
      if (value > MAX_MONEY) return `${label} is too large.`;
    }

    const taxText = String(form.taxPercentage).trim();
    const tax = Number(taxText);
    if (!taxText || !Number.isFinite(tax) || tax < 0 || tax > 100) {
      return "Tax percentage must be between 0 and 100.";
    }
    if (!hasAtMostTwoDecimals(taxText)) {
      return "Tax percentage can have at most 2 decimal places.";
    }
    if (totalAmount <= 0) return "Invoice total must be greater than zero.";
    if (totalAmount > MAX_MONEY) return "Invoice total is too large.";
    return "";
  };

  const handleSubmit = async (event) => {
    event.preventDefault();
    setError("");
    setMessage("");

    const validationMessage = validate();
    if (validationMessage) {
      setError(validationMessage);
      return;
    }

    setProcessing(true);
    try {
      const payload = {
        ...form,
        invoiceNumber: form.invoiceNumber.trim(),
        providerId: Number(form.providerId),
        patientId: Number(form.patientId),
        consultationFee: Number(form.consultationFee),
        diagnosticTestsFee: Number(form.diagnosticTestsFee),
        diagnosticScanFee: Number(form.diagnosticScanFee),
        medicationsFee: Number(form.medicationsFee),
        taxPercentage: Number(form.taxPercentage),
        taxAmount,
        totalAmount,
      };
      await generateInvoice(payload);
      setMessage("Invoice generated successfully.");
      setForm({
        ...base,
        providerId: form.providerId,
        invoiceNumber: `INV-${Date.now()}`,
      });
    } catch (submitError) {
      setError(submitError.userMessage || "Unable to generate invoice.");
    } finally {
      setProcessing(false);
    }
  };

  return (
    <Layout
      title="Generate Invoice"
      subtitle="Select a patient and enter valid service charges. Invoice totals must be greater than zero."
    >
      <div className="card page-card p-4">
        <Message type="success">{message}</Message>
        <Message type="danger">{error}</Message>

        <form onSubmit={handleSubmit}>
          <div className="row">
            <div className="col-md-4 mb-3">
              <label className="form-label" htmlFor="invoiceNumber">
                Invoice Number
              </label>
              <input
                id="invoiceNumber"
                className="form-control"
                name="invoiceNumber"
                value={form.invoiceNumber}
                onChange={handleChange}
                minLength="3"
                maxLength="30"
                pattern="[A-Za-z0-9][A-Za-z0-9/_-]{2,29}"
                title="Use letters, numbers, slash, underscore, or hyphen."
                required
                disabled={processing}
              />
            </div>

            <div className="col-md-4 mb-3">
              <label className="form-label" htmlFor="providerId">
                Provider ID
              </label>
              <input
                id="providerId"
                className="form-control"
                name="providerId"
                value={form.providerId}
                readOnly
              />
            </div>

            <div className="col-md-4 mb-3">
              <label className="form-label" htmlFor="patientId">
                Patient
              </label>
              <select
                id="patientId"
                className="form-select"
                name="patientId"
                value={form.patientId}
                onChange={handleChange}
                required
                disabled={processing}
              >
                <option value="">Select patient</option>
                {patients.map((patient) => (
                  <option key={patient.patientId} value={patient.patientId}>
                    {patient.fullName} (Patient ID: {patient.patientId})
                  </option>
                ))}
              </select>
            </div>
          </div>

          <div className="row">
            <div className="col-md-6 mb-3">
              <label className="form-label" htmlFor="invoiceDate">
                Invoice Date
              </label>
              <input
                id="invoiceDate"
                className="form-control"
                type="date"
                name="invoiceDate"
                value={form.invoiceDate}
                onChange={handleChange}
                max={today()}
                required
                disabled={processing}
              />
            </div>
            <div className="col-md-6 mb-3">
              <label className="form-label" htmlFor="dueDate">
                Due Date
              </label>
              <input
                id="dueDate"
                className="form-control"
                type="date"
                name="dueDate"
                value={form.dueDate}
                onChange={handleChange}
                min={form.invoiceDate || undefined}
                required
                disabled={processing}
              />
            </div>
          </div>

          <div className="row">
            {FEE_FIELDS.map(([name, label]) => (
              <div className="col-md-3 mb-3" key={name}>
                <label className="form-label" htmlFor={name}>{label}</label>
                <input
                  id={name}
                  className="form-control"
                  type="number"
                  name={name}
                  value={form[name]}
                  onChange={handleChange}
                  min="0"
                  max={MAX_MONEY}
                  step="0.01"
                  inputMode="decimal"
                  required
                  disabled={processing}
                />
              </div>
            ))}
          </div>

          <div className="row">
            <div className="col-md-4 mb-3">
              <label className="form-label" htmlFor="taxPercentage">Tax %</label>
              <input
                id="taxPercentage"
                className="form-control"
                type="number"
                name="taxPercentage"
                value={form.taxPercentage}
                onChange={handleChange}
                min="0"
                max="100"
                step="0.01"
                inputMode="decimal"
                required
                disabled={processing}
              />
            </div>
            <div className="col-md-4 mb-3">
              <label className="form-label">Tax Amount</label>
              <input className="form-control" value={taxAmount.toFixed(2)} readOnly />
            </div>
            <div className="col-md-4 mb-3">
              <label className="form-label">Total Amount</label>
              <input className="form-control" value={totalAmount.toFixed(2)} readOnly />
            </div>
          </div>

          <button
            type="submit"
            className="btn btn-primary"
            disabled={
              processing ||
              !form.providerId ||
              patients.length === 0 ||
              totalAmount <= 0
            }
          >
            {processing ? "Generating…" : "Generate Invoice"}
          </button>
        </form>
      </div>
    </Layout>
  );
}

export default GenerateInvoice;
