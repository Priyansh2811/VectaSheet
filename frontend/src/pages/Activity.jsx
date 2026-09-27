import { useEffect, useState } from 'react';
import { useParams } from 'react-router-dom';
import EmptyState from '../components/EmptyState';
import Loading from '../components/Loading';
import { useToast } from '../context/ToastContext';
import * as activityService from '../services/activityService';
import * as workspaceService from '../services/workspaceService';

const ACTION_VERBS = {
  CREATED: 'created', EDITED: 'edited', DELETED: 'deleted', MOVED: 'moved',
  SHARED: 'shared', COMMENTED: 'commented on', ASSIGNED: 'assigned',
  COMPLETED: 'completed', RESTORED: 'restored a version of', IMPORTED: 'imported', EXPORTED: 'exported', JOINED: 'joined',
};

const ENTITY_ICONS = { DOCUMENT: '📄', SPREADSHEET: '▦', TASK: '☑', WORKSPACE: '◧', MEMBER: '👤' };

export default function Activity() {
  const { workspaceId } = useParams();
  const { showToast } = useToast();

  const [entries, setEntries] = useState([]);
  const [members, setMembers] = useState([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    document.title = 'Activity — VectaSheet';
    load();
  }, [workspaceId]);

  async function load() {
    setLoading(true);
    try {
      const [act, mem] = await Promise.all([
        activityService.listActivity(workspaceId),
        workspaceService.listMembers(workspaceId),
      ]);
      setEntries(act);
      setMembers(mem);
    } catch (err) {
      showToast(err.message || 'Could not load activity', 'error');
    } finally {
      setLoading(false);
    }
  }

  function actorName(id) {
    return members.find((m) => m.userId === id)?.name || 'Someone';
  }

  if (loading) return <Loading />;

  return (
    <div style={{ maxWidth: 700 }}>
      <h1 style={{ fontSize: 20, fontWeight: 700, marginBottom: 18 }}>Activity</h1>

      {entries.length === 0 && (
        <EmptyState title="No activity yet." description="Actions across this workspace — creating, editing, completing, sharing — will show up here as they happen." />
      )}

      <div style={{ display: 'flex', flexDirection: 'column' }}>
        {entries.map((e) => (
          <div key={e.id} style={{ display: 'flex', gap: 10, padding: '10px 0', borderBottom: '1px solid var(--border)' }}>
            <span style={{ fontSize: 15 }}>{ENTITY_ICONS[e.entityType] || '•'}</span>
            <div style={{ flex: 1 }}>
              <div style={{ fontSize: 13 }}>
                <strong>{actorName(e.actorId)}</strong> {ACTION_VERBS[e.action] || e.action.toLowerCase()}
                {e.entityLabel ? <> "{e.entityLabel}"</> : null}
              </div>
              <div style={{ fontSize: 11.5, color: 'var(--text-tertiary)' }}>
                {new Date(e.createdAt).toLocaleString()}
              </div>
            </div>
          </div>
        ))}
      </div>
    </div>
  );
}
