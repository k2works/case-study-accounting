import React, { useCallback, useMemo, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import type { JournalEntrySummary } from '../../api/getJournalEntries';
import { deleteJournalEntry, deleteJournalEntryErrorMessage } from '../../api/deleteJournalEntry';
import {
  submitJournalEntryForApproval,
  submitForApprovalErrorMessage,
} from '../../api/submitJournalEntryForApproval';
import {
  approveJournalEntry,
  approveJournalEntryErrorMessage,
} from '../../api/approveJournalEntry';
import {
  ErrorMessage,
  SuccessNotification,
  Table,
  TableColumn,
  Button,
  Pagination,
} from '../common';
import { JournalEntryFilter, JournalEntryFilterValues } from './JournalEntryFilter';
import './JournalEntryList.css';

interface JournalEntryListProps {
  entries: JournalEntrySummary[];
  filterValues: JournalEntryFilterValues;
  onFilterChange: (values: JournalEntryFilterValues) => void;
  onSearch: () => void;
  onReset: () => void;
  onDelete: () => void;
  currentPage: number;
  totalPages: number;
  totalItems: number;
  itemsPerPage: number;
  onPageChange: (page: number) => void;
  onItemsPerPageChange: (size: number) => void;
}

const statusLabels: Record<string, string> = {
  DRAFT: '下書き',
  PENDING: '承認待ち',
  APPROVED: '承認済み',
  CONFIRMED: '確定',
};

const formatAmount = (amount: number): string => {
  return amount.toLocaleString('ja-JP', { maximumFractionDigits: 0 });
};

/**
 * 仕訳一覧コンポーネント
 */
export const JournalEntryList: React.FC<JournalEntryListProps> = ({
  entries,
  filterValues,
  onFilterChange,
  onSearch,
  onReset,
  onDelete,
  currentPage,
  totalPages,
  totalItems,
  itemsPerPage,
  onPageChange,
  onItemsPerPageChange,
}) => {
  const navigate = useNavigate();
  const [deleteSuccessMessage, setDeleteSuccessMessage] = useState<string | null>(null);
  const [deleteErrorMessage, setDeleteErrorMessage] = useState<string | null>(null);
  const [deletingEntryId, setDeletingEntryId] = useState<number | null>(null);
  const [submitSuccessMessage, setSubmitSuccessMessage] = useState<string | null>(null);
  const [submitErrorMessage, setSubmitErrorMessage] = useState<string | null>(null);
  const [submittingEntryId, setSubmittingEntryId] = useState<number | null>(null);
  const [approveSuccessMessage, setApproveSuccessMessage] = useState<string | null>(null);
  const [approveErrorMessage, setApproveErrorMessage] = useState<string | null>(null);
  const [approvingEntryId, setApprovingEntryId] = useState<number | null>(null);

  const handleEdit = useCallback(
    (entry: JournalEntrySummary) => {
      navigate(`/journal/entries/${entry.journalEntryId}/edit`);
    },
    [navigate]
  );

  const handleDelete = useCallback(
    async (entry: JournalEntrySummary) => {
      const isConfirmed = window.confirm(`仕訳「${entry.description}」を削除しますか？`);
      if (!isConfirmed) {
        return;
      }

      setDeleteErrorMessage(null);
      setDeleteSuccessMessage(null);
      setDeletingEntryId(entry.journalEntryId);

      try {
        const response = await deleteJournalEntry(entry.journalEntryId);
        if (!response.success) {
          throw new Error(response.errorMessage || '仕訳の削除に失敗しました');
        }
        setDeleteSuccessMessage(response.message || '仕訳を削除しました');
        onDelete();
      } catch (error) {
        setDeleteErrorMessage(deleteJournalEntryErrorMessage(error));
      } finally {
        setDeletingEntryId(null);
      }
    },
    [onDelete]
  );

  const handleSubmitForApproval = useCallback(
    async (entry: JournalEntrySummary) => {
      const isConfirmed = window.confirm(`仕訳「${entry.description}」を承認申請しますか？`);
      if (!isConfirmed) {
        return;
      }

      setSubmitErrorMessage(null);
      setSubmitSuccessMessage(null);
      setSubmittingEntryId(entry.journalEntryId);

      try {
        const response = await submitJournalEntryForApproval(entry.journalEntryId);
        if (!response.success) {
          throw new Error(response.errorMessage || '承認申請に失敗しました');
        }
        setSubmitSuccessMessage(response.message || '仕訳を承認申請しました');
        onDelete();
      } catch (error) {
        setSubmitErrorMessage(submitForApprovalErrorMessage(error));
      } finally {
        setSubmittingEntryId(null);
      }
    },
    [onDelete]
  );

  const handleApprove = useCallback(
    async (entry: JournalEntrySummary) => {
      const isConfirmed = window.confirm(`仕訳「${entry.description}」を承認しますか？`);
      if (!isConfirmed) {
        return;
      }

      setApproveErrorMessage(null);
      setApproveSuccessMessage(null);
      setApprovingEntryId(entry.journalEntryId);

      try {
        const response = await approveJournalEntry(entry.journalEntryId);
        if (!response.success) {
          throw new Error(response.errorMessage || '承認に失敗しました');
        }
        setApproveSuccessMessage(response.message || '仕訳を承認しました');
        onDelete();
      } catch (error) {
        setApproveErrorMessage(approveJournalEntryErrorMessage(error));
      } finally {
        setApprovingEntryId(null);
      }
    },
    [onDelete]
  );

  const columns = useMemo<TableColumn<JournalEntrySummary>[]>(
    () => [
      { key: 'journalEntryId', header: '仕訳番号', width: '100px' },
      { key: 'journalDate', header: '仕訳日付', width: '120px' },
      { key: 'description', header: '摘要' },
      {
        key: 'totalDebitAmount',
        header: '借方金額',
        width: '130px',
        align: 'right',
        render: (value) => <>{formatAmount(value as number)}</>,
      },
      {
        key: 'totalCreditAmount',
        header: '貸方金額',
        width: '130px',
        align: 'right',
        render: (value) => <>{formatAmount(value as number)}</>,
      },
      {
        key: 'status',
        header: 'ステータス',
        width: '110px',
        render: (value) => <>{statusLabels[value as string] || String(value)}</>,
      },
      {
        key: 'actions',
        header: '操作',
        width: '250px',
        align: 'center',
        render: (_, row) => (
          <div className="journal-entry-list__actions">
            <Button variant="text" size="small" onClick={() => handleEdit(row)}>
              編集
            </Button>
            {row.status === 'DRAFT' && (
              <Button
                variant="primary"
                size="small"
                onClick={() => void handleSubmitForApproval(row)}
                disabled={submittingEntryId === row.journalEntryId}
              >
                承認申請
              </Button>
            )}
            {row.status === 'PENDING' && (
              <Button
                variant="primary"
                size="small"
                onClick={() => void handleApprove(row)}
                disabled={approvingEntryId === row.journalEntryId}
              >
                承認
              </Button>
            )}
            <Button
              variant="danger"
              size="small"
              onClick={() => void handleDelete(row)}
              disabled={deletingEntryId === row.journalEntryId || row.status !== 'DRAFT'}
            >
              削除
            </Button>
          </div>
        ),
      },
    ],
    [
      approvingEntryId,
      deletingEntryId,
      handleApprove,
      handleDelete,
      handleEdit,
      handleSubmitForApproval,
      submittingEntryId,
    ]
  );

  return (
    <div className="journal-entry-list" data-testid="journal-entry-list">
      <JournalEntryFilter
        values={filterValues}
        onChange={onFilterChange}
        onSearch={onSearch}
        onReset={onReset}
      />
      {deleteSuccessMessage && (
        <div className="journal-entry-list__notification">
          <SuccessNotification
            message={deleteSuccessMessage}
            onDismiss={() => setDeleteSuccessMessage(null)}
          />
        </div>
      )}
      {deleteErrorMessage && (
        <div className="journal-entry-list__notification">
          <ErrorMessage
            message={deleteErrorMessage}
            onDismiss={() => setDeleteErrorMessage(null)}
          />
        </div>
      )}
      {submitSuccessMessage && (
        <div className="journal-entry-list__notification">
          <SuccessNotification
            message={submitSuccessMessage}
            onDismiss={() => setSubmitSuccessMessage(null)}
          />
        </div>
      )}
      {submitErrorMessage && (
        <div className="journal-entry-list__notification">
          <ErrorMessage
            message={submitErrorMessage}
            onDismiss={() => setSubmitErrorMessage(null)}
          />
        </div>
      )}
      {approveSuccessMessage && (
        <div className="journal-entry-list__notification">
          <SuccessNotification
            message={approveSuccessMessage}
            onDismiss={() => setApproveSuccessMessage(null)}
          />
        </div>
      )}
      {approveErrorMessage && (
        <div className="journal-entry-list__notification">
          <ErrorMessage
            message={approveErrorMessage}
            onDismiss={() => setApproveErrorMessage(null)}
          />
        </div>
      )}
      <Table
        columns={columns}
        data={entries}
        keyField="journalEntryId"
        emptyMessage="仕訳が登録されていません"
      />
      <Pagination
        currentPage={currentPage}
        totalPages={totalPages}
        totalItems={totalItems}
        itemsPerPage={itemsPerPage}
        onPageChange={onPageChange}
        onItemsPerPageChange={onItemsPerPageChange}
      />
    </div>
  );
};
