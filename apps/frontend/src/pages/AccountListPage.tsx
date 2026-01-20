import React, { useMemo, useState } from 'react';
import { Navigate, useLocation } from 'react-router-dom';
import { useAuth } from '../hooks/useAuth';
import { MainLayout, Loading, SuccessNotification } from '../views/common';
import { AccountList } from '../views/account/AccountList';

interface AccountListLocationState {
  successMessage?: string;
}

/**
 * 勘定科目一覧ページ
 */
const AccountListPage: React.FC = () => {
  const { isAuthenticated, isLoading, hasRole } = useAuth();
  const location = useLocation();
  const [successMessage, setSuccessMessage] = useState<string | null>(() => {
    const state = location.state as AccountListLocationState | null;
    return state?.successMessage ?? null;
  });

  const breadcrumbs = useMemo(
    () => [{ label: 'ホーム' }, { label: 'マスタ管理' }, { label: '勘定科目一覧' }],
    []
  );

  if (isLoading) {
    return <Loading message="認証情報を確認中..." fullScreen />;
  }

  if (!isAuthenticated) {
    return <Navigate to="/login" replace />;
  }

  if (!hasRole('ADMIN') && !hasRole('MANAGER')) {
    return <Navigate to="/" replace />;
  }

  return (
    <MainLayout breadcrumbs={breadcrumbs}>
      <div data-testid="account-list-page">
        <h1>勘定科目一覧</h1>
        {successMessage && (
          <div style={{ marginBottom: '16px' }}>
            <SuccessNotification
              message={successMessage}
              onDismiss={() => setSuccessMessage(null)}
            />
          </div>
        )}
        <AccountList />
      </div>
    </MainLayout>
  );
};

export default AccountListPage;
