import api from './api.js';
import { clearAuth, getToken } from '../utils/auth.js';
import { nowDateTime } from '../utils/date.js';

export async function logoutCurrentToken() {
  try {
    await api.post('/api/v1/auth/logout');
  } finally {
    clearAuth();
  }
}

export async function blacklistTokenAsAdmin(token = getToken()) {
  const response = await api.post('/api/v1/tokens/blacklist', {
    token,
    invalidatedAt: nowDateTime(),
  });
  return response.data;
}

export async function checkToken(token) {
  const response = await api.get('/api/v1/tokens/check', { params: { token } });
  return response.data;
}

export async function getAllBlacklistedTokens() {
  const response = await api.get('/api/v1/tokens/all');
  return response.data;
}

export async function deleteExpiredTokens(beforeDateTime) {
  const response = await api.delete('/api/v1/tokens/expired', { params: { beforeDateTime } });
  return response.data;
}
