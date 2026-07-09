import { useEffect, useMemo, useState } from "react";
import { Link } from "react-router-dom";
import Layout from "../common/Layout.jsx";
import Message from "../common/Message.jsx";
import { getUserId } from "../../utils/auth.js";
import { getProviderByUserId } from "../../services/healthcareProviderService.js";
import { getInvoicesByProviderId } from "../../services/invoiceService.js";
import { normalizeInvoiceStatus } from "../../utils/invoice.js";

function ProviderDashboard() {
  const [provider, setProvider] = useState(null);
  const [invoices, setInvoices] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState("");

  useEffect(() => {
    async function load() {
      setLoading(true);
      setError("");
      try {
        const profile = await getProviderByUserId(getUserId());
        setProvider(profile);
        const rows = await getInvoicesByProviderId(profile.providerId);
        setInvoices(Array.isArray(rows) ? rows : []);
      } catch (loadError) {
        setError(
          loadError.userMessage ||
            "Complete your provider profile before opening the dashboard.",
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
      total: invoices.length,
      open: Math.max(0, invoices.length - paid - cancelled),
      paid,
    };
  }, [invoices]);

  return (
    <Layout
      title="Provider Dashboard"
      subtitle="Generate invoices and review both open and completed billing records."
    >
      <Message type="warning">{error}</Message>
      <div className="provider-dashboard">
        <div className="role-hero">
          <h2>Care operations hub</h2>
          <p>Streamline billing, invoices, and patient follow-up from here.</p>
        </div>

        <div className="row g-3">
          <div className="col-md-4 col-xl-3">
            <div className="card page-card role-card p-4 h-100">
              <h5>Profile</h5>
              <p>{provider?.providerName || "Not completed"}</p>
              <Link to="/provider/profile" className="btn btn-primary">
                Open Profile
              </Link>
            </div>
          </div>

          <div className="col-md-4 col-xl-3">
            <div className="card page-card role-card p-4 h-100">
              <h5>Open Invoices</h5>
              <p className="display-6">{loading ? "—" : counts.open}</p>
              <Link
                to="/provider/invoices?filter=OPEN"
                className="btn btn-outline-primary"
              >
                View Open Invoices
              </Link>
            </div>
          </div>

          <div className="col-md-4 col-xl-3">
            <div className="card page-card role-card p-4 h-100">
              <h5>Paid Invoices</h5>
              <p className="display-6">{loading ? "—" : counts.paid}</p>
              <Link
                to="/provider/invoices?filter=PAID"
                className="btn btn-outline-success"
              >
                View Paid Invoices
              </Link>
            </div>
          </div>

          <div className="col-md-4 col-xl-3">
            <div className="card page-card role-card p-4 h-100">
              <h5>Create Invoice</h5>
              <p>{loading ? "—" : `${counts.total} total invoices`}</p>
              <Link
                to="/provider/generate-invoice"
                className="btn btn-outline-primary"
              >
                Generate
              </Link>
            </div>
          </div>
        </div>
      </div>
    </Layout>
  );
}

export default ProviderDashboard;
