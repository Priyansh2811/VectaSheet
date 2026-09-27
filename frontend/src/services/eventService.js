import { apiRequest } from './api';

export function listEvents(workspaceId) {
  return apiRequest(`/workspaces/${workspaceId}/events`);
}

export function createEvent(workspaceId, event) {
  return apiRequest(`/workspaces/${workspaceId}/events`, { method: 'POST', body: event });
}

export function updateEvent(id, updates) {
  return apiRequest(`/events/${id}`, { method: 'PATCH', body: updates });
}

export function deleteEvent(id) {
  return apiRequest(`/events/${id}`, { method: 'DELETE' });
}
