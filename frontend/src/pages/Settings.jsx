import { useEffect, useState } from 'react';
import { useOutletContext } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';
import { useTheme } from '../context/ThemeContext';
import { useToast } from '../context/ToastContext';
import { apiRequest } from '../services/api';
import * as authService from '../services/authService';
import * as workspaceService from '../services/workspaceService';

const TABS = ['Account', 'Appearance', 'Workspace', 'Security'];

export default function Settings() {
  const [tab, setTab] = useState('Account');
  const { workspace, setWorkspace } = useOutletContext();

  return (
    <div style={{ maxWidth: 720 }}>
      <h1 style={{ fontSize: 20, fontWeight: 700, marginBottom: 18 }}>Settings</h1>

      <div style={{ display: 'flex', gap: 4, borderBottom: '1px solid var(--border)', marginBottom: 24 }}>
        {TABS.map((t) => (
          <button
            key={t}
            onClick={() => setTab(t)}
            className="btn btn-ghost"
            style={{
              borderRadius: 0,
              borderBottom: tab === t ? '2px solid var(--text)' : '2px solid transparent',
              color: tab === t ? 'var(--text)' : 'var(--text-secondary)',
              fontWeight: tab === t ? 600 : 500,
              padding: '8px 4px',
              marginRight: 18,
            }}
          >
            {t}
          </button>
        ))}
      </div>

      {tab === 'Account' && <AccountTab />}
      {tab === 'Appearance' && <AppearanceTab />}
      {tab === 'Workspace' && <WorkspaceTab workspace={workspace} setWorkspace={setWorkspace} />}
      {tab === 'Security' && <SecurityTab />}
    </div>
  );
}

function AccountTab() {
  const { user, setUser } = useAuth();
  const { showToast } = useToast();
  const [name, setName] = useState(user?.name || '');
  const [saving, setSaving] = useState(false);

  async function handleSave(e) {
    e.preventDefault();
    setSaving(true);
    try {
      const updated = await apiRequest('/users/me', { method: 'PUT', body: { name } });
      setUser(updated);
      showToast('Profile updated', 'success');
    } catch (err) {
      showToast(err.message || 'Could not update profile', 'error');
    } finally {
      setSaving(false);
    }
  }

  return (
    <form onSubmit={handleSave} style={{ maxWidth: 380 }}>
      <div className="field">
        <label>Name</label>
        <input value={name} onChange={(e) => setName(e.target.value)} />
      </div>
      <div className="field">
        <label>Email</label>
        <input value={user?.email || ''} disabled />
      </div>
      <button className="btn btn-primary" disabled={saving}>{saving ? 'Saving…' : 'Save changes'}</button>
    </form>
  );
}

function AppearanceTab() {
  const { theme, setTheme } = useTheme();

  return (
    <div style={{ maxWidth: 380 }}>
      <div className="field">
        <label>Theme</label>
        <div style={{ display: 'flex', gap: 8 }}>
          {['light', 'dark'].map((t) => (
            <button
              key={t}
              onClick={() => setTheme(t)}
              className={theme === t ? 'btn btn-primary' : 'btn btn-secondary'}
              style={{ flex: 1, textTransform: 'capitalize' }}
            >
              {t === 'dark' ? 'Dark black' : t}
            </button>
          ))}
        </div>
      </div>
      <p style={{ fontSize: 12.5, color: 'var(--text-tertiary)' }}>
        Your theme preference is saved to this browser and respected automatically on your next visit.
      </p>
    </div>
  );
}

