import { useEffect, useState } from 'react';
import { useAuth } from '../context/AuthContext.jsx';
import api from '../services/api.js';

export default function ProfilePage() {
  const { user, updateUser } = useAuth();
  const [form, setForm] = useState({ name: '', email: '' });
  const [error, setError] = useState('');
  const [loading, setLoading] = useState(true);
  const [saving, setSaving] = useState(false);

  useEffect(() => {
    // Pre-fill with current user data from context
    if (user) {
      setForm({ name: user.name || '', email: user.email || '' });
      setLoading(false);
    }
  }, [user]);

  async function submit(event) {
    event.preventDefault();
    setSaving(true);
    setError('');

    // Client-side validation
    const nextErrors = {};
    if (!form.name.trim()) nextErrors.name = 'Name is required.';
    if (!/^\S+@\S+\.\S+$/.test(form.email)) nextErrors.email = 'Enter a valid email address.';
    if (Object.keys(nextErrors).length) {
      setSaving(false);
      // Show validation errors inline
      setForm(prev => ({ ...prev, _errors: nextErrors }));
      return;
    }

    try {
      await api.put(`/users/${user.id}`, { name: form.name, email: form.email });
      // Update the context user data
      updateUser({ name: form.name, email: form.email });
      setError('');
      alert('Profile updated successfully');
    } catch (e) {
      setError(e.response?.data?.message || e.response?.data?.fieldErrors?.email || 'Unable to update profile.');
    } finally {
      setSaving(false);
    }
  }

  if (loading) return <div className="page-card">Loading profile…</div>;

  return (
    <section>
      <div className="page-heading">
        <div>
          <p className="eyebrow">Account</p>
          <h1>Profile</h1>
          <p>Update your name and email address.</p>
        </div>
      </div>

      <div className="page-card">
        {error && <p className="server-error">{error}</p>}
        <form className="auth-form" onSubmit={submit} noValidate>
          <label>Name
            <input
              required
              maxLength="100"
              value={form.name}
              onChange={(e) => setForm({ ...form, name: e.target.value })}
              autoComplete="name"
            />
            {!form.name?.trim() && <span className="field-error">Name is required.</span>}
          </label>
          <label>Email
            <input
              type="email"
              required
              maxLength="255"
              value={form.email}
              onChange={(e) => setForm({ ...form, email: e.target.value })}
              autoComplete="email"
            />
            {!/^\S+@\S+\.\S+$/.test(form.email) && <span className="field-error">Enter a valid email address.</span>}
          </label>
          <button className="primary-button" disabled={saving} type="submit">
            {saving ? 'Saving…' : 'Save changes'}
          </button>
        </form>
      </div>
    </section>
  );
}