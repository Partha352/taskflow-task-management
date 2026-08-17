import { useEffect, useState } from 'react';
import api from '../services/api.js';

export default function UserManagementPage() {
  const [users, setUsers] = useState(null); const [search, setSearch] = useState(''); const [error, setError] = useState('');
  function load(query = '') { setError(''); api.get('/users', { params: query ? { search: query } : {} }).then((r) => setUsers(r.data)).catch((e) => setError(e.response?.data?.message || 'Unable to load users.')); }
  useEffect(() => load(), []);
  async function role(user, role) { try { const updated = (await api.put(`/users/${user.id}`, { name: user.name, email: user.email, role })).data; setUsers((all) => all.map((item) => item.id === user.id ? updated : item)); } catch (e) { setError(e.response?.data?.message || 'Unable to update role.'); } }
  async function remove(user) { if (!window.confirm(`Delete ${user.name}?`)) return; try { await api.delete(`/users/${user.id}`); setUsers((all) => all.filter((item) => item.id !== user.id)); } catch (e) { setError(e.response?.data?.message || 'Unable to delete user.'); } }
  return <section><div className="page-heading"><div><p className="eyebrow">Administration</p><h1>User management</h1><p>Search users and control their workspace roles.</p></div></div><form className="filter-bar" onSubmit={(e) => { e.preventDefault(); load(search); }}><input placeholder="Search name or email" value={search} onChange={(e) => setSearch(e.target.value)} /><button className="secondary-button">Search</button></form>{error && <p className="server-error">{error}</p>}{!users ? <div className="page-card">Loading users…</div> : <div className="table-wrap"><table><thead><tr><th>Name</th><th>Email</th><th>Role</th><th>Actions</th></tr></thead><tbody>{users.map((user) => <tr key={user.id}><td>{user.name}</td><td>{user.email}</td><td><select value={user.role} onChange={(e) => role(user, e.target.value)}><option>USER</option><option>ADMIN</option></select></td><td><button className="danger-link" onClick={() => remove(user)}>Delete</button></td></tr>)}</tbody></table></div>}</section>;
}
