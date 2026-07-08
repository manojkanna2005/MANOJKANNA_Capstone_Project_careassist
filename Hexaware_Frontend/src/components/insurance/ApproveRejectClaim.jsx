import { useEffect, useState } from 'react';
import { useNavigate, useParams } from 'react-router-dom';
import Layout from '../common/Layout.jsx';
import Message from '../common/Message.jsx';
import { approveClaim, getClaimById, rejectClaim } from '../../services/claimService.js';
import { money } from '../../utils/date.js';

function ApproveRejectClaim() {
  const { claimId } = useParams();
  const navigate = useNavigate();
  const [claim, setClaim] = useState(null);
  const [reason, setReason] = useState('');
  const [reasonError, setReasonError] = useState('');
  const [message, setMessage] = useState('');
  const [error, setError] = useState('');

  useEffect(() => {
    getClaimById(claimId)
      .then(setClaim)
      .catch((loadError) => setError(loadError.userMessage || 'Unable to load claim.'));
  }, [claimId]);

  const approve = async () => {
    setError('');
    try {
      await approveClaim(claimId);
      setMessage('Claim approved and the patient was notified by email.');
      setTimeout(() => navigate('/insurance/incoming-claims'), 1200);
    } catch (approveError) {
      setError(approveError.userMessage || 'Unable to approve claim.');
    }
  };

  const reject = async () => {
    setError('');
    setReasonError('');
    if (!reason.trim()) {
      setReasonError('Rejection reason is required.');
      return;
    }
    if (reason.trim().length > 255) {
      setReasonError('Rejection reason cannot exceed 255 characters.');
      return;
    }

    try {
      await rejectClaim(claimId, reason.trim());
      setMessage('Claim rejected and the patient was notified by email.');
      setTimeout(() => navigate('/insurance/incoming-claims'), 1200);
    } catch (rejectError) {
      setError(rejectError.userMessage || 'Unable to reject claim.');
    }
  };

  const insufficientCoverage =
    claim && Number(claim.claimAmount) > Number(claim.remainingCoverage);

  return (
    <Layout title="Approve / Reject Claim">
      <div className="card page-card p-4">
        <Message type="success">{message}</Message>
        <Message type="danger">{error}</Message>

        {claim && (
          <>
            <p><strong>Claim ID:</strong> {claim.claimId}</p>
            <p><strong>Patient:</strong> {claim.patientName}</p>
            <p><strong>Claim amount:</strong> {money(claim.claimAmount)}</p>
            <p><strong>Remaining coverage:</strong> {money(claim.remainingCoverage)}</p>
            {insufficientCoverage && (
              <div className="alert alert-danger">
                This claim exceeds the remaining policy coverage and cannot be approved.
              </div>
            )}
            <button
              type="button"
              className="btn btn-success me-2"
              onClick={approve}
              disabled={claim.status !== 'PENDING' || insufficientCoverage}
            >
              Approve
            </button>
          </>
        )}

        <hr />
        <label className="form-label" htmlFor="rejectionReason">Rejection Reason</label>
        <textarea
          id="rejectionReason"
          name="rejectionReason"
          className={`form-control ${reasonError ? 'is-invalid' : ''}`}
          value={reason}
          onChange={(event) => {
            setReason(event.target.value);
            setReasonError('');
          }}
          maxLength="255"
          rows="4"
        />
        {reasonError && <div className="invalid-feedback d-block">{reasonError}</div>}
        <button
          type="button"
          className="btn btn-danger mt-3"
          onClick={reject}
          disabled={claim?.status !== 'PENDING'}
        >
          Reject
        </button>
      </div>
    </Layout>
  );
}

export default ApproveRejectClaim;
