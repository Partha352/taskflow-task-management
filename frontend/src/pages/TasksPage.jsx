import { useEffect, useState } from 'react';
import { Link } from 'react-router-dom';
import api from '../services/api.js';

export default function TasksPage() {
  const [tasks, setTasks] = useState(null);
  const [error, setError] = useState('');
  const [filters, setFilters] = useState({ search: '', status: '', priority: '' });

  function load() {
    setTasks(null); setError('');
    const params = Object.fromEntries(Object.entries(filters).filter(([, value]) => value));
    api.get('/tasks', { params }).then((response) => setTasks(response.data))
      .catch((requestError) => setError(requestError.response?.data?.message || 'Unable to load tasks.'));
  }
  useEffect(load, []);

  async function remove(taskId) {
    if (!window.confirm('Delete this task?')) return;
    try { await api.delete(`/tasks/${taskId}`); setTasks((current) => current.filter((task) => task.id !== taskId)); }
    catch (requestError) { setError(requestError.response?.data?.message || 'Unable to delete task.'); }
  }

  return <section>
    <div className="page-heading"><div><p className="eyebrow">Task management</p><h1>My tasks</h1><p>Track tasks you created or that were assigned to you.</p></div><Link className="button" to="/tasks/new">Create task</Link></div>
    <form className="filter-bar" onSubmit={(event) => { event.preventDefault(); load(); }}>
      <input placeholder="Search title or description" value={filters.search} onChange={(e) => setFilters({ ...filters, search: e.target.value })} />
      <select value={filters.status} onChange={(e) => setFilters({ ...filters, status: e.target.value })}><option value="">All statuses</option><option>TODO</option><option>IN_PROGRESS</option><option>COMPLETED</option></select>
      <select value={filters.priority} onChange={(e) => setFilters({ ...filters, priority: e.target.value })}><option value="">All priorities</option><option>LOW</option><option>MEDIUM</option><option>HIGH</option></select>
      <button className="secondary-button" type="submit">Filter</button>
    </form>
    {error && <p className="server-error">{error}</p>}
    {!tasks ? <div className="page-card">Loading tasks…</div> : tasks.length === 0 ? <div className="page-card"><h2>No tasks found</h2><p>Create a task or adjust your filters.</p></div> : <div className="task-list">{tasks.map((task) => <article className="task-card" key={task.id}><div><div className="task-title-row"><h2>{task.title}</h2><span className={`badge ${task.priority.toLowerCase()}`}>{task.priority}</span></div><p>{task.description || 'No description'}</p><small>{task.status.replace('_', ' ')} · Due {task.dueDate || 'not set'}</small></div><div className="task-actions"><Link className="text-link" to={`/tasks/${task.id}`}>View</Link><Link className="text-link" to={`/tasks/${task.id}/edit`}>Edit</Link><button className="danger-link" onClick={() => remove(task.id)}>Delete</button></div></article>)}</div>}
  </section>;
}
