import { useEffect, useState } from 'react';
import Layout from '../common/Layout.jsx';
import Table from '../common/Table.jsx';
import Message from '../common/Message.jsx';
import { getPatientByUserId } from '../../services/patientService.js';
import { getInsuranceHistoryByPatientId, cancelInsurancePlan } from '../../services/patientInsuranceService.js';
import { getUserId } from '../../utils/auth.js';

function MyInsurance() {
  const [items, setItems] = useState([]);
  const [message, setMessage] = useState('');
  const [error, setError] = useState('');

  const load = async () => {
    try {
      const patient = await getPatientByUserId(getUserId());
      const data = await getInsuranceHistoryByPatientId(patient.patientId);
      setItems(data);
    } catch (err) {
      setError(err.userMessage || 'Unable to load insurance history');
    }
  };

  useEffect(() => { load(); }, []);

  const cancel = async (enrollmentId) => {
    await cancelInsurancePlan(enrollmentId);
    setMessage('Insurance plan cancelled.');
    load();
  };

  return (
    <Layout title="My Insurance" subtitle="View your selected insurance plans.">
      <Message type="success">{message}</Message>
      <Message type="danger">{error}</Message>
      <div className="card page-card p-4">
        <Table
          data={items}
          columns={[{ key: 'enrollmentId', label: 'Enrollment ID' }, { key: 'planId', label: 'Plan ID' }, { key: 'enrollmentDate', label: 'Start Date' }, { key: 'expiryDate', label: 'Expiry Date' }, { key: 'status', label: 'Status' }]}
          actions={(row) => row.status === 'ACTIVE' && <button className="btn btn-sm btn-outline-danger" onClick={() => cancel(row.enrollmentId)}>Cancel</button>}
        />
      </div>
    </Layout>
  );
}

export default MyInsurance;
