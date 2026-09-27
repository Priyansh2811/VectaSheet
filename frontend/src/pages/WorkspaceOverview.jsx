import { useOutletContext, Link, useParams } from 'react-router-dom';

export default function WorkspaceOverview() {
  const { workspace } = useOutletContext();
  const { workspaceId } = useParams();

  if (!workspace) return null;

  return (
    <div style={{ maxWidth: 900 }}>
      <h1 style={{ fontSize: 20, fontWeight: 700 }}>{workspace.name}</h1>
      {workspace.description && (
        <p style={{ marginTop: 6, fontSize: 13.5, color: 'var(--text-secondary)' }}>{workspace.description}</p>
      )}

      <div style={{ display: 'flex', gap: 8, marginTop: 16 }}>
        <span style={{ fontSize: 12, padding: '4px 10px', borderRadius: 14, border: '1px solid var(--border-strong)' }}>
          {workspace.memberCount} member{workspace.memberCount === 1 ? '' : 's'}
        </span>
        <span style={{ fontSize: 12, padding: '4px 10px', borderRadius: 14, border: '1px solid var(--border-strong)' }}>
          Your role: {workspace.myRole}
        </span>
      </div>

      <div style={{ marginTop: 32, display: 'grid', gridTemplateColumns: 'repeat(auto-fill, minmax(200px, 1fr))', gap: 14 }}>
        {[
          { to: 'docs', label: 'Docs', note: 'Rich text documents with autosave, history and comments' },
          { to: 'sheets', label: 'Sheets', note: 'Spreadsheet engine with a real formula language' },
          { to: 'tasks', label: 'Tasks', note: 'Task list with subtasks, priority and assignees' },
          { to: 'board', label: 'Board', note: 'Drag-and-drop Kanban, synced with Tasks' },
          { to: 'calendar', label: 'Calendar', note: 'Month view of events and task due dates' },
          { to: 'files', label: 'Files', note: 'Upload, download and manage workspace files' },
          { to: 'canvas', label: 'Canvas', note: 'Infinite canvas — coming in the next build phase' },
          { to: 'dashboard', label: 'Dashboard', note: 'Analytics widgets — coming in the next build phase' },
        ].map((m) => (
          <Link
            key={m.to}
            to={`/w/${workspaceId}/${m.to}`}
            className="card"
            style={{ padding: 16, display: 'block' }}
          >
            <div style={{ fontWeight: 600, fontSize: 14 }}>{m.label}</div>
            <div style={{ fontSize: 12.5, color: 'var(--text-tertiary)', marginTop: 4 }}>{m.note}</div>
          </Link>
        ))}
      </div>
    </div>
  );
}
