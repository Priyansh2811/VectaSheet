import { useEffect, useState } from 'react';
import { useNavigate, useParams } from 'react-router-dom';
import EmptyState from '../components/EmptyState';
import Loading from '../components/Loading';
import { useToast } from '../context/ToastContext';
import * as documentService from '../services/documentService';

export default function DocumentsList() {
  const { workspaceId } = useParams();
  const navigate = useNavigate();
  const { showToast } = useToast();

  const [docs, setDocs] = useState([]);
  const [loading, setLoading] = useState(true);
  const [creating, setCreating] = useState(false);

  useEffect(() => {
    document.title = 'Docs — VectaSheet';
    load();
  }, [workspaceId]);

  async function load() {
    setLoading(true);
    try {
      const data = await documentService.listDocuments(workspaceId);
      setDocs(data);
    } catch (err) {
      showToast(err.message || 'Could not load documents', 'error');
    } finally {
      setLoading(false);
    }
  }

  async function handleCreate() {
    setCreating(true);
    try {
      const doc = await documentService.createDocument(workspaceId, 'Untitled document');
      navigate(`/w/${workspaceId}/docs/${doc.id}`);
    } catch (err) {
      showToast(err.message || 'Could not create document', 'error');
    } finally {
      setCreating(false);
    }
  }

  async function handleDelete(e, doc) {
    e.stopPropagation();
    if (!window.confirm(`Delete "${doc.title}"?`)) return;
    try {
      await documentService.deleteDocument(doc.id);
      setDocs((prev) => prev.filter((d) => d.id !== doc.id));
      showToast('Document deleted', 'success');
    } catch (err) {
      showToast(err.message || 'Could not delete document', 'error');
    }
  }

  return (
    <div style={{ maxWidth: 900 }}>
      <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: 18 }}>
        <h1 style={{ fontSize: 20, fontWeight: 700 }}>Docs</h1>
        <button className="btn btn-primary" onClick={handleCreate} disabled={creating}>
          + New Document
        </button>
      </div>

      {loading && <Loading />}

      {!loading && docs.length === 0 && (
        <EmptyState
          title="No documents yet."
          description="Create your first document to start writing — with formatting, comments and full version history."
          actionLabel="Create Document"
          onAction={handleCreate}
        />
      )}

      {!loading && docs.length > 0 && (
        <div style={{ display: 'flex', flexDirection: 'column', gap: 2 }}>
          {docs.map((d) => (
            <div
              key={d.id}
              onClick={() => navigate(`/w/${workspaceId}/docs/${d.id}`)}
              className="card"
              style={{
                padding: '14px 16px',
                cursor: 'pointer',
                display: 'flex',
                alignItems: 'center',
                justifyContent: 'space-between',
                marginBottom: 6,
              }}
            >
              <div style={{ display: 'flex', alignItems: 'center', gap: 12 }}>
                <span style={{ fontSize: 16 }}>📄</span>
                <div>
                  <div style={{ fontWeight: 600, fontSize: 14 }}>{d.title || 'Untitled document'}</div>
                  <div style={{ fontSize: 12, color: 'var(--text-tertiary)' }}>
                    Edited {new Date(d.updatedAt).toLocaleString()}
                  </div>
                </div>
              </div>
              <button className="btn btn-danger" style={{ padding: '4px 10px', fontSize: 12 }} onClick={(e) => handleDelete(e, d)}>
                Delete
              </button>
            </div>
          ))}
        </div>
      )}
    </div>
  );
}
