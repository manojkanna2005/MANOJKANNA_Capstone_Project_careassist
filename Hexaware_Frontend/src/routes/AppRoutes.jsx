import { Navigate, Route, Routes } from "react-router-dom";
import ProtectedRoute from "../components/common/ProtectedRoute.jsx";
import Unauthorized from "../components/common/Unauthorized.jsx";
import Login from "../components/auth/Login.jsx";
import Register from "../components/auth/Register.jsx";
import Logout from "../components/auth/Logout.jsx";
import ForgotPassword from "../components/auth/ForgotPassword.jsx";
import AccountSettings from "../components/common/AccountSettings.jsx";

import PatientDashboard from "../components/patient/PatientDashboard.jsx";
import PatientProfile from "../components/patient/PatientProfile.jsx";
import ViewInsurancePlans from "../components/patient/ViewInsurancePlans.jsx";
import SelectInsurancePlan from "../components/patient/SelectInsurancePlan.jsx";
import MyInsurance from "../components/patient/MyInsurance.jsx";
import RequestInvoice from "../components/patient/RequestInvoice.jsx";
import MyInvoices from "../components/patient/MyInvoices.jsx";
import PayInvoice from "../components/patient/PayInvoice.jsx";
import SubmitClaim from "../components/patient/SubmitClaim.jsx";
import MyClaims from "../components/patient/MyClaims.jsx";
import ViewClaimStatus from "../components/patient/ViewClaimStatus.jsx";

import ProviderDashboard from "../components/provider/ProviderDashboard.jsx";
import ProviderProfile from "../components/provider/ProviderProfile.jsx";
import PatientRequests from "../components/provider/PatientRequests.jsx";
import GenerateInvoice from "../components/provider/GenerateInvoice.jsx";
import ProviderInvoices from "../components/provider/ProviderInvoices.jsx";
import NotifyPatient from "../components/provider/NotifyPatient.jsx";
import SubmitClaimForPatient from "../components/provider/SubmitClaimForPatient.jsx";

import InsuranceDashboard from "../components/insurance/InsuranceDashboard.jsx";
import InsuranceCompanyProfile from "../components/insurance/InsuranceCompanyProfile.jsx";
import ManageInsurancePlans from "../components/insurance/ManageInsurancePlans.jsx";
import IncomingClaims from "../components/insurance/IncomingClaims.jsx";
import ReviewClaim from "../components/insurance/ReviewClaim.jsx";
import ApproveRejectClaim from "../components/insurance/ApproveRejectClaim.jsx";
import ProcessClaimPayment from "../components/insurance/ProcessClaimPayment.jsx";
import PatientClaimHistory from "../components/insurance/PatientClaimHistory.jsx";

import AdminDashboard from "../components/admin/AdminDashboard.jsx";
import AdminProfile from "../components/admin/AdminProfile.jsx";
import ManageUsers from "../components/admin/ManageUsers.jsx";
import ManagePatients from "../components/admin/ManagePatients.jsx";
import ManageProviders from "../components/admin/ManageProviders.jsx";
import ManageInsuranceCompanies from "../components/admin/ManageInsuranceCompanies.jsx";
import AllClaims from "../components/admin/AllClaims.jsx";
import AllPayments from "../components/admin/AllPayments.jsx";
import CreateAdmin from "../components/admin/CreateAdmin.jsx";

const protect = (roles, component) => (
  <ProtectedRoute allowedRoles={roles}>{component}</ProtectedRoute>
);

