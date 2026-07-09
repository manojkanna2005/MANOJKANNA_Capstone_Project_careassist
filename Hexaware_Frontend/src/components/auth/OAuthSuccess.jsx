import { useEffect, useRef, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { getCurrentUser } from '../../services/authService.js';
import { clearAuth, normalizeRole, setAuth } from '../../utils/auth.js';
import Message from '../common/Message.jsx';

function OAuthSuccess() {
  const navigate = useNavigate();
  const started = useRef(false);
  const [error, setError] = useState('');

  useEffect(() => {
    if (started.current) return;
    started.current = true;

    const completeLogin = async () => {
      const hashParams = new URLSearchParams(
        window.location.hash.replace(/^#/, ''),
      );
      const token = hashParams.get('token');

      if (!token) {
        setError('GitHub login did not return a CareAssist token.');
        return;
      }

      try {
        localStorage.setItem('token', token);
        localStorage.setItem('tokenType', 'Bearer');

        const user = await getCurrentUser();

        setAuth({
          ...user,
          token,
          tokenType: 'Bearer',
        });

        window.history.replaceState(
          null,
          document.title,
          '/oauth-success',
        );

        const role = normalizeRole(user.role);

        if (role === 'ADMIN') {
          navigate('/admin/dashboard', { replace: true });
        } else if (role === 'PATIENT') {
          navigate('/patient/dashboard', { replace: true });
        } else if (role === 'PROVIDER') {
          navigate('/provider/dashboard', { replace: true });
        } else if (role === 'INSURANCE') {
          navigate('/insurance/dashboard', { replace: true });
        } else {
          navigate('/unauthorized', { replace: true });
        }
      } catch (err) {
        clearAuth();
        setError(err.userMessage || 'Unable to complete GitHub login.');
      }
    };

    completeLogin();
  }, [navigate]);

  return (
    <div className="auth-page">
      <div className="card auth-card p-4 text-center">
        <h2 className="brand-logo mb-3">CareAssist</h2>
        <Message type="danger">{error}</Message>
        {!error && <p className="mb-0">Completing GitHub login...</p>}
        {error && (
          <button
            type="button"
            className="btn btn-primary mt-3"
            onClick={() => navigate('/login', { replace: true })}
          >
            Return to login
          </button>
        )}
      </div>
    </div>
  );
}

export default OAuthSuccess;
