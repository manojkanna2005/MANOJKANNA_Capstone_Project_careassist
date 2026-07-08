import { useEffect, useState } from 'react';
import { Link } from 'react-router-dom';
import Layout from '../common/Layout.jsx';
import Table from '../common/Table.jsx';
import Message from '../common/Message.jsx';
import { getCompanyByUserId } from '../../services/insuranceCompanyService.js';
import { getClaimsByInsuranceCompanyId } from '../../services/claimService.js';
import { getUserId } from '../../utils/auth.js';
import { money } from '../../utils/date.js';

function IncomingClaims() {
  const [items, setItems] = useState([]); const [error, setError] = useState('');
  useEffect(() => { async function load() { try { const c = await getCompanyByUserId(getUserId()); setItems(await getClaimsByInsuranceCompanyId(c.companyId)); } catch (err) { setError(err.userMessage || 'Unable to load claims'); } } load(); }, []);
  return <Layout title="Incoming Claims"><Message type="danger">{error}</Message><div className="card page-card p-4"><Table data={items} columns={[{key:'claimId',label:'ID'}, {key:'patientId',label:'Patient'}, {key:'invoiceId',label:'Invoice'}, {key:'claimAmount',label:'Amount',render:(r)=>money(r.claimAmount)}, {key:'status',label:'Status'}]} actions={(row)=><div className="d-flex gap-2"><Link className="btn btn-sm btn-outline-primary" to={`/insurance/review-claim/${row.claimId}`}>Review</Link><Link className="btn btn-sm btn-primary" to={`/insurance/claim-decision/${row.claimId}`}>Decision</Link></div>} /></div></Layout>;
}
export default IncomingClaims;