function AppRoutes() {
  return (
    <Routes>
      <Route path="/" element={<Navigate to="/login" replace />} />
      <Route path="/login" element={<Login />} />
      <Route path="/register" element={<Register />} />
      <Route path="/forgot-password" element={<ForgotPassword />} />
      <Route path="/logout" element={<Logout />} />
      <Route path="/unauthorized" element={<Unauthorized />} />
      <Route path="/account/settings" element={protect(["PATIENT", "PROVIDER", "INSURANCE", "ADMIN"], <AccountSettings />)} />

      <Route
        path="/patient/dashboard"
        element={protect(["PATIENT"], <PatientDashboard />)}
      />
      <Route
        path="/patient/profile"
        element={protect(["PATIENT"], <PatientProfile />)}
      />
      <Route
        path="/patient/insurance-plans"
        element={protect(["PATIENT"], <ViewInsurancePlans />)}
      />
      <Route
        path="/patient/select-plan/:planId"
        element={protect(["PATIENT"], <SelectInsurancePlan />)}
      />
      <Route
        path="/patient/my-insurance"
        element={protect(["PATIENT"], <MyInsurance />)}
      />
      <Route
        path="/patient/request-invoice"
        element={protect(["PATIENT"], <RequestInvoice />)}
      />
      <Route
        path="/patient/invoices"
        element={protect(["PATIENT"], <MyInvoices />)}
      />
      <Route
        path="/patient/pay-invoice/:invoiceId"
        element={protect(["PATIENT"], <PayInvoice />)}
      />
      <Route
        path="/patient/submit-claim"
        element={protect(["PATIENT"], <SubmitClaim />)}
      />
      <Route
        path="/patient/claims"
        element={protect(["PATIENT"], <MyClaims />)}
      />
      <Route
        path="/patient/claim-status"
        element={protect(["PATIENT"], <ViewClaimStatus />)}
      />

      <Route
        path="/provider/dashboard"
        element={protect(["PROVIDER"], <ProviderDashboard />)}
      />
      <Route
        path="/provider/profile"
        element={protect(["PROVIDER"], <ProviderProfile />)}
      />
      <Route
        path="/provider/patient-requests"
        element={protect(["PROVIDER"], <PatientRequests />)}
      />
      <Route
        path="/provider/generate-invoice"
        element={protect(["PROVIDER"], <GenerateInvoice />)}
      />
      <Route
        path="/provider/invoices"
        element={protect(["PROVIDER"], <ProviderInvoices />)}
      />
      <Route
        path="/provider/notify-patient"
        element={protect(["PROVIDER"], <NotifyPatient />)}
      />
      <Route
        path="/provider/submit-claim"
        element={protect(["PROVIDER"], <SubmitClaimForPatient />)}
      />

      <Route
        path="/insurance/dashboard"
        element={protect(["INSURANCE"], <InsuranceDashboard />)}
      />
      <Route
        path="/insurance/profile"
        element={protect(["INSURANCE"], <InsuranceCompanyProfile />)}
      />
      <Route
        path="/insurance/plans"
        element={protect(["INSURANCE"], <ManageInsurancePlans />)}
      />
      <Route
        path="/insurance/incoming-claims"
        element={protect(["INSURANCE"], <IncomingClaims />)}
      />
      <Route
        path="/insurance/review-claim/:claimId"
        element={protect(["INSURANCE"], <ReviewClaim />)}
      />
      <Route
        path="/insurance/claim-decision/:claimId"
        element={protect(["INSURANCE"], <ApproveRejectClaim />)}
      />
      <Route
        path="/insurance/process-payment"
        element={protect(["INSURANCE"], <ProcessClaimPayment />)}
      />
      <Route
        path="/insurance/patient-claim-history"
        element={protect(["INSURANCE"], <PatientClaimHistory />)}
      />

      <Route
        path="/admin/dashboard"
        element={protect(["ADMIN"], <AdminDashboard />)}
      />
      <Route
        path="/admin/profile"
        element={protect(["ADMIN"], <AdminProfile />)}
      />
      <Route
        path="/admin/users"
        element={protect(["ADMIN"], <ManageUsers />)}
      />
      <Route
        path="/admin/patients"
        element={protect(["ADMIN"], <ManagePatients />)}
      />
      <Route
        path="/admin/providers"
        element={protect(["ADMIN"], <ManageProviders />)}
      />
      <Route
        path="/admin/insurance-companies"
        element={protect(["ADMIN"], <ManageInsuranceCompanies />)}
      />
      <Route path="/admin/claims" element={protect(["ADMIN"], <AllClaims />)} />
      <Route
        path="/admin/payments"
        element={protect(["ADMIN"], <AllPayments />)}
      />
      <Route
        path="/admin/create-admin"
        element={protect(["ADMIN"], <CreateAdmin />)}
      />

      <Route path="*" element={<Navigate to="/login" replace />} />
    </Routes>
  );
}

export default AppRoutes;
