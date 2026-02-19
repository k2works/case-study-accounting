import React, { useCallback, useEffect, useState } from 'react';
import { useLocation, useNavigate } from 'react-router-dom';
import { useAuth } from '../hooks/useAuth';
import {
  getAccountStructures,
  getAccountStructuresErrorMessage,
  type AccountStructure,
} from '../api/getAccountStructures';
import { Loading, ErrorMessage, SuccessNotification, Button } from '../views/common';
import { AccountStructureList } from '../views/account-structure/AccountStructureList';
import { ManagerPage } from './ManagerPage';

interface AccountStructureListLocationState {
  successMessage?: string;
}

const breadcrumbs = [{ label: 'ホーム' }, { label: 'マスタ管理' }, { label: '勘定科目体系' }];

const AccountStructureListPage: React.FC = () => {
  const { hasRole } = useAuth();
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

  return (
    <ManagerPage breadcrumbs={breadcrumbs}>
      <div data-testid="account-structure-list-page">
        <h1>勘定科目体系</h1>
        {hasRole('MANAGER') && (
          <div style={{ marginBottom: '16px' }}>
            <Button onClick={() => navigate('/master/account-structures/new')}>新規登録</Button>
          </div>
        )}
        {successMessage && (
          <div style={{ marginBottom: '16px' }}>
            <SuccessNotification
              message={successMessage}
              onDismiss={() => setSuccessMessage(null)}
            />
          </div>
        )}
        {isFetching && structures.length === 0 && <Loading message="勘定科目構成を読み込み中..." />}
        {errorMessage ? (
          <ErrorMessage message={errorMessage} onRetry={fetchStructures} />
        ) : (
          <AccountStructureList
            structures={structures}
            onEdit={(structure) =>
              navigate(`/master/account-structures/${structure.accountCode}/edit`)
            }
            onDelete={() => void fetchStructures()}
          />
        )}
      </div>
    </ManagerPage>
  );
};

export default AccountStructureListPage;
