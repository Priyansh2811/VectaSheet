import { apiRequest } from './api';

export function listDocuments(workspaceId) {
  return apiRequest(`/workspaces/${workspaceId}/documents`);
}

export function createDocument(workspaceId, title) {
  return apiRequest(`/workspaces/${workspaceId}/documents`, { method: 'POST', body: { title } });
}

export function getDocument(id) {
  return apiRequest(`/documents/${id}`);
}

export function saveDocument(id, { title, contentHtml, expectedVersion }) {
  return apiRequest(`/documents/${id}`, {
    method: 'PUT',
    body: { title, contentHtml, expectedVersion: expectedVersion ?? -1 },
  });
}

export function deleteDocument(id) {
  return apiRequest(`/documents/${id}`, { method: 'DELETE' });
}

export function listVersions(id) {
  return apiRequest(`/documents/${id}/versions`);
}

export function getVersion(id, versionId) {
  return apiRequest(`/documents/${id}/versions/${versionId}`);
}

export function restoreVersion(id, versionId) {
  return apiRequest(`/documents/${id}/versions/${versionId}/restore`, { method: 'POST' });
}

export function listComments(id) {
  return apiRequest(`/documents/${id}/comments`);
}

export function createComment(id, { body, quotedText }) {
  return apiRequest(`/documents/${id}/comments`, { method: 'POST', body: { body, quotedText } });
}

export function resolveComment(id, commentId, resolved) {
  return apiRequest(`/documents/${id}/comments/${commentId}`, { method: 'PATCH', body: { resolved } });
}

export function deleteComment(id, commentId) {
  return apiRequest(`/documents/${id}/comments/${commentId}`, { method: 'DELETE' });
}
