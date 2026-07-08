import { useEffect, useState } from 'react';
import Layout from '../common/Layout.jsx';
import Message from '../common/Message.jsx';
import { submitClaim } from '../../services/claimService.js';
import { getAllPatients } from '../../services/patientService.js';
import { getAllCompanies } from '../../services/insuranceCompanyService.js';
import { getInvoicesByPatientId } from '../../services/invoiceService.js';
import { nowDateTime, today } from '../../utils/date.js';

const empty = {
  patientId: '',
  invoiceId: '',
  companyId: '',
  diagnosis: '',
  treatment: '',
  dateOfService: today(),
  claimAmount: '',
  submissionDate: nowDateTime(),
  approvalDate: '',
  status: 'PENDING',
  rejectionReason: '',
};

function SubmitClaimForPatient() {
  const [form, setForm] = useState(empty);
  const [patients, setPatients] = useState([]);
  const [companies, setCompanies] = useState([]);
  const [invoices, setInvoices] = useState([]);
  const [message, setMessage] = useState('');
  const [error, setError] = useState('');

  useEffect(() => {
    async function load() {
      setError('');
      try {
        const [patientList, companyList] = await Promise.all([
          getAllPatients().catch(() => []),
          getAllCompanies().catch(() => []),
        ]);

        setPatients(patientList);
        setCompanies(companyList);
      } catch (err) {
        setError(err.userMessage || 'Unable to load patients and insurance companies');
      }
    }

    load();
  }, []);

  const handleChange = (e) => {
    setForm({ ...form, [e.target.name]: e.target.value });
  };

  const handlePatientChange = async (e) => {
    const patientId = e.target.value;
    setForm({ ...form, patientId, invoiceId: '', claimAmount: '' });
    setInvoices([]);

    if (!patientId) {
      return;
    }

    try {
      const patientInvoices = await getInvoicesByPatientId(patientId).catch(() => []);
      setInvoices(patientInvoices);
    } catch (err) {
      setError(err.userMessage || 'Unable to load invoices for selected patient');
    }
  };

  const handleInvoiceChange = (e) => {
    const invoiceId = e.target.value;
    const invoice = invoices.find((item) => String(item.invoiceId) === String(invoiceId));
    setForm({ ...form, invoiceId, claimAmount: invoice?.totalAmount || form.claimAmount });
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    setError('');
    setMessage('');

    try {
      await submitClaim({
        ...form,
        approvalDate: null,
        rejectionReason: null,
      });
      setMessage('Claim submitted for patient.');
      setForm({ ...empty });
      setInvoices([]);
    } catch (err) {
      setError(err.userMessage || 'Unable to submit claim');
    }
  };

  return (
    <Layout title="Submit Claim For Patient" subtitle="Select patient, invoice and insurance company.">
      <div className="card page-card p-4">
        <Message type="success">{message}</Message>
        <Message type="danger">{error}</Message>

        <form onSubmit={handleSubmit}>
          <div className="row">
            <div className="col-md-4 mb-3">
              <label className="form-label">Patient</label>
              <select className="form-select" name="patientId" value={form.patientId} onChange={handlePatientChange} required>
                <option value="">Select patient</option>
                {patients.map((patient) => (
                  <option key={patient.patientId} value={patient.patientId}>
                    {patient.fullName} (Patient ID: {patient.patientId})
                  </option>
                ))}
              </select>
            </div>

            <div className="col-md-4 mb-3">
              <label className="form-label">Invoice</label>
              <select className="form-select" name="invoiceId" value={form.invoiceId} onChange={handleInvoiceChange} required disabled={!form.patientId}>
                <option value="">Select invoice</option>
                {invoices.map((invoice) => (
                  <option key={invoice.invoiceId} value={invoice.invoiceId}>
                    {invoice.invoiceNumber} - {invoice.status} - ₹{invoice.totalAmount}
                  </option>
                ))}
              </select>
            </div>

            <div className="col-md-4 mb-3">
              <label className="form-label">Insurance Company</label>
              <select className="form-select" name="companyId" value={form.companyId} onChange={handleChange} required>
                <option value="">Select company</option>
                {companies.map((company) => (
                  <option key={company.companyId} value={company.companyId}>
                    {company.companyName} (Company ID: {company.companyId})
                  </option>
                ))}
              </select>
            </div>
          </div>

          <div className="row">
            <div className="col-md-6 mb-3">
              <label className="form-label">Diagnosis</label>
              <input className="form-control" name="diagnosis" value={form.diagnosis} onChange={handleChange} required />
            </div>
            <div className="col-md-6 mb-3">
              <label className="form-label">Treatment</label>
              <input className="form-control" name="treatment" value={form.treatment} onChange={handleChange} required />
            </div>
          </div>

          <div className="row">
            <div className="col-md-6 mb-3">
              <label className="form-label">Date of Service</label>
              <input className="form-control" type="date" name="dateOfService" value={form.dateOfService} onChange={handleChange} required />
            </div>
            <div className="col-md-6 mb-3">
              <label className="form-label">Claim Amount</label>
              <input className="form-control" type="number" name="claimAmount" value={form.claimAmount} onChange={handleChange} required min="1" />
            </div>
          </div>

          <button className="btn btn-primary" disabled={patients.length === 0 || companies.length === 0}>
            Submit Claim
          </button>
        </form>
      </div>
    </Layout>
  );
}

export default SubmitClaimForPatient;
