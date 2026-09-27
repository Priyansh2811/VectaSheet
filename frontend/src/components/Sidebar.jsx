import { useState } from 'react';
import { NavLink, useParams } from 'react-router-dom';

const NAV_ITEMS = [
  { key: 'overview', label: 'Overview', icon: '▤' },
  { key: 'docs', label: 'Docs', icon: '📄' },
  { key: 'canvas', label: 'Canvas', icon: '◇' },
  { key: 'sheets', label: 'Sheets', icon: '▦' },
  { key: 'tasks', label: 'Tasks', icon: '☑' },
  { key: 'board', label: 'Board', icon: '▥' },
  { key: 'calendar', label: 'Calendar', icon: '▧' },
  { key: 'dashboard', label: 'Dashboard', icon: '◫' },
  { key: 'files', label: 'Files', icon: '▢' },
  { key: 'automate', label: 'Automate', icon: '⚙' },
  { key: 'activity', label: 'Activity', icon: '◷' },
];

export default function Sidebar({ workspaceName }) {
  const [collapsed, setCollapsed] = useState(false);
  const { workspaceId } = useParams();

  return (
    <aside
      style={{
        width: collapsed ? 60 : 224,
        background: 'var(--sidebar-bg)',
        borderRight: '1px solid var(--border)',
        display: 'flex',
        flexDirection: 'column',
        transition: 'width 160ms ease',
        flexShrink: 0,
        height: '100%',
      }}
    >
      <div style={{ padding: '16px 14px', display: 'flex', alignItems: 'center', justifyContent: 'space-between' }}>
        {!collapsed && (
          <span style={{ fontSize: 13, fontWeight: 600, color: 'var(--text-secondary)', overflow: 'hidden', textOverflow: 'ellipsis', whiteSpace: 'nowrap' }}>
            {workspaceName || 'Workspace'}
          </span>
        )}
        <button
          className="btn btn-ghost"
          style={{ padding: '4px 8px', fontSize: 12 }}
          onClick={() => setCollapsed((c) => !c)}
          title={collapsed ? 'Expand sidebar' : 'Collapse sidebar'}
        >
          {collapsed ? '»' : '«'}
        </button>
      </div>

      <nav style={{ display: 'flex', flexDirection: 'column', gap: 2, padding: '4px 8px', flex: 1 }}>
        {NAV_ITEMS.map((item) => (
          <NavLink
            key={item.key}
            to={`/w/${workspaceId}/${item.key}`}
            style={({ isActive }) => ({
              display: 'flex',
              alignItems: 'center',
              gap: 10,
              padding: '8px 10px',
              borderRadius: 6,
              fontSize: 13.5,
              fontWeight: isActive ? 600 : 500,
              color: isActive ? 'var(--text)' : 'var(--text-secondary)',
              background: isActive ? 'var(--surface)' : 'transparent',
              border: isActive ? '1px solid var(--border)' : '1px solid transparent',
            })}
          >
            <span style={{ width: 16, textAlign: 'center' }}>{item.icon}</span>
            {!collapsed && <span>{item.label}</span>}
          </NavLink>
        ))}
      </nav>

      <div style={{ padding: '8px' }}>
        <NavLink
          to={`/w/${workspaceId}/settings`}
          style={({ isActive }) => ({
            display: 'flex',
            alignItems: 'center',
            gap: 10,
            padding: '8px 10px',
            borderRadius: 6,
            fontSize: 13.5,
            color: isActive ? 'var(--text)' : 'var(--text-secondary)',
            background: isActive ? 'var(--surface)' : 'transparent',
          })}
        >
          <span style={{ width: 16, textAlign: 'center' }}>⚙</span>
          {!collapsed && <span>Settings</span>}
        </NavLink>
      </div>
    </aside>
  );
}
