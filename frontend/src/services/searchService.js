import { apiRequest } from './api';

export function search(workspaceId, query) {
  return apiRequest(`/workspaces/${workspaceId}/search?q=${encodeURIComponent(query)}`);
}
