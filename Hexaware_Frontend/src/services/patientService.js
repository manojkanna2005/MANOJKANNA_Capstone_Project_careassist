import api from "./api.js";

export const createPatientProfile = (patient) =>
  api.post("/api/v1/patients/add", patient).then((r) => r.data);
export const updatePatientProfile = (patientId, patient) =>
  api.put(`/api/v1/patients/update/${patientId}`, patient).then((r) => r.data);
export const getPatientById = (patientId) =>
  api.get(`/api/v1/patients/${patientId}`).then((r) => r.data);
export const getPatientByUserId = (userId) =>
  api.get(`/api/v1/patients/user/${userId}`).then((r) => r.data);
export const getAllPatients = () =>
  api.get("/api/v1/patients/all").then((r) => r.data);
export const deletePatient = (patientId) =>
  api.delete(`/api/v1/patients/delete/${patientId}`).then((r) => r.data);
