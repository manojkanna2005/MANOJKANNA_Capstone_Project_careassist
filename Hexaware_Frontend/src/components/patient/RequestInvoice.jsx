import { useEffect, useState } from 'react';
import Layout from '../common/Layout.jsx';
import Message from '../common/Message.jsx';
import { createNotification } from '../../services/emailNotificationService.js';
import { getPatientByUserId } from '../../services/patientService.js';
import { getAllProviders } from '../../services/healthcareProviderService.js';
import { getUserId } from '../../utils/auth.js';
import { nowDateTime } from '../../utils/date.js';

function RequestInvoice() {
  const [patient, setPatient] = useState(null);
  const [providers, setProviders] = useState([]);
  const [form, setForm] = useState({
    providerUserId: '',
    message: 'Please generate invoice for my recent treatment.',
  });
  const [success, setSuccess] = useState('');
  const [error, setError] = useState('');

  useEffect(() => {
    async function load() {
      setError('');
      try {
        const [patientData, providerData] = await Promise.all([
          getPatientByUserId(getUserId()),
          getAllProviders().catch(() => []),
        ]);

        setPatient(patientData);
        setProviders(providerData);
      } catch (err) {
        setError(err.userMessage || 'Complete your patient profile first.');
      }
    }

    load();
  }, []);

  const handleChange = (e) => {
    setForm({
      ...form,
      [e.target.name]: e.target.value,
    });
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    setSuccess('');
    setError('');

    try {
      await createNotification({
        userId: Number(form.providerUserId),
        subject: `Invoice request from patient ${patient?.patientId}`,
        message: `${form.message} Patient ID: ${patient?.patientId}`,
        sentAt: nowDateTime(),
        status: 'PENDING',
      });

      setSuccess('Invoice request notification sent to provider.');
    } catch (err) {
      setError(err.userMessage || 'Unable to request invoice');
    }
  };

  return (
    <Layout title="Request Invoice" subtitle="Choose a healthcare provider and send an invoice request.">
      <div className="card page-card p-4">
        <Message type="success">{success}</Message>
        <Message type="danger">{error}</Message>

        <form onSubmit={handleSubmit}>
          <div className="mb-3">
            <label className="form-label">Healthcare Provider</label>
            <select
              className="form-select"
              name="providerUserId"
              value={form.providerUserId}
              onChange={handleChange}
              required
            >
              <option value="">Select provider</option>
              {providers.map((provider) => (
                <option key={provider.providerId} value={provider.userId}>
                  {provider.providerName} - {provider.specialization} (Provider ID: {provider.providerId})
                </option>
              ))}
            </select>
            <small className="text-muted">
              The request will be sent to the selected provider account.
            </small>
          </div>

          <div className="mb-3">
            <label className="form-label">Message</label>
            <textarea
              className="form-control"
              name="message"
              value={form.message}
              onChange={handleChange}
              required
              minLength="10"
            />
          </div>

          <button className="btn btn-primary" disabled={!patient || providers.length === 0}>
            Send Request
          </button>
        </form>
      </div>
    </Layout>
  );
}

export default RequestInvoice;