function WorkspaceTab({ workspace, setWorkspace }) {
  const { showToast } = useToast();
  const [members, setMembers] = useState([]);
  const [loading, setLoading] = useState(true);
  const [inviteEmail, setInviteEmail] = useState('');
  const [inviteRole, setInviteRole] = useState('EDITOR');
  const [inviting, setInviting] = useState(false);

  const canManage = workspace && ['OWNER', 'ADMIN'].includes(workspace.myRole);

  useEffect(() => {
    if (workspace) load();
  }, [workspace?.id]);

  async function load() {
    setLoading(true);
    try {
      const data = await workspaceService.listMembers(workspace.id);
      setMembers(data);
    } catch (err) {
      showToast(err.message || 'Could not load members', 'error');
    } finally {
      setLoading(false);
    }
  }

  async function handleInvite(e) {
    e.preventDefault();
    if (!inviteEmail.trim()) return;
    setInviting(true);
    try {
      await workspaceService.inviteMember(workspace.id, { email: inviteEmail.trim(), role: inviteRole });
      showToast('Member added', 'success');
      setInviteEmail('');
      load();
    } catch (err) {
      showToast(err.message || 'Could not add member', 'error');
    } finally {
      setInviting(false);
    }
  }

  async function handleRoleChange(userId, role) {
    try {
      await workspaceService.updateMemberRole(workspace.id, userId, role);
      showToast('Role updated', 'success');
      load();
    } catch (err) {
      showToast(err.message || 'Could not update role', 'error');
    }
  }

  async function handleRemove(userId) {
    if (!window.confirm('Remove this member from the workspace?')) return;
    try {
      await workspaceService.removeMember(workspace.id, userId);
      showToast('Member removed', 'success');
      load();
    } catch (err) {
      showToast(err.message || 'Could not remove member', 'error');
    }
  }

  if (!workspace) return null;

  return (
    <div style={{ maxWidth: 560 }}>
      <h3 style={{ fontSize: 14, fontWeight: 600, marginBottom: 12 }}>Members</h3>

      {canManage && (
        <form onSubmit={handleInvite} style={{ display: 'flex', gap: 8, marginBottom: 18 }}>
          <input
            placeholder="person@company.com"
            value={inviteEmail}
            onChange={(e) => setInviteEmail(e.target.value)}
            style={{ flex: 1, background: 'var(--surface)', border: '1px solid var(--border-strong)', borderRadius: 6, padding: '9px 12px', fontSize: 13 }}
          />
          <select
            value={inviteRole}
            onChange={(e) => setInviteRole(e.target.value)}
            style={{ background: 'var(--surface)', border: '1px solid var(--border-strong)', borderRadius: 6, padding: '9px 10px', fontSize: 13 }}
          >
            <option value="ADMIN">Admin</option>
            <option value="EDITOR">Editor</option>
            <option value="COMMENTER">Commenter</option>
            <option value="VIEWER">Viewer</option>
          </select>
          <button className="btn btn-primary" disabled={inviting}>{inviting ? 'Adding…' : 'Invite'}</button>
        </form>
      )}

      {loading ? (
        <div style={{ fontSize: 13, color: 'var(--text-tertiary)' }}>Loading members…</div>
      ) : (
        <div style={{ display: 'flex', flexDirection: 'column', gap: 2 }}>
          {members.map((m) => (
            <div key={m.userId} style={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between', padding: '10px 0', borderBottom: '1px solid var(--border)' }}>
              <div>
                <div style={{ fontSize: 13.5, fontWeight: 500 }}>{m.name}</div>
                <div style={{ fontSize: 12, color: 'var(--text-tertiary)' }}>{m.email}</div>
              </div>
              <div style={{ display: 'flex', alignItems: 'center', gap: 8 }}>
                {canManage && m.role !== 'OWNER' ? (
                  <select
                    value={m.role}
                    onChange={(e) => handleRoleChange(m.userId, e.target.value)}
                    style={{ fontSize: 12.5, background: 'var(--surface)', border: '1px solid var(--border-strong)', borderRadius: 6, padding: '5px 8px' }}
                  >
                    <option value="ADMIN">Admin</option>
                    <option value="EDITOR">Editor</option>
                    <option value="COMMENTER">Commenter</option>
                    <option value="VIEWER">Viewer</option>
                  </select>
                ) : (
                  <span style={{ fontSize: 12.5, color: 'var(--text-secondary)' }}>{m.role}</span>
                )}
                {canManage && m.role !== 'OWNER' && (
                  <button className="btn btn-danger" style={{ padding: '4px 8px', fontSize: 12 }} onClick={() => handleRemove(m.userId)}>
                    Remove
                  </button>
                )}
              </div>
            </div>
          ))}
        </div>
      )}
    </div>
  );
}

function SecurityTab() {
  const { showToast } = useToast();
  const [currentPassword, setCurrentPassword] = useState('');
  const [newPassword, setNewPassword] = useState('');
  const [saving, setSaving] = useState(false);
  const [error, setError] = useState('');

  async function handleSubmit(e) {
    e.preventDefault();
    if (newPassword.length < 8) {
      setError('New password must be at least 8 characters');
      return;
    }
    setError('');
    setSaving(true);
    try {
      await authService.changePassword(currentPassword, newPassword);
      showToast('Password changed', 'success');
      setCurrentPassword('');
      setNewPassword('');
    } catch (err) {
      showToast(err.message || 'Could not change password', 'error');
    } finally {
      setSaving(false);
    }
  }

  return (
    <form onSubmit={handleSubmit} style={{ maxWidth: 380 }}>
      <div className="field">
        <label>Current password</label>
        <input type="password" value={currentPassword} onChange={(e) => setCurrentPassword(e.target.value)} />
      </div>
      <div className="field">
        <label>New password</label>
        <input type="password" value={newPassword} onChange={(e) => setNewPassword(e.target.value)} />
        {error && <span className="field-error">{error}</span>}
      </div>
      <button className="btn btn-primary" disabled={saving}>{saving ? 'Updating…' : 'Change password'}</button>
    </form>
  );
}
