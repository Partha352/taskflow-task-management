import { useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import AuthLayout from '../components/AuthLayout.jsx';
import { useAuth } from '../context/AuthContext.jsx';

export default function RegisterPage() {
  const navigate = useNavigate();
  const [form, setForm] = useState({ name: '', email: '', password: '', confirmPassword: '' });
  const [errors, setErrors] = useState({});
  const [serverError, setServerError] = useState('');
  const [submitting, setSubmitting] = useState(false);
  const { register } = useAuth();

  async function submit(event) {
    event.preventDefault();
    const nextErrors = {};
    if (!form.name.trim()) nextErrors.name = 'Name is required.';
    if (!/^\S+@\S+\.\S+$/.test(form.email)) nextErrors.email = 'Enter a valid email address.';
    if (form.password.length < 6) nextErrors.password = 'Password must contain at least 6 characters.';
    if (form.password !== form.confirmPassword) nextErrors.confirmPassword = 'Passwords do not match.';
    setErrors(nextErrors);
    if (Object.keys(nextErrors).length) return;
    setSubmitting(true); setServerError('');
    try { await register({ name: form.name, email: form.email, password: form.password }); navigate('/login', { state: { message: 'Account created. Please sign in.' } }); }
    catch (error) { setServerError(error.message); }
    finally { setSubmitting(false); }
  }

  return (
    <AuthLayout title="Create your account" subtitle="Start organizing work with TaskFlow.">
      <form className="auth-form" onSubmit={submit} noValidate>
        <label>Name
          <input value={form.name} onChange={(e) => setForm({ ...form, name: e.target.value })} autoComplete="name" />
          {errors.name && <span className="field-error">{errors.name}</span>}
        </label>
        <label>Email
          <input type="email" value={form.email} onChange={(e) => setForm({ ...form, email: e.target.value })} autoComplete="email" />
          {errors.email && <span className="field-error">{errors.email}</span>}
        </label>
        <label>Password
          <input type="password" value={form.password} onChange={(e) => setForm({ ...form, password: e.target.value })} autoComplete="new-password" />
          {errors.password && <span className="field-error">{errors.password}</span>}
        </label>
        <label>Confirm password
          <input type="password" value={form.confirmPassword} onChange={(e) => setForm({ ...form, confirmPassword: e.target.value })} autoComplete="new-password" />
          {errors.confirmPassword && <span className="field-error">{errors.confirmPassword}</span>}
        </label>
        {serverError && <p className="server-error">{serverError}</p>}
        <button className="primary-button" disabled={submitting} type="submit">{submitting ? 'Creating account…' : 'Create account'}</button>
      </form>
      <p className="auth-switch">Already have an account? <Link to="/login">Sign in</Link></p>
    </AuthLayout>
  );
}
