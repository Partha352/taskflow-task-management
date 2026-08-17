import { useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import AuthLayout from '../components/AuthLayout.jsx';
import { useAuth } from '../context/AuthContext.jsx';

export default function LoginPage() {
  const navigate = useNavigate();
  const [form, setForm] = useState({ email: '', password: '' });
  const [errors, setErrors] = useState({});
  const [serverError, setServerError] = useState('');
  const [submitting, setSubmitting] = useState(false);
  const { login } = useAuth();

  async function submit(event) {
    event.preventDefault();
    const nextErrors = {};
    if (!/^\S+@\S+\.\S+$/.test(form.email)) nextErrors.email = 'Enter a valid email address.';
    if (!form.password) nextErrors.password = 'Password is required.';
    setErrors(nextErrors);
    if (Object.keys(nextErrors).length) return;
    setSubmitting(true); setServerError('');
    try { const user = await login(form); navigate(user.role === 'ADMIN' ? '/admin' : '/dashboard'); }
    catch (error) { setServerError(error.message); }
    finally { setSubmitting(false); }
  }

  return (
    <AuthLayout title="Welcome back" subtitle="Sign in to view and manage your tasks.">
      <form className="auth-form" onSubmit={submit} noValidate>
        <label>Email
          <input type="email" value={form.email} onChange={(e) => setForm({ ...form, email: e.target.value })} autoComplete="email" />
          {errors.email && <span className="field-error">{errors.email}</span>}
        </label>
        <label>Password
          <input type="password" value={form.password} onChange={(e) => setForm({ ...form, password: e.target.value })} autoComplete="current-password" />
          {errors.password && <span className="field-error">{errors.password}</span>}
        </label>
        {serverError && <p className="server-error">{serverError}</p>}
        <button className="primary-button" disabled={submitting} type="submit">{submitting ? 'Signing in…' : 'Sign in'}</button>
      </form>
      <p className="auth-switch">New to TaskFlow? <Link to="/register">Create an account</Link></p>
    </AuthLayout>
  );
}
