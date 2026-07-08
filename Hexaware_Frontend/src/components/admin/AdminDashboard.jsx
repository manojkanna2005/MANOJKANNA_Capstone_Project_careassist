import { useEffect, useState } from "react";
import Layout from "../common/Layout.jsx";
import Message from "../common/Message.jsx";
import Table from "../common/Table.jsx";
import {
  getDashboardSummary,
  getAdminClaims,
  getAdminPayments,
} from "../../services/adminService.js";
import { money } from "../../utils/date.js";

function AdminDashboard() {
  const [summary, setSummary] = useState({});
  const [claims, setClaims] = useState([]);
  const [payments, setPayments] = useState([]);
  const [error, setError] = useState("");
  useEffect(() => {
    async function load() {
      try {
        setSummary(await getDashboardSummary());
        setClaims(await getAdminClaims());
        setPayments(await getAdminPayments());
      } catch (err) {
        setError(err.userMessage || "Unable to load dashboard");
      }
    }
    load();
  }, []);
  return (
    <Layout
      title="Admin Dashboard"
      subtitle="System dashboard for claims and payments."
    >
      <Message type="danger">{error}</Message>
      <div className="row g-3 mb-4">
        {Object.entries(summary || {}).map(([k, v]) => (
          <div className="col-md-3" key={k}>
            <div className="card page-card p-3">
              <small className="text-muted">{k}</small>
              <h4>{String(v)}</h4>
            </div>
          </div>
        ))}
      </div>
      <div className="card page-card p-4 mb-4">
        <h5>Recent Claims</h5>
        <Table
          data={claims.slice(0, 5)}
          columns={[
            { key: "claimId", label: "Claim" },
            { key: "patientId", label: "Patient" },
            {
              key: "claimAmount",
              label: "Amount",
              render: (r) => money(r.claimAmount),
            },
            { key: "status", label: "Status" },
          ]}
        />
      </div>
      <div className="card page-card p-4">
        <h5>Recent Payments</h5>
        <Table
          data={payments.slice(0, 5)}
          columns={[
            { key: "paymentId", label: "Payment" },
            { key: "claimId", label: "Claim" },
            {
              key: "paymentAmount",
              label: "Amount",
              render: (r) => money(r.paymentAmount),
            },
            { key: "paymentMode", label: "Mode" },
          ]}
        />
      </div>
    </Layout>
  );
}
export default AdminDashboard;
