import { useEffect, useState } from 'react';
import Layout from '../common/Layout.jsx';
import Table from '../common/Table.jsx';
import Message from '../common/Message.jsx';
import { getAllPatients, deletePatient } from '../../services/patientService.js';

function ManagePatients() {
  const [items, setItems] = useState([]); const [error, setError] = useState(''); const [message, setMessage] = useState('');
  const load = () => getAllPatients().then(setItems).catch((err)=>setError(err.userMessage || 'Unable to load patients'));
  useEffect(()=>{load();},[]);
  const remove = async (id) => { if (!confirm('Delete this patient profile?')) return; await deletePatient(id); setMessage('Patient deleted.'); load(); };
  return <Layout title="Manage Patients"><Message type="success">{message}</Message><Message type="danger">{error}</Message><div className="card page-card p-4"><Table data={items} columns={[{key:'patientId',label:'Patient ID'}, {key:'userId',label:'User ID'}, {key:'fullName',label:'Name'}, {key:'gender',label:'Gender'}, {key:'symptoms',label:'Symptoms'}, {key:'treatment',label:'Treatment'}]} actions={(row)=><button className="btn btn-sm btn-outline-danger" onClick={()=>remove(row.patientId)}>Delete</button>} /></div></Layout>;
}
export default ManagePatients;
