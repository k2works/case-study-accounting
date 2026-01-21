import React, { useCallback, useEffect, useMemo, useState } from 'react';
import { Navigate, useLocation, useNavigate } from 'react-router-dom';
import { useAuth } from '../hooks/useAuth';
import { getAccounts, getAccountsErrorMessage } from '../api/getAccounts';
import type { Account } from '../api/getAccounts';
import { MainLayout, Loading, SuccessNotification, ErrorMessage } from '../views/common';
import { AccountList } from '../views/account/AccountList';

interface AccountListLocationState {
  successMessage?: string;
}

interface AccountListState {
  accounts: Account[];
  isLoading: boolean;
  errorMessage: string | null;
}

const initialState: AccountListState = {
  accounts: [],
  isLoading: false,
  errorMessage: null,
};

const useAccountListFetch = () => {
  const [state, setState] = useState<AccountListState>(initialState);

  const fetchAccounts = useCallback(async () => {
    setState((prev) => ({ ...prev, isLoading: true, errorMessage: null }));
    try {
      const data = await getAccounts();
      setState({ accounts: data, isLoading: false, errorMessage: null });
    } catch (error) {
      setState((prev) => ({
        ...prev,
        isLoading: false,
        errorMessage: getAccountsErrorMessage(error),
      }));
    }
  }, []);

  useEffect(() => {
    void fetchAccounts();
  }, [fetchAccounts]);

  return { state, fetchAccounts };
};

interface AccountListContentProps {
  state: AccountListState;
  fetchAccounts: () => Promise<void>;
  successMessage: string | null;
  onDismissSuccess: () => void;
  onEdit: (account: Account) => void;
}

const AccountListContent: React.FC<AccountListContentProps> = ({
  state,
  fetchAccounts,
  successMessage,
  onDismissSuccess,
  onEdit,
}) => {
  const shouldShowList = state.accounts.length > 0 || !state.isLoading;

  return (
    <div data-testid="account-list-page">
      <h1>勘定科目一覧</h1>
      {successMessage && (
        <div style={{ marginBottom: '16px' }}>
          <SuccessNotification message={successMessage} onDismiss={onDismissSuccess} />
        </div>
      )}
      {state.isLoading && state.accounts.length === 0 && (
        <Loading message="勘定科目を読み込み中..." />
      )}
      {state.errorMessage ? (
        <ErrorMessage message={state.errorMessage} onRetry={fetchAccounts} />
      ) : (
        shouldShowList && (
          <AccountList accounts={state.accounts} onEdit={onEdit} onDelete={fetchAccounts} />
        )
      )}
    </div>
  );
};

/**
 * 勘定科目一覧ページ
 */
const AccountListPage: React.FC = () => {
  const { isAuthenticated, isLoading, hasRole } = useAuth();
  const location = useLocation();
  const navigate = useNavigate();
  const { state, fetchAccounts } = useAccountListFetch();
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
      <AccountListContent
        state={state}
        fetchAccounts={fetchAccounts}
        successMessage={successMessage}
        onDismissSuccess={() => setSuccessMessage(null)}
        onEdit={(account) => navigate(`/master/accounts/${account.accountId}/edit`)}
      />
    </MainLayout>
  );
};

export default AccountListPage;
