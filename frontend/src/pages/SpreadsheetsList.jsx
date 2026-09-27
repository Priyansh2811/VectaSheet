import { useEffect, useState } from 'react';
import { useNavigate, useParams } from 'react-router-dom';
import EmptyState from '../components/EmptyState';
import Loading from '../components/Loading';
import { useToast } from '../context/ToastContext';
import * as spreadsheetService from '../services/spreadsheetService';

export default function SpreadsheetsList() {
  const { workspaceId } = useParams();
  const navigate = useNavigate();
  const { showToast } = useToast();

  const [sheets, setSheets] = useState([]);
  const [loading, setLoading] = useState(true);
  const [creating, setCreating] = useState(false);

  useEffect(() => {
    document.title = 'Sheets — VectaSheet';
    load();
  }, [workspaceId]);

  async function load() {
    setLoading(true);
    try {
      const data = await spreadsheetService.listSpreadsheets(workspaceId);
      setSheets(data);
    } catch (err) {
      showToast(err.message || 'Could not load spreadsheets', 'error');
    } finally {
      setLoading(false);
    }
  }

  async function handleCreate() {
    const name = window.prompt('Name your spreadsheet', 'Untitled Spreadsheet');
    if (!name || !name.trim()) return;
    setCreating(true);
    try {
      const sheet = await spreadsheetService.createSpreadsheet(workspaceId, name.trim());
      navigate(`/w/${workspaceId}/sheets/${sheet.id}`);
    } catch (err) {
      showToast(err.message || 'Could not create spreadsheet', 'error');
    } finally {
      setCreating(false);
    }
  }

  return (
    <div style={{ maxWidth: 900 }}>
      <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: 18 }}>
        <h1 style={{ fontSize: 20, fontWeight: 700 }}>Sheets</h1>
        <button className="btn btn-primary" onClick={handleCreate} disabled={creating}>
          + New Spreadsheet
        </button>
      </div>

      {loading && <Loading />}

      {!loading && sheets.length === 0 && (
        <EmptyState
          title="No spreadsheets yet."
          description="Create your first spreadsheet to start working with rows, columns and formulas."
          actionLabel="Create Spreadsheet"
          onAction={handleCreate}
        />
      )}

      {!loading && sheets.length > 0 && (
        <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fill, minmax(200px, 1fr))', gap: 12 }}>
          {sheets.map((s) => (
            <button
              key={s.id}
              className="card"
              onClick={() => navigate(`/w/${workspaceId}/sheets/${s.id}`)}
              style={{ padding: 16, textAlign: 'left', cursor: 'pointer' }}
            >
              <div style={{ fontWeight: 600, fontSize: 14 }}>▦ {s.name}</div>
              <div style={{ fontSize: 12, color: 'var(--text-tertiary)', marginTop: 6 }}>
                Updated {new Date(s.updatedAt).toLocaleDateString()}
              </div>
            </button>
          ))}
        </div>
      )}
    </div>
  );
}
