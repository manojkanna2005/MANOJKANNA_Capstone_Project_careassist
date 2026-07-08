import { useEffect, useState } from 'react';
import Layout from '../common/Layout.jsx';
import Table from '../common/Table.jsx';
import Message from '../common/Message.jsx';
import { approveClaim, deleteClaim, getAllClaims, rejectClaim } from '../../services/claimService.js';
import { money } from '../../utils/date.js';

function AllClaims() {
  const [items, setItems] = useState([]);
  const [message, setMessage] = useState('');
  const [error, setError] = useState('');

  const load = () =>
    getAllClaims()
      .then(setItems)
      .catch((loadError) => setError(loadError.userMessage || 'Unable to load claims.'));

  useEffect(() => {
    load();
  }, []);

  const approve = async (id) => {
    try {
      await approveClaim(id);
      setMessage('Claim approved.');
      load();
    } catch (approveError) {
      setError(approveError.userMessage || 'Unable to approve claim.');
    }
  };

  const reject = async (id) => {
    const reason = window.prompt('Rejection reason');
    if (!reason) return;
    try {
      await rejectClaim(id, reason);
      setMessage('Claim rejected.');
      load();
    } catch (rejectError) {
      setError(rejectError.userMessage || 'Unable to reject claim.');
    }
  };

  const remove = async (id) => {
    if (!window.confirm('Delete claim and its medical documents?')) return;
    try {
      await deleteClaim(id);
      setMessage('Claim deleted.');
      load();
    } catch (deleteError) {
      setError(deleteError.userMessage || 'Unable to delete claim.');
    }
  };

  return (
    <Layout title="All Claims">
      <Message type="success">{message}</Message>
      <Message type="danger">{error}</Message>
      <div className="card page-card p-4">
        <Table
          data={items}
          columns={[
            { key: 'claimId', label: 'ID' },
            { key: 'patientName', label: 'Patient' },
            { key: 'companyName', label: 'Company' },
            { key: 'invoiceNumber', label: 'Invoice' },
            { key: 'invoiceAmount', label: 'Invoice Amount', render: (row) => money(row.invoiceAmount) },
            { key: 'claimAmount', label: 'Claim Amount', render: (row) => money(row.claimAmount) },
            { key: 'submissionDate', label: 'Submitted' },
            { key: 'approvalDate', label: 'Approved' },
            { key: 'status', label: 'Status' },
          ]}
          actions={(row) => (
            <div className="d-flex gap-2 flex-wrap">
              {row.status === 'PENDING' && (
                <>
                  <button className="btn btn-sm btn-success" onClick={() => approve(row.claimId)}>Approve</button>
                  <button className="btn btn-sm btn-warning" onClick={() => reject(row.claimId)}>Reject</button>
                </>
              )}
              <button className="btn btn-sm btn-outline-danger" onClick={() => remove(row.claimId)}>Delete</button>
            </div>
          )}
        />
      </div>
    </Layout>
  );
}

export default AllClaims;
