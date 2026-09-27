import { useEffect, useState } from 'react';
import { useParams } from 'react-router-dom';
import Loading from '../components/Loading';
import TaskDetailPanel from '../components/TaskDetailPanel';
import { useToast } from '../context/ToastContext';
import * as taskService from '../services/taskService';
import * as workspaceService from '../services/workspaceService';

const COLUMNS = [
  { key: 'TODO', label: 'Todo' },
  { key: 'IN_PROGRESS', label: 'In Progress' },
  { key: 'REVIEW', label: 'Review' },
  { key: 'DONE', label: 'Done' },
];

const PRIORITY_COLORS = { LOW: '#6b7280', MEDIUM: '#3d5a80', HIGH: '#c2751b', URGENT: '#b3392c' };

export default function KanbanBoard() {
  const { workspaceId } = useParams();
  const { showToast } = useToast();

  const [tasks, setTasks] = useState([]);
  const [members, setMembers] = useState([]);
  const [loading, setLoading] = useState(true);
  const [activeTask, setActiveTask] = useState(null);
  const [dragTaskId, setDragTaskId] = useState(null);
  const [dragOverColumn, setDragOverColumn] = useState(null);

  useEffect(() => {
    document.title = 'Board — VectaSheet';
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
      showToast(err.message || 'Could not load board', 'error');
    } finally {
      setLoading(false);
    }
  }

  async function handleAddCard(status) {
    const title = window.prompt('New task title');
    if (!title || !title.trim()) return;
    try {
      const task = await taskService.createTask(workspaceId, { title: title.trim() });
      if (status !== 'TODO') {
        const updated = await taskService.updateTask(task.id, { status });
        setTasks((prev) => [...prev, updated]);
      } else {
        setTasks((prev) => [...prev, task]);
      }
    } catch (err) {
      showToast(err.message || 'Could not create task', 'error');
    }
  }

  function handleDragStart(taskId) {
    setDragTaskId(taskId);
  }

  function handleDragOver(e, columnKey) {
    e.preventDefault();
    setDragOverColumn(columnKey);
  }

  async function handleDrop(e, columnKey) {
    e.preventDefault();
    setDragOverColumn(null);
    const taskId = dragTaskId;
    setDragTaskId(null);
    if (!taskId) return;

    const task = tasks.find((t) => t.id === taskId);
    if (!task || task.status === columnKey) return;

    const columnTasks = tasks.filter((t) => t.status === columnKey);
    const newPosition = columnTasks.length;

    // Optimistic update so the drag feels instant; reconciled with the server response.
    setTasks((prev) => prev.map((t) => (t.id === taskId ? { ...t, status: columnKey, position: newPosition } : t)));

    try {
      const updated = await taskService.updateTask(taskId, { status: columnKey, position: newPosition });
      setTasks((prev) => prev.map((t) => (t.id === taskId ? updated : t)));
    } catch (err) {
      showToast(err.message || 'Could not move task', 'error');
      load();
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

  function memberName(id) {
    return members.find((m) => m.userId === id)?.name;
  }

  if (loading) return <Loading />;

  return (
    <div>
      <h1 style={{ fontSize: 20, fontWeight: 700, marginBottom: 18 }}>Board</h1>

      <div style={{ display: 'flex', gap: 14, overflowX: 'auto', paddingBottom: 10 }}>
        {COLUMNS.map((col) => {
          const colTasks = tasks
            .filter((t) => t.status === col.key)
            .sort((a, b) => a.position - b.position);

          return (
            <div
              key={col.key}
              onDragOver={(e) => handleDragOver(e, col.key)}
              onDrop={(e) => handleDrop(e, col.key)}
              style={{
                width: 250,
                flexShrink: 0,
                background: dragOverColumn === col.key ? 'var(--sidebar-bg)' : 'transparent',
                borderRadius: 8,
                padding: 8,
                border: '1px solid var(--border)',
                minHeight: 200,
              }}
            >
              <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: 10, padding: '0 4px' }}>
                <span style={{ fontSize: 12.5, fontWeight: 600, color: 'var(--text-secondary)' }}>
                  {col.label} <span style={{ color: 'var(--text-tertiary)' }}>({colTasks.length})</span>
                </span>
                <button className="btn btn-ghost" style={{ padding: '2px 6px', fontSize: 14 }} onClick={() => handleAddCard(col.key)} title="Add task">+</button>
              </div>

              <div style={{ display: 'flex', flexDirection: 'column', gap: 8 }}>
                {colTasks.map((t) => (
                  <div
                    key={t.id}
                    draggable
                    onDragStart={() => handleDragStart(t.id)}
                    onClick={() => setActiveTask(t)}
                    className="card"
                    style={{ padding: 10, cursor: 'grab', opacity: dragTaskId === t.id ? 0.4 : 1 }}
                  >
                    <div style={{ display: 'flex', alignItems: 'center', gap: 6, marginBottom: 6 }}>
                      <span style={{ width: 7, height: 7, borderRadius: '50%', background: PRIORITY_COLORS[t.priority] }} />
                      <span style={{ fontSize: 10.5, color: 'var(--text-tertiary)', textTransform: 'uppercase', letterSpacing: '0.03em' }}>{t.priority}</span>
                    </div>
                    <div style={{ fontSize: 13, fontWeight: 500, marginBottom: 6 }}>{t.title}</div>
                    <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
                      <span style={{ fontSize: 11, color: 'var(--text-tertiary)' }}>
                        {t.dueDate || ''}
                        {t.subtaskCount > 0 ? ` · ${t.subtaskDoneCount}/${t.subtaskCount}` : ''}
                      </span>
                      {memberName(t.assigneeId) && (
                        <span
                          title={memberName(t.assigneeId)}
                          style={{ width: 20, height: 20, borderRadius: '50%', background: 'var(--accent)', color: '#fff', fontSize: 10.5, display: 'flex', alignItems: 'center', justifyContent: 'center' }}
                        >
                          {memberName(t.assigneeId).slice(0, 1).toUpperCase()}
                        </span>
                      )}
                    </div>
                  </div>
                ))}
              </div>
            </div>
          );
        })}
      </div>

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
