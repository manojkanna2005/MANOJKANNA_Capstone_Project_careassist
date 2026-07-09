import api from "./api.js";

export const createProviderProfile = (provider) =>
  api.post("/api/v1/providers/add", provider).then((r) => r.data);
export const updateProviderProfile = (providerId, provider) =>
  api
    .put(`/api/v1/providers/update/${providerId}`, provider)
    .then((r) => r.data);
export const getProviderById = (providerId) =>
  api.get(`/api/v1/providers/${providerId}`).then((r) => r.data);
export const getProviderByUserId = (userId) =>
  api.get(`/api/v1/providers/user/${userId}`).then((r) => r.data);
export const getAllProviders = () =>
  api.get("/api/v1/providers/all").then((r) => r.data);
export const deleteProvider = (providerId) =>
  api.delete(`/api/v1/providers/delete/${providerId}`).then((r) => r.data);
