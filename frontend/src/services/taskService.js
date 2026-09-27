import { apiRequest } from './api';

export function listTasks(workspaceId) {
  return apiRequest(`/workspaces/${workspaceId}/tasks`);
}

export function createTask(workspaceId, task) {
  return apiRequest(`/workspaces/${workspaceId}/tasks`, { method: 'POST', body: task });
}

export function getTask(id) {
  return apiRequest(`/tasks/${id}`);
}

export function updateTask(id, updates) {
  return apiRequest(`/tasks/${id}`, { method: 'PATCH', body: updates });
}

export function deleteTask(id) {
  return apiRequest(`/tasks/${id}`, { method: 'DELETE' });
}

export function listSubtasks(id) {
  return apiRequest(`/tasks/${id}/subtasks`);
}
