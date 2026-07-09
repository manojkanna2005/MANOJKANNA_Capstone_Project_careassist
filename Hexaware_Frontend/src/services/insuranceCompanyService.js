import api from "./api.js";

export const createCompanyProfile = (company) =>
  api.post("/api/v1/insurance-companies/add", company).then((r) => r.data);
export const updateCompanyProfile = (companyId, company) =>
  api
    .put(`/api/v1/insurance-companies/update/${companyId}`, company)
    .then((r) => r.data);
export const getCompanyById = (companyId) =>
  api.get(`/api/v1/insurance-companies/${companyId}`).then((r) => r.data);
export const getCompanyByUserId = (userId) =>
  api.get(`/api/v1/insurance-companies/user/${userId}`).then((r) => r.data);
export const getAllCompanies = () =>
  api.get("/api/v1/insurance-companies/all").then((r) => r.data);
export const deleteCompany = (companyId) =>
  api
    .delete(`/api/v1/insurance-companies/delete/${companyId}`)
    .then((r) => r.data);
