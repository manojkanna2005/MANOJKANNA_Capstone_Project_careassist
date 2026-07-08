import { useEffect, useMemo, useState } from 'react';
import Layout from '../common/Layout.jsx';
import Message from '../common/Message.jsx';
import { getProviderByUserId } from '../../services/healthcareProviderService.js';
import { getAllPatients } from '../../services/patientService.js';
import { generateInvoice } from '../../services/invoiceService.js';
import { getUserId } from '../../utils/auth.js';
import { nowDateTime, today } from '../../utils/date.js';

const base = {
  invoiceNumber: '',
  providerId: '',
  patientId: '',
  invoiceDate: today(),
  dueDate: today(),
  consultationFee: 0,
  diagnosticTestsFee: 0,
  diagnosticScanFee: 0,
  medicationsFee: 0,
  taxPercentage: 8,
  status: 'PENDING',
  createdAt: nowDateTime(),
};

function GenerateInvoice() {
  const [form, setForm] = useState(base);
  const [patients, setPatients] = useState([]);
  const [message, setMessage] = useState('');
  const [error, setError] = useState('');

  useEffect(() => {
    async function load() {
      setError('');
      try {
        const [provider, patientList] = await Promise.all([
          getProviderByUserId(getUserId()),
          getAllPatients().catch(() => []),
        ]);

        setPatients(patientList);
        setForm((f) => ({
          ...f,
          providerId: provider.providerId,
          invoiceNumber: `INV-${Date.now()}`,
        }));
      } catch (err) {
        setError(err.userMessage || 'Complete your provider profile first.');
      }
    }

    load();
  }, []);

  const billAmount = useMemo(
    () => ['consultationFee', 'diagnosticTestsFee', 'diagnosticScanFee', 'medicationsFee']
      .reduce((sum, key) => sum + Number(form[key] || 0), 0),
    [form],
  );
  const taxAmount = useMemo(
    () => billAmount * Number(form.taxPercentage || 0) / 100,
    [billAmount, form.taxPercentage],
  );
  const totalAmount = useMemo(() => billAmount + taxAmount, [billAmount, taxAmount]);

  const handleChange = (e) => {
    setForm({ ...form, [e.target.name]: e.target.value });
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    setError('');
    setMessage('');

    try {
      await generateInvoice({ ...form, taxAmount, totalAmount });
      setMessage('Invoice generated successfully.');
      setForm({
        ...base,
        providerId: form.providerId,
        invoiceNumber: `INV-${Date.now()}`,
      });
    } catch (err) {
      setError(err.userMessage || 'Unable to generate invoice');
    }
  };

  return (
    <Layout title="Generate Invoice" subtitle="Select a patient and enter service charges.">
      <div className="card page-card p-4">
        <Message type="success">{message}</Message>
        <Message type="danger">{error}</Message>

        <form onSubmit={handleSubmit}>
          <div className="row">
            <div className="col-md-4 mb-3">
              <label className="form-label">Invoice Number</label>
              <input className="form-control" name="invoiceNumber" value={form.invoiceNumber} onChange={handleChange} required />
            </div>

            <div className="col-md-4 mb-3">
              <label className="form-label">Provider ID</label>
              <input className="form-control" name="providerId" value={form.providerId} readOnly />
            </div>

            <div className="col-md-4 mb-3">
              <label className="form-label">Patient</label>
              <select className="form-select" name="patientId" value={form.patientId} onChange={handleChange} required>
                <option value="">Select patient</option>
                {patients.map((patient) => (
                  <option key={patient.patientId} value={patient.patientId}>
                    {patient.fullName} (Patient ID: {patient.patientId})
                  </option>
                ))}
              </select>
            </div>
          </div>

          <div className="row">
            <div className="col-md-6 mb-3">
              <label className="form-label">Invoice Date</label>
              <input className="form-control" type="date" name="invoiceDate" value={form.invoiceDate} onChange={handleChange} required />
            </div>
            <div className="col-md-6 mb-3">
              <label className="form-label">Due Date</label>
              <input className="form-control" type="date" name="dueDate" value={form.dueDate} onChange={handleChange} required />
            </div>
          </div>

          <div className="row">
            {['consultationFee', 'diagnosticTestsFee', 'diagnosticScanFee', 'medicationsFee'].map((name) => (
              <div className="col-md-3 mb-3" key={name}>
                <label className="form-label">{name}</label>
                <input className="form-control" type="number" name={name} value={form[name]} onChange={handleChange} min="0" required />
              </div>
            ))}
          </div>

          <div className="row">
            <div className="col-md-4 mb-3">
              <label className="form-label">Tax %</label>
              <input className="form-control" type="number" name="taxPercentage" value={form.taxPercentage} onChange={handleChange} min="0" max="100" required />
            </div>
            <div className="col-md-4 mb-3">
              <label className="form-label">Tax Amount</label>
              <input className="form-control" value={taxAmount.toFixed(2)} readOnly />
            </div>
            <div className="col-md-4 mb-3">
              <label className="form-label">Total Amount</label>
              <input className="form-control" value={totalAmount.toFixed(2)} readOnly />
            </div>
          </div>

          <button className="btn btn-primary" disabled={!form.providerId || patients.length === 0}>
            Generate Invoice
          </button>
        </form>
      </div>
    </Layout>
  );
}

export default GenerateInvoice;
