const BASE_URL = '/api';

let accessToken = localStorage.getItem('vs_access_token') || null;
let refreshToken = localStorage.getItem('vs_refresh_token') || null;

export function setTokens(tokens) {
  accessToken = tokens?.accessToken || null;
  refreshToken = tokens?.refreshToken || null;

  if (accessToken) localStorage.setItem('vs_access_token', accessToken);
  else localStorage.removeItem('vs_access_token');

  if (refreshToken) localStorage.setItem('vs_refresh_token', refreshToken);
  else localStorage.removeItem('vs_refresh_token');
}

export function getAccessToken() {
  return accessToken;
}

export function getRefreshToken() {
  return refreshToken;
}

async function doRefresh() {
  if (!refreshToken) throw new Error('No refresh token');
  const res = await fetch(`${BASE_URL}/auth/refresh`, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({ refreshToken }),
  });
  if (!res.ok) throw new Error('Refresh failed');
  const data = await res.json();
  setTokens(data);
  return data;
}

export async function apiRequest(path, { method = 'GET', body, retry = true } = {}) {
  const headers = { 'Content-Type': 'application/json' };
  if (accessToken) headers.Authorization = `Bearer ${accessToken}`;

  const res = await fetch(`${BASE_URL}${path}`, {
    method,
    headers,
    body: body ? JSON.stringify(body) : undefined,
  });

  if (res.status === 401 && retry && refreshToken) {
    try {
      await doRefresh();
      return apiRequest(path, { method, body, retry: false });
    } catch {
      setTokens(null);
      window.location.href = '/login';
      throw new Error('Session expired');
    }
  }

  if (res.status === 204) return null;

  let data = null;
  try {
    data = await res.json();
  } catch {
    // no body
  }

  if (!res.ok) {
    const message = data?.message || 'Something went wrong. Please try again.';
    const error = new Error(message);
    error.status = res.status;
    error.fieldErrors = data?.fieldErrors;
    throw error;
  }

  return data;
}
