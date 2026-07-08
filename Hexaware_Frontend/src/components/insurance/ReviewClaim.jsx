import { useEffect, useState } from 'react';
import { useParams, Link } from 'react-router-dom';
import Layout from '../common/Layout.jsx';
import Message from '../common/Message.jsx';
import { getClaimById } from '../../services/claimService.js';
import { money } from '../../utils/date.js';

function ReviewClaim() {
  const { claimId } = useParams(); const [claim, setClaim] = useState(null); const [error, setError] = useState('');
  useEffect(() => { getClaimById(claimId).then(setClaim).catch((err) => setError(err.userMessage || 'Unable to load claim')); }, [claimId]);
  return <Layout title="Review Claim"><Message type="danger">{error}</Message><div className="card page-card p-4">{claim && <div><p><strong>Claim ID:</strong> {claim.claimId}</p><p><strong>Patient:</strong> {claim.patientId}</p><p><strong>Invoice:</strong> {claim.invoiceId}</p><p><strong>Diagnosis:</strong> {claim.diagnosis}</p><p><strong>Treatment:</strong> {claim.treatment}</p><p><strong>Amount:</strong> {money(claim.claimAmount)}</p><p><strong>Status:</strong> {claim.status}</p><Link className="btn btn-primary" to={`/insurance/claim-decision/${claim.claimId}`}>Approve / Reject</Link></div>}</div></Layout>;
}
export default ReviewClaim;
