import { useEffect, useState } from 'react';
import Layout from '../common/Layout.jsx';
import Table from '../common/Table.jsx';
import Message from '../common/Message.jsx';
import { getAllPayments, deletePayment } from '../../services/claimPaymentService.js';
import { money } from '../../utils/date.js';

function AllPayments() {
  const [items, setItems] = useState([]); const [message, setMessage] = useState(''); const [error, setError] = useState('');
  const load = () => getAllPayments().then(setItems).catch((err)=>setError(err.userMessage || 'Unable to load payments'));
  useEffect(()=>{load();},[]);
  const remove = async (id) => { if (!confirm('Delete payment?')) return; await deletePayment(id); setMessage('Payment deleted.'); load(); };
  return <Layout title="All Payments"><Message type="success">{message}</Message><Message type="danger">{error}</Message><div className="card page-card p-4"><Table data={items} columns={[{key:'paymentId',label:'Payment ID'}, {key:'claimId',label:'Claim ID'}, {key:'paymentDate',label:'Date'}, {key:'paymentAmount',label:'Amount',render:(r)=>money(r.paymentAmount)}, {key:'paymentMode',label:'Mode'}, {key:'transactionReference',label:'Reference'}]} actions={(row)=><button className="btn btn-sm btn-outline-danger" onClick={()=>remove(row.paymentId)}>Delete</button>} /></div></Layout>;
}
export default AllPayments;
