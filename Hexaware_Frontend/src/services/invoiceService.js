import api from "./api.js";

function extractArray(payload) {
  if (Array.isArray(payload)) {
    return payload;
  }

  if (!payload || typeof payload !== "object") {
    return [];
  }

  const candidates = [
    "data",
    "content",
    "items",
    "invoices",
    "result",
    "results",
    "list",
    "records",
  ];
  for (const key of candidates) {
    const value = payload[key];
    if (Array.isArray(value)) {
      return value;
    }
  }

  return [];
}

export const generateInvoice = (invoice) =>
  api.post("/api/v1/invoices/generate", invoice).then((r) => r.data);
export const updateInvoice = (invoiceId, invoice) =>
  api.put(`/api/v1/invoices/update/${invoiceId}`, invoice).then((r) => r.data);
export const getInvoiceById = (invoiceId) =>
  api.get(`/api/v1/invoices/${invoiceId}`).then((r) => r.data);
export const getMyInvoices = () =>
  api.get("/api/v1/invoices/my").then((r) => extractArray(r.data));
export const getInvoicesByPatientId = (patientId) =>
  api
    .get(`/api/v1/invoices/patient/${patientId}`)
    .then((r) => extractArray(r.data));
export const getInvoicesByProviderId = (providerId) =>
  api
    .get(`/api/v1/invoices/provider/${providerId}`)
    .then((r) => extractArray(r.data));
export const updateInvoiceStatus = (invoiceId, status) =>
  api
    .patch(`/api/v1/invoices/status/${invoiceId}`, null, { params: { status } })
    .then((r) => r.data);
export const getAllInvoices = () =>
  api.get("/api/v1/invoices/all").then((r) => extractArray(r.data));
export const deleteInvoice = (invoiceId) =>
  api.delete(`/api/v1/invoices/delete/${invoiceId}`).then((r) => r.data);

export async function downloadInvoicePdf(invoiceId) {
  const response = await api.get(`/api/v1/invoices/${invoiceId}/pdf`, {
    responseType: "blob",
  });
  const url = window.URL.createObjectURL(
    new Blob([response.data], { type: "application/pdf" }),
  );
  const link = document.createElement("a");
  link.href = url;
  link.setAttribute("download", `invoice-${invoiceId}.pdf`);
  document.body.appendChild(link);
  link.click();
  link.remove();
  window.URL.revokeObjectURL(url);
}

export const emailInvoicePdf = (invoiceId, email) =>
  api
    .post(`/api/v1/invoices/${invoiceId}/email-pdf`, null, {
      params: email ? { email } : {},
    })
    .then((r) => r.data);
