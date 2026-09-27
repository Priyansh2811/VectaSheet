import { apiRequest } from './api';

export function listNotifications() {
  return apiRequest('/notifications');
}

export function unreadCount() {
  return apiRequest('/notifications/unread-count');
}

export function setRead(id, read) {
  return apiRequest(`/notifications/${id}`, { method: 'PATCH', body: { read } });
}

export function markAllRead() {
  return apiRequest('/notifications/mark-all-read', { method: 'POST' });
}

export function deleteNotification(id) {
  return apiRequest(`/notifications/${id}`, { method: 'DELETE' });
}
