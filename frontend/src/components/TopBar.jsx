import { useEffect, useRef, useState } from 'react';
import { useNavigate, useParams } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';
import { useToast } from '../context/ToastContext';
import ThemeToggle from './ThemeToggle';
import * as searchService from '../services/searchService';
import * as notificationService from '../services/notificationService';

const RESULT_ROUTE = { DOCUMENT: 'docs', SPREADSHEET: 'sheets', TASK: 'tasks' };

export default function TopBar() {
  const { user, logout } = useAuth();
  const { showToast } = useToast();
  const navigate = useNavigate();
  const { workspaceId } = useParams();

  const [query, setQuery] = useState('');
  const [results, setResults] = useState([]);
  const [showResults, setShowResults] = useState(false);
  const searchTimer = useRef(null);

  const [notifOpen, setNotifOpen] = useState(false);
  const [notifications, setNotifications] = useState([]);
  const [unread, setUnread] = useState(0);

  useEffect(() => {
    if (!user) return;
    notificationService.unreadCount().then((r) => setUnread(r.count)).catch(() => {});
    const interval = setInterval(() => {
      notificationService.unreadCount().then((r) => setUnread(r.count)).catch(() => {});
    }, 30000);
    return () => clearInterval(interval);
  }, [user]);

  function handleSearchChange(e) {
    const q = e.target.value;
    setQuery(q);
    clearTimeout(searchTimer.current);
    if (!workspaceId || !q.trim()) {
      setResults([]);
      setShowResults(false);
      return;
    }
    searchTimer.current = setTimeout(async () => {
      try {
        const data = await searchService.search(workspaceId, q.trim());
        setResults(data);
        setShowResults(true);
      } catch {
        setResults([]);
      }
    }, 300);
  }

  function handleResultClick(r) {
    setShowResults(false);
    setQuery('');
    const route = RESULT_ROUTE[r.type];
    if (route === 'tasks') navigate(`/w/${workspaceId}/tasks`);
    else navigate(`/w/${workspaceId}/${route}/${r.id}`);
  }

  async function openNotifications() {
    setNotifOpen((v) => !v);
    if (!notifOpen) {
      try {
        const list = await notificationService.listNotifications();
        setNotifications(list);
      } catch (err) {
        showToast(err.message || 'Could not load notifications', 'error');
      }
    }
  }

  async function handleMarkAllRead() {
    try {
      await notificationService.markAllRead();
      setNotifications((prev) => prev.map((n) => ({ ...n, read: true })));
      setUnread(0);
    } catch (err) {
      showToast(err.message || 'Could not update notifications', 'error');
    }
  }

  async function handleNotificationClick(n) {
    try {
      await notificationService.setRead(n.id, true);
      setNotifications((prev) => prev.map((x) => (x.id === n.id ? { ...x, read: true } : x)));
      setUnread((c) => Math.max(0, c - (n.read ? 0 : 1)));
    } catch {
      // non-fatal
    }
  }

  async function handleLogout() {
    try {
      await logout();
      showToast('Signed out', 'success');
      navigate('/login');
    } catch {
      showToast('Could not sign out. Please try again.', 'error');
    }
  }

  return (
    <header
      style={{
        height: 52,
        borderBottom: '1px solid var(--border)',
        display: 'flex',
        alignItems: 'center',
        justifyContent: 'space-between',
        padding: '0 16px',
        background: 'var(--bg)',
        flexShrink: 0,
        position: 'relative',
      }}
    >
      <div style={{ display: 'flex', alignItems: 'center', gap: 10 }}>
        <div style={{ fontWeight: 700, fontSize: 14, letterSpacing: '-0.01em' }}>VectaSheet</div>
      </div>

      <div style={{ flex: 1, maxWidth: 420, margin: '0 24px', position: 'relative' }}>
        <input
          value={query}
          onChange={handleSearchChange}
          onFocus={() => query && setShowResults(true)}
          onBlur={() => setTimeout(() => setShowResults(false), 150)}
          placeholder={workspaceId ? 'Search this workspace…' : 'Open a workspace to search'}
          disabled={!workspaceId}
          style={{
            width: '100%',
            background: 'var(--surface)',
            border: '1px solid var(--border-strong)',
            borderRadius: 6,
            padding: '7px 12px',
            fontSize: 13,
            color: 'var(--text)',
          }}
        />
        {showResults && (
          <div style={{ position: 'absolute', top: '110%', left: 0, right: 0, background: 'var(--surface)', border: '1px solid var(--border)', borderRadius: 8, boxShadow: 'var(--shadow)', zIndex: 50, maxHeight: 320, overflow: 'auto' }}>
            {results.length === 0 && <div style={{ padding: 12, fontSize: 12.5, color: 'var(--text-tertiary)' }}>No results</div>}
            {results.map((r) => (
              <div
                key={r.type + r.id}
                onMouseDown={() => handleResultClick(r)}
                style={{ padding: '9px 12px', fontSize: 12.5, cursor: 'pointer', borderBottom: '1px solid var(--border)' }}
              >
                <div style={{ fontWeight: 500 }}>{r.title}</div>
                <div style={{ fontSize: 11, color: 'var(--text-tertiary)' }}>{r.snippet}</div>
              </div>
            ))}
          </div>
        )}
      </div>

      <div style={{ display: 'flex', alignItems: 'center', gap: 10 }}>
        <ThemeToggle />

        <div style={{ position: 'relative' }}>
          <button className="btn btn-ghost" onClick={openNotifications} style={{ position: 'relative', padding: '8px 10px' }}>
            🔔
            {unread > 0 && (
              <span style={{ position: 'absolute', top: 2, right: 2, background: 'var(--danger)', color: '#fff', fontSize: 9.5, borderRadius: 8, padding: '1px 4px', lineHeight: 1.2 }}>
                {unread}
              </span>
            )}
          </button>
          {notifOpen && (
            <div style={{ position: 'absolute', top: '110%', right: 0, width: 300, background: 'var(--surface)', border: '1px solid var(--border)', borderRadius: 8, boxShadow: 'var(--shadow)', zIndex: 50, maxHeight: 360, overflow: 'auto' }}>
              <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', padding: '8px 12px', borderBottom: '1px solid var(--border)' }}>
                <span style={{ fontSize: 12.5, fontWeight: 600 }}>Notifications</span>
                <button className="btn btn-ghost" style={{ fontSize: 11, padding: '2px 6px' }} onClick={handleMarkAllRead}>Mark all read</button>
              </div>
              {notifications.length === 0 && <div style={{ padding: 12, fontSize: 12.5, color: 'var(--text-tertiary)' }}>No notifications yet.</div>}
              {notifications.map((n) => (
                <div
                  key={n.id}
                  onClick={() => handleNotificationClick(n)}
                  style={{ padding: '10px 12px', fontSize: 12.5, borderBottom: '1px solid var(--border)', cursor: 'pointer', opacity: n.read ? 0.55 : 1, background: n.read ? 'transparent' : 'var(--sidebar-bg)' }}
                >
                  <div>{n.message}</div>
                  <div style={{ fontSize: 10.5, color: 'var(--text-tertiary)', marginTop: 2 }}>{new Date(n.createdAt).toLocaleString()}</div>
                </div>
              ))}
            </div>
          )}
        </div>

        <div
          title={user?.email}
          style={{
            width: 30,
            height: 30,
            borderRadius: '50%',
            background: 'var(--accent)',
            color: '#fff',
            display: 'flex',
            alignItems: 'center',
            justifyContent: 'center',
            fontSize: 12.5,
            fontWeight: 600,
          }}
        >
          {(user?.name || '?').slice(0, 1).toUpperCase()}
        </div>
        <button className="btn btn-ghost" onClick={handleLogout}>Sign out</button>
      </div>
    </header>
  );
}
