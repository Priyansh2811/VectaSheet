import { apiRequest, setTokens } from './api';

export async function register({ name, email, password }) {
  const data = await apiRequest('/auth/register', { method: 'POST', body: { name, email, password } });
  setTokens(data);
  return data.user;
}

export async function login({ email, password }) {
  const data = await apiRequest('/auth/login', { method: 'POST', body: { email, password } });
  setTokens(data);
  return data.user;
}

export async function logout(refreshToken) {
  try {
    await apiRequest('/auth/logout', { method: 'POST', body: { refreshToken } });
  } finally {
    setTokens(null);
  }
}

export async function fetchMe() {
  return apiRequest('/users/me');
}

export async function changePassword(currentPassword, newPassword) {
  return apiRequest('/auth/change-password', { method: 'POST', body: { currentPassword, newPassword } });
}
