import api from "./api.js";

export const createAdmin = (admin) =>
  api.post("/api/v1/admin/add", admin).then((r) => r.data);
export const updateAdmin = (adminId, admin) =>
  api.put(`/api/v1/admin/update/${adminId}`, admin).then((r) => r.data);
export const getAdminById = (adminId) =>
  api.get(`/api/v1/admin/${adminId}`).then((r) => r.data);
export const getAdminByUserId = (userId) =>
  api.get(`/api/v1/admin/user/${userId}`).then((r) => r.data);
export const getAllAdmins = () => api.get("/api/v1/admin/all").then((r) => r.data);
export const getDashboardSummary = () =>
  api.get("/api/v1/admin/dashboard").then((r) => r.data);
export const getAdminUsers = () =>
  api.get("/api/v1/admin/users").then((r) => r.data);
export const getAdminClaims = () =>
  api.get("/api/v1/admin/claims").then((r) => r.data);
export const getAdminPayments = () =>
  api.get("/api/v1/admin/payments").then((r) => r.data);
export const updateUserStatus = (userId, active) =>
  api
    .patch(`/api/v1/admin/users/${userId}/status`, null, { params: { active } })
    .then((r) => r.data);
