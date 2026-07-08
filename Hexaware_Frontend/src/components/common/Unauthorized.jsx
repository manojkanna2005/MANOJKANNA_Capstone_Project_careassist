import { Link } from 'react-router-dom';
import { getRole } from '../../utils/auth.js';

function Unauthorized() {
  const role = getRole();
  const home = role ? `/${role.toLowerCase()}/dashboard` : '/login';

  return (
    <div className="auth-page">
      <div className="card auth-card p-4 text-center">
        <h2 className="text-danger">Unauthorized</h2>
        <p className="text-muted">You do not have permission to open this page.</p>
        <Link className="btn btn-primary" to={home}>Go Back</Link>
      </div>
    </div>
  );
}

export default Unauthorized;
