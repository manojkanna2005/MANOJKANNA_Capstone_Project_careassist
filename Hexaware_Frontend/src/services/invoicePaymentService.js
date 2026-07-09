import api from "./api.js";

export const processInvoicePayment = (payment) =>
  api.post("/api/v1/invoice-payments/process", payment).then((response) => response.data);

export const getMyInvoicePayments = () =>
  api.get("/api/v1/invoice-payments/my").then((response) => response.data);

export const getInvoicePayment = (invoiceId) =>
  api
    .get(`/api/v1/invoice-payments/invoice/${invoiceId}`)
    .then((response) => response.data);
