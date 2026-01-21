import React, { useCallback, useEffect, useMemo, useState } from 'react';
import { Navigate, useLocation, useNavigate } from 'react-router-dom';
import { useAuth } from '../hooks/useAuth';
import { getAccounts, getAccountsErrorMessage } from '../api/getAccounts';
import type { Account } from '../api/getAccounts';
import { MainLayout, Loading, SuccessNotification, ErrorMessage } from '../views/common';
import { AccountList } from '../views/account/AccountList';
import type { AccountSearchParams } from '../api/getAccounts';
import type { AccountFilterValues } from '../views/account/AccountFilter';

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

const initialFilterValues: AccountFilterValues = {
  type: '',
  keyword: '',
};

const useAccountListFetch = () => {
  const [state, setState] = useState<AccountListState>(initialState);
  const [filterValues, setFilterValues] = useState<AccountFilterValues>(initialFilterValues);

  const fetchAccounts = useCallback(async (params?: AccountSearchParams) => {
    setState((prev) => ({ ...prev, isLoading: true, errorMessage: null }));
    try {
      const data = await getAccounts(params);
      setState({ accounts: data, isLoading: false, errorMessage: null });
    } catch (error) {
      setState((prev) => ({
        ...prev,
        isLoading: false,
        errorMessage: getAccountsErrorMessage(error),
      }));
    }
  }, []);

  const handleSearch = useCallback(() => {
    const params: AccountSearchParams = {};
    if (filterValues.type) params.type = filterValues.type;
    if (filterValues.keyword) params.keyword = filterValues.keyword;
    void fetchAccounts(Object.keys(params).length > 0 ? params : undefined);
  }, [fetchAccounts, filterValues]);

  const handleReset = useCallback(() => {
    setFilterValues(initialFilterValues);
    void fetchAccounts();
  }, [fetchAccounts]);

  useEffect(() => {
    void fetchAccounts();
  }, [fetchAccounts]);

  return {
    state,
    filterValues,
    setFilterValues,
    fetchAccounts,
    handleSearch,
    handleReset,
  };
};

interface AccountListContentProps {
  state: AccountListState;
  fetchAccounts: (params?: AccountSearchParams) => Promise<void>;
  filterValues: AccountFilterValues;
  onFilterChange: (values: AccountFilterValues) => void;
  onSearch: () => void;
  onReset: () => void;
  successMessage: string | null;
  onDismissSuccess: () => void;
  onEdit: (account: Account) => void;
}

const AccountListContent: React.FC<AccountListContentProps> = ({
  state,
  fetchAccounts,
  filterValues,
  onFilterChange,
  onSearch,
  onReset,
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
          <AccountList
            accounts={state.accounts}
            filterValues={filterValues}
            onFilterChange={onFilterChange}
            onSearch={onSearch}
            onReset={onReset}
            onEdit={onEdit}
            onDelete={onSearch}
          />
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
  const { state, filterValues, setFilterValues, fetchAccounts, handleSearch, handleReset } =
    useAccountListFetch();
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
        filterValues={filterValues}
        onFilterChange={setFilterValues}
        onSearch={handleSearch}
        onReset={handleReset}
        successMessage={successMessage}
        onDismissSuccess={() => setSuccessMessage(null)}
        onEdit={(account) => navigate(`/master/accounts/${account.accountId}/edit`)}
      />
    </MainLayout>
  );
};

export default AccountListPage;
