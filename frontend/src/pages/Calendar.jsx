import { useEffect, useMemo, useState } from 'react';
import { useParams } from 'react-router-dom';
import Loading from '../components/Loading';
import { useToast } from '../context/ToastContext';
import * as eventService from '../services/eventService';
import * as taskService from '../services/taskService';

function pad(n) { return String(n).padStart(2, '0'); }
function toDateKey(y, m, d) { return `${y}-${pad(m + 1)}-${pad(d)}`; }

export default function Calendar() {
  const { workspaceId } = useParams();
  const { showToast } = useToast();

  const [cursor, setCursor] = useState(() => { const d = new Date(); d.setDate(1); return d; });
  const [events, setEvents] = useState([]);
  const [tasks, setTasks] = useState([]);
  const [loading, setLoading] = useState(true);
  const [selectedDay, setSelectedDay] = useState(null);

  useEffect(() => {
    document.title = 'Calendar — VectaSheet';
    load();
  }, [workspaceId]);

  async function load() {
    setLoading(true);
    try {
      const [ev, tk] = await Promise.all([
        eventService.listEvents(workspaceId),
        taskService.listTasks(workspaceId),
      ]);
      setEvents(ev);
      setTasks(tk.filter((t) => t.dueDate));
    } catch (err) {
      showToast(err.message || 'Could not load calendar', 'error');
    } finally {
      setLoading(false);
    }
  }

  const itemsByDay = useMemo(() => {
    const map = {};
    events.forEach((e) => {
      const key = e.startAt.slice(0, 10);
      (map[key] ||= []).push({ kind: 'event', title: e.title, id: e.id, allDay: e.allDay });
    });
    tasks.forEach((t) => {
      (map[t.dueDate] ||= []).push({ kind: 'task', title: t.title, id: t.id, status: t.status });
    });
    return map;
  }, [events, tasks]);

  async function handleAddEvent(dateKey) {
    const title = window.prompt('Event title');
    if (!title || !title.trim()) return;
    try {
      await eventService.createEvent(workspaceId, {
        title: title.trim(),
        startAt: `${dateKey}T09:00:00`,
        endAt: `${dateKey}T10:00:00`,
        allDay: false,
      });
      load();
    } catch (err) {
      showToast(err.message || 'Could not create event', 'error');
    }
  }

  async function handleDeleteEvent(id) {
    if (!window.confirm('Delete this event?')) return;
    try {
      await eventService.deleteEvent(id);
      load();
    } catch (err) {
      showToast(err.message || 'Could not delete event', 'error');
    }
  }

  function changeMonth(delta) {
    setCursor((prev) => {
      const d = new Date(prev);
      d.setMonth(d.getMonth() + delta);
      return d;
    });
  }

  const year = cursor.getFullYear();
  const month = cursor.getMonth();
  const firstDayOfWeek = new Date(year, month, 1).getDay();
  const daysInMonth = new Date(year, month + 1, 0).getDate();
  const cells = [];
  for (let i = 0; i < firstDayOfWeek; i++) cells.push(null);
  for (let d = 1; d <= daysInMonth; d++) cells.push(d);

  const todayKey = toDateKey(new Date().getFullYear(), new Date().getMonth(), new Date().getDate());

  if (loading) return <Loading />;

  return (
    <div>
      <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: 18 }}>
        <h1 style={{ fontSize: 20, fontWeight: 700 }}>
          {cursor.toLocaleString('default', { month: 'long' })} {year}
        </h1>
        <div style={{ display: 'flex', gap: 8 }}>
          <button className="btn btn-ghost" onClick={() => changeMonth(-1)}>← Prev</button>
          <button className="btn btn-secondary" onClick={() => setCursor(() => { const d = new Date(); d.setDate(1); return d; })}>Today</button>
          <button className="btn btn-ghost" onClick={() => changeMonth(1)}>Next →</button>
        </div>
      </div>

      <div style={{ display: 'grid', gridTemplateColumns: 'repeat(7, 1fr)', gap: 1, background: 'var(--border)', border: '1px solid var(--border)', borderRadius: 8, overflow: 'hidden' }}>
        {['Sun', 'Mon', 'Tue', 'Wed', 'Thu', 'Fri', 'Sat'].map((d) => (
          <div key={d} style={{ background: 'var(--sidebar-bg)', padding: '8px 10px', fontSize: 11.5, fontWeight: 600, color: 'var(--text-secondary)' }}>{d}</div>
        ))}
        {cells.map((day, i) => {
          if (day === null) return <div key={i} style={{ background: 'var(--bg)', minHeight: 90 }} />;
          const key = toDateKey(year, month, day);
          const items = itemsByDay[key] || [];
          const isToday = key === todayKey;
          return (
            <div
              key={i}
              onClick={() => setSelectedDay(key)}
              style={{
                background: 'var(--surface)',
                minHeight: 90,
                padding: 6,
                cursor: 'pointer',
                outline: selectedDay === key ? '2px solid var(--accent)' : 'none',
                outlineOffset: -2,
              }}
            >
              <div style={{ fontSize: 11.5, fontWeight: isToday ? 700 : 500, color: isToday ? 'var(--accent)' : 'var(--text-secondary)', marginBottom: 4 }}>
                {day}
              </div>
              <div style={{ display: 'flex', flexDirection: 'column', gap: 2 }}>
                {items.slice(0, 3).map((item) => (
                  <div
                    key={item.kind + item.id}
                    onClick={(e) => { e.stopPropagation(); if (item.kind === 'event') handleDeleteEvent(item.id); }}
                    title={item.kind === 'event' ? 'Click to delete' : `Task: ${item.status}`}
                    style={{
                      fontSize: 10.5,
                      padding: '2px 5px',
                      borderRadius: 4,
                      background: item.kind === 'event' ? 'var(--accent)' : 'var(--sidebar-bg)',
                      color: item.kind === 'event' ? '#fff' : 'var(--text-secondary)',
                      whiteSpace: 'nowrap',
                      overflow: 'hidden',
                      textOverflow: 'ellipsis',
                    }}
                  >
                    {item.kind === 'task' ? '☑ ' : ''}{item.title}
                  </div>
                ))}
                {items.length > 3 && <div style={{ fontSize: 10, color: 'var(--text-tertiary)' }}>+{items.length - 3} more</div>}
              </div>
            </div>
          );
        })}
      </div>

      {selectedDay && (
        <div style={{ marginTop: 14, display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
          <span style={{ fontSize: 13, color: 'var(--text-secondary)' }}>Selected: {selectedDay}</span>
          <button className="btn btn-primary" onClick={() => handleAddEvent(selectedDay)}>+ Add event on this day</button>
        </div>
      )}

      <p style={{ marginTop: 18, fontSize: 12, color: 'var(--text-tertiary)' }}>
        Only month view is implemented so far — week and agenda views are noted as future work.
      </p>
    </div>
  );
}
