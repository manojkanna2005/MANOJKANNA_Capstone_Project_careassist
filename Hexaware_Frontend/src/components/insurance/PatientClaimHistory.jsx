import { useEffect, useState } from 'react';
import Layout from '../common/Layout.jsx';
import Table from '../common/Table.jsx';
import Message from '../common/Message.jsx';
import { getCompanyByUserId } from '../../services/insuranceCompanyService.js';
import { getClaimsByInsuranceCompanyId } from '../../services/claimService.js';
import { getAllPatients } from '../../services/patientService.js';
import { getUserId } from '../../utils/auth.js';
import { money } from '../../utils/date.js';

function PatientClaimHistory() {
  const [claims, setClaims] = useState([]);
  const [patients, setPatients] = useState([]);
  const [patientId, setPatientId] = useState('');
  const [error, setError] = useState('');

  useEffect(() => {
    async function load() {
      try {
        const company = await getCompanyByUserId(getUserId());
        const [companyClaims, patientList] = await Promise.all([
          getClaimsByInsuranceCompanyId(company.companyId),
          getAllPatients(),
        ]);
        setClaims(companyClaims);
        setPatients(patientList);
      } catch (loadError) {
        setError(loadError.userMessage || 'Unable to load claim history.');
      }
    }
    load();
  }, []);

  const filtered = patientId
    ? claims.filter((claim) => String(claim.patientId) === String(patientId))
    : claims;

  return (
    <Layout title="Patient Claim History">
      <Message type="danger">{error}</Message>
      <div className="card page-card p-4">
        <div className="mb-3">
          <label className="form-label">Patient</label>
          <select className="form-select" value={patientId} onChange={(event) => setPatientId(event.target.value)}>
            <option value="">All patients</option>
            {patients.map((patient) => (
              <option key={patient.patientId} value={patient.patientId}>
                {patient.fullName} (Patient ID: {patient.patientId})
              </option>
            ))}
          </select>
        </div>

        <Table
          data={filtered}
          columns={[
            { key: 'claimId', label: 'Claim' },
            { key: 'patientName', label: 'Patient Name' },
            { key: 'invoiceNumber', label: 'Invoice' },
            { key: 'invoiceAmount', label: 'Invoice Amount', render: (row) => money(row.invoiceAmount) },
            { key: 'claimAmount', label: 'Claim Amount', render: (row) => money(row.claimAmount) },
            { key: 'submissionDate', label: 'Submitted' },
            { key: 'approvalDate', label: 'Approved' },
            { key: 'status', label: 'Status' },
            { key: 'rejectionReason', label: 'Rejection Reason' },
          ]}
        />
      </div>
    </Layout>
  );
}

export default PatientClaimHistory;
