import { getAccessToken } from './api';

export async function listFiles(workspaceId) {
  const res = await fetch(`/api/workspaces/${workspaceId}/files`, {
    headers: { Authorization: `Bearer ${getAccessToken()}` },
  });
  if (!res.ok) throw new Error('Could not load files');
  return res.json();
}

export async function uploadFile(workspaceId, file) {
  const formData = new FormData();
  formData.append('file', file);
  const res = await fetch(`/api/workspaces/${workspaceId}/files`, {
    method: 'POST',
    headers: { Authorization: `Bearer ${getAccessToken()}` },
    body: formData,
  });
  if (!res.ok) {
    const data = await res.json().catch(() => null);
    throw new Error(data?.message || 'Could not upload file');
  }
  return res.json();
}

export function downloadFileUrl(fileId) {
  return `/api/files/${fileId}/download`;
}

export async function downloadFile(fileId, filename) {
  const res = await fetch(`/api/files/${fileId}/download`, {
    headers: { Authorization: `Bearer ${getAccessToken()}` },
  });
  if (!res.ok) throw new Error('Could not download file');
  const blob = await res.blob();
  const url = window.URL.createObjectURL(blob);
  const a = document.createElement('a');
  a.href = url;
  a.download = filename;
  document.body.appendChild(a);
  a.click();
  a.remove();
  window.URL.revokeObjectURL(url);
}

export async function deleteFile(fileId) {
  const res = await fetch(`/api/files/${fileId}`, {
    method: 'DELETE',
    headers: { Authorization: `Bearer ${getAccessToken()}` },
  });
  if (!res.ok) throw new Error('Could not delete file');
}
