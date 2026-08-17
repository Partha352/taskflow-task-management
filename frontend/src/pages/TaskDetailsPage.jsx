import { useEffect, useState } from 'react';
import { Link, useNavigate, useParams } from 'react-router-dom';
import api from '../services/api.js';
import { useAuth } from '../context/AuthContext.jsx';

export default function TaskDetailsPage() {
  const { taskId } = useParams(); const navigate = useNavigate(); const [task, setTask] = useState(null); const [error, setError] = useState('');
  const { isAdmin } = useAuth();
  const [assignUserId, setAssignUserId] = useState('');
  const [users, setUsers] = useState([]);
  const [loadingUsers, setLoadingUsers] = useState(false);

  useEffect(() => { api.get(`/tasks/${taskId}`).then((r) => setTask(r.data)).catch((e) => setError(e.response?.data?.message || 'Unable to load task.')); }, [taskId]);

  // Load users for admin assignment
  useEffect(() => {
    if (isAdmin) {
      setLoadingUsers(true);
      api.get('/users').then((r) => setUsers(r.data)).catch(() => {}).finally(() => setLoadingUsers(false));
    }
  }, [isAdmin]);

  async function assignTask() {
    if (!assignUserId) return;
    try {
      await api.put(`/tasks/${taskId}/assign/${assignUserId}`);
      setTask(prev => {
        const updated = { ...prev };
        updated.assignedTo = users.find(u => u.id === Number(assignUserId));
        return updated;
      });
      setAssignUserId('');
    } catch (e) {
      setError(e.response?.data?.message || 'Unable to assign task.');
    }
  }

  async function remove() {
    if (!window.confirm('Delete this task?')) return;
    try { await api.delete(`/tasks/${taskId}`); navigate('/tasks'); }
    catch (e) { setError(e.response?.data?.message || 'Unable to delete task.'); }
  }

  if (error) return <div className="page-card"><p className="server-error">{error}</p></div>;
  if (!task) return <div className="page-card">Loading task…</div>;
    return <section className="form-card"><div className="page-heading"><div><p className="eyebrow">Task details</p><h1>{task.title}</h1></div><span className={`badge ${task.priority.toLowerCase()}`}>{task.priority}</span></div><p className="task-description">{task.description || 'No description provided.'}</p><dl className="details-grid"><div><dt>Status</dt><dd>{task.status.replace('_', ' ')}</dd></div><div><dt>Due date</dt><dd>{task.dueDate || 'Not set'}</dd></div><div><dt>Created by</dt><dd>{task.createdBy.name}</dd></div><div><dt>Assigned to</dt><dd>{task.assignedTo?.name || 'Unassigned'}
  {isAdmin && (
    <div className="assign-control">
      <select value={assignUserId} onChange={(e) => setAssignUserId(e.target.value)} disabled={loadingUsers}>
        <option value="">Assign to…</option>
        {users.map(u => <option key={u.id} value={u.id}>{u.name} ({u.email})</option>)}
      </select>
      <button className="secondary-button" onClick={assignTask} disabled={!assignUserId || loadingUsers}>Assign</button>
    </div>
  )}</dd></div></dl><div className="form-actions"><Link className="secondary-button" to="/tasks">Back</Link><Link className="secondary-button" to={`/tasks/${taskId}/edit`}>Edit</Link><button className="danger-button" onClick={remove}>Delete</button></div></section>;
}
