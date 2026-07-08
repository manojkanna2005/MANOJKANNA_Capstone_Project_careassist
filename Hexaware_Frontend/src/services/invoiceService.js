import api from './api.js';

export const generateInvoice = (invoice) => api.post('/api/v1/invoices/generate', invoice).then((r) => r.data);
export const updateInvoice = (invoiceId, invoice) => api.put(`/api/v1/invoices/update/${invoiceId}`, invoice).then((r) => r.data);
export const getInvoiceById = (invoiceId) => api.get(`/api/v1/invoices/${invoiceId}`).then((r) => r.data);
export const getInvoicesByPatientId = (patientId) => api.get(`/api/v1/invoices/patient/${patientId}`).then((r) => r.data);
export const getInvoicesByProviderId = (providerId) => api.get(`/api/v1/invoices/provider/${providerId}`).then((r) => r.data);
export const markInvoiceAsPaid = (invoiceId) => api.patch(`/api/v1/invoices/pay/${invoiceId}`).then((r) => r.data);
export const updateInvoiceStatus = (invoiceId, status) => api.patch(`/api/v1/invoices/status/${invoiceId}`, null, { params: { status } }).then((r) => r.data);
export const getAllInvoices = () => api.get('/api/v1/invoices/all').then((r) => r.data);
export const deleteInvoice = (invoiceId) => api.delete(`/api/v1/invoices/delete/${invoiceId}`).then((r) => r.data);

export async function downloadInvoicePdf(invoiceId) {
  const response = await api.get(`/api/v1/invoices/${invoiceId}/pdf`, { responseType: 'blob' });
  const url = window.URL.createObjectURL(new Blob([response.data], { type: 'application/pdf' }));
  const link = document.createElement('a');
  link.href = url;
  link.setAttribute('download', `invoice-${invoiceId}.pdf`);
  document.body.appendChild(link);
  link.click();
  link.remove();
  window.URL.revokeObjectURL(url);
}

export const emailInvoicePdf = (invoiceId, email) => api.post(`/api/v1/invoices/${invoiceId}/email-pdf`, null, { params: email ? { email } : {} }).then((r) => r.data);
