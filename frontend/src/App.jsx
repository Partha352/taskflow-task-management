import { Navigate, Route, Routes } from 'react-router-dom';
import AppLayout from './components/AppLayout.jsx';
import NotFoundPage from './pages/NotFoundPage.jsx';
import LoginPage from './pages/LoginPage.jsx';
import RegisterPage from './pages/RegisterPage.jsx';
import ProtectedRoute from './components/ProtectedRoute.jsx';
import { useAuth } from './context/AuthContext.jsx';
import DashboardPage from './pages/DashboardPage.jsx';
import TasksPage from './pages/TasksPage.jsx';
import TaskFormPage from './pages/TaskFormPage.jsx';
import TaskDetailsPage from './pages/TaskDetailsPage.jsx';
import AdminDashboardPage from './pages/AdminDashboardPage.jsx';
import UserManagementPage from './pages/UserManagementPage.jsx';
import ProfilePage from './pages/ProfilePage.jsx';

export default function App() {
  const { user } = useAuth();
  return (
    <Routes>
      <Route path="/" element={<Navigate to={user ? '/dashboard' : '/login'} replace />} />
      <Route path="/login" element={<LoginPage />} />
      <Route path="/register" element={<RegisterPage />} />
      <Route element={<ProtectedRoute />}>
      <Route element={<AppLayout />}>
        <Route path="dashboard" element={<DashboardPage />} />
        <Route path="tasks" element={<TasksPage />} />
        <Route path="tasks/new" element={<TaskFormPage />} />
        <Route path="tasks/:taskId" element={<TaskDetailsPage />} />
        <Route path="tasks/:taskId/edit" element={<TaskFormPage edit />} />
        <Route path="profile" element={<ProfilePage />} />
      </Route></Route>
      <Route element={<ProtectedRoute adminOnly />}><Route element={<AppLayout />}><Route path="admin" element={<AdminDashboardPage />} /><Route path="admin/users" element={<UserManagementPage />} /></Route></Route>
      <Route path="*" element={<NotFoundPage />} />
    </Routes>
  );
}
