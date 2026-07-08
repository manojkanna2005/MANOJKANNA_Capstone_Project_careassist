import { useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { logoutCurrentToken } from '../../services/tokenBlacklistService.js';

function Logout() {
  const navigate = useNavigate();

  useEffect(() => {
    async function doLogout() {
      await logoutCurrentToken();
      navigate('/login', { replace: true });
    }
    doLogout();
  }, [navigate]);

  return <div className="auth-page">Logging out...</div>;
}

export default Logout;
