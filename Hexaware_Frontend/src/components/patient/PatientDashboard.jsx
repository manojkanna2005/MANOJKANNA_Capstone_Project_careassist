import { useEffect, useMemo, useState } from "react";
import { Link } from "react-router-dom";
import Layout from "../common/Layout.jsx";
import Message from "../common/Message.jsx";
import { getUserId } from "../../utils/auth.js";
import { getPatientByUserId } from "../../services/patientService.js";
import { getMyInvoices } from "../../services/invoiceService.js";
import { getClaimsByPatientId } from "../../services/claimService.js";
import { normalizeInvoiceStatus } from "../../utils/invoice.js";

function PatientDashboard() {
  const [profile, setProfile] = useState(null);
  const [invoices, setInvoices] = useState([]);
  const [claims, setClaims] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState("");

  useEffect(() => {
    async function load() {
      setLoading(true);
      setError("");
      try {
        const patient = await getPatientByUserId(getUserId());
        setProfile(patient);
        const [invoiceRows, claimRows] = await Promise.all([
          getMyInvoices(),
          getClaimsByPatientId(patient.patientId),
        ]);
        setInvoices(Array.isArray(invoiceRows) ? invoiceRows : []);
        setClaims(Array.isArray(claimRows) ? claimRows : []);
      } catch (loadError) {
        setError(
          loadError.userMessage ||
            "Complete your patient profile before opening the dashboard.",
        );
      } finally {
        setLoading(false);
      }
    }

    load();
  }, []);

  const counts = useMemo(() => {
    const paid = invoices.filter(
      (invoice) =>
        normalizeInvoiceStatus(invoice.status) === "PAID" ||
        Number(invoice.remainingAmount ?? invoice.totalAmount ?? 0) <= 0,
    ).length;

    const cancelled = invoices.filter(
      (invoice) => normalizeInvoiceStatus(invoice.status) === "CANCELLED",
    ).length;

    return {
      totalInvoices: invoices.length,
      openInvoices: Math.max(0, invoices.length - paid - cancelled),
      paidInvoices: paid,
      claims: claims.length,
    };
  }, [claims, invoices]);

  return (
    <Layout
      title="Patient Dashboard"
      subtitle="View open bills, completed invoices, insurance plans, and claims."
    >
      <Message type="warning">{error}</Message>
      <div className="patient-dashboard">
        <div className="role-hero">
          <h2>Welcome back</h2>
          <p>Track your care journey, invoices, and claims in one place.</p>
        </div>

        <div className="row g-3">
          <div className="col-md-4 col-xl-3">
            <div className="card page-card role-card p-4 h-100">
              <h5>Profile</h5>
              <p>{profile ? profile.fullName : "Not completed"}</p>
              <Link to="/patient/profile" className="btn btn-primary">
                Open Profile
              </Link>
            </div>
          </div>

          <div className="col-md-4 col-xl-3">
            <div className="card page-card role-card p-4 h-100">
              <h5>Open Invoices</h5>
              <p className="display-6">{loading ? "—" : counts.openInvoices}</p>
              <Link
                to="/patient/invoices?filter=ALL"
                className="btn btn-outline-primary"
              >
                View Invoices
              </Link>
            </div>
          </div>

          <div className="col-md-4 col-xl-3">
            <div className="card page-card role-card p-4 h-100">
              <h5>Paid Invoices</h5>
              <p className="display-6">{loading ? "—" : counts.paidInvoices}</p>
              <Link
                to="/patient/invoices?filter=PAID"
                className="btn btn-outline-success"
              >
                View Paid Invoices
              </Link>
            </div>
          </div>

          <div className="col-md-4 col-xl-3">
            <div className="card page-card role-card p-4 h-100">
              <h5>Claims</h5>
              <p className="display-6">{loading ? "—" : counts.claims}</p>
              <Link to="/patient/claims" className="btn btn-outline-primary">
                View Claims
              </Link>
            </div>
          </div>
        </div>

        {!loading && !error && counts.totalInvoices === 0 && (
          <div className="alert alert-info mt-4 mb-0">
            No invoices are linked to your account yet.
          </div>
        )}
      </div>
    </Layout>
  );
}

export default PatientDashboard;
