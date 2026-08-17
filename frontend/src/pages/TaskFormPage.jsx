import { useEffect, useState } from 'react';
import { Link, useNavigate, useParams } from 'react-router-dom';
import api from '../services/api.js';

const blank = { title: '', description: '', status: 'TODO', priority: 'MEDIUM', dueDate: '' };

export default function TaskFormPage({ edit = false }) {
  const { taskId } = useParams(); const navigate = useNavigate();
  const [form, setForm] = useState(blank); const [error, setError] = useState(''); const [loading, setLoading] = useState(edit); const [saving, setSaving] = useState(false);
  useEffect(() => { if (edit) api.get(`/tasks/${taskId}`).then((r) => setForm({ ...r.data, dueDate: r.data.dueDate || '' })).catch((e) => setError(e.response?.data?.message || 'Unable to load task.')).finally(() => setLoading(false)); }, [edit, taskId]);
  async function submit(event) { event.preventDefault(); setSaving(true); setError(''); try { const payload = { ...form, dueDate: form.dueDate || null }; const response = edit ? await api.put(`/tasks/${taskId}`, payload) : await api.post('/tasks', payload); navigate(`/tasks/${response.data.id}`); } catch (e) { setError(e.response?.data?.message || 'Unable to save task.'); } finally { setSaving(false); } }
  if (loading) return <div className="page-card">Loading task…</div>;
  return <section className="form-card"><p className="eyebrow">Task management</p><h1>{edit ? 'Edit task' : 'Create task'}</h1>{error && <p className="server-error">{error}</p>}<form className="task-form" onSubmit={submit}><label>Title<input required maxLength="200" value={form.title} onChange={(e) => setForm({ ...form, title: e.target.value })} /></label><label>Description<textarea maxLength="2000" value={form.description || ''} onChange={(e) => setForm({ ...form, description: e.target.value })} /></label><div className="form-grid"><label>Status<select value={form.status} onChange={(e) => setForm({ ...form, status: e.target.value })}><option>TODO</option><option>IN_PROGRESS</option><option>COMPLETED</option></select></label><label>Priority<select value={form.priority} onChange={(e) => setForm({ ...form, priority: e.target.value })}><option>LOW</option><option>MEDIUM</option><option>HIGH</option></select></label><label>Due date<input type="date" value={form.dueDate} onChange={(e) => setForm({ ...form, dueDate: e.target.value })} /></label></div><div className="form-actions"><Link className="secondary-button" to="/tasks">Cancel</Link><button className="primary-button" disabled={saving}>{saving ? 'Saving…' : 'Save task'}</button></div></form></section>;
}
