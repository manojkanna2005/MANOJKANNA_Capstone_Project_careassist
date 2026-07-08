import { useEffect, useState } from 'react';
import Layout from '../common/Layout.jsx';
import Message from '../common/Message.jsx';
import Table from '../common/Table.jsx';
import { getUserId } from '../../utils/auth.js';
import { getCompanyByUserId } from '../../services/insuranceCompanyService.js';
import { createPlan, getPlansByCompanyId, activatePlan, deactivatePlan } from '../../services/insurancePlanService.js';
import { money } from '../../utils/date.js';

const empty = { companyId: '', planName: '', planDescription: '', coverageAmount: '', premiumAmount: '', validityMonths: 12, active: true };
function ManageInsurancePlans() {
  const [form, setForm] = useState(empty); const [plans, setPlans] = useState([]); const [message, setMessage] = useState(''); const [error, setError] = useState('');
  const load = async () => { try { const company = await getCompanyByUserId(getUserId()); setForm((f) => ({ ...f, companyId: company.companyId })); setPlans(await getPlansByCompanyId(company.companyId)); } catch (err) { setError(err.userMessage || 'Complete company profile before managing plans'); } };
  useEffect(() => { load(); }, []);
  const handleChange = (e) => setForm({ ...form, [e.target.name]: e.target.type === 'checkbox' ? e.target.checked : e.target.value });
  const handleSubmit = async (e) => { e.preventDefault(); setMessage(''); setError(''); try { await createPlan(form); setMessage('Plan created successfully.'); setForm({ ...empty, companyId: form.companyId }); load(); } catch (err) { setError(err.userMessage || 'Unable to create plan'); } };
  const toggle = async (plan) => { if (plan.active) await deactivatePlan(plan.planId); else await activatePlan(plan.planId); load(); };
  return <Layout title="Manage Insurance Plans"><Message type="success">{message}</Message><Message type="danger">{error}</Message><div className="card page-card p-4 mb-4"><h5>Add Plan</h5><form onSubmit={handleSubmit}><div className="row"><div className="col-md-4 mb-3"><label className="form-label">Company ID</label><input className="form-control" name="companyId" value={form.companyId} readOnly /></div><div className="col-md-4 mb-3"><label className="form-label">Plan Name</label><input className="form-control" name="planName" value={form.planName} onChange={handleChange} required /></div><div className="col-md-4 mb-3"><label className="form-label">Validity Months</label><input className="form-control" type="number" name="validityMonths" value={form.validityMonths} onChange={handleChange} required min="1" max="120" /></div></div><div className="mb-3"><label className="form-label">Description</label><textarea className="form-control" name="planDescription" value={form.planDescription} onChange={handleChange} required minLength="10" /></div><div className="row"><div className="col-md-6 mb-3"><label className="form-label">Coverage Amount</label><input className="form-control" type="number" name="coverageAmount" value={form.coverageAmount} onChange={handleChange} required min="1" /></div><div className="col-md-6 mb-3"><label className="form-label">Premium Amount</label><input className="form-control" type="number" name="premiumAmount" value={form.premiumAmount} onChange={handleChange} required min="1" /></div></div><button className="btn btn-primary" disabled={!form.companyId}>Add Plan</button></form></div><div className="card page-card p-4"><Table data={plans} columns={[{key:'planId',label:'ID'}, {key:'planName',label:'Name'}, {key:'coverageAmount',label:'Coverage',render:(r)=>money(r.coverageAmount)}, {key:'premiumAmount',label:'Premium',render:(r)=>money(r.premiumAmount)}, {key:'validityMonths',label:'Months'}, {key:'active',label:'Active', render:(r)=>r.active ? 'Yes' : 'No'}]} actions={(row)=><button className="btn btn-sm btn-outline-primary" onClick={()=>toggle(row)}>{row.active ? 'Deactivate' : 'Activate'}</button>} /></div></Layout>;
}
export default ManageInsurancePlans;
