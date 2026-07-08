import { useEffect, useState } from 'react';
import { Link } from 'react-router-dom';
import Layout from '../common/Layout.jsx';
import Message from '../common/Message.jsx';
import { getUserId } from '../../utils/auth.js';
import { getPatientByUserId } from '../../services/patientService.js';
import { getInvoicesByPatientId } from '../../services/invoiceService.js';
import { getClaimsByPatientId } from '../../services/claimService.js';

function PatientDashboard() {
  const [profile, setProfile] = useState(null);
  const [counts, setCounts] = useState({ invoices: 0, claims: 0 });
  const [error, setError] = useState('');

  useEffect(() => {
    async function load() {
      try {
        const patient = await getPatientByUserId(getUserId());
        setProfile(patient);
        const [invoices, claims] = await Promise.all([
          getInvoicesByPatientId(patient.patientId).catch(() => []),
          getClaimsByPatientId(patient.patientId).catch(() => []),
        ]);
        setCounts({ invoices: invoices.length, claims: claims.length });
      } catch {
        setError('Complete your patient profile first.');
      }
    }
    load();
  }, []);

  return (
    <Layout title="Patient Dashboard" subtitle="View insurance plans, invoices and claims.">
      <Message type="warning">{error}</Message>
      <div className="row g-3">
        <div className="col-md-4"><div className="card page-card p-4"><h5>Profile</h5><p>{profile ? profile.fullName : 'Not completed'}</p><Link to="/patient/profile" className="btn btn-primary">Open Profile</Link></div></div>
        <div className="col-md-4"><div className="card page-card p-4"><h5>Invoices</h5><p className="display-6">{counts.invoices}</p><Link to="/patient/invoices" className="btn btn-outline-primary">View Invoices</Link></div></div>
        <div className="col-md-4"><div className="card page-card p-4"><h5>Claims</h5><p className="display-6">{counts.claims}</p><Link to="/patient/claims" className="btn btn-outline-primary">View Claims</Link></div></div>
      </div>
    </Layout>
  );
}

export default PatientDashboard;
