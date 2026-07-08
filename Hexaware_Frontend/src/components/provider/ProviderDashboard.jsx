import { useEffect, useState } from 'react';
import { Link } from 'react-router-dom';
import Layout from '../common/Layout.jsx';
import Message from '../common/Message.jsx';
import { getUserId } from '../../utils/auth.js';
import { getProviderByUserId } from '../../services/healthcareProviderService.js';
import { getInvoicesByProviderId } from '../../services/invoiceService.js';

function ProviderDashboard() {
  const [provider, setProvider] = useState(null);
  const [count, setCount] = useState(0);
  const [error, setError] = useState('');
  useEffect(() => { async function load() { try { const p = await getProviderByUserId(getUserId()); setProvider(p); const inv = await getInvoicesByProviderId(p.providerId).catch(() => []); setCount(inv.length); } catch { setError('Complete your provider profile first.'); } } load(); }, []);
  return <Layout title="Provider Dashboard" subtitle="Generate invoices and notify patients."><Message type="warning">{error}</Message><div className="row g-3"><div className="col-md-4"><div className="card page-card p-4"><h5>Profile</h5><p>{provider?.providerName || 'Not completed'}</p><Link to="/provider/profile" className="btn btn-primary">Open Profile</Link></div></div><div className="col-md-4"><div className="card page-card p-4"><h5>Invoices</h5><p className="display-6">{count}</p><Link to="/provider/invoices" className="btn btn-outline-primary">View Invoices</Link></div></div><div className="col-md-4"><div className="card page-card p-4"><h5>Create Invoice</h5><p>Generate bill for patient.</p><Link to="/provider/generate-invoice" className="btn btn-outline-primary">Generate</Link></div></div></div></Layout>;
}
export default ProviderDashboard;
