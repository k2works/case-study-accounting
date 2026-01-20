import { Routes, Route, Navigate } from 'react-router-dom';
import { useAuth } from './hooks/useAuth';
import LoginPage from './pages/LoginPage';
import DashboardPage from './pages/DashboardPage';
import RegisterUserPage from './pages/RegisterUserPage';
import AccountListPage from './pages/AccountListPage';
import CreateAccountPage from './pages/CreateAccountPage';
import EditAccountPage from './pages/EditAccountPage';
import { Loading } from './views/common';

/**
 * 認証が必要なルートのガード
 */
const PrivateRoute: React.FC<{ children: React.ReactNode }> = ({ children }) => {
  const { isAuthenticated, isLoading } = useAuth();

  if (isLoading) {
    return <Loading message="認証情報を確認中..." fullScreen />;
  }

  if (!isAuthenticated) {
    return <Navigate to="/login" replace />;
  }

  return <>{children}</>;
};

/**
 * 管理者のみアクセス可能なルートのガード
 */
const AdminRoute: React.FC<{ children: React.ReactNode }> = ({ children }) => {
  const { isAuthenticated, isLoading, hasRole } = useAuth();

  if (isLoading) {
    return <Loading message="認証情報を確認中..." fullScreen />;
  }

  if (!isAuthenticated) {
    return <Navigate to="/login" replace />;
  }

  if (!hasRole('ADMIN')) {
    return <Navigate to="/" replace />;
  }

  return <>{children}</>;
};

/**
 * 管理者または経理責任者のみアクセス可能なルートのガード
 */
const ManagerRoute: React.FC<{ children: React.ReactNode }> = ({ children }) => {
  const { isAuthenticated, isLoading, hasRole } = useAuth();

  if (isLoading) {
    return <Loading message="認証情報を確認中..." fullScreen />;
  }

  if (!isAuthenticated) {
    return <Navigate to="/login" replace />;
  }

  if (!hasRole('ADMIN') && !hasRole('MANAGER')) {
    return <Navigate to="/" replace />;
  }

  return <>{children}</>;
};

export const App = () => {
  return (
    <Routes>
      <Route path="/login" element={<LoginPage />} />
      <Route
        path="/"
        element={
          <PrivateRoute>
            <DashboardPage />
          </PrivateRoute>
        }
      />
      <Route
        path="/system/users"
        element={
          <AdminRoute>
            <RegisterUserPage />
          </AdminRoute>
        }
      />
      <Route
        path="/master/accounts"
        element={
          <ManagerRoute>
            <AccountListPage />
          </ManagerRoute>
        }
      />
      <Route
        path="/master/accounts/new"
        element={
          <ManagerRoute>
            <CreateAccountPage />
          </ManagerRoute>
        }
      />
      <Route
        path="/master/accounts/:id/edit"
        element={
          <ManagerRoute>
            <EditAccountPage />
          </ManagerRoute>
        }
      />
      <Route path="*" element={<Navigate to="/" replace />} />
    </Routes>
  );
};
