import { useEffect, useState } from 'react';
import { Link, useParams } from 'react-router-dom';
import Layout from '../common/Layout.jsx';
import Message from '../common/Message.jsx';
import ClaimDocumentList from '../common/ClaimDocumentList.jsx';
import { getClaimById, getClaimDocuments } from '../../services/claimService.js';
import { money } from '../../utils/date.js';

function ReviewClaim() {
  const { claimId } = useParams();
  const [claim, setClaim] = useState(null);
  const [documents, setDocuments] = useState([]);
  const [error, setError] = useState('');

  useEffect(() => {
    async function load() {
      try {
        const [claimData, documentData] = await Promise.all([
          getClaimById(claimId),
          getClaimDocuments(claimId),
        ]);
        setClaim(claimData);
        setDocuments(documentData);
      } catch (loadError) {
        setError(loadError.userMessage || 'Unable to load claim review details.');
      }
    }

    load();
  }, [claimId]);

  return (
    <Layout title="Review Claim" subtitle="Verify policy coverage, invoice details, and medical documents.">
      <Message type="danger">{error}</Message>
      <div className="card page-card p-4">
        {claim && (
          <>
            <div className="row">
              <div className="col-md-6">
                <p><strong>Claim ID:</strong> {claim.claimId}</p>
                <p><strong>Patient:</strong> {claim.patientName} ({claim.patientEmail})</p>
                <p><strong>Provider:</strong> {claim.providerName || '-'}</p>
                <p><strong>Insurance company:</strong> {claim.companyName}</p>
                <p><strong>Plan:</strong> {claim.planName}</p>
              </div>
              <div className="col-md-6">
                <p><strong>Invoice:</strong> {claim.invoiceNumber} ({money(claim.invoiceAmount)})</p>
                <p><strong>Claim amount:</strong> {money(claim.claimAmount)}</p>
                <p><strong>Coverage amount:</strong> {money(claim.coverageAmount)}</p>
                <p><strong>Approved coverage used:</strong> {money(claim.approvedCoverageUsed)}</p>
                <p><strong>Remaining coverage:</strong> {money(claim.remainingCoverage)}</p>
              </div>
            </div>

            <hr />
            <div className="row">
              <div className="col-md-6">
                <p><strong>Diagnosis:</strong> {claim.diagnosis}</p>
                <p><strong>Treatment:</strong> {claim.treatment}</p>
              </div>
              <div className="col-md-6">
                <p><strong>Date of service:</strong> {claim.dateOfService}</p>
                <p><strong>Submitted:</strong> {claim.submissionDate}</p>
                <p><strong>Status:</strong> {claim.status}</p>
              </div>
            </div>

            <h5 className="mt-3">Medical Documents</h5>
            <ClaimDocumentList documents={documents} onError={setError} />

            {claim.status === 'PENDING' && (
              <Link className="btn btn-primary mt-4" to={`/insurance/claim-decision/${claim.claimId}`}>
                Approve / Reject
              </Link>
            )}
          </>
        )}
      </div>
    </Layout>
  );
}

export default ReviewClaim;
