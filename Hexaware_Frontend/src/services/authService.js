import api from './api.js';
import { setAuth, clearAuth } from '../utils/auth.js';

export async function login(credentials) {
  const response = await api.post('/api/v1/auth/login', credentials);
  setAuth(response.data);
  return response.data;
}

export async function register(user) {
  const response = await api.post('/api/v1/auth/register', user);
  return response.data;
}

export async function forgotPassword(email) {
  const response = await api.post('/api/v1/auth/forgot-password', { email });
  return response.data;
}

export async function logout() {
  try {
    await api.post('/api/v1/auth/logout');
  } finally {
    clearAuth();
  }
}

export async function testAuthApi() {
  const response = await api.get('/api/v1/auth/test');
  return response.data;
}
