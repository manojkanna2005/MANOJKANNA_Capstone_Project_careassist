import { useEffect, useState } from 'react';
import Layout from '../common/Layout.jsx';
import Table from '../common/Table.jsx';
import Message from '../common/Message.jsx';
import { getProviderByUserId } from '../../services/healthcareProviderService.js';
import { downloadInvoicePdf, emailInvoicePdf, getInvoicesByProviderId, updateInvoiceStatus } from '../../services/invoiceService.js';
import { getUserId } from '../../utils/auth.js';
import { money } from '../../utils/date.js';

function ProviderInvoices() {
  const [items, setItems] = useState([]);
  const [providerId, setProviderId] = useState('');
  const [message, setMessage] = useState('');
  const [error, setError] = useState('');

  const load = async () => {
    try {
      const provider = await getProviderByUserId(getUserId());
      setProviderId(provider.providerId);
      setItems(await getInvoicesByProviderId(provider.providerId));
    } catch (err) {
      setError(err.userMessage || 'Unable to load invoices');
    }
  };

  useEffect(() => { load(); }, []);

  const changeStatus = async (id, status) => {
    setMessage('');
    setError('');
    try {
      await updateInvoiceStatus(id, status);
      setMessage('Invoice status updated.');
      load();
    } catch (err) {
      setError(err.userMessage || 'Unable to update invoice status');
    }
  };

  const handleDownload = async (invoiceId) => {
    setMessage('');
    setError('');
    try {
      await downloadInvoicePdf(invoiceId);
    } catch (err) {
      setError(err.userMessage || 'Unable to download invoice PDF');
    }
  };

  const handleEmail = async (invoiceId) => {
    setMessage('');
    setError('');
    try {
      await emailInvoicePdf(invoiceId);
      setMessage('Invoice PDF emailed to the patient.');
    } catch (err) {
      setError(err.userMessage || 'Unable to email invoice PDF');
    }
  };

  return (
    <Layout title="Provider Invoices" subtitle={`Provider ID: ${providerId}`}>
      <Message type="success">{message}</Message>
      <Message type="danger">{error}</Message>
      <div className="card page-card p-4">
        <Table
          data={items}
          columns={[
            { key: 'invoiceId', label: 'ID' },
            { key: 'patientId', label: 'Patient' },
            { key: 'invoiceNumber', label: 'Number' },
            { key: 'totalAmount', label: 'Total', render: (r) => money(r.totalAmount) },
            { key: 'status', label: 'Status' },
          ]}
          actions={(row) => (
            <div className="d-flex flex-wrap gap-2 align-items-center">
              <select className="form-select form-select-sm" value={row.status} onChange={(e) => changeStatus(row.invoiceId, e.target.value)}>
                <option>PENDING</option>
                <option>PAID</option>
                <option>OVERDUE</option>
                <option>CANCELLED</option>
              </select>
              <button className="btn btn-sm btn-outline-primary" type="button" onClick={() => handleDownload(row.invoiceId)}>PDF</button>
              <button className="btn btn-sm btn-outline-secondary" type="button" onClick={() => handleEmail(row.invoiceId)}>Email</button>
            </div>
          )}
        />
      </div>
    </Layout>
  );
}
export default ProviderInvoices;
