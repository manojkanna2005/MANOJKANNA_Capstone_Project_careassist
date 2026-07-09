import { useEffect, useState } from "react";
import { Link } from "react-router-dom";
import Layout from "../common/Layout.jsx";
import Table from "../common/Table.jsx";
import Message from "../common/Message.jsx";
import { getCompanyByUserId } from "../../services/insuranceCompanyService.js";
import { getActionableClaimsByInsuranceCompanyId } from "../../services/claimService.js";
import { getUserId } from "../../utils/auth.js";
import { money } from "../../utils/date.js";

function IncomingClaims() {
  const [items, setItems] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState("");

  useEffect(() => {
    async function load() {
      setLoading(true);
      setError("");
      try {
        const company = await getCompanyByUserId(getUserId());
        const claims = await getActionableClaimsByInsuranceCompanyId(
          company.companyId,
        );
        setItems(Array.isArray(claims) ? claims : []);
      } catch (loadError) {
        setError(loadError.userMessage || "Unable to load active claims.");
      } finally {
        setLoading(false);
      }
    }

    load();
  }, []);

  return (
    <Layout
      title="Active Insurance Claims"
      subtitle="Pending claims and approved claims awaiting payment only. Paid and rejected claims are kept in history."
    >
      <Message type="danger">{error}</Message>
      <div className="card page-card p-4">
        {!loading && items.length === 0 ? (
          <div className="alert alert-success mb-0">
            No claims currently require insurance action.
          </div>
        ) : (
          <Table
            data={items}
            columns={[
              { key: "claimId", label: "Claim" },
              { key: "patientName", label: "Patient" },
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
                key: "remainingCoverage",
                label: "Coverage Remaining",
                render: (row) => money(row.remainingCoverage),
              },
              { key: "planName", label: "Plan" },
              { key: "documentCount", label: "Documents" },
              { key: "submissionDate", label: "Submitted" },
              { key: "status", label: "Status" },
            ]}
            actions={(row) => (
              <div className="d-flex gap-2 flex-wrap">
                <Link
                  className="btn btn-sm btn-outline-primary"
                  to={`/insurance/review-claim/${row.claimId}`}
                >
                  Review
                </Link>
                {row.status === "PENDING" && (
                  <Link
                    className="btn btn-sm btn-primary"
                    to={`/insurance/claim-decision/${row.claimId}`}
                  >
                    Decision
                  </Link>
                )}
                {row.status === "APPROVED" &&
                  !row.insurancePaymentProcessed && (
                    <Link
                      className="btn btn-sm btn-success"
                      to="/insurance/process-payment"
                    >
                      Pay Claim
                    </Link>
                  )}
              </div>
            )}
          />
        )}
      </div>
    </Layout>
  );
}

export default IncomingClaims;
