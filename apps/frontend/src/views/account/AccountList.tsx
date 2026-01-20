import React, { useCallback, useEffect, useMemo, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { getAccounts, getAccountsErrorMessage, Account } from '../../api/getAccounts';
import { ErrorMessage, Loading, Table, TableColumn, Button } from '../common';
import './AccountList.css';

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

/**
 * 勘定科目一覧コンポーネント
 */
export const AccountList: React.FC = () => {
  const [state, setState] = useState<AccountListState>(initialState);
  const navigate = useNavigate();

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

  const columns = useMemo<TableColumn<Account>[]>(
    () => [
      { key: 'accountCode', header: '勘定科目コード', width: '160px' },
      { key: 'accountName', header: '勘定科目名' },
      { key: 'accountType', header: '勘定科目種別', width: '160px' },
      {
        key: 'actions',
        header: '操作',
        width: '120px',
        align: 'center',
        render: (_, row) => (
          <Button
            variant="text"
            size="small"
            onClick={() => navigate(`/master/accounts/${row.accountId}/edit`)}
          >
            編集
          </Button>
        ),
      },
    ],
    [navigate]
  );

  if (state.isLoading && state.accounts.length === 0) {
    return <Loading message="勘定科目を読み込み中..." />;
  }

  return (
    <div className="account-list" data-testid="account-list">
      {state.errorMessage ? (
        <ErrorMessage message={state.errorMessage} onRetry={fetchAccounts} />
      ) : (
        <Table
          columns={columns}
          data={state.accounts}
          keyField="accountId"
          isLoading={state.isLoading}
          emptyMessage="勘定科目が登録されていません"
        />
      )}
    </div>
  );
};
