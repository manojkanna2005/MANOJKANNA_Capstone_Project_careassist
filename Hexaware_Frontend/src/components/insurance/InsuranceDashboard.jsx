import { useEffect, useMemo, useState } from "react";
import { Link } from "react-router-dom";
import Layout from "../common/Layout.jsx";
import Message from "../common/Message.jsx";
import { getUserId } from "../../utils/auth.js";
import { getCompanyByUserId } from "../../services/insuranceCompanyService.js";
import { getActionableClaimsByInsuranceCompanyId } from "../../services/claimService.js";

function InsuranceDashboard() {
  const [company, setCompany] = useState(null);
  const [claims, setClaims] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState("");

  useEffect(() => {
    async function load() {
      setLoading(true);
      setError("");
      try {
        const companyProfile = await getCompanyByUserId(getUserId());
        setCompany(companyProfile);
        const actionableClaims = await getActionableClaimsByInsuranceCompanyId(
          companyProfile.companyId,
        );
        setClaims(Array.isArray(actionableClaims) ? actionableClaims : []);
      } catch (loadError) {
        setError(
          loadError.userMessage ||
            "Complete your insurance company profile before reviewing claims.",
        );
      } finally {
        setLoading(false);
      }
    }

    load();
  }, []);

  const counts = useMemo(
    () => ({
      pending: claims.filter((claim) => claim.status === "PENDING").length,
      awaitingPayment: claims.filter(
        (claim) =>
          claim.status === "APPROVED" && !claim.insurancePaymentProcessed,
      ).length,
      total: claims.length,
    }),
    [claims],
  );

  return (
    <Layout
      title="Insurance Dashboard"
      subtitle="Review pending claims and settle approved claims. Paid claims are kept in history and are not shown as active work."
    >
      <Message type="danger">{error}</Message>
      <div className="insurance-dashboard">
        <div className="role-hero">
          <h2>Claims review center</h2>
          <p>
            Only claims that still need a decision or insurance payment appear
            here.
          </p>
        </div>

        <div className="row g-3">
          <div className="col-md-3">
            <div className="card page-card role-card p-4 h-100">
              <h5>Profile</h5>
              <p>{company?.companyName || "Not completed"}</p>
              <Link to="/insurance/profile" className="btn btn-primary">
                Open Profile
              </Link>
            </div>
          </div>

          <div className="col-md-3">
            <div className="card page-card role-card p-4 h-100">
              <h5>Pending Decisions</h5>
              <p className="display-6">{loading ? "—" : counts.pending}</p>
              <Link
                to="/insurance/incoming-claims"
                className="btn btn-outline-primary"
              >
                Review Claims
              </Link>
            </div>
          </div>

          <div className="col-md-3">
            <div className="card page-card role-card p-4 h-100">
              <h5>Awaiting Payment</h5>
              <p className="display-6">
                {loading ? "—" : counts.awaitingPayment}
              </p>
              <Link
                to="/insurance/process-payment"
                className="btn btn-outline-primary"
              >
                Process Payments
              </Link>
            </div>
          </div>

          <div className="col-md-3">
            <div className="card page-card role-card p-4 h-100">
              <h5>Plans</h5>
              <p>Manage coverage limits and premiums.</p>
              <Link to="/insurance/plans" className="btn btn-outline-primary">
                Manage Plans
              </Link>
            </div>
          </div>
        </div>

        {!loading && !error && counts.total === 0 && (
          <div className="alert alert-success mt-4 mb-0">
            No claims currently require insurance action. Paid and rejected
            claims remain available in Patient Claim History.
          </div>
        )}
      </div>
    </Layout>
  );
}

export default InsuranceDashboard;
