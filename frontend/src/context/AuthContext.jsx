import { createContext, useContext, useMemo, useState } from 'react';
import api from '../services/api.js';

const AuthContext = createContext(null);
const TOKEN_KEY = 'taskflow_token';
const USER_KEY = 'taskflow_user';

function readUser() {
  try { return JSON.parse(localStorage.getItem(USER_KEY)); } catch { return null; }
}

function messageFrom(error) {
  const body = error.response?.data;
  if (body?.fieldErrors) return Object.values(body.fieldErrors)[0];
  return body?.message || 'Unable to complete your request. Please try again.';
}

export function AuthProvider({ children }) {
  const [user, setUser] = useState(readUser);

  function saveSession(response) {
    const nextUser = { id: response.userId, name: response.name, email: response.email, role: response.role };
    localStorage.setItem(TOKEN_KEY, response.token);
    localStorage.setItem(USER_KEY, JSON.stringify(nextUser));
    setUser(nextUser);
    return nextUser;
  }

  async function login(credentials) {
    try { return saveSession((await api.post('/auth/login', credentials)).data); }
    catch (error) { throw new Error(messageFrom(error)); }
  }

    async function register(details) {
    try { await api.post('/auth/register', details); }
    catch (error) { throw new Error(messageFrom(error)); }
  }

  function updateUser(partial) {
    const nextUser = { ...user, ...partial };
    localStorage.setItem(USER_KEY, JSON.stringify(nextUser));
    setUser(nextUser);
    return nextUser;
  }

  function logout() {
    localStorage.removeItem(TOKEN_KEY);
    localStorage.removeItem(USER_KEY);
    setUser(null);
  }

  const value = useMemo(() => ({ user, login, register, logout, updateUser, isAdmin: user?.role === 'ADMIN' }), [user]);
  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>;
}

export function useAuth() {
  const context = useContext(AuthContext);
  if (!context) throw new Error('useAuth must be used inside AuthProvider');
  return context;
}
