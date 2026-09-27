import { useEffect, useMemo, useState } from 'react';
import { useParams } from 'react-router-dom';
import EmptyState from '../components/EmptyState';
import Loading from '../components/Loading';
import TaskDetailPanel from '../components/TaskDetailPanel';
import { useToast } from '../context/ToastContext';
import * as taskService from '../services/taskService';
import * as workspaceService from '../services/workspaceService';

const PRIORITY_COLORS = { LOW: '#6b7280', MEDIUM: '#3d5a80', HIGH: '#c2751b', URGENT: '#b3392c' };

export default function TasksList() {
  const { workspaceId } = useParams();
  const { showToast } = useToast();

  const [tasks, setTasks] = useState([]);
  const [members, setMembers] = useState([]);
  const [loading, setLoading] = useState(true);
  const [statusFilter, setStatusFilter] = useState('ALL');
  const [assigneeFilter, setAssigneeFilter] = useState('ALL');
  const [activeTask, setActiveTask] = useState(null);
  const [newTitle, setNewTitle] = useState('');

  useEffect(() => {
    document.title = 'Tasks — VectaSheet';
    load();
  }, [workspaceId]);

  async function load() {
    setLoading(true);
    try {
      const [t, m] = await Promise.all([
        taskService.listTasks(workspaceId),
        workspaceService.listMembers(workspaceId),
      ]);
      setTasks(t);
      setMembers(m);
    } catch (err) {
      showToast(err.message || 'Could not load tasks', 'error');
    } finally {
      setLoading(false);
    }
  }

  async function handleCreate() {
    if (!newTitle.trim()) return;
    try {
      const task = await taskService.createTask(workspaceId, { title: newTitle.trim() });
      setTasks((prev) => [...prev, task]);
      setNewTitle('');
    } catch (err) {
      showToast(err.message || 'Could not create task', 'error');
    }
  }

  function handleUpdated(updated) {
    setTasks((prev) => prev.map((t) => (t.id === updated.id ? updated : t)));
    setActiveTask(updated);
  }

  function handleDeleted(id) {
    setTasks((prev) => prev.filter((t) => t.id !== id));
    setActiveTask(null);
  }

  const filtered = useMemo(() => {
    return tasks.filter((t) => {
      if (statusFilter !== 'ALL' && t.status !== statusFilter) return false;
      if (assigneeFilter !== 'ALL' && (t.assigneeId || 'none') !== assigneeFilter) return false;
      return true;
    });
  }, [tasks, statusFilter, assigneeFilter]);

  function memberName(id) {
    return members.find((m) => m.userId === id)?.name || 'Unassigned';
  }

  return (
    <div style={{ maxWidth: 900 }}>
      <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: 18 }}>
        <h1 style={{ fontSize: 20, fontWeight: 700 }}>Tasks</h1>
        <div style={{ display: 'flex', gap: 8 }}>
          <select value={statusFilter} onChange={(e) => setStatusFilter(e.target.value)} style={selectStyle}>
            <option value="ALL">All statuses</option>
            <option value="TODO">Todo</option>
            <option value="IN_PROGRESS">In Progress</option>
            <option value="REVIEW">Review</option>
            <option value="DONE">Done</option>
          </select>
          <select value={assigneeFilter} onChange={(e) => setAssigneeFilter(e.target.value)} style={selectStyle}>
            <option value="ALL">Everyone</option>
            <option value="none">Unassigned</option>
            {members.map((m) => <option key={m.userId} value={m.userId}>{m.name}</option>)}
          </select>
        </div>
      </div>

      <div style={{ display: 'flex', gap: 8, marginBottom: 16 }}>
        <input
          placeholder="Quick add a task and press Enter…"
          value={newTitle}
          onChange={(e) => setNewTitle(e.target.value)}
          onKeyDown={(e) => { if (e.key === 'Enter') handleCreate(); }}
          style={{ flex: 1, border: '1px solid var(--border-strong)', borderRadius: 6, padding: '8px 12px', fontSize: 13 }}
        />
        <button className="btn btn-primary" onClick={handleCreate}>Add</button>
      </div>

      {loading && <Loading />}

      {!loading && filtered.length === 0 && (
        <EmptyState title="No tasks yet." description="Add a task above, or switch to the Board view to drag tasks between columns." />
      )}

      {!loading && filtered.length > 0 && (
        <div style={{ display: 'flex', flexDirection: 'column', gap: 2 }}>
          {filtered.map((t) => (
            <div
              key={t.id}
              onClick={() => setActiveTask(t)}
              className="card"
              style={{ padding: '12px 14px', cursor: 'pointer', display: 'flex', alignItems: 'center', gap: 12, marginBottom: 4 }}
            >
              <span style={{ width: 8, height: 8, borderRadius: '50%', background: PRIORITY_COLORS[t.priority], flexShrink: 0 }} />
              <div style={{ flex: 1, minWidth: 0 }}>
                <div style={{ fontSize: 13.5, fontWeight: 500, textDecoration: t.status === 'DONE' ? 'line-through' : 'none' }}>
                  {t.title}
                </div>
                <div style={{ fontSize: 11.5, color: 'var(--text-tertiary)' }}>
                  {t.status.replace('_', ' ')} · {memberName(t.assigneeId)}
                  {t.dueDate ? ` · Due ${t.dueDate}` : ''}
                  {t.subtaskCount > 0 ? ` · ${t.subtaskDoneCount}/${t.subtaskCount} subtasks` : ''}
                </div>
              </div>
            </div>
          ))}
        </div>
      )}

      {activeTask && (
        <TaskDetailPanel
          task={activeTask}
          members={members}
          onClose={() => setActiveTask(null)}
          onUpdated={handleUpdated}
          onDeleted={handleDeleted}
        />
      )}
    </div>
  );
}

const selectStyle = {
  fontSize: 12.5,
  background: 'var(--surface)',
  border: '1px solid var(--border-strong)',
  borderRadius: 6,
  padding: '7px 10px',
};
