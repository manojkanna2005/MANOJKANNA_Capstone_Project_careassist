import { useEffect, useState } from 'react';
import { useNavigate, useParams } from 'react-router-dom';
import Layout from '../common/Layout.jsx';
import Message from '../common/Message.jsx';
import { getPlanById } from '../../services/insurancePlanService.js';
import { selectInsurancePlan } from '../../services/patientInsuranceService.js';
import { getPatientByUserId } from '../../services/patientService.js';
import { getUserId } from '../../utils/auth.js';
import { addMonths, today, money } from '../../utils/date.js';

function SelectInsurancePlan() {
  const { planId } = useParams();
  const navigate = useNavigate();
  const [plan, setPlan] = useState(null);
  const [form, setForm] = useState({ patientId: '', planId, enrollmentDate: today(), expiryDate: '', status: 'ACTIVE' });
  const [message, setMessage] = useState('');
  const [error, setError] = useState('');

  useEffect(() => {
    async function load() {
      try {
        const [planData, patient] = await Promise.all([getPlanById(planId), getPatientByUserId(getUserId())]);
        setPlan(planData);
        const enrollmentDate = today();
        setForm({ patientId: patient.patientId, planId: planData.planId, enrollmentDate, expiryDate: addMonths(enrollmentDate, planData.validityMonths), status: 'ACTIVE' });
      } catch (err) {
        setError(err.userMessage || 'Complete your profile before selecting a plan.');
      }
    }
    load();
  }, [planId]);

  const handleSubmit = async (e) => {
    e.preventDefault();
    setError('');

    if (!plan?.active) {
      setError('This insurance plan is inactive and cannot be selected.');
      return;
    }
    if (!form.patientId || Number(form.patientId) <= 0 || !form.planId || Number(form.planId) <= 0) {
      setError('A valid patient and insurance plan are required.');
      return;
    }
    if (!form.enrollmentDate || !form.expiryDate || form.expiryDate <= form.enrollmentDate) {
      setError('The insurance coverage dates are invalid.');
      return;
    }

    try {
      await selectInsurancePlan({
        ...form,
        patientId: Number(form.patientId),
        planId: Number(form.planId),
        status: 'ACTIVE',
      });
      setMessage('Insurance plan selected successfully.');
      setTimeout(() => navigate('/patient/my-insurance'), 1000);
    } catch (err) {
      setError(err.userMessage || 'Unable to select plan');
    }
  };

  return (
    <Layout title="Select Insurance Plan">
      <Message type="success">{message}</Message>
      <Message type="danger">{error}</Message>
      <div className="card page-card p-4">
        {plan && <p><strong>{plan.planName}</strong> - Coverage {money(plan.coverageAmount)} / Premium {money(plan.premiumAmount)}</p>}
        <form onSubmit={handleSubmit}>
          <div className="row">
            <div className="col-md-4 mb-3"><label className="form-label">Patient ID</label><input className="form-control" name="patientId" value={form.patientId} readOnly /></div>
            <div className="col-md-4 mb-3"><label className="form-label">Plan ID</label><input className="form-control" name="planId" value={form.planId} readOnly /></div>
            <div className="col-md-4 mb-3"><label className="form-label">Status</label><input className="form-control" name="status" value="ACTIVE" readOnly /></div>
          </div>
          <div className="row">
            <div className="col-md-6 mb-3"><label className="form-label">Enrollment Date</label><input className="form-control" type="date" name="enrollmentDate" value={form.enrollmentDate} readOnly required /></div>
            <div className="col-md-6 mb-3"><label className="form-label">Expiry Date</label><input className="form-control" type="date" name="expiryDate" value={form.expiryDate} readOnly required /></div>
          </div>
          <button className="btn btn-primary" disabled={!form.patientId}>Confirm Selection</button>
        </form>
      </div>
    </Layout>
  );
}

export default SelectInsurancePlan;
