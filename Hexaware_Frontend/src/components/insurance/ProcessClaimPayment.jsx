import { useEffect, useState } from 'react';
import Layout from '../common/Layout.jsx';
import Message from '../common/Message.jsx';
import { processClaimPayment } from '../../services/claimPaymentService.js';
import { getClaimsByInsuranceCompanyId } from '../../services/claimService.js';
import { getCompanyByUserId } from '../../services/insuranceCompanyService.js';
import { getUserId } from '../../utils/auth.js';
import { nowDateTime } from '../../utils/date.js';

function ProcessClaimPayment() {
  const [claims, setClaims] = useState([]);
  const [form, setForm] = useState({
    claimId: '',
    paymentDate: nowDateTime(),
    paymentAmount: '',
    paymentMode: 'UPI',
    transactionReference: '',
  });
  const [message, setMessage] = useState('');
  const [error, setError] = useState('');

  useEffect(() => {
    async function load() {
      setError('');
      try {
        const company = await getCompanyByUserId(getUserId());
        const companyClaims = await getClaimsByInsuranceCompanyId(company.companyId).catch(() => []);
        setClaims(companyClaims.filter((claim) => claim.status === 'APPROVED'));
      } catch (err) {
        setError(err.userMessage || 'Complete your insurance company profile first.');
      }
    }

    load();
  }, []);

  const handleChange = (e) => {
    setForm({ ...form, [e.target.name]: e.target.value });
  };

  const handleClaimChange = (e) => {
    const claimId = e.target.value;
    const claim = claims.find((item) => String(item.claimId) === String(claimId));
    setForm({
      ...form,
      claimId,
      paymentAmount: claim?.claimAmount || form.paymentAmount,
    });
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    setMessage('');
    setError('');

    try {
      await processClaimPayment(form);
      setMessage('Claim payment processed.');
    } catch (err) {
      setError(err.userMessage || 'Unable to process payment');
    }
  };

  return (
    <Layout title="Process Claim Payment" subtitle="Select an approved claim and process payment.">
      <div className="card page-card p-4">
        <Message type="success">{message}</Message>
        <Message type="danger">{error}</Message>

        <form onSubmit={handleSubmit}>
          <div className="row">
            <div className="col-md-6 mb-3">
              <label className="form-label">Approved Claim</label>
              <select className="form-select" name="claimId" value={form.claimId} onChange={handleClaimChange} required>
                <option value="">Select approved claim</option>
                {claims.map((claim) => (
                  <option key={claim.claimId} value={claim.claimId}>
                    Claim {claim.claimId} - Patient {claim.patientId} - ₹{claim.claimAmount}
                  </option>
                ))}
              </select>
            </div>

            <div className="col-md-6 mb-3">
              <label className="form-label">Payment Date</label>
              <input className="form-control" type="datetime-local" name="paymentDate" value={form.paymentDate} onChange={handleChange} required />
            </div>
          </div>

          <div className="row">
            <div className="col-md-6 mb-3">
              <label className="form-label">Amount</label>
              <input className="form-control" type="number" name="paymentAmount" value={form.paymentAmount} onChange={handleChange} required min="1" />
            </div>

            <div className="col-md-6 mb-3">
              <label className="form-label">Mode</label>
              <select className="form-select" name="paymentMode" value={form.paymentMode} onChange={handleChange}>
                <option>CASH</option>
                <option>CARD</option>
                <option>UPI</option>
                <option>NET_BANKING</option>
                <option>CHEQUE</option>
              </select>
            </div>
          </div>

          <div className="mb-3">
            <label className="form-label">Transaction Reference</label>
            <input className="form-control" name="transactionReference" value={form.transactionReference} onChange={handleChange} required minLength="5" />
          </div>

          <button className="btn btn-primary" disabled={claims.length === 0}>Process Payment</button>
        </form>
      </div>
    </Layout>
  );
}

export default ProcessClaimPayment;
