import React, { useCallback, useMemo, useState } from 'react';
import { useAuth } from '../../hooks/useAuth';
import type { AccountStructure } from '../../api/getAccountStructures';
import {
  deleteAccountStructure,
  getDeleteAccountStructureErrorMessage,
} from '../../api/deleteAccountStructure';
import { ErrorMessage, SuccessNotification, Table, TableColumn, Button } from '../common';
import './AccountStructureList.css';

interface AccountStructureListProps {
  structures: AccountStructure[];
  onEdit: (structure: AccountStructure) => void;
  onDelete: () => void;
}

export const AccountStructureList: React.FC<AccountStructureListProps> = ({
  structures,
  onEdit,
  onDelete,
}) => {
  const { hasRole } = useAuth();
  const canManage = hasRole('MANAGER');
  const [deleteSuccessMessage, setDeleteSuccessMessage] = useState<string | null>(null);
  const [deleteErrorMessage, setDeleteErrorMessage] = useState<string | null>(null);
  const [deletingAccountCode, setDeletingAccountCode] = useState<string | null>(null);

  const handleDelete = useCallback(
    async (structure: AccountStructure) => {
      const isConfirmed = window.confirm(
        `勘定科目構成「${structure.accountCode}」を削除しますか？`
      );
      if (!isConfirmed) {
        return;
      }

      setDeleteErrorMessage(null);
      setDeleteSuccessMessage(null);
      setDeletingAccountCode(structure.accountCode);

      try {
        const response = await deleteAccountStructure(structure.accountCode);
        if (!response.success) {
          throw new Error(response.errorMessage || '勘定科目構成の削除に失敗しました');
        }
        setDeleteSuccessMessage(response.message || '勘定科目構成を削除しました');
        onDelete();
      } catch (error) {
        setDeleteErrorMessage(getDeleteAccountStructureErrorMessage(error));
      } finally {
        setDeletingAccountCode(null);
      }
    },
    [onDelete]
  );

  const columns = useMemo<TableColumn<AccountStructure>[]>(
    () => [
      { key: 'accountCode', header: '勘定科目コード', width: '160px' },
      { key: 'accountName', header: '勘定科目名' },
      { key: 'accountPath', header: 'パス' },
      { key: 'hierarchyLevel', header: '階層', width: '80px', align: 'center' },
      { key: 'parentAccountCode', header: '親科目コード', width: '160px' },
      { key: 'displayOrder', header: '表示順', width: '80px', align: 'center' },
      ...(canManage
        ? [
            {
              key: 'actions',
              header: '操作',
              width: '180px',
              align: 'center' as const,
              render: (_: unknown, row: AccountStructure) => (
                <div className="account-structure-list__actions">
                  <Button variant="text" size="small" onClick={() => onEdit(row)}>
                    編集
                  </Button>
                  <Button
                    variant="danger"
                    size="small"
                    onClick={() => void handleDelete(row)}
                    disabled={deletingAccountCode === row.accountCode}
                  >
                    削除
                  </Button>
                </div>
              ),
            } satisfies TableColumn<AccountStructure>,
          ]
        : []),
    ],
    [canManage, deletingAccountCode, handleDelete, onEdit]
  );

  return (
    <div data-testid="account-structure-list">
      {deleteSuccessMessage && (
        <div className="account-structure-list__notification">
          <SuccessNotification
            message={deleteSuccessMessage}
            onDismiss={() => setDeleteSuccessMessage(null)}
          />
        </div>
      )}
      {deleteErrorMessage && (
        <div className="account-structure-list__notification">
          <ErrorMessage
            message={deleteErrorMessage}
            onDismiss={() => setDeleteErrorMessage(null)}
          />
        </div>
      )}
      <Table
        columns={columns}
        data={structures}
        keyField="accountCode"
        emptyMessage="勘定科目構成が登録されていません"
      />
    </div>
  );
};
