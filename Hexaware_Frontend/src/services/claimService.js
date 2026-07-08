import api from './api.js';

export const submitClaim = (claim) => api.post('/api/v1/claims/submit', claim).then((r) => r.data);
export const getClaimById = (claimId) => api.get(`/api/v1/claims/${claimId}`).then((r) => r.data);
export const getClaimsByPatientId = (patientId) => api.get(`/api/v1/claims/patient/${patientId}`).then((r) => r.data);
export const getClaimsByInsuranceCompanyId = (companyId) => api.get(`/api/v1/claims/company/${companyId}`).then((r) => r.data);
export const getAllClaims = () => api.get('/api/v1/claims/all').then((r) => r.data);
export const approveClaim = (claimId) => api.patch(`/api/v1/claims/approve/${claimId}`).then((r) => r.data);
export const rejectClaim = (claimId, rejectionReason) => api.patch(`/api/v1/claims/reject/${claimId}`, null, { params: { rejectionReason } }).then((r) => r.data);
export const deleteClaim = (claimId) => api.delete(`/api/v1/claims/delete/${claimId}`).then((r) => r.data);
