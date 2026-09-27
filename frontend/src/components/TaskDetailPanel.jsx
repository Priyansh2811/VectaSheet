import { useEffect, useState } from 'react';
import * as taskService from '../services/taskService';
import { useToast } from '../context/ToastContext';

const STATUS_OPTIONS = ['TODO', 'IN_PROGRESS', 'REVIEW', 'DONE'];
const PRIORITY_OPTIONS = ['LOW', 'MEDIUM', 'HIGH', 'URGENT'];

export default function TaskDetailPanel({ task, members, onClose, onUpdated, onDeleted }) {
  const { showToast } = useToast();
  const [title, setTitle] = useState(task.title);
  const [description, setDescription] = useState(task.description || '');
  const [status, setStatus] = useState(task.status);
  const [priority, setPriority] = useState(task.priority);
  const [assigneeId, setAssigneeId] = useState(task.assigneeId || '');
  const [dueDate, setDueDate] = useState(task.dueDate || '');
  const [subtasks, setSubtasks] = useState([]);
  const [newSubtask, setNewSubtask] = useState('');
  const [saving, setSaving] = useState(false);

  useEffect(() => {
    loadSubtasks();
  }, [task.id]);

  async function loadSubtasks() {
    try {
      const list = await taskService.listSubtasks(task.id);
      setSubtasks(list);
    } catch {
      // non-fatal; leave empty
    }
  }

  async function handleSave() {
    setSaving(true);
    try {
      const updated = await taskService.updateTask(task.id, {
        title, description, status, priority,
        assigneeId: assigneeId || '',
        dueDate: dueDate || '',
      });
      onUpdated(updated);
      showToast('Task saved', 'success');
    } catch (err) {
      showToast(err.message || 'Could not save task', 'error');
    } finally {
      setSaving(false);
    }
  }

  async function handleDelete() {
    if (!window.confirm('Delete this task?')) return;
    try {
      await taskService.deleteTask(task.id);
      onDeleted(task.id);
    } catch (err) {
      showToast(err.message || 'Could not delete task', 'error');
    }
  }

  async function handleAddSubtask() {
    if (!newSubtask.trim()) return;
    try {
      await taskService.createTask(task.workspaceId, { title: newSubtask.trim(), parentTaskId: task.id });
      setNewSubtask('');
      loadSubtasks();
    } catch (err) {
      showToast(err.message || 'Could not add subtask', 'error');
    }
  }

  async function toggleSubtask(sub) {
    try {
      await taskService.updateTask(sub.id, { status: sub.status === 'DONE' ? 'TODO' : 'DONE' });
      loadSubtasks();
    } catch (err) {
      showToast(err.message || 'Could not update subtask', 'error');
    }
  }

  return (
    <div
      onClick={onClose}
      style={{ position: 'fixed', inset: 0, background: 'rgba(0,0,0,0.25)', zIndex: 1000, display: 'flex', justifyContent: 'flex-end' }}
    >
      <div
        onClick={(e) => e.stopPropagation()}
        style={{ width: 420, maxWidth: '100%', height: '100%', background: 'var(--surface)', borderLeft: '1px solid var(--border)', padding: 20, overflow: 'auto' }}
      >
        <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: 16 }}>
          <span style={{ fontSize: 12, color: 'var(--text-tertiary)' }}>Task</span>
          <button className="btn btn-ghost" onClick={onClose}>✕</button>
        </div>

        <input
          value={title}
          onChange={(e) => setTitle(e.target.value)}
          style={{ width: '100%', fontSize: 16, fontWeight: 700, border: 'none', outline: 'none', background: 'transparent', marginBottom: 12 }}
        />

        <textarea
          rows={4}
          placeholder="Description…"
          value={description}
          onChange={(e) => setDescription(e.target.value)}
          style={{ width: '100%', border: '1px solid var(--border-strong)', borderRadius: 6, padding: 8, fontSize: 13, marginBottom: 16, resize: 'vertical' }}
        />

        <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: 10, marginBottom: 16 }}>
          <div className="field" style={{ marginBottom: 0 }}>
            <label>Status</label>
            <select value={status} onChange={(e) => setStatus(e.target.value)}>
              {STATUS_OPTIONS.map((s) => <option key={s} value={s}>{s.replace('_', ' ')}</option>)}
            </select>
          </div>
          <div className="field" style={{ marginBottom: 0 }}>
            <label>Priority</label>
            <select value={priority} onChange={(e) => setPriority(e.target.value)}>
              {PRIORITY_OPTIONS.map((p) => <option key={p} value={p}>{p}</option>)}
            </select>
          </div>
          <div className="field" style={{ marginBottom: 0 }}>
            <label>Assignee</label>
            <select value={assigneeId} onChange={(e) => setAssigneeId(e.target.value)}>
              <option value="">Unassigned</option>
              {members?.map((m) => <option key={m.userId} value={m.userId}>{m.name}</option>)}
            </select>
          </div>
          <div className="field" style={{ marginBottom: 0 }}>
            <label>Due date</label>
            <input type="date" value={dueDate} onChange={(e) => setDueDate(e.target.value)} />
          </div>
        </div>

        <div style={{ marginBottom: 16 }}>
          <label style={{ fontSize: 13, fontWeight: 500, color: 'var(--text-secondary)' }}>Subtasks</label>
          <div style={{ display: 'flex', flexDirection: 'column', gap: 4, marginTop: 8 }}>
            {subtasks.map((s) => (
              <label key={s.id} style={{ display: 'flex', alignItems: 'center', gap: 8, fontSize: 13 }}>
                <input type="checkbox" checked={s.status === 'DONE'} onChange={() => toggleSubtask(s)} />
                <span style={{ textDecoration: s.status === 'DONE' ? 'line-through' : 'none', color: s.status === 'DONE' ? 'var(--text-tertiary)' : 'var(--text)' }}>
                  {s.title}
                </span>
              </label>
            ))}
          </div>
          <div style={{ display: 'flex', gap: 6, marginTop: 8 }}>
            <input
              placeholder="Add a subtask…"
              value={newSubtask}
              onChange={(e) => setNewSubtask(e.target.value)}
              onKeyDown={(e) => { if (e.key === 'Enter') handleAddSubtask(); }}
              style={{ flex: 1, border: '1px solid var(--border-strong)', borderRadius: 6, padding: '6px 10px', fontSize: 12.5 }}
            />
            <button className="btn btn-secondary" style={{ fontSize: 12 }} onClick={handleAddSubtask}>Add</button>
          </div>
        </div>

        <div style={{ display: 'flex', gap: 8 }}>
          <button className="btn btn-primary" onClick={handleSave} disabled={saving} style={{ flex: 1 }}>
            {saving ? 'Saving…' : 'Save'}
          </button>
          <button className="btn btn-danger" onClick={handleDelete}>Delete</button>
        </div>
      </div>
    </div>
  );
}
