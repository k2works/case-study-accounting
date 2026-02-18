import { Routes, Route, Navigate } from 'react-router-dom';
import { useAuth } from './hooks/useAuth';
import LoginPage from './pages/LoginPage';
import DashboardPage from './pages/DashboardPage';
import RegisterUserPage from './pages/RegisterUserPage';
import UserListPage from './pages/UserListPage';
import AccountListPage from './pages/AccountListPage';
import CreateAccountPage from './pages/CreateAccountPage';
import EditAccountPage from './pages/EditAccountPage';
import AccountStructureListPage from './pages/AccountStructureListPage';
import CreateAccountStructurePage from './pages/CreateAccountStructurePage';
import EditAccountStructurePage from './pages/EditAccountStructurePage';
import JournalEntryListPage from './pages/JournalEntryListPage';
import CreateJournalEntryPage from './pages/CreateJournalEntryPage';
import EditJournalEntryPage from './pages/EditJournalEntryPage';
import GeneralLedgerPage from './pages/GeneralLedgerPage';
import SubsidiaryLedgerPage from './pages/SubsidiaryLedgerPage';
import DailyBalancePage from './pages/DailyBalancePage';
import MonthlyBalancePage from './pages/MonthlyBalancePage';
import TrialBalancePage from './pages/TrialBalancePage';
import BalanceSheetPage from './pages/BalanceSheetPage';
import ProfitAndLossPage from './pages/ProfitAndLossPage';
import UserEditPage from './pages/UserEditPage';
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
        path="/users"
        element={
          <AdminRoute>
            <UserListPage />
          </AdminRoute>
        }
      />
      <Route
        path="/users/:id/edit"
        element={
          <PrivateRoute>
            <UserEditPage />
          </PrivateRoute>
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
      <Route
        path="/master/account-structures"
        element={
          <ManagerRoute>
            <AccountStructureListPage />
          </ManagerRoute>
        }
      />
      <Route
        path="/master/account-structures/new"
        element={
          <ManagerRoute>
            <CreateAccountStructurePage />
          </ManagerRoute>
        }
      />
      <Route
        path="/master/account-structures/:code/edit"
        element={
          <ManagerRoute>
            <EditAccountStructurePage />
          </ManagerRoute>
        }
      />
      <Route
        path="/journal/entries"
        element={
          <PrivateRoute>
            <JournalEntryListPage />
          </PrivateRoute>
        }
      />
      <Route
        path="/journal/entries/new"
        element={
          <PrivateRoute>
            <CreateJournalEntryPage />
          </PrivateRoute>
        }
      />
      <Route
        path="/journal/entries/:id/edit"
        element={
          <PrivateRoute>
            <EditJournalEntryPage />
          </PrivateRoute>
        }
      />
      <Route
        path="/general-ledger"
        element={
          <PrivateRoute>
            <GeneralLedgerPage />
          </PrivateRoute>
        }
      />
      <Route
        path="/subsidiary-ledger"
        element={
          <PrivateRoute>
            <SubsidiaryLedgerPage />
          </PrivateRoute>
        }
      />
      <Route
        path="/ledger/daily-balance"
        element={
          <PrivateRoute>
            <DailyBalancePage />
          </PrivateRoute>
        }
      />
      <Route
        path="/ledger/monthly-balance"
        element={
          <PrivateRoute>
            <MonthlyBalancePage />
          </PrivateRoute>
        }
      />
      <Route
        path="/ledger/trial-balance"
        element={
          <PrivateRoute>
            <TrialBalancePage />
          </PrivateRoute>
        }
      />
      <Route
        path="/financial-statements/balance-sheet"
        element={
          <PrivateRoute>
            <BalanceSheetPage />
          </PrivateRoute>
        }
      />
      <Route
        path="/financial-statements/income-statement"
        element={
          <PrivateRoute>
            <ProfitAndLossPage />
          </PrivateRoute>
        }
      />
      <Route path="*" element={<Navigate to="/" replace />} />
    </Routes>
  );
};
