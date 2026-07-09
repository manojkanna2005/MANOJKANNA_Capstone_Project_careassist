import { useEffect, useState } from "react";
import Layout from "../common/Layout.jsx";
import Table from "../common/Table.jsx";
import Message from "../common/Message.jsx";
import ClaimDocumentList from "../common/ClaimDocumentList.jsx";
import { getPatientByUserId } from "../../services/patientService.js";
import {
  getClaimDocuments,
  getClaimsByPatientId,
} from "../../services/claimService.js";
import { getUserId } from "../../utils/auth.js";
import { money } from "../../utils/date.js";

function MyClaims() {
  const [claims, setClaims] = useState([]);
  const [selectedClaimId, setSelectedClaimId] = useState(null);
  const [documents, setDocuments] = useState([]);
  const [error, setError] = useState("");

  useEffect(() => {
    async function load() {
      try {
        const patient = await getPatientByUserId(getUserId());
        const response = await getClaimsByPatientId(patient.patientId);
        setClaims(Array.isArray(response) ? response : []);
      } catch (loadError) {
        setError(loadError.userMessage || "Unable to load claims.");
      }
    }
    load();
  }, []);

  const showDocuments = async (claimId) => {
    setError("");
    try {
      setSelectedClaimId(claimId);
      setDocuments(await getClaimDocuments(claimId));
    } catch (loadError) {
      setError(loadError.userMessage || "Unable to load medical documents.");
    }
  };

  return (
    <Layout title="My Claims">
      <Message type="danger">{error}</Message>
      <div className="card page-card p-4">
        <Table
          data={claims}
          columns={[
            { key: "claimId", label: "Claim ID" },
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
              render: (row) => money(row.approvedAmount),
            },
            {
              key: "insurancePaidAmount",
              label: "Insurance Paid",
              render: (row) => money(row.insurancePaidAmount),
            },
            { key: "companyName", label: "Insurance Company" },
            { key: "submissionDate", label: "Submitted" },
            { key: "approvalDate", label: "Decision Date" },
            { key: "status", label: "Status" },
            { key: "rejectionReason", label: "Reason" },
            { key: "documentCount", label: "Documents" },
          ]}
          actions={(row) => (
            <button
              type="button"
              className="btn btn-sm btn-outline-primary"
              onClick={() => showDocuments(row.claimId)}
            >
              View Documents
            </button>
          )}
        />

        {selectedClaimId && (
          <div className="mt-4">
            <h5>Medical Documents for Claim {selectedClaimId}</h5>
            <ClaimDocumentList documents={documents} onError={setError} />
          </div>
        )}
      </div>
    </Layout>
  );
}

export default MyClaims;
