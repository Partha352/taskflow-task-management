import { useEffect, useState } from 'react';
import { Link } from 'react-router-dom';
import api from '../services/api.js';
import { useAuth } from '../context/AuthContext.jsx';

const metrics = [
  ['totalTasks', 'Total tasks'],
  ['pendingTasks', 'Pending'],
  ['inProgressTasks', 'In progress'],
  ['completedTasks', 'Completed'],
  ['highPriorityTasks', 'High priority'],
  ['overdueTasks', 'Overdue']
];

export default function DashboardPage() {
  const { user } = useAuth();
  const [dashboard, setDashboard] = useState(null);
  const [error, setError] = useState('');

  useEffect(() => {
    api.get('/dashboard/user').then((response) => setDashboard(response.data))
      .catch((requestError) => setError(requestError.response?.data?.message || 'Unable to load your dashboard.'));
  }, []);

  if (error) return <section className="page-card"><h1>Dashboard</h1><p className="server-error">{error}</p></section>;
  if (!dashboard) return <section className="page-card"><p className="eyebrow">Dashboard</p><h1>Loading your tasks…</h1></section>;

  return (
    <section>
      <div className="page-heading">
        <div><p className="eyebrow">Your workspace</p><h1>Welcome back, {user?.name}</h1><p>See what needs your attention today.</p></div>
        <Link className="button" to="/tasks/new">Create task</Link>
      </div>
      <div className="metric-grid">
        {metrics.map(([key, label]) => <article className="metric-card" key={key}><span>{label}</span><strong>{dashboard[key]}</strong></article>)}
      </div>
      <div className="dashboard-grid">
        <article className="page-card"><p className="eyebrow">Workload</p><h2>{dashboard.createdTasks} created · {dashboard.assignedTasks} assigned</h2><p>Tasks you created and tasks assigned to you are both included in the totals above.</p></article>
        <article className="page-card"><p className="eyebrow">Next step</p><h2>Keep work moving</h2><p>{dashboard.overdueTasks ? `${dashboard.overdueTasks} task(s) need immediate attention.` : 'You have no overdue tasks.'}</p><Link className="text-link" to="/tasks">View my tasks →</Link></article>
      </div>
    </section>
  );
}
