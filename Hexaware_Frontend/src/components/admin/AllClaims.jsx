import { useEffect, useState } from 'react';
import Layout from '../common/Layout.jsx';
import Table from '../common/Table.jsx';
import Message from '../common/Message.jsx';
import { getAllClaims, approveClaim, rejectClaim, deleteClaim } from '../../services/claimService.js';
import { money } from '../../utils/date.js';

function AllClaims() {
  const [items, setItems] = useState([]); const [message, setMessage] = useState(''); const [error, setError] = useState('');
  const load = () => getAllClaims().then(setItems).catch((err)=>setError(err.userMessage || 'Unable to load claims'));
  useEffect(()=>{load();},[]);
  const approve = async (id) => { await approveClaim(id); setMessage('Claim approved.'); load(); };
  const reject = async (id) => { const reason = prompt('Rejection reason'); if (!reason) return; await rejectClaim(id, reason); setMessage('Claim rejected.'); load(); };
  const remove = async (id) => { if (!confirm('Delete claim?')) return; await deleteClaim(id); setMessage('Claim deleted.'); load(); };
  return <Layout title="All Claims"><Message type="success">{message}</Message><Message type="danger">{error}</Message><div className="card page-card p-4"><Table data={items} columns={[{key:'claimId',label:'ID'}, {key:'patientId',label:'Patient'}, {key:'companyId',label:'Company'}, {key:'invoiceId',label:'Invoice'}, {key:'claimAmount',label:'Amount',render:(r)=>money(r.claimAmount)}, {key:'status',label:'Status'}]} actions={(row)=><div className="d-flex gap-2 flex-wrap"><button className="btn btn-sm btn-success" onClick={()=>approve(row.claimId)}>Approve</button><button className="btn btn-sm btn-warning" onClick={()=>reject(row.claimId)}>Reject</button><button className="btn btn-sm btn-outline-danger" onClick={()=>remove(row.claimId)}>Delete</button></div>} /></div></Layout>;
}
export default AllClaims;
