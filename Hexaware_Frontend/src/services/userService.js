import api from "./api.js";
import { setAuth } from "../utils/auth.js";

export const createUser = (user) =>
  api.post("/api/v1/users/add", user).then((r) => r.data);
export const updateUser = (userId, user) =>
  api.put(`/api/v1/users/update/${userId}`, user).then((r) => r.data);
export const updateUserAccount = (userId, account) =>
  api.patch(`/api/v1/users/${userId}/account`, account).then((r) => r.data);
export const getUserById = (userId) =>
  api.get(`/api/v1/users/${userId}`).then((r) => r.data);
export const getUserAccount = (userId) =>
  api.get(`/api/v1/users/${userId}/account`).then((r) => r.data);
export const getUserByEmail = (email) =>
  api.get(`/api/v1/users/email/${email}`).then((r) => r.data);
export const getAllUsers = () =>
  api.get("/api/v1/users/all").then((r) => r.data);
export const getUsersByRole = (role) =>
  api.get(`/api/v1/users/role/${role}`).then((r) => r.data);
export const getAllUsersExceptAdmin = () =>
  api.get("/api/v1/users/except-admin").then((r) => r.data);
export const activateUser = (userId) =>
  api.patch(`/api/v1/users/activate/${userId}`).then((r) => r.data);
export const deactivateUser = (userId) =>
  api.patch(`/api/v1/users/deactivate/${userId}`).then((r) => r.data);
export const deleteUser = (userId) =>
  api.delete(`/api/v1/users/delete/${userId}`).then((r) => r.data);

export async function uploadProfilePicture(userId, file) {
  const formData = new FormData();
  formData.append("file", file);
  const response = await api.post(
    `/api/v1/users/${userId}/profile-picture`,
    formData,
    {
      headers: { "Content-Type": "multipart/form-data" },
    },
  );
  refreshStoredUser(response.data);
  return response.data;
}

export async function deleteProfilePicture(userId) {
  const response = await api.delete(`/api/v1/users/${userId}/profile-picture`);
  refreshStoredUser(response.data);
  return response.data;
}

export async function changePassword(userId, payload) {
  const response = await api.patch(
    `/api/v1/users/${userId}/change-password`,
    payload,
  );
  return response.data;
}

export function refreshStoredUser(user) {
  const token = localStorage.getItem("token");
  const tokenType = localStorage.getItem("tokenType") || "Bearer";
  setAuth({
    token,
    tokenType,
    userId: user.userId,
    username: user.username,
    email: user.email,
    role: user.role,
    profilePicture: user.profilePicture,
  });
}
