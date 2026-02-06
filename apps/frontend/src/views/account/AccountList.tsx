import React, { useCallback, useMemo, useState } from 'react';
import type { Account } from '../../api/getAccounts';
import { deleteAccount, getDeleteAccountErrorMessage } from '../../api/deleteAccount';
import { ErrorMessage, SuccessNotification, Table, TableColumn, Button } from '../common';
import { AccountFilter, AccountFilterValues } from './AccountFilter';
import './AccountList.css';

interface AccountListProps {
  accounts: Account[];
  filterValues: AccountFilterValues;
  onFilterChange: (values: AccountFilterValues) => void;
  onSearch: () => void;
  onReset: () => void;
  onEdit: (account: Account) => void;
  onDelete: () => void;
}

/**
 * 勘定科目一覧コンポーネント
 */
export const AccountList: React.FC<AccountListProps> = ({
  accounts,
  filterValues,
  onFilterChange,
  onSearch,
  onReset,
  onEdit,
  onDelete,
}) => {
  const [deleteSuccessMessage, setDeleteSuccessMessage] = useState<string | null>(null);
  const [deleteErrorMessage, setDeleteErrorMessage] = useState<string | null>(null);
  const [deletingAccountId, setDeletingAccountId] = useState<number | null>(null);

  const handleDelete = useCallback(
    async (account: Account) => {
      const isConfirmed = window.confirm(`勘定科目「${account.accountName}」を削除しますか？`);
      if (!isConfirmed) {
        return;
      }

      setDeleteErrorMessage(null);
      setDeleteSuccessMessage(null);
      setDeletingAccountId(account.accountId);

      try {
        const response = await deleteAccount(account.accountId);
        if (!response.success) {
          throw new Error(response.errorMessage || '勘定科目の削除に失敗しました');
        }
        setDeleteSuccessMessage(response.message || '勘定科目を削除しました');
        onDelete();
      } catch (error) {
        setDeleteErrorMessage(getDeleteAccountErrorMessage(error));
      } finally {
        setDeletingAccountId(null);
      }
    },
    [onDelete]
  );

  const columns = useMemo<TableColumn<Account>[]>(
    () => [
      { key: 'accountCode', header: '勘定科目コード', width: '160px' },
      { key: 'accountName', header: '勘定科目名' },
      { key: 'accountType', header: '勘定科目種別', width: '160px' },
      {
        key: 'actions',
        header: '操作',
        width: '180px',
        align: 'center',
        render: (_, row) => (
          <div className="account-list__actions">
            <Button variant="text" size="small" onClick={() => onEdit(row)}>
              編集
            </Button>
            <Button
              variant="danger"
              size="small"
              onClick={() => void handleDelete(row)}
              disabled={deletingAccountId === row.accountId}
            >
              削除
            </Button>
          </div>
        ),
      },
    ],
    [deletingAccountId, handleDelete, onEdit]
  );

  return (
    <div className="account-list" data-testid="account-list">
      <AccountFilter
        values={filterValues}
        onChange={onFilterChange}
        onSearch={onSearch}
        onReset={onReset}
      />
      {deleteSuccessMessage && (
        <div className="account-list__notification">
          <SuccessNotification
            message={deleteSuccessMessage}
            onDismiss={() => setDeleteSuccessMessage(null)}
          />
        </div>
      )}
      {deleteErrorMessage && (
        <div className="account-list__notification">
          <ErrorMessage
            message={deleteErrorMessage}
            onDismiss={() => setDeleteErrorMessage(null)}
          />
        </div>
      )}
      <Table
        columns={columns}
        data={accounts}
        keyField="accountId"
        emptyMessage="勘定科目が登録されていません"
      />
    </div>
  );
};
