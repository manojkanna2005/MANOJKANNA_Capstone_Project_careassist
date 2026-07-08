import { useEffect, useState } from 'react';
import { Link } from 'react-router-dom';
import Layout from '../common/Layout.jsx';
import Table from '../common/Table.jsx';
import Message from '../common/Message.jsx';
import { getActivePlans } from '../../services/insurancePlanService.js';
import { money } from '../../utils/date.js';

function ViewInsurancePlans() {
  const [plans, setPlans] = useState([]);
  const [error, setError] = useState('');

  useEffect(() => {
    getActivePlans().then(setPlans).catch((err) => setError(err.userMessage || 'Unable to load insurance plans'));
  }, []);

  return (
    <Layout title="Available Insurance Plans" subtitle="Select a suitable insurance plan.">
      <Message type="danger">{error}</Message>
      <div className="card page-card p-4">
        <Table
          data={plans}
          columns={[
            { key: 'planId', label: 'Plan ID' },
            { key: 'planName', label: 'Plan Name' },
            { key: 'companyId', label: 'Company ID' },
            { key: 'coverageAmount', label: 'Coverage', render: (r) => money(r.coverageAmount) },
            { key: 'premiumAmount', label: 'Premium', render: (r) => money(r.premiumAmount) },
            { key: 'validityMonths', label: 'Validity Months' },
          ]}
          actions={(plan) => <Link className="btn btn-sm btn-primary" to={`/patient/select-plan/${plan.planId}`}>Select</Link>}
        />
      </div>
    </Layout>
  );
}

export default ViewInsurancePlans;
