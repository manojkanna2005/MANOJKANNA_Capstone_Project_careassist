import api from "./api.js";

export const selectInsurancePlan = (patientInsurance) =>
  api
    .post("/api/v1/patient-insurance/select", patientInsurance)
    .then((r) => r.data);
export const updatePatientInsurance = (enrollmentId, patientInsurance) =>
  api
    .put(`/api/v1/patient-insurance/update/${enrollmentId}`, patientInsurance)
    .then((r) => r.data);
export const getPatientInsuranceById = (enrollmentId) =>
  api.get(`/api/v1/patient-insurance/${enrollmentId}`).then((r) => r.data);
export const getActiveInsuranceByPatientId = (patientId) =>
  api
    .get(`/api/v1/patient-insurance/patient/${patientId}/active`)
    .then((r) => r.data);
export const getActiveInsurancesByPatientId = (patientId) =>
  api
    .get(`/api/v1/patient-insurance/patient/${patientId}/active-all`)
    .then((r) => r.data);
export const getInsuranceHistoryByPatientId = (patientId) =>
  api
    .get(`/api/v1/patient-insurance/patient/${patientId}/history`)
    .then((r) => r.data);
export const cancelInsurancePlan = (enrollmentId) =>
  api
    .patch(`/api/v1/patient-insurance/cancel/${enrollmentId}`)
    .then((r) => r.data);
export const deletePatientInsurance = (enrollmentId) =>
  api
    .delete(`/api/v1/patient-insurance/delete/${enrollmentId}`)
    .then((r) => r.data);
