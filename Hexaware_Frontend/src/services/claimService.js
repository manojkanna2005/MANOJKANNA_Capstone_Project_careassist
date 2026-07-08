import api from './api.js';

export const submitClaim = (claim) =>
  api.post('/api/v1/claims/submit', claim).then((response) => response.data);

export const submitClaimWithDocuments = (claim, documents) => {
  const formData = new FormData();
  formData.append(
    'claim',
    new Blob([JSON.stringify(claim)], { type: 'application/json' }),
  );

  Array.from(documents || []).forEach((document) => {
    formData.append('documents', document);
  });

  return api
    .post('/api/v1/claims/submit-with-documents', formData)
    .then((response) => response.data);
};

export const uploadClaimDocuments = (claimId, documents) => {
  const formData = new FormData();
  Array.from(documents || []).forEach((document) => {
    formData.append('documents', document);
  });
  return api
    .post(`/api/v1/claims/${claimId}/documents`, formData)
    .then((response) => response.data);
};

export const getClaimDocuments = (claimId) =>
  api.get(`/api/v1/claims/${claimId}/documents`).then((response) => response.data);

export const downloadClaimDocument = async (metadata) => {
  const response = await api.get(
    `/api/v1/claims/documents/${metadata.documentId}/download`,
    { responseType: 'blob' },
  );
  const url = window.URL.createObjectURL(response.data);
  const anchor = document.createElement('a');
  anchor.href = url;
  anchor.download = metadata.originalFileName || `claim-document-${metadata.documentId}`;
  document.body.appendChild(anchor);
  anchor.click();
  anchor.remove();
  window.URL.revokeObjectURL(url);
};

export const deleteClaimDocument = (documentId) =>
  api.delete(`/api/v1/claims/documents/${documentId}`).then((response) => response.data);

export const getClaimById = (claimId) =>
  api.get(`/api/v1/claims/${claimId}`).then((response) => response.data);
export const getClaimsByPatientId = (patientId) =>
  api.get(`/api/v1/claims/patient/${patientId}`).then((response) => response.data);
export const getClaimsByInsuranceCompanyId = (companyId) =>
  api.get(`/api/v1/claims/company/${companyId}`).then((response) => response.data);
export const getAllClaims = () =>
  api.get('/api/v1/claims/all').then((response) => response.data);
export const approveClaim = (claimId) =>
  api.patch(`/api/v1/claims/approve/${claimId}`).then((response) => response.data);
export const rejectClaim = (claimId, rejectionReason) =>
  api
    .patch(`/api/v1/claims/reject/${claimId}`, null, {
      params: { rejectionReason },
    })
    .then((response) => response.data);
export const deleteClaim = (claimId) =>
  api.delete(`/api/v1/claims/delete/${claimId}`).then((response) => response.data);
