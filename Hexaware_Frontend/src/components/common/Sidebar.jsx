import { useState } from "react";
import { NavLink, useNavigate } from "react-router-dom";
import { getAuth } from "../../utils/auth.js";
import { logoutCurrentToken } from "../../services/tokenBlacklistService.js";
import ProfileImage from "./ProfileImage.jsx";
const menus = {
  PATIENT: [
    ["Dashboard", "/patient/dashboard"],
    ["Profile", "/patient/profile"],
    ["Account Settings", "/account/settings"],
    ["Insurance Plans", "/patient/insurance-plans"],
    ["My Insurance", "/patient/my-insurance"],
    ["Request Invoice", "/patient/request-invoice"],
    ["My Invoices", "/patient/invoices"],
    ["Submit Claim", "/patient/submit-claim"],
    ["My Claims", "/patient/claims"],
    ["Claim Status", "/patient/claim-status"],
  ],
  PROVIDER: [
    ["Dashboard", "/provider/dashboard"],
    ["Profile", "/provider/profile"],
    ["Account Settings", "/account/settings"],
    ["Patient Requests", "/provider/patient-requests"],
    ["Generate Invoice", "/provider/generate-invoice"],
    ["Provider Invoices", "/provider/invoices"],
    ["Notify Patient", "/provider/notify-patient"],
    ["Submit Claim", "/provider/submit-claim"],
  ],
  INSURANCE: [
    ["Dashboard", "/insurance/dashboard"],
    ["Profile", "/insurance/profile"],
    ["Account Settings", "/account/settings"],
    ["Manage Plans", "/insurance/plans"],
    ["Incoming Claims", "/insurance/incoming-claims"],
    ["Process Payment", "/insurance/process-payment"],
    ["Claim History", "/insurance/patient-claim-history"],
  ],
  ADMIN: [
    ["Dashboard", "/admin/dashboard"],
    ["Profile", "/admin/profile"],
    ["Account Settings", "/account/settings"],
    ["Manage Users", "/admin/users"],
    ["Patients", "/admin/patients"],
    ["Providers", "/admin/providers"],
    ["Insurance Companies", "/admin/insurance-companies"],
    ["All Claims", "/admin/claims"],
    ["All Payments", "/admin/payments"],
    ["Create Admin", "/admin/create-admin"],
  ],
};

function Sidebar() {
  const [open, setOpen] = useState(true);
  const navigate = useNavigate();
  const auth = getAuth();
  const links = menus[auth.role] || [];

  const handleLogout = async () => {
    await logoutCurrentToken();
    navigate("/login", { replace: true });
  };

  return (
    <aside className={`sidebar p-3 ${open ? "" : "closed"}`}>
      <div className="d-flex align-items-center justify-content-between mb-3">
        {open && <h4 className="brand-logo text-white mb-0">CareAssist</h4>}
        <button
          className="btn btn-sm btn-outline-light"
          onClick={() => setOpen(!open)}
          title="Menu"
        >
          ☰
        </button>
      </div>

      {open && (
        <div className="d-flex align-items-center gap-2 small text-white-50 mb-3">
          <ProfileImage
            src={auth.profilePicture}
            size={48}
            alt={auth.username}
          />
          <div>
            <div className="text-white fw-semibold">{auth.username}</div>
            <div>{auth.role}</div>
          </div>
        </div>
      )}
      <nav className="d-grid gap-1">
        {links.map(([label, path]) => (
          <NavLink key={path} to={path} title={label}>
            <span>{open ? label : label.charAt(0)}</span>
          </NavLink>
        ))}
        <button
          className="link-button mt-3"
          onClick={handleLogout}
          title="Logout"
        >
          <span>{open ? "Logout" : "⏻"}</span>
        </button>
      </nav>
    </aside>
  );
}

export default Sidebar;
