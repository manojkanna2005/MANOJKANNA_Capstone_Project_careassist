import { useEffect, useState } from 'react';
import Layout from '../common/Layout.jsx';
import Table from '../common/Table.jsx';
import Message from '../common/Message.jsx';
import { getPatientByUserId } from '../../services/patientService.js';
import { getClaimsByPatientId } from '../../services/claimService.js';
import { getUserId } from '../../utils/auth.js';
import { money } from '../../utils/date.js';

function MyClaims() {
  const [claims, setClaims] = useState([]);
  const [error, setError] = useState('');
  useEffect(() => { async function load() { try { const patient = await getPatientByUserId(getUserId()); setClaims(await getClaimsByPatientId(patient.patientId)); } catch (err) { setError(err.userMessage || 'Unable to load claims'); } } load(); }, []);
  return <Layout title="My Claims"><Message type="danger">{error}</Message><div className="card page-card p-4"><Table data={claims} columns={[{key:'claimId',label:'Claim ID'}, {key:'invoiceId',label:'Invoice ID'}, {key:'claimAmount',label:'Amount', render:(r)=>money(r.claimAmount)}, {key:'submissionDate',label:'Submitted'}, {key:'approvalDate',label:'Approved'}, {key:'status',label:'Status'}, {key:'rejectionReason',label:'Reason'}]} /></div></Layout>;
}
export default MyClaims;
