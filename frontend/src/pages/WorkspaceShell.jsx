import { useEffect, useState } from 'react';
import { Outlet, useParams, Link, useLocation } from 'react-router-dom';
import Sidebar from '../components/Sidebar';
import TopBar from '../components/TopBar';
import Loading from '../components/Loading';
import { useToast } from '../context/ToastContext';
import { getWorkspace } from '../services/workspaceService';

const MODULE_LABELS = {
  overview: 'Overview',
  docs: 'Docs',
  canvas: 'Canvas',
  sheets: 'Sheets',
  tasks: 'Tasks',
  board: 'Board',
  calendar: 'Calendar',
  dashboard: 'Dashboard',
  files: 'Files',
  automate: 'Automate',
  activity: 'Activity',
  settings: 'Settings',
};

export default function WorkspaceShell() {
  const { workspaceId } = useParams();
  const location = useLocation();
  const { showToast } = useToast();

  const [workspace, setWorkspace] = useState(null);
  const [loading, setLoading] = useState(true);
  const [notFound, setNotFound] = useState(false);

  useEffect(() => {
    let cancelled = false;
    async function load() {
      setLoading(true);
      setNotFound(false);
      try {
        const ws = await getWorkspace(workspaceId);
        if (!cancelled) setWorkspace(ws);
      } catch (err) {
        if (!cancelled) {
          if (err.status === 404 || err.status === 403) {
            setNotFound(true);
          } else {
            showToast(err.message || 'Could not load workspace', 'error');
          }
        }
      } finally {
        if (!cancelled) setLoading(false);
      }
    }
    load();
    return () => { cancelled = true; };
  }, [workspaceId]);

  useEffect(() => {
    const segs = location.pathname.split('/').filter(Boolean); // ['w', workspaceId, module, ...rest]
    const seg = segs[2];
    const label = MODULE_LABELS[seg] || 'Workspace';
    document.title = workspace ? `${label} — ${workspace.name} — VectaSheet` : 'VectaSheet';
  }, [location.pathname, workspace]);

  if (loading) return <Loading full />;

  if (notFound) {
    return (
      <div style={{ minHeight: '100vh', display: 'flex', alignItems: 'center', justifyContent: 'center', flexDirection: 'column', gap: 10 }}>
        <h1 style={{ fontSize: 20, fontWeight: 700 }}>Workspace not found</h1>
        <p style={{ fontSize: 13.5, color: 'var(--text-secondary)' }}>
          It may have been deleted, or you don't have access to it.
        </p>
        <Link to="/dashboard" className="btn btn-secondary" style={{ marginTop: 8 }}>Back to Dashboard</Link>
      </div>
    );
  }

  const activeSegment = location.pathname.split('/').filter(Boolean)[2];

  return (
    <div style={{ display: 'flex', height: '100vh', overflow: 'hidden' }}>
      <Sidebar workspaceName={workspace?.name} />
      <div style={{ flex: 1, display: 'flex', flexDirection: 'column', minWidth: 0 }}>
        <TopBar />
        <div style={{ padding: '10px 20px 0', fontSize: 12.5, color: 'var(--text-tertiary)' }}>
          <Link to="/dashboard" style={{ color: 'var(--text-tertiary)' }}>Dashboard</Link>
          {' / '}
          <span style={{ color: 'var(--text-secondary)' }}>{workspace?.name}</span>
          {' / '}
          <span>{MODULE_LABELS[activeSegment] || activeSegment}</span>
        </div>
        <main style={{ flex: 1, overflow: 'auto', padding: 20 }}>
          <Outlet context={{ workspace, setWorkspace }} />
        </main>
      </div>
    </div>
  );
}
