import { Navigate } from 'react-router-dom';
import { getRole, isLoggedIn, normalizeRole } from '../../utils/auth.js';

function ProtectedRoute({ allowedRoles = [], children }) {
  if (!isLoggedIn()) {
    return <Navigate to="/login" replace />;
  }

  const currentRole = getRole();
  const normalizedAllowed = allowedRoles.map(normalizeRole);

  if (normalizedAllowed.length > 0 && !normalizedAllowed.includes(currentRole)) {
    return <Navigate to="/unauthorized" replace />;
  }

  return children;
}

export default ProtectedRoute;
