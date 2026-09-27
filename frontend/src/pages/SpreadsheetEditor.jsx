import { useCallback, useEffect, useMemo, useRef, useState } from 'react';
import { useNavigate, useParams } from 'react-router-dom';
import { useToast } from '../context/ToastContext';
import * as svc from '../services/spreadsheetService';
import { formatRef } from '../utils/cellRef';
import Loading from '../components/Loading';

function keyOf(row, col) { return `${row}:${col}`; }

export default function SpreadsheetEditor() {
  const { spreadsheetId, workspaceId } = useParams();
  const navigate = useNavigate();
  const { showToast } = useToast();

  const [spreadsheet, setSpreadsheet] = useState(null);
  const [sheets, setSheets] = useState([]);
  const [activeSheetId, setActiveSheetId] = useState(null);
  const [cells, setCells] = useState({}); // key -> CellDto
  const [loading, setLoading] = useState(true);
  const [saveStatus, setSaveStatus] = useState('Saved'); // Saving... | Saved | Offline

  const [selected, setSelected] = useState({ row: 0, col: 0 });
  const [editing, setEditing] = useState(false);
  const [editValue, setEditValue] = useState('');

  const undoStack = useRef([]);
  const redoStack = useRef([]);
  const gridRef = useRef(null);

  const activeSheet = sheets.find((s) => s.id === activeSheetId);
  const rowCount = activeSheet?.rowCount ?? 50;
  const colCount = activeSheet?.colCount ?? 20;

  useEffect(() => {
    loadAll();
  }, [spreadsheetId]);

  useEffect(() => {
    if (activeSheetId) loadCells(activeSheetId);
  }, [activeSheetId]);

  useEffect(() => {
    if (spreadsheet) document.title = `${spreadsheet.name} — VectaSheet`;
  }, [spreadsheet]);

  async function loadAll() {
    setLoading(true);
    try {
      const [ss, sh] = await Promise.all([
        svc.getSpreadsheet(spreadsheetId),
        svc.listSheets(spreadsheetId),
      ]);
      setSpreadsheet(ss);
      setSheets(sh);
      setActiveSheetId(sh[0]?.id ?? null);
    } catch (err) {
      showToast(err.message || 'Could not load spreadsheet', 'error');
    } finally {
      setLoading(false);
    }
  }

  async function loadCells(sheetId) {
    try {
      const list = await svc.listCells(sheetId);
      const map = {};
      list.forEach((c) => { map[keyOf(c.row, c.col)] = c; });
      setCells(map);
    } catch (err) {
      showToast(err.message || 'Could not load cells', 'error');
    }
  }

  const cellAt = useCallback((row, col) => cells[keyOf(row, col)], [cells]);

  function startEdit(row, col, initialValue) {
    setSelected({ row, col });
    setEditing(true);
    setEditValue(initialValue !== undefined ? initialValue : (cellAt(row, col)?.rawInput || ''));
  }

  async function commitEdit(moveRow = 1, moveCol = 0) {
    const { row, col } = selected;
    const existing = cellAt(row, col);
    const previousRaw = existing?.rawInput ?? '';

    setEditing(false);

    if (editValue === previousRaw) {
      moveSelection(moveRow, moveCol);
      return;
    }

    setSaveStatus('Saving...');
    try {
      const updated = await svc.updateCell(activeSheetId, {
        row, col,
        rawInput: editValue,
        expectedVersion: existing?.version ?? -1,
      });
      applyUpdatedCells(updated);
      undoStack.current.push({ row, col, previousRaw, nextRaw: editValue });
      redoStack.current = [];
      setSaveStatus('Saved');
    } catch (err) {
      setSaveStatus('Offline');
      if (err.status === 409) {
        showToast('This cell was changed by someone else — reloading latest values.', 'error');
        loadCells(activeSheetId);
      } else {
        showToast(err.message || 'Could not save cell', 'error');
      }
    }
    moveSelection(moveRow, moveCol);
  }

  function applyUpdatedCells(updatedList) {
    setCells((prev) => {
      const next = { ...prev };
      updatedList.forEach((c) => { next[keyOf(c.row, c.col)] = c; });
      return next;
    });
  }

  function moveSelection(dRow, dCol) {
    setSelected((prev) => ({
      row: Math.max(0, Math.min(rowCount - 1, prev.row + dRow)),
      col: Math.max(0, Math.min(colCount - 1, prev.col + dCol)),
    }));
  }

  async function handleUndo() {
    const action = undoStack.current.pop();
    if (!action) return;
    const existing = cellAt(action.row, action.col);
    try {
      const updated = await svc.updateCell(activeSheetId, {
        row: action.row, col: action.col,
        rawInput: action.previousRaw,
        expectedVersion: existing?.version ?? -1,
      });
      applyUpdatedCells(updated);
      redoStack.current.push(action);
    } catch {
      showToast('Could not undo — the cell may have changed', 'error');
    }
  }

  async function handleRedo() {
    const action = redoStack.current.pop();
    if (!action) return;
    const existing = cellAt(action.row, action.col);
    try {
      const updated = await svc.updateCell(activeSheetId, {
        row: action.row, col: action.col,
        rawInput: action.nextRaw,
        expectedVersion: existing?.version ?? -1,
      });
      applyUpdatedCells(updated);
      undoStack.current.push(action);
    } catch {
      showToast('Could not redo — the cell may have changed', 'error');
    }
  }

  function handleKeyDown(e) {
    if (editing) {
      if (e.key === 'Enter') { e.preventDefault(); commitEdit(1, 0); }
      else if (e.key === 'Tab') { e.preventDefault(); commitEdit(0, 1); }
      else if (e.key === 'Escape') { e.preventDefault(); setEditing(false); }
      return;
    }

    if ((e.ctrlKey || e.metaKey) && e.key.toLowerCase() === 'z') { e.preventDefault(); handleUndo(); return; }
    if ((e.ctrlKey || e.metaKey) && e.key.toLowerCase() === 'y') { e.preventDefault(); handleRedo(); return; }

    if (e.key === 'ArrowUp') { e.preventDefault(); moveSelection(-1, 0); }
    else if (e.key === 'ArrowDown') { e.preventDefault(); moveSelection(1, 0); }
    else if (e.key === 'ArrowLeft') { e.preventDefault(); moveSelection(0, -1); }
    else if (e.key === 'ArrowRight') { e.preventDefault(); moveSelection(0, 1); }
    else if (e.key === 'Enter' || e.key === 'F2') { e.preventDefault(); startEdit(selected.row, selected.col); }
    else if (e.key === 'Delete' || e.key === 'Backspace') {
      e.preventDefault();
      quickSave(selected.row, selected.col, '');
    } else if (e.key.length === 1 && !e.ctrlKey && !e.metaKey) {
      startEdit(selected.row, selected.col, e.key);
    }
  }

  async function quickSave(row, col, rawInput) {
    const existing = cellAt(row, col);
    setSaveStatus('Saving...');
    try {
      const updated = await svc.updateCell(activeSheetId, {
        row, col, rawInput, expectedVersion: existing?.version ?? -1,
      });
      applyUpdatedCells(updated);
      setSaveStatus('Saved');
    } catch (err) {
      setSaveStatus('Offline');
      showToast(err.message || 'Could not save', 'error');
    }
  }

  async function handleAddSheet() {
    try {
      const sheet = await svc.createSheet(spreadsheetId, null);
      setSheets((prev) => [...prev, sheet]);
      setActiveSheetId(sheet.id);
    } catch (err) {
      showToast(err.message || 'Could not add sheet', 'error');
    }
  }

  async function handleRenameSheet(sheet) {
    const name = window.prompt('Rename sheet', sheet.name);
    if (!name || !name.trim() || name === sheet.name) return;
    try {
      const updated = await svc.renameSheet(sheet.id, name.trim());
      setSheets((prev) => prev.map((s) => (s.id === sheet.id ? updated : s)));
    } catch (err) {
      showToast(err.message || 'Could not rename sheet', 'error');
    }
  }

  async function handleDuplicateSheet(sheet) {
    try {
      const copy = await svc.duplicateSheet(sheet.id);
      setSheets((prev) => [...prev, copy]);
      setActiveSheetId(copy.id);
    } catch (err) {
      showToast(err.message || 'Could not duplicate sheet', 'error');
    }
  }

  async function handleDeleteSheet(sheet) {
    if (!window.confirm(`Delete "${sheet.name}"? This can't be undone.`)) return;
    try {
      await svc.deleteSheet(sheet.id);
      const remaining = sheets.filter((s) => s.id !== sheet.id);
      setSheets(remaining);
      if (activeSheetId === sheet.id) setActiveSheetId(remaining[0]?.id ?? null);
    } catch (err) {
      showToast(err.message || 'Could not delete sheet', 'error');
    }
  }

  const columns = useMemo(() => Array.from({ length: colCount }, (_, i) => i), [colCount]);
  const rows = useMemo(() => Array.from({ length: rowCount }, (_, i) => i), [rowCount]);

  if (loading) return <Loading full />;
  if (!spreadsheet) return null;

  return (
    <div style={{ display: 'flex', flexDirection: 'column', height: 'calc(100vh - 100px)' }} onKeyDown={handleKeyDown} tabIndex={0}>
      <div style={{ display: 'flex', alignItems: 'center', gap: 12, marginBottom: 10 }}>
        <button className="btn btn-ghost" onClick={() => navigate(`/w/${workspaceId}/sheets`)}>← Sheets</button>
        <h1 style={{ fontSize: 16, fontWeight: 700 }}>{spreadsheet.name}</h1>
        <span style={{ fontSize: 12, color: 'var(--text-tertiary)', marginLeft: 'auto' }}>{saveStatus}</span>
        <button className="btn btn-ghost" style={{ padding: '5px 8px' }} onClick={handleUndo} title="Undo (Ctrl+Z)">↶</button>
        <button className="btn btn-ghost" style={{ padding: '5px 8px' }} onClick={handleRedo} title="Redo (Ctrl+Y)">↷</button>
      </div>

      {/* Formula bar */}
      <div style={{ display: 'flex', alignItems: 'center', gap: 8, marginBottom: 8, border: '1px solid var(--border-strong)', borderRadius: 6, padding: '6px 10px', background: 'var(--surface)' }}>
        <span style={{ fontSize: 12.5, fontFamily: 'var(--font-mono)', color: 'var(--text-secondary)', minWidth: 40 }}>
          {formatRef(selected.row, selected.col)}
        </span>
        <input
          value={editing ? editValue : (cellAt(selected.row, selected.col)?.rawInput || '')}
          onChange={(e) => { setEditing(true); setEditValue(e.target.value); }}
          onFocus={() => { if (!editing) startEdit(selected.row, selected.col); }}
          onKeyDown={(e) => { if (e.key === 'Enter') { e.preventDefault(); commitEdit(1, 0); } }}
          placeholder="Enter a value or formula, e.g. =SUM(A1:A5)"
          style={{ flex: 1, border: 'none', outline: 'none', background: 'transparent', fontFamily: 'var(--font-mono)', fontSize: 13 }}
        />
      </div>

      {/* Grid */}
      <div ref={gridRef} style={{ flex: 1, overflow: 'auto', border: '1px solid var(--border)', borderRadius: 8 }}>
        <table style={{ borderCollapse: 'collapse', fontSize: 12.5 }}>
          <thead>
            <tr>
              <th style={headerCellStyle(40)} />
              {columns.map((c) => (
                <th key={c} style={headerCellStyle(90)}>{formatRef(0, c).replace(/\d+$/, '')}</th>
              ))}
            </tr>
          </thead>
          <tbody>
            {rows.map((r) => (
              <tr key={r}>
                <td style={rowHeaderStyle}>{r + 1}</td>
                {columns.map((c) => {
                  const cell = cellAt(r, c);
                  const isSelected = selected.row === r && selected.col === c;
                  const isEditing = isSelected && editing;
                  const isError = cell?.valueType === 'ERROR';
                  return (
                    <td
                      key={c}
                      onClick={() => { setSelected({ row: r, col: c }); setEditing(false); }}
                      onDoubleClick={() => startEdit(r, c)}
                      style={{
                        ...cellStyle,
                        outline: isSelected ? '2px solid var(--accent)' : 'none',
                        outlineOffset: -2,
                        color: isError ? 'var(--danger)' : 'var(--text)',
                        background: isError ? 'rgba(179,57,44,0.06)' : 'var(--surface)',
                      }}
                      title={cell?.modifiedAt ? `Last edited ${new Date(cell.modifiedAt).toLocaleString()}` : undefined}
                    >
                      {isEditing ? (
                        <input
                          autoFocus
                          value={editValue}
                          onChange={(e) => setEditValue(e.target.value)}
                          onBlur={() => commitEdit(1, 0)}
                          style={{ width: '100%', border: 'none', outline: 'none', background: 'transparent', fontFamily: 'var(--font-mono)', fontSize: 12.5 }}
                        />
                      ) : (
                        cell?.computedValue ?? ''
                      )}
                    </td>
                  );
                })}
              </tr>
            ))}
          </tbody>
        </table>
      </div>

      {/* Sheet tabs */}
      <div style={{ display: 'flex', alignItems: 'center', gap: 4, marginTop: 8, borderTop: '1px solid var(--border)', paddingTop: 8 }}>
        {sheets.map((s) => (
          <div
            key={s.id}
            onClick={() => setActiveSheetId(s.id)}
            onDoubleClick={() => handleRenameSheet(s)}
            style={{
              padding: '6px 12px',
              borderRadius: 6,
              fontSize: 12.5,
              cursor: 'pointer',
              background: s.id === activeSheetId ? 'var(--sidebar-bg)' : 'transparent',
              fontWeight: s.id === activeSheetId ? 600 : 500,
              color: s.id === activeSheetId ? 'var(--text)' : 'var(--text-secondary)',
              display: 'flex',
              alignItems: 'center',
              gap: 6,
            }}
          >
            {s.name}
            {s.id === activeSheetId && (
              <span style={{ display: 'flex', gap: 4 }}>
                <span onClick={(e) => { e.stopPropagation(); handleDuplicateSheet(s); }} title="Duplicate" style={{ opacity: 0.6 }}>⧉</span>
                <span onClick={(e) => { e.stopPropagation(); handleDeleteSheet(s); }} title="Delete" style={{ opacity: 0.6 }}>✕</span>
              </span>
            )}
          </div>
        ))}
        <button className="btn btn-ghost" style={{ padding: '4px 10px' }} onClick={handleAddSheet}>+ Sheet</button>
      </div>
    </div>
  );
}

function headerCellStyle(width) {
  return {
    minWidth: width,
    width,
    padding: '6px 8px',
    background: 'var(--sidebar-bg)',
    borderBottom: '1px solid var(--border)',
    borderRight: '1px solid var(--border)',
    position: 'sticky',
    top: 0,
    fontWeight: 600,
    color: 'var(--text-secondary)',
    textAlign: 'center',
  };
}

const rowHeaderStyle = {
  minWidth: 40,
  width: 40,
  padding: '4px 8px',
  background: 'var(--sidebar-bg)',
  borderBottom: '1px solid var(--border)',
  borderRight: '1px solid var(--border)',
  position: 'sticky',
  left: 0,
  fontWeight: 600,
  color: 'var(--text-secondary)',
  textAlign: 'center',
};

const cellStyle = {
  minWidth: 90,
  width: 90,
  height: 26,
  padding: '2px 6px',
  borderBottom: '1px solid var(--border)',
  borderRight: '1px solid var(--border)',
  whiteSpace: 'nowrap',
  overflow: 'hidden',
  textOverflow: 'ellipsis',
  cursor: 'cell',
  fontFamily: 'var(--font-mono)',
};
