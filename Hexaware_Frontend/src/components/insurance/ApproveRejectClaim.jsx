import { useState } from 'react';
import { useNavigate, useParams } from 'react-router-dom';
import Layout from '../common/Layout.jsx';
import Message from '../common/Message.jsx';
import { approveClaim, rejectClaim } from '../../services/claimService.js';

function ApproveRejectClaim() {
  const { claimId } = useParams(); const navigate = useNavigate(); const [reason, setReason] = useState(''); const [message, setMessage] = useState(''); const [error, setError] = useState('');
  const approve = async () => { try { await approveClaim(claimId); setMessage('Claim approved.'); setTimeout(() => navigate('/insurance/incoming-claims'), 1000); } catch (err) { setError(err.userMessage || 'Unable to approve claim'); } };
  const reject = async () => { if (!reason.trim()) { setError('Rejection reason is required.'); return; } try { await rejectClaim(claimId, reason); setMessage('Claim rejected.'); setTimeout(() => navigate('/insurance/incoming-claims'), 1000); } catch (err) { setError(err.userMessage || 'Unable to reject claim'); } };
  return <Layout title="Approve / Reject Claim"><div className="card page-card p-4"><Message type="success">{message}</Message><Message type="danger">{error}</Message><p>Claim ID: {claimId}</p><button className="btn btn-success me-2" onClick={approve}>Approve</button><hr /><label className="form-label">Rejection Reason</label><textarea className="form-control mb-3" value={reason} onChange={(e)=>setReason(e.target.value)} /><button className="btn btn-danger" onClick={reject}>Reject</button></div></Layout>;
}
export default ApproveRejectClaim;
