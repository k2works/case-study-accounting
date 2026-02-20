import React, { useCallback, useMemo, useState } from 'react';
import { useAuth } from '../../hooks/useAuth';
import type { AutoJournalPattern } from '../../api/getAutoJournalPatterns';
import {
  deleteAutoJournalPattern,
  getDeleteAutoJournalPatternErrorMessage,
} from '../../api/deleteAutoJournalPattern';
import { ErrorMessage, SuccessNotification, Table, type TableColumn, Button } from '../common';
import './AutoJournalPatternList.css';

interface AutoJournalPatternListProps {
  patterns: AutoJournalPattern[];
  onEdit: (pattern: AutoJournalPattern) => void;
  onDelete: () => void;
}

const renderActiveBadge = (isActive: boolean): React.ReactNode => {
  return (
    <span className={`auto-journal-pattern-list__status ${isActive ? 'is-active' : 'is-inactive'}`}>
      {isActive ? '有効' : '無効'}
    </span>
  );
};

export const AutoJournalPatternList: React.FC<AutoJournalPatternListProps> = ({
  patterns,
  onEdit,
  onDelete,
}) => {
  const { hasRole } = useAuth();
  const canManage = hasRole('ADMIN') || hasRole('MANAGER');
  const [deleteSuccessMessage, setDeleteSuccessMessage] = useState<string | null>(null);
  const [deleteErrorMessage, setDeleteErrorMessage] = useState<string | null>(null);
  const [deletingPatternId, setDeletingPatternId] = useState<number | null>(null);

  const handleDelete = useCallback(
    async (pattern: AutoJournalPattern) => {
      const isConfirmed = window.confirm(
        `自動仕訳パターン「${pattern.patternName}」を削除しますか？`
      );
      if (!isConfirmed) {
        return;
      }

      setDeleteErrorMessage(null);
      setDeleteSuccessMessage(null);
      setDeletingPatternId(pattern.patternId);

      try {
        const response = await deleteAutoJournalPattern(pattern.patternId);
        if (!response.success) {
          throw new Error(response.errorMessage || '自動仕訳パターンの削除に失敗しました');
        }
        setDeleteSuccessMessage(response.message || '自動仕訳パターンを削除しました');
        onDelete();
      } catch (error) {
        setDeleteErrorMessage(getDeleteAutoJournalPatternErrorMessage(error));
      } finally {
        setDeletingPatternId(null);
      }
    },
    [onDelete]
  );

  const columns = useMemo<TableColumn<AutoJournalPattern>[]>(
    () => [
      { key: 'patternCode', header: 'パターンコード', width: '160px' },
      { key: 'patternName', header: 'パターン名' },
      { key: 'sourceTableName', header: 'ソーステーブル名', width: '220px' },
      {
        key: 'isActive',
        header: '状態',
        width: '100px',
        align: 'center',
        render: (value: unknown) => renderActiveBadge(Boolean(value)),
      },
      ...(canManage
        ? [
            {
              key: 'actions',
              header: '操作',
              width: '180px',
              align: 'center' as const,
              render: (_: unknown, row: AutoJournalPattern) => (
                <div className="auto-journal-pattern-list__actions">
                  <Button
                    variant="text"
                    size="small"
                    onClick={() => onEdit(row)}
                    data-testid={`auto-journal-pattern-edit-${row.patternId}`}
                  >
                    編集
                  </Button>
                  <Button
                    variant="danger"
                    size="small"
                    onClick={() => void handleDelete(row)}
                    disabled={deletingPatternId === row.patternId}
                    data-testid={`auto-journal-pattern-delete-${row.patternId}`}
                  >
                    削除
                  </Button>
                </div>
              ),
            } satisfies TableColumn<AutoJournalPattern>,
          ]
        : []),
    ],
    [canManage, deletingPatternId, handleDelete, onEdit]
  );

  return (
    <div className="auto-journal-pattern-list" data-testid="auto-journal-pattern-list">
      {deleteSuccessMessage && (
        <div className="auto-journal-pattern-list__notification">
          <SuccessNotification
            message={deleteSuccessMessage}
            onDismiss={() => setDeleteSuccessMessage(null)}
          />
        </div>
      )}
      {deleteErrorMessage && (
        <div className="auto-journal-pattern-list__notification">
          <ErrorMessage
            message={deleteErrorMessage}
            onDismiss={() => setDeleteErrorMessage(null)}
          />
        </div>
      )}
      <Table
        columns={columns}
        data={patterns}
        keyField="patternId"
        emptyMessage="自動仕訳パターンが登録されていません"
        getRowTestId={(row) => `auto-journal-pattern-row-${row.patternCode}`}
      />
    </div>
  );
};
