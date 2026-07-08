import { useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { login } from '../../services/authService.js';
import Message from '../common/Message.jsx';
import { normalizeRole } from '../../utils/auth.js';

function Login() {
  const navigate = useNavigate();
  const [form, setForm] = useState({ email: '', password: '' });
  const [error, setError] = useState('');
  const [loading, setLoading] = useState(false);

  const handleChange = (e) => setForm({ ...form, [e.target.name]: e.target.value });

  const handleSubmit = async (e) => {
    e.preventDefault();
    setError('');
    setLoading(true);
    try {
      const data = await login(form);
      const role = normalizeRole(data.role);
      if (role === 'ADMIN') navigate('/admin/dashboard', { replace: true });
      else if (role === 'PATIENT') navigate('/patient/dashboard', { replace: true });
      else if (role === 'PROVIDER') navigate('/provider/dashboard', { replace: true });
      else if (role === 'INSURANCE') navigate('/insurance/dashboard', { replace: true });
      else navigate('/unauthorized', { replace: true });
    } catch (err) {
      setError(err.userMessage || 'Invalid email or password');
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="auth-page">
      <div className="card auth-card p-4">
        <h2 className="brand-logo text-center mb-2">CareAssist</h2>
        <p className="text-center text-muted">Medical Billing and Claims Management</p>
        <Message type="danger">{error}</Message>
        <form onSubmit={handleSubmit}>
          <div className="mb-3">
            <label className="form-label">Email</label>
            <input className="form-control" name="email" type="email" value={form.email} onChange={handleChange} required />
          </div>
          <div className="mb-3">
            <label className="form-label">Password</label>
            <input className="form-control" name="password" type="password" value={form.password} onChange={handleChange} required />
          </div>
          <button className="btn btn-primary w-100" disabled={loading}>{loading ? 'Logging in...' : 'Login'}</button>
        </form>
        <div className="text-center mt-3 d-grid gap-2">
          <Link to="/forgot-password">Forgot Password?</Link>
          <Link to="/register">New user? Register here</Link>
        </div>
      </div>
    </div>
  );
}

export default Login;
