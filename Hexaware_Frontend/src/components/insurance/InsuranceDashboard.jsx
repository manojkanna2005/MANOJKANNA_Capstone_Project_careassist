import { useEffect, useState } from 'react';
import { Link } from 'react-router-dom';
import Layout from '../common/Layout.jsx';
import Message from '../common/Message.jsx';
import { getUserId } from '../../utils/auth.js';
import { getCompanyByUserId } from '../../services/insuranceCompanyService.js';
import { getClaimsByInsuranceCompanyId } from '../../services/claimService.js';

function InsuranceDashboard() {
  const [company, setCompany] = useState(null); const [claimCount, setClaimCount] = useState(0); const [error, setError] = useState('');
  useEffect(() => { async function load() { try { const c = await getCompanyByUserId(getUserId()); setCompany(c); const claims = await getClaimsByInsuranceCompanyId(c.companyId).catch(() => []); setClaimCount(claims.length); } catch { setError('Complete your insurance company profile first.'); } } load(); }, []);
  return <Layout title="Insurance Dashboard" subtitle="Review claims and process payments."><Message type="warning">{error}</Message><div className="row g-3"><div className="col-md-4"><div className="card page-card p-4"><h5>Profile</h5><p>{company?.companyName || 'Not completed'}</p><Link to="/insurance/profile" className="btn btn-primary">Open Profile</Link></div></div><div className="col-md-4"><div className="card page-card p-4"><h5>Incoming Claims</h5><p className="display-6">{claimCount}</p><Link to="/insurance/incoming-claims" className="btn btn-outline-primary">Review Claims</Link></div></div><div className="col-md-4"><div className="card page-card p-4"><h5>Plans</h5><p>Manage company plans.</p><Link to="/insurance/plans" className="btn btn-outline-primary">Manage Plans</Link></div></div></div></Layout>;
}
export default InsuranceDashboard;
