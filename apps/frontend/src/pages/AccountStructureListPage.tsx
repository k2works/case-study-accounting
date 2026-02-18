import React, { useCallback, useEffect, useMemo, useState } from 'react';
import { Navigate, useLocation, useNavigate } from 'react-router-dom';
import { useAuth } from '../hooks/useAuth';
import {
  getAccountStructures,
  getAccountStructuresErrorMessage,
  type AccountStructure,
} from '../api/getAccountStructures';
import { MainLayout, Loading, ErrorMessage, SuccessNotification, Button } from '../views/common';
import { AccountStructureList } from '../views/account-structure/AccountStructureList';

interface AccountStructureListLocationState {
  successMessage?: string;
}

interface AccountStructureListContentProps {
  structures: AccountStructure[];
  isFetching: boolean;
  errorMessage: string | null;
  successMessage: string | null;
  isManager: boolean;
  onDismissSuccess: () => void;
  onRetry: () => Promise<void>;
  onCreate: () => void;
  onEdit: (structure: AccountStructure) => void;
}

const AccountStructureListContent: React.FC<AccountStructureListContentProps> = ({
  structures,
  isFetching,
  errorMessage,
  successMessage,
  isManager,
  onDismissSuccess,
  onRetry,
  onCreate,
  onEdit,
}) => {
  return (
    <div data-testid="account-structure-list-page">
      <h1>勘定科目体系</h1>
      {isManager && (
        <div style={{ marginBottom: '16px' }}>
          <Button onClick={onCreate}>新規登録</Button>
        </div>
      )}
      {successMessage && (
        <div style={{ marginBottom: '16px' }}>
          <SuccessNotification message={successMessage} onDismiss={onDismissSuccess} />
        </div>
      )}
      {isFetching && structures.length === 0 && <Loading message="勘定科目構成を読み込み中..." />}
      {errorMessage ? (
        <ErrorMessage message={errorMessage} onRetry={onRetry} />
      ) : (
        <AccountStructureList
          structures={structures}
          onEdit={onEdit}
          onDelete={() => void onRetry()}
        />
      )}
    </div>
  );
};

const AccountStructureListPage: React.FC = () => {
  const { isAuthenticated, isLoading, hasRole } = useAuth();
  const location = useLocation();
  const navigate = useNavigate();
  const [structures, setStructures] = useState<AccountStructure[]>([]);
  const [isFetching, setIsFetching] = useState(false);
  const [errorMessage, setErrorMessage] = useState<string | null>(null);
  const [successMessage, setSuccessMessage] = useState<string | null>(() => {
    const state = location.state as AccountStructureListLocationState | null;
    return state?.successMessage ?? null;
  });

  const fetchStructures = useCallback(async () => {
    setIsFetching(true);
    setErrorMessage(null);
    try {
      const data = await getAccountStructures();
      setStructures(data);
    } catch (error) {
      setErrorMessage(getAccountStructuresErrorMessage(error));
    } finally {
      setIsFetching(false);
    }
  }, []);

  useEffect(() => {
    void fetchStructures();
  }, [fetchStructures]);

  const breadcrumbs = useMemo(
    () => [{ label: 'ホーム' }, { label: 'マスタ管理' }, { label: '勘定科目体系' }],
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
      <AccountStructureListContent
        structures={structures}
        isFetching={isFetching}
        errorMessage={errorMessage}
        successMessage={successMessage}
        isManager={hasRole('MANAGER')}
        onDismissSuccess={() => setSuccessMessage(null)}
        onRetry={fetchStructures}
        onCreate={() => navigate('/master/account-structures/new')}
        onEdit={(structure) => navigate(`/master/account-structures/${structure.accountCode}/edit`)}
      />
    </MainLayout>
  );
};

export default AccountStructureListPage;
