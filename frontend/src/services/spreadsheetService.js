import { apiRequest } from './api';

export function listSpreadsheets(workspaceId) {
  return apiRequest(`/workspaces/${workspaceId}/spreadsheets`);
}

export function createSpreadsheet(workspaceId, name) {
  return apiRequest(`/workspaces/${workspaceId}/spreadsheets`, { method: 'POST', body: { name } });
}

export function getSpreadsheet(id) {
  return apiRequest(`/spreadsheets/${id}`);
}

export function renameSpreadsheet(id, name) {
  return apiRequest(`/spreadsheets/${id}`, { method: 'PATCH', body: { name } });
}

export function deleteSpreadsheet(id) {
  return apiRequest(`/spreadsheets/${id}`, { method: 'DELETE' });
}

export function listSheets(spreadsheetId) {
  return apiRequest(`/spreadsheets/${spreadsheetId}/sheets`);
}

export function createSheet(spreadsheetId, name) {
  return apiRequest(`/spreadsheets/${spreadsheetId}/sheets`, { method: 'POST', body: { name } });
}

export function renameSheet(sheetId, name) {
  return apiRequest(`/sheets/${sheetId}`, { method: 'PATCH', body: { name } });
}

export function duplicateSheet(sheetId) {
  return apiRequest(`/sheets/${sheetId}/duplicate`, { method: 'POST' });
}

export function deleteSheet(sheetId) {
  return apiRequest(`/sheets/${sheetId}`, { method: 'DELETE' });
}

export function listCells(sheetId) {
  return apiRequest(`/sheets/${sheetId}/cells`);
}

export function updateCell(sheetId, { row, col, rawInput, expectedVersion }) {
  return apiRequest(`/sheets/${sheetId}/cells`, {
    method: 'PUT',
    body: { row, col, rawInput, expectedVersion: expectedVersion ?? -1 },
  });
}
