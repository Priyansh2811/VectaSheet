import { apiRequest } from './api';

export function listActivity(workspaceId, limit = 50) {
  return apiRequest(`/workspaces/${workspaceId}/activity?limit=${limit}`);
}
