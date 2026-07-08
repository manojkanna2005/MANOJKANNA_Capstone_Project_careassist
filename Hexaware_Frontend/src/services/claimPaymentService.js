import api from './api.js';

export const processClaimPayment = (payment) => api.post('/api/v1/claim-payments/process', payment).then((r) => r.data);
export const getPaymentById = (paymentId) => api.get(`/api/v1/claim-payments/${paymentId}`).then((r) => r.data);
export const getPaymentByClaimId = (claimId) => api.get(`/api/v1/claim-payments/claim/${claimId}`).then((r) => r.data);
export const getAllPayments = () => api.get('/api/v1/claim-payments/all').then((r) => r.data);
export const deletePayment = (paymentId) => api.delete(`/api/v1/claim-payments/delete/${paymentId}`).then((r) => r.data);
