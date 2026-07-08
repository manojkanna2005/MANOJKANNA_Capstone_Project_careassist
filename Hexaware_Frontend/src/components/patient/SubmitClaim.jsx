import { useEffect, useState } from "react";
import Layout from "../common/Layout.jsx";
import Message from "../common/Message.jsx";
import { submitClaim } from "../../services/claimService.js";
import { getPatientByUserId } from "../../services/patientService.js";
import { getInvoicesByPatientId } from "../../services/invoiceService.js";
import { getInsuranceHistoryByPatientId } from "../../services/patientInsuranceService.js";
import { getPlanById } from "../../services/insurancePlanService.js";
import { getAllCompanies } from "../../services/insuranceCompanyService.js";
import { getUserId } from "../../utils/auth.js";
import { nowDateTime, today } from "../../utils/date.js";

const empty = {
  patientId: "",
  invoiceId: "",
  companyId: "",
  diagnosis: "",
  treatment: "",
  dateOfService: today(),
  claimAmount: "",
  submissionDate: nowDateTime(),
  approvalDate: "",
  status: "PENDING",
  rejectionReason: "",
};

function SubmitClaim() {
  const [form, setForm] = useState(empty);
  const [invoices, setInvoices] = useState([]);
  const [companies, setCompanies] = useState([]);
  const [message, setMessage] = useState("");
  const [error, setError] = useState("");

  useEffect(() => {
    async function load() {
      try {
        const patient = await getPatientByUserId(getUserId());
        const [patientInvoices, insuranceHistory, companyList] =
          await Promise.all([
            getInvoicesByPatientId(patient.patientId).catch(() => []),
            getInsuranceHistoryByPatientId(patient.patientId).catch(() => []),
            getAllCompanies().catch(() => []),
          ]);

        const unpaidInvoices = patientInvoices.filter(
          (invoice) => String(invoice.status).toUpperCase() !== "PAID",
        );
        const active =
          insuranceHistory.find((item) => item.status === "ACTIVE") ||
          insuranceHistory[0];
        let companyId = "";

        if (active?.planId) {
          const plan = await getPlanById(active.planId).catch(() => null);
          companyId = plan?.companyId || "";
        }

        setInvoices(unpaidInvoices);
        setCompanies(companyList);
        setForm((prev) => ({
          ...prev,
          patientId: patient.patientId,
          companyId,
        }));
      } catch (err) {
        setError(
          err.userMessage ||
            "Complete patient profile and select an insurance plan before submitting claim.",
        );
      }
    }

    load();
  }, []);

  const handleChange = (e) => {
    setForm({ ...form, [e.target.name]: e.target.value });
  };

  const handleInvoice = (e) => {
    const invoiceId = e.target.value;
    const invoice = invoices.find(
      (item) => String(item.invoiceId) === String(invoiceId),
    );
    setForm({
      ...form,
      invoiceId,
      claimAmount: invoice?.totalAmount || form.claimAmount,
    });
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    setMessage("");
    setError("");

    try {
      await submitClaim({
        ...form,
        approvalDate: form.approvalDate || null,
        rejectionReason: form.rejectionReason || null,
      });
      setMessage("Claim submitted successfully.");
      setForm({
        ...empty,
        patientId: form.patientId,
        companyId: form.companyId,
      });
    } catch (err) {
      setError(err.userMessage || "Unable to submit claim");
    }
  };

  return (
    <Layout
      title="Submit Claim"
      subtitle="Submit claim after invoice generation."
    >
      <div className="card page-card p-4">
        <Message type="success">{message}</Message>
        <Message type="danger">{error}</Message>

        <form onSubmit={handleSubmit}>
          <div className="row">
            <div className="col-md-4 mb-3">
              <label className="form-label">Patient ID</label>
              <input
                className="form-control"
                name="patientId"
                value={form.patientId}
                readOnly
              />
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
                <option value="">Select Invoice</option>
                {invoices.map((invoice) => (
                  <option key={invoice.invoiceId} value={invoice.invoiceId}>
                    {invoice.invoiceNumber} - {invoice.status} - ₹
                    {invoice.totalAmount}
                  </option>
                ))}
              </select>
            </div>

            <div className="col-md-4 mb-3">
              <label className="form-label">Insurance Company</label>
              <select
                className="form-select"
                name="companyId"
                value={form.companyId}
                onChange={handleChange}
                required
              >
                <option value="">Select company</option>
                {companies.map((company) => (
                  <option key={company.companyId} value={company.companyId}>
                    {company.companyName} (Company ID: {company.companyId})
                  </option>
                ))}
              </select>
            </div>
          </div>

          <div className="row">
            <div className="col-md-6 mb-3">
              <label className="form-label">Diagnosis</label>
              <input
                className="form-control"
                name="diagnosis"
                value={form.diagnosis}
                onChange={handleChange}
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
                required
              />
            </div>
          </div>

          <div className="row">
            <div className="col-md-4 mb-3">
              <label className="form-label">Date of Service</label>
              <input
                className="form-control"
                type="date"
                name="dateOfService"
                value={form.dateOfService}
                onChange={handleChange}
                required
              />
            </div>
            <div className="col-md-4 mb-3">
              <label className="form-label">Claim Amount</label>
              <input
                className="form-control"
                type="number"
                name="claimAmount"
                value={form.claimAmount}
                onChange={handleChange}
                min="1"
                required
              />
            </div>
            <div className="col-md-4 mb-3">
              <label className="form-label">Status</label>
              <select
                className="form-select"
                name="status"
                value={form.status}
                onChange={handleChange}
              >
                <option>PENDING</option>
              </select>
            </div>
          </div>

          <button
            className="btn btn-primary"
            disabled={!form.patientId || companies.length === 0}
          >
            Submit Claim
          </button>
        </form>
      </div>
    </Layout>
  );
}

export default SubmitClaim;
