import React from 'react';
import { describe, it, expect, vi, beforeEach } from 'vitest';
import { render, screen, within } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { JournalEntryList } from './JournalEntryList';
import type { JournalEntrySummary } from '../../api/getJournalEntries';
import { useNavigate } from 'react-router-dom';
import { deleteJournalEntry } from '../../api/deleteJournalEntry';
import { submitJournalEntryForApproval } from '../../api/submitJournalEntryForApproval';

vi.mock('react-router-dom', () => ({
  useNavigate: vi.fn(),
}));

vi.mock('../../api/deleteJournalEntry', () => ({
  deleteJournalEntry: vi.fn(),
  deleteJournalEntryErrorMessage: (error: unknown) =>
    error instanceof Error ? error.message : '仕訳の削除に失敗しました',
}));

vi.mock('../../api/submitJournalEntryForApproval', () => ({
  submitJournalEntryForApproval: vi.fn(),
  submitForApprovalErrorMessage: (error: unknown) =>
    error instanceof Error ? error.message : '承認申請に失敗しました',
}));

vi.mock('../common', () => ({
  ErrorMessage: ({ message, onDismiss }: { message: string; onDismiss?: () => void }) => (
    <button data-testid="error-message" onClick={onDismiss}>
      {message}
    </button>
  ),
  SuccessNotification: ({ message, onDismiss }: { message: string; onDismiss: () => void }) => (
    <button data-testid="success-notification" onClick={onDismiss}>
      {message}
    </button>
  ),
  Table: ({
    columns,
    data,
    emptyMessage,
  }: {
    columns: {
      key: string;
      header: string;
      render?: (value: unknown, row: JournalEntrySummary) => React.ReactNode;
    }[];
    data: JournalEntrySummary[];
    keyField: string;
    emptyMessage: string;
  }) => (
    <table data-testid="table">
      <thead>
        <tr>
          {columns.map((col) => (
            <th key={col.key}>{col.header}</th>
          ))}
        </tr>
      </thead>
      <tbody>
        {data.length === 0 ? (
          <tr>
            <td colSpan={columns.length} data-testid="empty-message">
              {emptyMessage}
            </td>
          </tr>
        ) : (
          data.map((row) => (
            <tr key={row.journalEntryId} data-testid={`row-${row.journalEntryId}`}>
              {columns.map((col) => (
                <td key={col.key}>
                  {col.render
                    ? col.render(row[col.key as keyof JournalEntrySummary], row)
                    : String(row[col.key as keyof JournalEntrySummary] ?? '')}
                </td>
              ))}
            </tr>
          ))
        )}
      </tbody>
    </table>
  ),
  TableColumn: {},
  Button: ({
    children,
    onClick,
    disabled,
    ...rest
  }: {
    children: React.ReactNode;
    onClick?: () => void;
    disabled?: boolean;
    variant?: string;
    size?: string;
  }) => (
    <button onClick={onClick} disabled={disabled} {...rest}>
      {children}
    </button>
  ),
  Pagination: ({
    currentPage,
    totalPages,
    onPageChange,
  }: {
    currentPage: number;
    totalPages: number;
    totalItems: number;
    itemsPerPage: number;
    onPageChange: (page: number) => void;
    onItemsPerPageChange: (size: number) => void;
  }) => (
    <div data-testid="pagination">
      <span data-testid="current-page">{currentPage}</span>
      <span data-testid="total-pages">{totalPages}</span>
      <button data-testid="next-page-btn" onClick={() => onPageChange(currentPage + 1)}>
        次へ
      </button>
    </div>
  ),
}));

vi.mock('./JournalEntryFilter', () => ({
  JournalEntryFilter: ({
    onSearch,
    onReset,
  }: {
    values: unknown;
    onChange: (values: unknown) => void;
    onSearch: () => void;
    onReset: () => void;
  }) => (
    <div data-testid="journal-entry-filter">
      <button data-testid="filter-search" onClick={onSearch}>
        検索
      </button>
      <button data-testid="filter-reset" onClick={onReset}>
        リセット
      </button>
    </div>
  ),
}));

vi.mock('./JournalEntryList.css', () => ({}));

const mockUseNavigate = vi.mocked(useNavigate);
const mockDeleteJournalEntry = vi.mocked(deleteJournalEntry);
const mockSubmitJournalEntryForApproval = vi.mocked(submitJournalEntryForApproval);

const mockEntries: JournalEntrySummary[] = [
  {
    journalEntryId: 1,
    journalDate: '2024-01-01',
    description: '売上計上',
    totalDebitAmount: 1000,
    totalCreditAmount: 1000,
    status: 'DRAFT',
    version: 1,
  },
  {
    journalEntryId: 2,
    journalDate: '2024-01-15',
    description: '仕入計上',
    totalDebitAmount: 2000,
    totalCreditAmount: 2000,
    status: 'APPROVED',
    version: 1,
  },
  {
    journalEntryId: 3,
    journalDate: '2024-01-20',
    description: '経費精算',
    totalDebitAmount: 3000,
    totalCreditAmount: 3000,
    status: 'PENDING',
    version: 1,
  },
];

const defaultFilterValues = {
  status: '',
  dateFrom: '',
  dateTo: '',
  accountId: '',
  amountFrom: '',
  amountTo: '',
  description: '',
};

