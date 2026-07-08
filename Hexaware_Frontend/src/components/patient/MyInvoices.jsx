import { useEffect, useState } from 'react';
import { Link } from 'react-router-dom';
import Layout from '../common/Layout.jsx';
import Table from '../common/Table.jsx';
import Message from '../common/Message.jsx';
import { getPatientByUserId } from '../../services/patientService.js';
import { downloadInvoicePdf, emailInvoicePdf, getInvoicesByPatientId } from '../../services/invoiceService.js';
import { getUserId } from '../../utils/auth.js';
import { money } from '../../utils/date.js';

function MyInvoices() {
  const [invoices, setInvoices] = useState([]);
  const [message, setMessage] = useState('');
  const [error, setError] = useState('');

  useEffect(() => {
    async function load() {
      try {
        const patient = await getPatientByUserId(getUserId());
        setInvoices(await getInvoicesByPatientId(patient.patientId));
      } catch (err) {
        setError(err.userMessage || 'Unable to load invoices');
      }
    }
    load();
  }, []);

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
      setMessage('Invoice PDF sent to your registered email.');
    } catch (err) {
      setError(err.userMessage || 'Unable to email invoice PDF');
    }
  };

  return (
    <Layout title="My Invoices">
      <Message type="success">{message}</Message>
      <Message type="danger">{error}</Message>
      <div className="card page-card p-4">
        <Table
          data={invoices}
          columns={[
            { key: 'invoiceId', label: 'ID' },
            { key: 'invoiceNumber', label: 'Number' },
            { key: 'invoiceDate', label: 'Date' },
            { key: 'dueDate', label: 'Due Date' },
            { key: 'totalAmount', label: 'Total', render: (r) => money(r.totalAmount) },
            { key: 'status', label: 'Status' },
          ]}
          actions={(row) => (
            <div className="d-flex flex-wrap gap-2">
              {row.status !== 'PAID' && <Link className="btn btn-sm btn-success" to={`/patient/pay-invoice/${row.invoiceId}`}>Pay</Link>}
              <button className="btn btn-sm btn-outline-primary" type="button" onClick={() => handleDownload(row.invoiceId)}>PDF</button>
              <button className="btn btn-sm btn-outline-secondary" type="button" onClick={() => handleEmail(row.invoiceId)}>Email</button>
            </div>
          )}
        />
      </div>
    </Layout>
  );
}
export default MyInvoices;
