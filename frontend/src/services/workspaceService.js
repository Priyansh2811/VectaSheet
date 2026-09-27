import { apiRequest } from './api';

export function listWorkspaces() {
  return apiRequest('/workspaces');
}

export function getWorkspace(id) {
  return apiRequest(`/workspaces/${id}`);
}

export function createWorkspace({ name, description }) {
  return apiRequest('/workspaces', { method: 'POST', body: { name, description } });
}

export function updateWorkspace(id, updates) {
  return apiRequest(`/workspaces/${id}`, { method: 'PATCH', body: updates });
}

export function deleteWorkspace(id) {
  return apiRequest(`/workspaces/${id}`, { method: 'DELETE' });
}

export function listMembers(id) {
  return apiRequest(`/workspaces/${id}/members`);
}

export function inviteMember(id, { email, role }) {
  return apiRequest(`/workspaces/${id}/members`, { method: 'POST', body: { email, role } });
}

export function updateMemberRole(id, userId, role) {
  return apiRequest(`/workspaces/${id}/members/${userId}`, { method: 'PATCH', body: { role } });
}

export function removeMember(id, userId) {
  return apiRequest(`/workspaces/${id}/members/${userId}`, { method: 'DELETE' });
}
