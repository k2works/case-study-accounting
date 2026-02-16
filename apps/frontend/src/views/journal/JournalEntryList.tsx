import React, { useCallback, useMemo, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { useAuth } from '../../hooks/useAuth';
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
  confirmJournalEntry,
  confirmJournalEntryErrorMessage,
} from '../../api/confirmJournalEntry';
import { rejectJournalEntry, rejectJournalEntryErrorMessage } from '../../api/rejectJournalEntry';
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

interface NotificationItem {
  type: 'success' | 'error';
  message: string;
  onDismiss: () => void;
}

const NotificationList: React.FC<{ items: NotificationItem[] }> = ({ items }) => (
  <>
    {items
      .filter((item) => item.message)
      .map((item, idx) => (
        <div className="journal-entry-list__notification" key={idx}>
          {item.type === 'success' ? (
            <SuccessNotification message={item.message} onDismiss={item.onDismiss} />
          ) : (
            <ErrorMessage message={item.message} onDismiss={item.onDismiss} />
          )}
        </div>
      ))}
  </>
);

interface ActionButtonsProps {
  row: JournalEntrySummary;
  canEdit: boolean;
  canCreate: boolean;
  canApprove: boolean;
  onEdit: (row: JournalEntrySummary) => void;
  onSubmit: (row: JournalEntrySummary) => Promise<void>;
  onApprove: (row: JournalEntrySummary) => Promise<void>;
  onReject: (row: JournalEntrySummary) => Promise<void>;
  onConfirm: (row: JournalEntrySummary) => Promise<void>;
  onDelete: (row: JournalEntrySummary) => Promise<void>;
  submittingId: number | null;
  approvingId: number | null;
  rejectingId: number | null;
  confirmingId: number | null;
  deletingId: number | null;
}

const DraftActions: React.FC<
  Pick<
    ActionButtonsProps,
    | 'row'
    | 'canEdit'
    | 'canCreate'
    | 'onEdit'
    | 'onSubmit'
    | 'onDelete'
    | 'submittingId'
    | 'deletingId'
  >
> = ({ row, canEdit, canCreate, onEdit, onSubmit, onDelete, submittingId, deletingId }) => (
  <>
    {canEdit && (
      <Button variant="text" size="small" onClick={() => onEdit(row)}>
        編集
      </Button>
    )}
    {canCreate && (
      <Button
        variant="primary"
        size="small"
        onClick={() => void onSubmit(row)}
        disabled={submittingId === row.journalEntryId}
      >
        承認申請
      </Button>
    )}
    {canEdit && (
      <Button
        variant="danger"
        size="small"
        onClick={() => void onDelete(row)}
        disabled={deletingId === row.journalEntryId}
      >
        削除
      </Button>
    )}
  </>
);

const WorkflowActions: React.FC<
  Pick<
    ActionButtonsProps,
    | 'row'
    | 'canApprove'
    | 'onApprove'
    | 'onReject'
    | 'onConfirm'
    | 'approvingId'
    | 'rejectingId'
    | 'confirmingId'
  >
> = ({
  row,
  canApprove,
  onApprove,
  onReject,
  onConfirm,
  approvingId,
  rejectingId,
  confirmingId,
}) => {
  if (!canApprove) return null;
  if (row.status === 'PENDING') {
    return (
      <>
        <Button
          variant="primary"
          size="small"
          onClick={() => void onApprove(row)}
          disabled={approvingId === row.journalEntryId}
        >
          承認
        </Button>
        <Button
          variant="danger"
          size="small"
          onClick={() => void onReject(row)}
          disabled={rejectingId === row.journalEntryId}
        >
          差し戻し
        </Button>
      </>
    );
  }
  if (row.status === 'APPROVED') {
    return (
      <Button
        variant="primary"
        size="small"
        onClick={() => void onConfirm(row)}
        disabled={confirmingId === row.journalEntryId}
      >
        確定
      </Button>
    );
  }
  return null;
};

const ActionButtons: React.FC<ActionButtonsProps> = (props) => (
  <div className="journal-entry-list__actions">
    {props.row.status === 'DRAFT' && (
      <DraftActions
        row={props.row}
        canEdit={props.canEdit}
        canCreate={props.canCreate}
        onEdit={props.onEdit}
        onSubmit={props.onSubmit}
        onDelete={props.onDelete}
        submittingId={props.submittingId}
        deletingId={props.deletingId}
      />
    )}
    <WorkflowActions
      row={props.row}
      canApprove={props.canApprove}
      onApprove={props.onApprove}
      onReject={props.onReject}
      onConfirm={props.onConfirm}
      approvingId={props.approvingId}
      rejectingId={props.rejectingId}
      confirmingId={props.confirmingId}
    />
  </div>
);

