import { NavLink, Outlet, useNavigate } from 'react-router-dom';
import { useAuth } from '../context/AuthContext.jsx';

const navItems = [
  ['Dashboard', '/dashboard'],
  ['My Tasks', '/tasks'],
  ['Create Task', '/tasks/new'],
  ['Profile', '/profile']
];

export default function AppLayout() {
  const { user, logout, isAdmin } = useAuth();
  const navigate = useNavigate();
  function signOut() { logout(); navigate('/login'); }
  return (
    <div className="app-shell">
      <aside className="sidebar">
        <NavLink className="brand" to="/dashboard">TaskFlow</NavLink>
        <nav aria-label="Primary navigation">
          {navItems.map(([label, path]) => (
            <NavLink key={path} className="nav-link" to={path}>{label}</NavLink>
          ))}
          {isAdmin && <NavLink className="nav-link" to="/admin">Admin Dashboard</NavLink>}
          {isAdmin && <NavLink className="nav-link" to="/admin/users">User Management</NavLink>}
        </nav>
      </aside>
      <main className="main-content">
        <header className="navbar">
          <span className="navbar-title">Task Management</span>
          <div className="user-actions"><span className="user-name">{user?.name}</span><button className="profile-button" type="button" onClick={signOut} aria-label="Sign out">{user?.name?.[0] || 'U'}</button></div>
        </header>
        <section className="page-content"><Outlet /></section>
      </main>
    </div>
  );
}
