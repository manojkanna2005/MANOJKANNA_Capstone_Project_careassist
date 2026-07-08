import { useEffect, useMemo, useState } from 'react';
import Layout from '../common/Layout.jsx';
import Message from '../common/Message.jsx';
import ClaimDocumentInput from '../common/ClaimDocumentInput.jsx';
import { submitClaimWithDocuments } from '../../services/claimService.js';
import { getPatientByUserId } from '../../services/patientService.js';
import { getInvoicesByPatientId } from '../../services/invoiceService.js';
import { getInsuranceHistoryByPatientId } from '../../services/patientInsuranceService.js';
import { getUserId } from '../../utils/auth.js';
import { today } from '../../utils/date.js';

const empty = {
  patientId: '',
  invoiceId: '',
  enrollmentId: '',
  companyId: '',
  diagnosis: '',
  treatment: '',
  dateOfService: today(),
  claimAmount: '',
};

function isCurrentlyActive(policy) {
  const current = today();
  return (
    String(policy.status).toUpperCase() === 'ACTIVE' &&
    policy.planActive !== false &&
    policy.enrollmentDate <= current &&
    policy.expiryDate >= current
  );
}

function SubmitClaim() {
  const [form, setForm] = useState(empty);
  const [invoices, setInvoices] = useState([]);
  const [policies, setPolicies] = useState([]);
  const [documents, setDocuments] = useState([]);
  const [documentKey, setDocumentKey] = useState(0);
  const [message, setMessage] = useState('');
  const [error, setError] = useState('');

  useEffect(() => {
    async function load() {
      try {
        const patient = await getPatientByUserId(getUserId());
        const [patientInvoices, insuranceHistory] = await Promise.all([
          getInvoicesByPatientId(patient.patientId).catch(() => []),
          getInsuranceHistoryByPatientId(patient.patientId).catch(() => []),
        ]);

        setInvoices(
          patientInvoices.filter(
            (invoice) => String(invoice.status).toUpperCase() !== 'CANCELLED',
          ),
        );
        setPolicies(insuranceHistory.filter(isCurrentlyActive));
        setForm((previous) => ({
          ...previous,
          patientId: patient.patientId,
        }));
      } catch (loadError) {
        setError(
          loadError.userMessage ||
            'Complete your patient profile and select an active insurance plan before submitting a claim.',
        );
      }
    }

    load();
  }, []);

  const selectedInvoice = useMemo(
    () => invoices.find((invoice) => String(invoice.invoiceId) === String(form.invoiceId)),
    [invoices, form.invoiceId],
  );

  const selectedPolicy = useMemo(
    () => policies.find((policy) => String(policy.enrollmentId) === String(form.enrollmentId)),
    [policies, form.enrollmentId],
  );

  const handleChange = (event) => {
    setForm({ ...form, [event.target.name]: event.target.value });
  };

  const handleInvoice = (event) => {
    const invoiceId = event.target.value;
    const invoice = invoices.find(
      (item) => String(item.invoiceId) === String(invoiceId),
    );
    setForm({
      ...form,
      invoiceId,
      claimAmount: invoice?.totalAmount || '',
    });
  };

  const handlePolicy = (event) => {
    const enrollmentId = event.target.value;
    const policy = policies.find(
      (item) => String(item.enrollmentId) === String(enrollmentId),
    );
    setForm({
      ...form,
      enrollmentId,
      companyId: policy?.companyId || '',
    });
  };

  const handleSubmit = async (event) => {
    event.preventDefault();
    setMessage('');
    setError('');

    try {
      await submitClaimWithDocuments(
        {
          ...form,
          patientId: Number(form.patientId),
          invoiceId: Number(form.invoiceId),
          enrollmentId: Number(form.enrollmentId),
          companyId: Number(form.companyId),
          claimAmount: Number(form.claimAmount),
          submissionDate: null,
          approvalDate: null,
          status: 'PENDING',
          rejectionReason: null,
        },
        documents,
      );

      setMessage('Claim and medical documents submitted successfully.');
      setForm({ ...empty, patientId: form.patientId });
      setDocuments([]);
      setDocumentKey((value) => value + 1);
    } catch (submitError) {
      setError(submitError.userMessage || 'Unable to submit claim.');
    }
  };

  return (
    <Layout
      title="Submit Claim"
      subtitle="Select an active policy, attach medical documents, and submit your claim."
    >
      <div className="card page-card p-4">
        <Message type="success">{message}</Message>
        <Message type="danger">{error}</Message>

        {policies.length === 0 && (
          <div className="alert alert-warning">
            No currently active insurance policy was found. Select or renew a plan first.
          </div>
        )}

        <form onSubmit={handleSubmit}>
          <div className="row">
            <div className="col-md-4 mb-3">
              <label className="form-label">Patient ID</label>
              <input
                className="form-control"
                name="patientId"
                value={form.patientId}
                readOnly
              />
            </div>

            <div className="col-md-4 mb-3">
              <label className="form-label">Invoice</label>
              <select
                className="form-select"
                name="invoiceId"
                value={form.invoiceId}
                onChange={handleInvoice}
                required
              >
                <option value="">Select invoice</option>
                {invoices.map((invoice) => (
                  <option key={invoice.invoiceId} value={invoice.invoiceId}>
                    {invoice.invoiceNumber} · {invoice.status} · ₹{invoice.totalAmount}
                  </option>
                ))}
              </select>
            </div>

            <div className="col-md-4 mb-3">
              <label className="form-label">Active Insurance Policy</label>
              <select
                className="form-select"
                name="enrollmentId"
                value={form.enrollmentId}
                onChange={handlePolicy}
                required
              >
                <option value="">Select active policy</option>
                {policies.map((policy) => (
                  <option key={policy.enrollmentId} value={policy.enrollmentId}>
                    {policy.planName} · {policy.companyName} · coverage ₹{policy.coverageAmount}
                  </option>
                ))}
              </select>
            </div>
          </div>

          <div className="row">
            <div className="col-md-6 mb-3">
              <label className="form-label">Insurance Company</label>
              <input
                className="form-control"
                name="companyId"
                value={selectedPolicy?.companyName || ''}
                readOnly
                required
              />
            </div>
            <div className="col-md-6 mb-3">
              <label className="form-label">Policy Period</label>
              <input
                className="form-control"
                value={
                  selectedPolicy
                    ? `${selectedPolicy.enrollmentDate} to ${selectedPolicy.expiryDate}`
                    : ''
                }
                readOnly
              />
            </div>
          </div>

          <div className="row">
            <div className="col-md-6 mb-3">
              <label className="form-label">Diagnosis</label>
              <input
                className="form-control"
                name="diagnosis"
                value={form.diagnosis}
                onChange={handleChange}
                minLength="3"
                maxLength="100"
                required
              />
            </div>
            <div className="col-md-6 mb-3">
              <label className="form-label">Treatment</label>
              <input
                className="form-control"
                name="treatment"
                value={form.treatment}
                onChange={handleChange}
                minLength="3"
                maxLength="100"
                required
              />
            </div>
          </div>

          <div className="row">
            <div className="col-md-6 mb-3">
              <label className="form-label">Date of Service</label>
              <input
                className="form-control"
                type="date"
                name="dateOfService"
                value={form.dateOfService}
                onChange={handleChange}
                min={selectedPolicy?.enrollmentDate || undefined}
                max={selectedPolicy ? (selectedPolicy.expiryDate < today() ? selectedPolicy.expiryDate : today()) : today()}
                required
              />
            </div>
            <div className="col-md-6 mb-3">
              <label className="form-label">Claim Amount</label>
              <input
                className="form-control"
                type="number"
                name="claimAmount"
                value={form.claimAmount}
                onChange={handleChange}
                min="1"
                max={
                  selectedInvoice && selectedPolicy
                    ? Math.min(
                        Number(selectedInvoice.totalAmount),
                        Number(selectedPolicy.coverageAmount),
                      )
                    : undefined
                }
                step="0.01"
                required
              />
              {selectedInvoice && selectedPolicy && (
                <div className="form-text">
                  Invoice total: ₹{selectedInvoice.totalAmount}; policy coverage: ₹
                  {selectedPolicy.coverageAmount}.
                </div>
              )}
            </div>
          </div>

          <ClaimDocumentInput
            key={documentKey}
            files={documents}
            onChange={(selected) => setDocuments(selected)}
          />

          <button
            className="btn btn-primary"
            disabled={!form.patientId || policies.length === 0}
          >
            Submit Claim
          </button>
        </form>
      </div>
    </Layout>
  );
}

export default SubmitClaim;
