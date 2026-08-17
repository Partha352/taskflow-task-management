import { useEffect, useState } from 'react';
import { Link } from 'react-router-dom';
import api from '../services/api.js';

export default function AdminDashboardPage() {
  const [data, setData] = useState(null); const [error, setError] = useState('');
  useEffect(() => { api.get('/dashboard/admin').then((r) => setData(r.data)).catch((e) => setError(e.response?.data?.message || 'Unable to load admin dashboard.')); }, []);
  if (error) return <div className="page-card"><p className="server-error">{error}</p></div>;
  if (!data) return <div className="page-card">Loading admin dashboard…</div>;
  const metrics = [['totalUsers', 'Total users'], ['adminUsers', 'Admins'], ['standardUsers', 'Standard users'], ['totalTasks', 'Total tasks'], ['completedTasks', 'Completed'], ['overdueTasks', 'Overdue']];
  return <section><div className="page-heading"><div><p className="eyebrow">Administration</p><h1>Admin dashboard</h1><p>Monitor your TaskFlow workspace.</p></div><Link className="button" to="/admin/users">Manage users</Link></div><div className="metric-grid">{metrics.map(([key, label]) => <article className="metric-card" key={key}><span>{label}</span><strong>{data[key]}</strong></article>)}</div><div className="dashboard-grid"><article className="page-card"><p className="eyebrow">Task status</p><h2>{data.pendingTasks} pending · {data.inProgressTasks} in progress</h2><p>{data.highPriorityTasks} high-priority task(s) across the workspace.</p></article><article className="page-card"><p className="eyebrow">Attention</p><h2>{data.overdueTasks} overdue task(s)</h2><p>Use task assignment and user management to keep work moving.</p></article></div></section>;
}
