import { useEffect, useState } from 'react';
import { useNavigate, useParams } from 'react-router-dom';
import Layout from '../common/Layout.jsx';
import Message from '../common/Message.jsx';
import { getInvoiceById, markInvoiceAsPaid } from '../../services/invoiceService.js';
import { money } from '../../utils/date.js';

function PayInvoice() {
  const { invoiceId } = useParams();
  const navigate = useNavigate();
  const [invoice, setInvoice] = useState(null);
  const [message, setMessage] = useState('');
  const [error, setError] = useState('');

  useEffect(() => { getInvoiceById(invoiceId).then(setInvoice).catch((err) => setError(err.userMessage || 'Unable to load invoice')); }, [invoiceId]);

  const pay = async () => {
    try {
      await markInvoiceAsPaid(invoiceId);
      setMessage('Invoice marked as paid.');
      setTimeout(() => navigate('/patient/invoices'), 1000);
    } catch (err) { setError(err.userMessage || 'Payment update failed'); }
  };

  return (
    <Layout title="Pay Invoice">
      <Message type="success">{message}</Message><Message type="danger">{error}</Message>
      <div className="card page-card p-4">
        {invoice && <><h5>{invoice.invoiceNumber}</h5><p>Total: {money(invoice.totalAmount)}</p><p>Status: {invoice.status}</p><button className="btn btn-success" onClick={pay} disabled={invoice.status === 'PAID'}>Mark as Paid</button></>}
      </div>
    </Layout>
  );
}

export default PayInvoice;