describe('JournalEntryList', () => {
  const defaultProps = {
    entries: mockEntries,
    filterValues: defaultFilterValues,
    onFilterChange: vi.fn(),
    onSearch: vi.fn(),
    onReset: vi.fn(),
    onDelete: vi.fn(),
    currentPage: 1,
    totalPages: 1,
    totalItems: 3,
    itemsPerPage: 20,
    onPageChange: vi.fn(),
    onItemsPerPageChange: vi.fn(),
  };

  beforeEach(() => {
    vi.clearAllMocks();
    mockUseNavigate.mockReturnValue(vi.fn());
    mockDeleteJournalEntry.mockResolvedValue({ success: true, message: '仕訳を削除しました' });
    mockSubmitJournalEntryForApproval.mockResolvedValue({
      success: true,
      journalEntryId: 1,
      status: 'PENDING',
      message: '仕訳を承認申請しました',
    });
  });

  it('仕訳一覧を表示する', () => {
    render(<JournalEntryList {...defaultProps} />);

    expect(screen.getByTestId('journal-entry-list')).toBeInTheDocument();
    expect(screen.getByTestId('table')).toBeInTheDocument();
    expect(screen.getByTestId('row-1')).toBeInTheDocument();
    expect(screen.getByTestId('row-2')).toBeInTheDocument();
  });

  it('空の場合にメッセージを表示する', () => {
    render(<JournalEntryList {...defaultProps} entries={[]} />);

    expect(screen.getByTestId('empty-message')).toHaveTextContent('仕訳が登録されていません');
  });

  it('フィルタコンポーネントが表示される', () => {
    render(<JournalEntryList {...defaultProps} />);

    expect(screen.getByTestId('journal-entry-filter')).toBeInTheDocument();
  });

  it('ページネーションが表示される', () => {
    render(<JournalEntryList {...defaultProps} />);

    expect(screen.getByTestId('pagination')).toBeInTheDocument();
    expect(screen.getByTestId('current-page')).toHaveTextContent('1');
  });

  it('編集ボタンクリックで編集ページに遷移する', async () => {
    const navigate = vi.fn();
    mockUseNavigate.mockReturnValue(navigate);

    render(<JournalEntryList {...defaultProps} />);

    const user = userEvent.setup();
    const editButtons = screen.getAllByText('編集');
    await user.click(editButtons[0]);

    expect(navigate).toHaveBeenCalledWith('/journal/entries/1/edit');
  });

  it('DRAFT でない仕訳の削除ボタンは無効化される', () => {
    render(<JournalEntryList {...defaultProps} />);

    const deleteButtons = screen.getAllByText('削除');
    // 2番目のエントリ（APPROVED）の削除ボタンは disabled
    expect(deleteButtons[1]).toBeDisabled();
  });

  it('削除ボタンをクリックして確認後に削除を実行する', async () => {
    vi.spyOn(window, 'confirm').mockReturnValue(true);

    render(<JournalEntryList {...defaultProps} />);

    const user = userEvent.setup();
    const deleteButtons = screen.getAllByText('削除');
    await user.click(deleteButtons[0]); // DRAFT のエントリ

    expect(window.confirm).toHaveBeenCalled();
    expect(mockDeleteJournalEntry).toHaveBeenCalledWith(1);
  });

  it('削除確認をキャンセルすると削除されない', async () => {
    vi.spyOn(window, 'confirm').mockReturnValue(false);

    render(<JournalEntryList {...defaultProps} />);

    const user = userEvent.setup();
    const deleteButtons = screen.getAllByText('削除');
    await user.click(deleteButtons[0]);

    expect(window.confirm).toHaveBeenCalled();
    expect(mockDeleteJournalEntry).not.toHaveBeenCalled();
  });

  it('DRAFT の仕訳に承認申請ボタンが表示される', () => {
    render(<JournalEntryList {...defaultProps} />);

    const submitButtons = screen.getAllByText('承認申請');
    expect(submitButtons).toHaveLength(1);
  });

  it('DRAFT 以外の仕訳に承認申請ボタンが表示されない', () => {
    render(<JournalEntryList {...defaultProps} />);

    const approvedRow = screen.getByTestId('row-2');
    const pendingRow = screen.getByTestId('row-3');

    expect(within(approvedRow).queryByText('承認申請')).not.toBeInTheDocument();
    expect(within(pendingRow).queryByText('承認申請')).not.toBeInTheDocument();
  });

  it('承認申請ボタンで確認ダイアログが表示される', async () => {
    vi.spyOn(window, 'confirm').mockReturnValue(true);

    render(<JournalEntryList {...defaultProps} />);

    const user = userEvent.setup();
    await user.click(screen.getByText('承認申請'));

    expect(window.confirm).toHaveBeenCalled();
    expect(mockSubmitJournalEntryForApproval).toHaveBeenCalledWith(1);
  });

  it('承認申請成功時に成功メッセージを表示する', async () => {
    vi.spyOn(window, 'confirm').mockReturnValue(true);

    render(<JournalEntryList {...defaultProps} />);

    const user = userEvent.setup();
    await user.click(screen.getByText('承認申請'));

    expect(await screen.findByTestId('success-notification')).toHaveTextContent(
      '仕訳を承認申請しました'
    );
  });
});
