import React, { useCallback, useEffect, useMemo, useState } from 'react';
import { Navigate, useNavigate, useParams } from 'react-router-dom';
import { useAuth } from '../hooks/useAuth';
import { getAccount, getAccountErrorMessage } from '../api/getAccount';
import type { Account } from '../api/getAccounts';
import { MainLayout, Loading, ErrorMessage } from '../views/common';
import { EditAccountForm } from '../views/account/EditAccountForm';

interface AccountState {
  account: Account | null;
  isLoading: boolean;
  errorMessage: string | null;
}

const initialState: AccountState = {
  account: null,
  isLoading: false,
  errorMessage: null,
};

/**
 * 勘定科目を取得するカスタムフック
 */
const useAccountFetch = (accountId: number, isInvalidId: boolean) => {
  const [state, setState] = useState<AccountState>(initialState);

  const fetchAccount = useCallback(async () => {
    if (isInvalidId) {
      setState({ account: null, isLoading: false, errorMessage: '勘定科目が見つかりませんでした' });
      return;
    }

    setState((prev) => ({ ...prev, isLoading: true, errorMessage: null }));
    try {
      const data = await getAccount(accountId);
      setState({ account: data, isLoading: false, errorMessage: null });
    } catch (error) {
      setState((prev) => ({
        ...prev,
        isLoading: false,
        errorMessage: getAccountErrorMessage(error),
      }));
    }
  }, [accountId, isInvalidId]);

  useEffect(() => {
    void fetchAccount();
  }, [fetchAccount]);

  return { state, fetchAccount };
};

/**
 * 勘定科目編集ページコンテンツ
 */
const EditAccountContent: React.FC<{
  state: AccountState;
  fetchAccount: () => Promise<void>;
  navigate: ReturnType<typeof useNavigate>;
}> = ({ state, fetchAccount, navigate }) => {
  const breadcrumbs = useMemo(
    () => [
      { label: 'ホーム' },
      { label: 'マスタ管理' },
      { label: '勘定科目一覧', path: '/master/accounts' },
      { label: '勘定科目編集' },
    ],
    []
  );

  return (
    <MainLayout breadcrumbs={breadcrumbs}>
      <div data-testid="edit-account-page">
        <h1>勘定科目編集</h1>
        {state.isLoading && <Loading message="勘定科目を読み込み中..." />}
        {state.errorMessage && <ErrorMessage message={state.errorMessage} onRetry={fetchAccount} />}
        {state.account && (
          <EditAccountForm
            account={state.account}
            onSuccess={(message) =>
              navigate('/master/accounts', { replace: true, state: { successMessage: message } })
            }
          />
        )}
      </div>
    </MainLayout>
  );
};

/**
 * 勘定科目編集ページ
 */
const EditAccountPage: React.FC = () => {
  const { isAuthenticated, isLoading, hasRole } = useAuth();
  const { id } = useParams();
  const navigate = useNavigate();

  const accountId = Number(id);
  const isInvalidId = Number.isNaN(accountId);
  const { state, fetchAccount } = useAccountFetch(accountId, isInvalidId);

  if (isLoading) {
    return <Loading message="認証情報を確認中..." fullScreen />;
  }

  if (!isAuthenticated) {
    return <Navigate to="/login" replace />;
  }

  if (!hasRole('ADMIN') && !hasRole('MANAGER')) {
    return <Navigate to="/" replace />;
  }

  return <EditAccountContent state={state} fetchAccount={fetchAccount} navigate={navigate} />;
};

export default EditAccountPage;
