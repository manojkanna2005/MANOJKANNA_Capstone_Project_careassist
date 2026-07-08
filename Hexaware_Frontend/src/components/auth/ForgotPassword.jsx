import { useState } from 'react';
import { Link } from 'react-router-dom';
import { forgotPassword } from '../../services/authService.js';
import Message from '../common/Message.jsx';

function ForgotPassword() {
  const [email, setEmail] = useState('');
  const [message, setMessage] = useState('');
  const [error, setError] = useState('');
  const [loading, setLoading] = useState(false);

  const handleSubmit = async (e) => {
    e.preventDefault();
    setMessage('');
    setError('');
    setLoading(true);

    try {
      const response = await forgotPassword(email);
      setMessage(response || 'Temporary password sent to your registered email.');
    } catch (err) {
      setError(err.userMessage || 'Unable to process forgot password request');
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="auth-page">
      <div className="card auth-card p-4">
        <h2 className="brand-logo text-center mb-2">Forgot Password</h2>
        <p className="text-center text-muted">Enter your registered email to receive a temporary password.</p>
        <Message type="success">{message}</Message>
        <Message type="danger">{error}</Message>
        <form onSubmit={handleSubmit}>
          <div className="mb-3">
            <label className="form-label">Email</label>
            <input className="form-control" type="email" value={email} onChange={(e) => setEmail(e.target.value)} required />
          </div>
          <button className="btn btn-primary w-100" disabled={loading}>{loading ? 'Sending...' : 'Send Temporary Password'}</button>
        </form>
        <div className="text-center mt-3">
          <Link to="/login">Back to login</Link>
        </div>
      </div>
    </div>
  );
}

export default ForgotPassword;
