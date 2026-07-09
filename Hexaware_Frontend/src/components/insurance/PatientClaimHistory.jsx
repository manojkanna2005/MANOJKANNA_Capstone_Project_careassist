import { useEffect, useMemo, useState } from "react";
import Layout from "../common/Layout.jsx";
import Table from "../common/Table.jsx";
import Message from "../common/Message.jsx";
import { getCompanyByUserId } from "../../services/insuranceCompanyService.js";
import { getClaimsByInsuranceCompanyId } from "../../services/claimService.js";
import { getUserId } from "../../utils/auth.js";
import { money } from "../../utils/date.js";
import { formatInvoiceDate, getInvoiceBadgeClass } from "../../utils/invoice.js";

function PatientClaimHistory() {
  const [claims, setClaims] = useState([]);
  const [patientId, setPatientId] = useState("");
  const [status, setStatus] = useState("ALL");
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState("");

  useEffect(() => {
    async function load() {
      setLoading(true);
      setError("");
      try {
        const company = await getCompanyByUserId(getUserId());
        const companyClaims = await getClaimsByInsuranceCompanyId(
          company.companyId,
        );
        setClaims(Array.isArray(companyClaims) ? companyClaims : []);
      } catch (loadError) {
        setError(loadError.userMessage || "Unable to load claim history.");
      } finally {
        setLoading(false);
      }
    }

    load();
  }, []);

  const patients = useMemo(() => {
    const byId = new Map();
    claims.forEach((claim) => {
      if (claim.patientId) {
        byId.set(String(claim.patientId), claim.patientName || `Patient ${claim.patientId}`);
      }
    });
    return [...byId.entries()].map(([id, name]) => ({ id, name }));
  }, [claims]);

  const filtered = useMemo(
    () =>
      claims.filter((claim) => {
        const matchesPatient =
          !patientId || String(claim.patientId) === String(patientId);
        const settlementStatus = claim.insurancePaymentProcessed
          ? "PAID"
          : String(claim.status || "").toUpperCase();
        const matchesStatus = status === "ALL" || settlementStatus === status;
        return matchesPatient && matchesStatus;
      }),
    [claims, patientId, status],
  );

  return (
    <Layout
      title="Patient Claim History"
      subtitle="Completed insurance payments remain available here but are excluded from the active insurance dashboard."
    >
      <Message type="danger">{error}</Message>
      <div className="card page-card p-4">
        <div className="row g-3 mb-3">
          <div className="col-md-6">
            <label className="form-label" htmlFor="historyPatient">
              Patient
            </label>
            <select
              id="historyPatient"
              className="form-select"
              value={patientId}
              onChange={(event) => setPatientId(event.target.value)}
            >
              <option value="">All patients</option>
              {patients.map((patient) => (
                <option key={patient.id} value={patient.id}>
                  {patient.name} (Patient ID: {patient.id})
                </option>
              ))}
            </select>
          </div>

          <div className="col-md-6">
            <label className="form-label" htmlFor="historyStatus">
              Claim status
            </label>
            <select
              id="historyStatus"
              className="form-select"
              value={status}
              onChange={(event) => setStatus(event.target.value)}
            >
              <option value="ALL">All statuses</option>
              <option value="PENDING">Pending</option>
              <option value="APPROVED">Approved, awaiting payment</option>
              <option value="PAID">Insurance paid</option>
              <option value="REJECTED">Rejected</option>
            </select>
          </div>
        </div>

        <Table
          data={filtered}
          emptyMessage={loading ? "Loading claim history…" : "No claims match the selected filters."}
          columns={[
            { key: "claimId", label: "Claim" },
            { key: "patientName", label: "Patient Name" },
            { key: "invoiceNumber", label: "Invoice" },
            {
              key: "invoiceAmount",
              label: "Invoice Amount",
              render: (row) => money(row.invoiceAmount),
            },
            {
              key: "claimAmount",
              label: "Requested",
              render: (row) => money(row.claimAmount),
            },
            {
              key: "approvedAmount",
              label: "Approved",
              render: (row) =>
                row.approvedAmount == null ? "—" : money(row.approvedAmount),
            },
            {
              key: "insurancePaidAmount",
              label: "Insurance Paid",
              render: (row) =>
                row.insurancePaymentProcessed
                  ? money(row.insurancePaidAmount)
                  : "—",
            },
            {
              key: "settlementStatus",
              label: "Settlement",
              render: (row) => {
                const value = row.insurancePaymentProcessed
                  ? "PAID"
                  : String(row.status || "UNKNOWN").toUpperCase();
                return (
                  <span className={`badge ${getInvoiceBadgeClass(value)}`}>
                    {value}
                  </span>
                );
              },
            },
            {
              key: "insurancePaymentDate",
              label: "Payment Date",
              render: (row) =>
                row.insurancePaymentProcessed
                  ? formatInvoiceDate(row.insurancePaymentDate, true)
                  : "—",
            },
            {
              key: "insuranceTransactionReference",
              label: "Reference",
              render: (row) => row.insuranceTransactionReference || "—",
            },
            { key: "rejectionReason", label: "Rejection Reason" },
          ]}
        />
      </div>
    </Layout>
  );
}

export default PatientClaimHistory;