// eslint-disable-next-line complexity -- フック内に複数のアクションハンドラを集約しているため
const useJournalEntryActions = (onRefresh: () => void) => {
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
  const [rejectSuccessMessage, setRejectSuccessMessage] = useState<string | null>(null);
  const [rejectErrorMessage, setRejectErrorMessage] = useState<string | null>(null);
  const [rejectingEntryId, setRejectingEntryId] = useState<number | null>(null);
  const [confirmSuccessMessage, setConfirmSuccessMessage] = useState<string | null>(null);
  const [confirmErrorMessage, setConfirmErrorMessage] = useState<string | null>(null);
  const [confirmingEntryId, setConfirmingEntryId] = useState<number | null>(null);

  const handleEdit = useCallback(
    (entry: JournalEntrySummary) => {
      navigate(`/journal/entries/${entry.journalEntryId}/edit`);
    },
    [navigate]
  );

  const handleDelete = useCallback(
    async (entry: JournalEntrySummary) => {
      const isConfirmed = window.confirm(`仕訳「${entry.description}」を削除しますか？`);
      if (!isConfirmed) return;
      setDeleteErrorMessage(null);
      setDeleteSuccessMessage(null);
      setDeletingEntryId(entry.journalEntryId);
      try {
        const response = await deleteJournalEntry(entry.journalEntryId);
        if (!response.success) throw new Error(response.errorMessage || '仕訳の削除に失敗しました');
        setDeleteSuccessMessage(response.message || '仕訳を削除しました');
        onRefresh();
      } catch (error) {
        setDeleteErrorMessage(deleteJournalEntryErrorMessage(error));
      } finally {
        setDeletingEntryId(null);
      }
    },
    [onRefresh]
  );

  const handleSubmitForApproval = useCallback(
    async (entry: JournalEntrySummary) => {
      const isConfirmed = window.confirm(`仕訳「${entry.description}」を承認申請しますか？`);
      if (!isConfirmed) return;
      setSubmitErrorMessage(null);
      setSubmitSuccessMessage(null);
      setSubmittingEntryId(entry.journalEntryId);
      try {
        const response = await submitJournalEntryForApproval(entry.journalEntryId);
        if (!response.success) throw new Error(response.errorMessage || '承認申請に失敗しました');
        setSubmitSuccessMessage(response.message || '仕訳を承認申請しました');
        onRefresh();
      } catch (error) {
        setSubmitErrorMessage(submitForApprovalErrorMessage(error));
      } finally {
        setSubmittingEntryId(null);
      }
    },
    [onRefresh]
  );

  const handleApprove = useCallback(
    async (entry: JournalEntrySummary) => {
      const isConfirmed = window.confirm(`仕訳「${entry.description}」を承認しますか？`);
      if (!isConfirmed) return;
      setApproveErrorMessage(null);
      setApproveSuccessMessage(null);
      setApprovingEntryId(entry.journalEntryId);
      try {
        const response = await approveJournalEntry(entry.journalEntryId);
        if (!response.success) throw new Error(response.errorMessage || '承認に失敗しました');
        setApproveSuccessMessage(response.message || '仕訳を承認しました');
        onRefresh();
      } catch (error) {
        setApproveErrorMessage(approveJournalEntryErrorMessage(error));
      } finally {
        setApprovingEntryId(null);
      }
    },
    [onRefresh]
  );

  const handleReject = useCallback(
    async (entry: JournalEntrySummary) => {
      const rejectionReason = window.prompt(
        `仕訳「${entry.description}」を差し戻します。\n差し戻し理由を入力してください：`
      );
      if (rejectionReason === null) return;
      if (rejectionReason.trim() === '') {
        setRejectErrorMessage('差し戻し理由は必須です');
        return;
      }
      setRejectErrorMessage(null);
      setRejectSuccessMessage(null);
      setRejectingEntryId(entry.journalEntryId);
      try {
        const response = await rejectJournalEntry(entry.journalEntryId, rejectionReason.trim());
        if (!response.success) throw new Error(response.errorMessage || '差し戻しに失敗しました');
        setRejectSuccessMessage(response.message || '仕訳を差し戻しました');
        onRefresh();
      } catch (error) {
        setRejectErrorMessage(rejectJournalEntryErrorMessage(error));
      } finally {
        setRejectingEntryId(null);
      }
    },
    [onRefresh]
  );

  const handleConfirm = useCallback(
    async (entry: JournalEntrySummary) => {
      const isConfirmed = window.confirm(`仕訳「${entry.description}」を確定しますか？`);
      if (!isConfirmed) return;
      setConfirmErrorMessage(null);
      setConfirmSuccessMessage(null);
      setConfirmingEntryId(entry.journalEntryId);
      try {
        const response = await confirmJournalEntry(entry.journalEntryId);
        if (!response.success) throw new Error(response.errorMessage || '確定に失敗しました');
        setConfirmSuccessMessage(response.message || '仕訳を確定しました');
        onRefresh();
      } catch (error) {
        setConfirmErrorMessage(confirmJournalEntryErrorMessage(error));
      } finally {
        setConfirmingEntryId(null);
      }
    },
    [onRefresh]
  );

  const notifications: NotificationItem[] = [
    {
      type: 'success',
      message: deleteSuccessMessage ?? '',
      onDismiss: () => setDeleteSuccessMessage(null),
    },
    {
      type: 'error',
      message: deleteErrorMessage ?? '',
      onDismiss: () => setDeleteErrorMessage(null),
    },
    {
      type: 'success',
      message: submitSuccessMessage ?? '',
      onDismiss: () => setSubmitSuccessMessage(null),
    },
    {
      type: 'error',
      message: submitErrorMessage ?? '',
      onDismiss: () => setSubmitErrorMessage(null),
    },
    {
      type: 'success',
      message: rejectSuccessMessage ?? '',
      onDismiss: () => setRejectSuccessMessage(null),
    },
    {
      type: 'error',
      message: rejectErrorMessage ?? '',
      onDismiss: () => setRejectErrorMessage(null),
    },
    {
      type: 'success',
      message: approveSuccessMessage ?? '',
      onDismiss: () => setApproveSuccessMessage(null),
    },
    {
      type: 'error',
      message: approveErrorMessage ?? '',
      onDismiss: () => setApproveErrorMessage(null),
    },
    {
      type: 'success',
      message: confirmSuccessMessage ?? '',
      onDismiss: () => setConfirmSuccessMessage(null),
    },
    {
      type: 'error',
      message: confirmErrorMessage ?? '',
      onDismiss: () => setConfirmErrorMessage(null),
    },
  ];

  return {
    handleEdit,
    handleDelete,
    handleSubmitForApproval,
    handleApprove,
    handleReject,
    handleConfirm,
    deletingEntryId,
    submittingEntryId,
    approvingEntryId,
    rejectingEntryId,
    confirmingEntryId,
    notifications,
  };
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
  const { hasRole } = useAuth();
  const canEdit = hasRole('USER');
  const canApprove = hasRole('MANAGER');
  const canCreate = hasRole('USER');
  const {
    handleEdit,
    handleDelete,
    handleSubmitForApproval,
    handleApprove,
    handleReject,
    handleConfirm,
    deletingEntryId,
    submittingEntryId,
    approvingEntryId,
    rejectingEntryId,
    confirmingEntryId,
    notifications,
  } = useJournalEntryActions(onDelete);

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
        width: '320px',
        align: 'center',
        render: (_, row) => (
          <ActionButtons
            row={row}
            canEdit={canEdit}
            canCreate={canCreate}
            canApprove={canApprove}
            onEdit={handleEdit}
            onSubmit={handleSubmitForApproval}
            onApprove={handleApprove}
            onReject={handleReject}
            onConfirm={handleConfirm}
            onDelete={handleDelete}
            submittingId={submittingEntryId}
            approvingId={approvingEntryId}
            rejectingId={rejectingEntryId}
            confirmingId={confirmingEntryId}
            deletingId={deletingEntryId}
          />
        ),
      },
    ],
    [
      approvingEntryId,
      canApprove,
      canCreate,
      canEdit,
      confirmingEntryId,
      deletingEntryId,
      handleApprove,
      handleConfirm,
      handleDelete,
      handleEdit,
      handleReject,
      handleSubmitForApproval,
      rejectingEntryId,
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
      <NotificationList items={notifications} />
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
