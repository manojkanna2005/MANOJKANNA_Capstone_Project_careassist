import { useEffect, useState } from 'react';
import Layout from '../common/Layout.jsx';
import Message from '../common/Message.jsx';
import { createNotification } from '../../services/emailNotificationService.js';
import { getAllPatients } from '../../services/patientService.js';
import { nowDateTime } from '../../utils/date.js';

function NotifyPatient() {
  const [patients, setPatients] = useState([]);
  const [form, setForm] = useState({
    userId: '',
    subject: 'Invoice generated',
    message: 'Your invoice has been generated. Please login and check your invoice.',
    sentAt: nowDateTime(),
    status: 'PENDING',
  });
  const [message, setMessage] = useState('');
  const [error, setError] = useState('');

  useEffect(() => {
    getAllPatients()
      .then(setPatients)
      .catch((err) => setError(err.userMessage || 'Unable to load patients'));
  }, []);

  const handleChange = (e) => {
    setForm({ ...form, [e.target.name]: e.target.value });
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    setMessage('');
    setError('');

    try {
      await createNotification({
        ...form,
        userId: Number(form.userId),
      });
      setMessage('Notification created for patient.');
    } catch (err) {
      setError(err.userMessage || 'Unable to notify patient');
    }
  };

  return (
    <Layout title="Notify Patient" subtitle="Select a patient and send invoice notification.">
      <div className="card page-card p-4">
        <Message type="success">{message}</Message>
        <Message type="danger">{error}</Message>

        <form onSubmit={handleSubmit}>
          <div className="mb-3">
            <label className="form-label">Patient</label>
            <select className="form-select" name="userId" value={form.userId} onChange={handleChange} required>
              <option value="">Select patient</option>
              {patients.map((patient) => (
                <option key={patient.patientId} value={patient.userId}>
                  {patient.fullName} (Patient ID: {patient.patientId})
                </option>
              ))}
            </select>
          </div>

          <div className="mb-3">
            <label className="form-label">Subject</label>
            <input className="form-control" name="subject" value={form.subject} onChange={handleChange} required />
          </div>

          <div className="mb-3">
            <label className="form-label">Message</label>
            <textarea className="form-control" name="message" value={form.message} onChange={handleChange} required minLength="10" />
          </div>

          <button className="btn btn-primary" disabled={patients.length === 0}>Send Notification</button>
        </form>
      </div>
    </Layout>
  );
}

export default NotifyPatient;
