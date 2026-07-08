import api from './api.js';

export const createPlan = (plan) => api.post('/api/v1/insurance-plans/add', plan).then((r) => r.data);
export const updatePlan = (planId, plan) => api.put(`/api/v1/insurance-plans/update/${planId}`, plan).then((r) => r.data);
export const getPlanById = (planId) => api.get(`/api/v1/insurance-plans/${planId}`).then((r) => r.data);
export const getAllPlans = () => api.get('/api/v1/insurance-plans/all').then((r) => r.data);
export const getActivePlans = () => api.get('/api/v1/insurance-plans/active').then((r) => r.data);
export const getPlansByCompanyId = (companyId) => api.get(`/api/v1/insurance-plans/company/${companyId}`).then((r) => r.data);
export const activatePlan = (planId) => api.patch(`/api/v1/insurance-plans/activate/${planId}`).then((r) => r.data);
export const deactivatePlan = (planId) => api.patch(`/api/v1/insurance-plans/deactivate/${planId}`).then((r) => r.data);
export const deletePlan = (planId) => api.delete(`/api/v1/insurance-plans/delete/${planId}`).then((r) => r.data);
