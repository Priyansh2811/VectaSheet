import { useEffect, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';
import { useToast } from '../context/ToastContext';
import { listWorkspaces, createWorkspace } from '../services/workspaceService';
import TopBar from '../components/TopBar';
import EmptyState from '../components/EmptyState';
import Loading from '../components/Loading';

function greeting() {
  const h = new Date().getHours();
  if (h < 12) return 'Good morning';
  if (h < 18) return 'Good afternoon';
  return 'Good evening';
}

export default function Dashboard() {
  const { user } = useAuth();
  const { showToast } = useToast();
  const navigate = useNavigate();

  const [workspaces, setWorkspaces] = useState([]);
  const [loading, setLoading] = useState(true);
  const [creating, setCreating] = useState(false);

  useEffect(() => {
    document.title = 'Dashboard — VectaSheet';
    load();
  }, []);

  async function load() {
    setLoading(true);
    try {
      const data = await listWorkspaces();
      setWorkspaces(data);
    } catch (err) {
      showToast(err.message || 'Could not load your workspaces', 'error');
    } finally {
      setLoading(false);
    }
  }

  async function handleCreateWorkspace() {
    const name = window.prompt('Name your new workspace');
    if (!name || !name.trim()) return;

    setCreating(true);
    try {
      const ws = await createWorkspace({ name: name.trim() });
      showToast('Workspace created', 'success');
      navigate(`/w/${ws.id}/overview`);
    } catch (err) {
      showToast(err.message || 'Could not create workspace', 'error');
    } finally {
      setCreating(false);
    }
  }

  return (
    <div style={{ minHeight: '100vh', background: 'var(--bg)' }}>
      <TopBar />
      <main style={{ maxWidth: 1040, margin: '0 auto', padding: '40px 24px' }}>
        <nav style={{ fontSize: 12.5, color: 'var(--text-tertiary)', marginBottom: 18 }}>
          Home / Dashboard
        </nav>

        <h1 style={{ fontSize: 24, fontWeight: 700, letterSpacing: '-0.01em' }}>
          {greeting()}, {user?.name?.split(' ')[0] || 'there'}
        </h1>
        <p style={{ marginTop: 6, fontSize: 13.5, color: 'var(--text-secondary)' }}>
          Pick up where you left off, or start something new.
        </p>

        <section style={{ marginTop: 32 }}>
          <div style={{ display: 'flex', gap: 10, flexWrap: 'wrap' }}>
            <button className="btn btn-primary" onClick={handleCreateWorkspace} disabled={creating}>
              + New Workspace
            </button>
            <button className="btn btn-secondary" disabled title="Open a workspace first">New Document</button>
            <button className="btn btn-secondary" disabled title="Open a workspace first">New Spreadsheet</button>
            <button className="btn btn-secondary" disabled title="Open a workspace first">New Canvas</button>
            <button className="btn btn-secondary" disabled title="Open a workspace first">New Project</button>
            <button className="btn btn-secondary" disabled title="Coming in a later phase">Import File</button>
          </div>
        </section>

        <section style={{ marginTop: 36 }}>
          <h2 style={{ fontSize: 15, fontWeight: 600, marginBottom: 14 }}>Your workspaces</h2>

          {loading && <Loading />}

          {!loading && workspaces.length === 0 && (
            <EmptyState
              title="No workspaces yet."
              description="Create your first workspace to start organizing projects, sheets, canvases and tasks."
              actionLabel="Create Workspace"
              onAction={handleCreateWorkspace}
            />
          )}

          {!loading && workspaces.length > 0 && (
            <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fill, minmax(220px, 1fr))', gap: 14 }}>
              {workspaces.map((ws) => (
                <button
                  key={ws.id}
                  className="card"
                  onClick={() => navigate(`/w/${ws.id}/overview`)}
                  style={{
                    padding: 16,
                    textAlign: 'left',
                    cursor: 'pointer',
                    display: 'flex',
                    flexDirection: 'column',
                    gap: 6,
                  }}
                >
                  <span style={{ fontWeight: 600, fontSize: 14 }}>{ws.name}</span>
                  <span style={{ fontSize: 12.5, color: 'var(--text-secondary)' }}>
                    {ws.memberCount} member{ws.memberCount === 1 ? '' : 's'} · {ws.myRole?.toLowerCase()}
                  </span>
                </button>
              ))}
            </div>
          )}
        </section>
      </main>
    </div>
  );
}
