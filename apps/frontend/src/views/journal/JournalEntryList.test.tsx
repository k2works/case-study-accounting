import React from 'react';
import { describe, it, expect, vi, beforeEach } from 'vitest';
import { render, screen, within } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { JournalEntryList } from './JournalEntryList';
import type { JournalEntrySummary } from '../../api/getJournalEntries';
import { useNavigate } from 'react-router-dom';
import { deleteJournalEntry } from '../../api/deleteJournalEntry';
import { submitJournalEntryForApproval } from '../../api/submitJournalEntryForApproval';
import { approveJournalEntry } from '../../api/approveJournalEntry';
import { rejectJournalEntry } from '../../api/rejectJournalEntry';
import { confirmJournalEntry } from '../../api/confirmJournalEntry';

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

vi.mock('../../api/approveJournalEntry', () => ({
  approveJournalEntry: vi.fn(),
  approveJournalEntryErrorMessage: (error: unknown) =>
    error instanceof Error ? error.message : '承認に失敗しました',
}));

vi.mock('../../api/rejectJournalEntry', () => ({
  rejectJournalEntry: vi.fn(),
  rejectJournalEntryErrorMessage: (error: unknown) =>
    error instanceof Error ? error.message : '差し戻しに失敗しました',
}));

vi.mock('../../api/confirmJournalEntry', () => ({
  confirmJournalEntry: vi.fn(),
  confirmJournalEntryErrorMessage: (error: unknown) =>
    error instanceof Error ? error.message : '確定に失敗しました',
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
const mockApproveJournalEntry = vi.mocked(approveJournalEntry);
const mockRejectJournalEntry = vi.mocked(rejectJournalEntry);
const mockConfirmJournalEntry = vi.mocked(confirmJournalEntry);

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
  {
    journalEntryId: 4,
    journalDate: '2024-01-25',
    description: '確定済み仕訳',
    totalDebitAmount: 4000,
    totalCreditAmount: 4000,
    status: 'CONFIRMED',
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
    totalItems: 4,
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
    mockApproveJournalEntry.mockResolvedValue({
      success: true,
      journalEntryId: 3,
      status: 'APPROVED',
      message: '仕訳を承認しました',
    });
    mockRejectJournalEntry.mockResolvedValue({
      success: true,
      journalEntryId: 3,
      status: 'DRAFT',
      rejectedBy: 'manager',
      rejectionReason: '金額に誤りがあります',
      message: '仕訳を差し戻しました',
    });
    mockConfirmJournalEntry.mockResolvedValue({
      success: true,
      journalEntryId: 2,
      status: 'CONFIRMED',
      message: '仕訳を確定しました',
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

  it('PENDING の仕訳に承認ボタンが表示される', () => {
    render(<JournalEntryList {...defaultProps} />);

    const approveButtons = screen.getAllByText('承認');
    expect(approveButtons).toHaveLength(1);
  });

  it('PENDING 以外の仕訳に承認ボタンが表示されない', () => {
    render(<JournalEntryList {...defaultProps} />);

    const draftRow = screen.getByTestId('row-1');
    const approvedRow = screen.getByTestId('row-2');

    expect(within(draftRow).queryByText('承認')).not.toBeInTheDocument();
    expect(within(approvedRow).queryByText('承認')).not.toBeInTheDocument();
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

  it('承認ボタンで確認ダイアログが表示される', async () => {
    vi.spyOn(window, 'confirm').mockReturnValue(true);

    render(<JournalEntryList {...defaultProps} />);

    const user = userEvent.setup();
    await user.click(screen.getByText('承認'));

    expect(window.confirm).toHaveBeenCalled();
    expect(mockApproveJournalEntry).toHaveBeenCalledWith(3);
  });

  it('承認成功時に成功メッセージを表示する', async () => {
    vi.spyOn(window, 'confirm').mockReturnValue(true);

    render(<JournalEntryList {...defaultProps} />);

    const user = userEvent.setup();
    await user.click(screen.getByText('承認'));

    expect(await screen.findByTestId('success-notification')).toHaveTextContent(
      '仕訳を承認しました'
    );
  });

  it('PENDING の仕訳に差し戻しボタンが表示される', () => {
    render(<JournalEntryList {...defaultProps} />);

    const pendingRow = screen.getByTestId('row-3');
    expect(within(pendingRow).getByText('差し戻し')).toBeInTheDocument();
  });

  it('PENDING 以外の仕訳に差し戻しボタンが表示されない', () => {
    render(<JournalEntryList {...defaultProps} />);

    const draftRow = screen.getByTestId('row-1');
    const approvedRow = screen.getByTestId('row-2');

    expect(within(draftRow).queryByText('差し戻し')).not.toBeInTheDocument();
    expect(within(approvedRow).queryByText('差し戻し')).not.toBeInTheDocument();
  });

  it('差し戻しボタンで理由入力ダイアログが表示される', async () => {
    vi.spyOn(window, 'prompt').mockReturnValue('金額に誤りがあります');

    render(<JournalEntryList {...defaultProps} />);

    const user = userEvent.setup();
    await user.click(screen.getByText('差し戻し'));

    expect(window.prompt).toHaveBeenCalled();
    expect(mockRejectJournalEntry).toHaveBeenCalledWith(3, '金額に誤りがあります');
  });

  it('差し戻し成功時に成功メッセージを表示する', async () => {
    vi.spyOn(window, 'prompt').mockReturnValue('金額に誤りがあります');

    render(<JournalEntryList {...defaultProps} />);

    const user = userEvent.setup();
    await user.click(screen.getByText('差し戻し'));

    expect(await screen.findByTestId('success-notification')).toHaveTextContent(
      '仕訳を差し戻しました'
    );
  });

  it('差し戻しダイアログでキャンセルすると差し戻しされない', async () => {
    vi.spyOn(window, 'prompt').mockReturnValue(null);

    render(<JournalEntryList {...defaultProps} />);

    const user = userEvent.setup();
    await user.click(screen.getByText('差し戻し'));

    expect(window.prompt).toHaveBeenCalled();
    expect(mockRejectJournalEntry).not.toHaveBeenCalled();
  });

  it('差し戻し理由が空の場合はエラーメッセージを表示する', async () => {
    vi.spyOn(window, 'prompt').mockReturnValue('');

    render(<JournalEntryList {...defaultProps} />);

    const user = userEvent.setup();
    await user.click(screen.getByText('差し戻し'));

    expect(mockRejectJournalEntry).not.toHaveBeenCalled();
    expect(await screen.findByTestId('error-message')).toHaveTextContent('差し戻し理由は必須です');
  });

  it('差し戻し失敗時にエラーメッセージを表示する', async () => {
    vi.spyOn(window, 'prompt').mockReturnValue('理由あり');
    mockRejectJournalEntry.mockRejectedValue(new Error('差し戻しに失敗しました'));

    render(<JournalEntryList {...defaultProps} />);

    const user = userEvent.setup();
    await user.click(screen.getByText('差し戻し'));

    expect(await screen.findByTestId('error-message')).toHaveTextContent('差し戻しに失敗しました');
  });

  it('差し戻し成功メッセージを閉じることができる', async () => {
    vi.spyOn(window, 'prompt').mockReturnValue('金額に誤りがあります');

    render(<JournalEntryList {...defaultProps} />);

    const user = userEvent.setup();
    await user.click(screen.getByText('差し戻し'));

    const notification = await screen.findByTestId('success-notification');
    expect(notification).toHaveTextContent('仕訳を差し戻しました');
    await user.click(notification);
    expect(screen.queryByText('仕訳を差し戻しました')).not.toBeInTheDocument();
  });

  it('差し戻しエラーメッセージを閉じることができる', async () => {
    vi.spyOn(window, 'prompt').mockReturnValue('');

    render(<JournalEntryList {...defaultProps} />);

    const user = userEvent.setup();
    await user.click(screen.getByText('差し戻し'));

    const errorMsg = await screen.findByTestId('error-message');
    expect(errorMsg).toHaveTextContent('差し戻し理由は必須です');
    await user.click(errorMsg);
    expect(screen.queryByText('差し戻し理由は必須です')).not.toBeInTheDocument();
  });

  it('差し戻し API が success:false を返した場合エラーメッセージを表示する', async () => {
    vi.spyOn(window, 'prompt').mockReturnValue('理由あり');
    mockRejectJournalEntry.mockResolvedValue({
      success: false,
      errorMessage: '差し戻しに失敗しました',
    });

    render(<JournalEntryList {...defaultProps} />);

    const user = userEvent.setup();
    await user.click(screen.getByText('差し戻し'));

    expect(await screen.findByTestId('error-message')).toHaveTextContent('差し戻しに失敗しました');
  });

  // --- 確定（handleConfirm）テスト ---
  it('APPROVED の仕訳に確定ボタンが表示される', () => {
    render(<JournalEntryList {...defaultProps} />);

    const approvedRow = screen.getByTestId('row-2');
    expect(within(approvedRow).getByText('確定')).toBeInTheDocument();
  });

  it('APPROVED 以外の仕訳に確定ボタンが表示されない', () => {
    render(<JournalEntryList {...defaultProps} />);

    const draftRow = screen.getByTestId('row-1');
    const pendingRow = screen.getByTestId('row-3');

    expect(within(draftRow).queryByText('確定')).not.toBeInTheDocument();
    expect(within(pendingRow).queryByText('確定')).not.toBeInTheDocument();
  });

  it('確定ボタンで確認ダイアログが表示される', async () => {
    vi.spyOn(window, 'confirm').mockReturnValue(true);

    render(<JournalEntryList {...defaultProps} />);

    const user = userEvent.setup();
    const approvedRow = screen.getByTestId('row-2');
    await user.click(within(approvedRow).getByText('確定'));

    expect(window.confirm).toHaveBeenCalled();
    expect(mockConfirmJournalEntry).toHaveBeenCalledWith(2);
  });

  it('確定確認をキャンセルすると確定されない', async () => {
    vi.spyOn(window, 'confirm').mockReturnValue(false);

    render(<JournalEntryList {...defaultProps} />);

    const user = userEvent.setup();
    const approvedRow = screen.getByTestId('row-2');
    await user.click(within(approvedRow).getByText('確定'));

    expect(window.confirm).toHaveBeenCalled();
    expect(mockConfirmJournalEntry).not.toHaveBeenCalled();
  });

  it('確定成功時に成功メッセージを表示する', async () => {
    vi.spyOn(window, 'confirm').mockReturnValue(true);

    render(<JournalEntryList {...defaultProps} />);

    const user = userEvent.setup();
    const approvedRow = screen.getByTestId('row-2');
    await user.click(within(approvedRow).getByText('確定'));

    expect(await screen.findByTestId('success-notification')).toHaveTextContent(
      '仕訳を確定しました'
    );
  });

  it('確定失敗時にエラーメッセージを表示する', async () => {
    vi.spyOn(window, 'confirm').mockReturnValue(true);
    mockConfirmJournalEntry.mockRejectedValue(new Error('確定に失敗しました'));

    render(<JournalEntryList {...defaultProps} />);

    const user = userEvent.setup();
    const approvedRow = screen.getByTestId('row-2');
    await user.click(within(approvedRow).getByText('確定'));

    expect(await screen.findByTestId('error-message')).toHaveTextContent('確定に失敗しました');
  });

  it('確定 API が success:false を返した場合エラーメッセージを表示する', async () => {
    vi.spyOn(window, 'confirm').mockReturnValue(true);
    mockConfirmJournalEntry.mockResolvedValue({
      success: false,
      errorMessage: '確定に失敗しました',
    });

    render(<JournalEntryList {...defaultProps} />);

    const user = userEvent.setup();
    const approvedRow = screen.getByTestId('row-2');
    await user.click(within(approvedRow).getByText('確定'));

    expect(await screen.findByTestId('error-message')).toHaveTextContent('確定に失敗しました');
  });

  it('確定成功メッセージを閉じることができる', async () => {
    vi.spyOn(window, 'confirm').mockReturnValue(true);

    render(<JournalEntryList {...defaultProps} />);

    const user = userEvent.setup();
    const approvedRow = screen.getByTestId('row-2');
    await user.click(within(approvedRow).getByText('確定'));

    const notification = await screen.findByTestId('success-notification');
    expect(notification).toHaveTextContent('仕訳を確定しました');
    await user.click(notification);
    expect(screen.queryByText('仕訳を確定しました')).not.toBeInTheDocument();
  });

  it('確定エラーメッセージを閉じることができる', async () => {
    vi.spyOn(window, 'confirm').mockReturnValue(true);
    mockConfirmJournalEntry.mockRejectedValue(new Error('確定に失敗'));

    render(<JournalEntryList {...defaultProps} />);

    const user = userEvent.setup();
    const approvedRow = screen.getByTestId('row-2');
    await user.click(within(approvedRow).getByText('確定'));

    const errorMsg = await screen.findByTestId('error-message');
    expect(errorMsg).toHaveTextContent('確定に失敗');
    await user.click(errorMsg);
    expect(screen.queryByText('確定に失敗')).not.toBeInTheDocument();
  });

  // --- 削除のエラーパス ---
  it('削除失敗時にエラーメッセージを表示する', async () => {
    vi.spyOn(window, 'confirm').mockReturnValue(true);
    mockDeleteJournalEntry.mockRejectedValue(new Error('削除に失敗しました'));

    render(<JournalEntryList {...defaultProps} />);

    const user = userEvent.setup();
    const deleteButtons = screen.getAllByText('削除');
    await user.click(deleteButtons[0]);

    expect(await screen.findByTestId('error-message')).toHaveTextContent('削除に失敗しました');
  });

  it('削除 API が success:false を返した場合エラーメッセージを表示する', async () => {
    vi.spyOn(window, 'confirm').mockReturnValue(true);
    mockDeleteJournalEntry.mockResolvedValue({
      success: false,
      errorMessage: '削除権限がありません',
    });

    render(<JournalEntryList {...defaultProps} />);

    const user = userEvent.setup();
    const deleteButtons = screen.getAllByText('削除');
    await user.click(deleteButtons[0]);

    expect(await screen.findByTestId('error-message')).toHaveTextContent('削除権限がありません');
  });

  it('削除成功時に成功メッセージを表示する', async () => {
    vi.spyOn(window, 'confirm').mockReturnValue(true);

    render(<JournalEntryList {...defaultProps} />);

    const user = userEvent.setup();
    const deleteButtons = screen.getAllByText('削除');
    await user.click(deleteButtons[0]);

    expect(await screen.findByTestId('success-notification')).toHaveTextContent(
      '仕訳を削除しました'
    );
  });

  it('削除成功メッセージを閉じることができる', async () => {
    vi.spyOn(window, 'confirm').mockReturnValue(true);

    render(<JournalEntryList {...defaultProps} />);

    const user = userEvent.setup();
    const deleteButtons = screen.getAllByText('削除');
    await user.click(deleteButtons[0]);

    const notification = await screen.findByTestId('success-notification');
    await user.click(notification);
    expect(screen.queryByText('仕訳を削除しました')).not.toBeInTheDocument();
  });

  it('削除エラーメッセージを閉じることができる', async () => {
    vi.spyOn(window, 'confirm').mockReturnValue(true);
    mockDeleteJournalEntry.mockRejectedValue(new Error('削除エラー'));

    render(<JournalEntryList {...defaultProps} />);

    const user = userEvent.setup();
    const deleteButtons = screen.getAllByText('削除');
    await user.click(deleteButtons[0]);

    const errorMsg = await screen.findByTestId('error-message');
    expect(errorMsg).toHaveTextContent('削除エラー');
    await user.click(errorMsg);
    expect(screen.queryByText('削除エラー')).not.toBeInTheDocument();
  });

  // --- 承認申請のエラーパス ---
  it('承認申請失敗時にエラーメッセージを表示する', async () => {
    vi.spyOn(window, 'confirm').mockReturnValue(true);
    mockSubmitJournalEntryForApproval.mockRejectedValue(new Error('承認申請に失敗しました'));

    render(<JournalEntryList {...defaultProps} />);

    const user = userEvent.setup();
    await user.click(screen.getByText('承認申請'));

    expect(await screen.findByTestId('error-message')).toHaveTextContent('承認申請に失敗しました');
  });

  it('承認申請 API が success:false を返した場合エラーメッセージを表示する', async () => {
    vi.spyOn(window, 'confirm').mockReturnValue(true);
    mockSubmitJournalEntryForApproval.mockResolvedValue({
      success: false,
      journalEntryId: 1,
      status: 'DRAFT',
      errorMessage: '承認申請権限がありません',
    });

    render(<JournalEntryList {...defaultProps} />);

    const user = userEvent.setup();
    await user.click(screen.getByText('承認申請'));

    expect(await screen.findByTestId('error-message')).toHaveTextContent(
      '承認申請権限がありません'
    );
  });

  it('承認申請確認をキャンセルすると承認申請されない', async () => {
    vi.spyOn(window, 'confirm').mockReturnValue(false);

    render(<JournalEntryList {...defaultProps} />);

    const user = userEvent.setup();
    await user.click(screen.getByText('承認申請'));

    expect(window.confirm).toHaveBeenCalled();
    expect(mockSubmitJournalEntryForApproval).not.toHaveBeenCalled();
  });

  // --- 承認のエラーパス ---
  it('承認失敗時にエラーメッセージを表示する', async () => {
    vi.spyOn(window, 'confirm').mockReturnValue(true);
    mockApproveJournalEntry.mockRejectedValue(new Error('承認に失敗しました'));

    render(<JournalEntryList {...defaultProps} />);

    const user = userEvent.setup();
    await user.click(screen.getByText('承認'));

    expect(await screen.findByTestId('error-message')).toHaveTextContent('承認に失敗しました');
  });

  it('承認 API が success:false を返した場合エラーメッセージを表示する', async () => {
    vi.spyOn(window, 'confirm').mockReturnValue(true);
    mockApproveJournalEntry.mockResolvedValue({
      success: false,
      journalEntryId: 3,
      status: 'PENDING',
      errorMessage: '承認権限がありません',
    });

    render(<JournalEntryList {...defaultProps} />);

    const user = userEvent.setup();
    await user.click(screen.getByText('承認'));

    expect(await screen.findByTestId('error-message')).toHaveTextContent('承認権限がありません');
  });

  it('承認確認をキャンセルすると承認されない', async () => {
    vi.spyOn(window, 'confirm').mockReturnValue(false);

    render(<JournalEntryList {...defaultProps} />);

    const user = userEvent.setup();
    await user.click(screen.getByText('承認'));

    expect(window.confirm).toHaveBeenCalled();
    expect(mockApproveJournalEntry).not.toHaveBeenCalled();
  });

  // --- 通知の dismiss ---
  it('承認申請成功メッセージを閉じることができる', async () => {
    vi.spyOn(window, 'confirm').mockReturnValue(true);

    render(<JournalEntryList {...defaultProps} />);

    const user = userEvent.setup();
    await user.click(screen.getByText('承認申請'));

    const notification = await screen.findByTestId('success-notification');
    expect(notification).toHaveTextContent('仕訳を承認申請しました');
    await user.click(notification);
    expect(screen.queryByText('仕訳を承認申請しました')).not.toBeInTheDocument();
  });

  it('承認成功メッセージを閉じることができる', async () => {
    vi.spyOn(window, 'confirm').mockReturnValue(true);

    render(<JournalEntryList {...defaultProps} />);

    const user = userEvent.setup();
    await user.click(screen.getByText('承認'));

    const notification = await screen.findByTestId('success-notification');
    expect(notification).toHaveTextContent('仕訳を承認しました');
    await user.click(notification);
    expect(screen.queryByText('仕訳を承認しました')).not.toBeInTheDocument();
  });

  it('承認申請エラーメッセージを閉じることができる', async () => {
    vi.spyOn(window, 'confirm').mockReturnValue(true);
    mockSubmitJournalEntryForApproval.mockRejectedValue(new Error('申請エラー'));

    render(<JournalEntryList {...defaultProps} />);

    const user = userEvent.setup();
    await user.click(screen.getByText('承認申請'));

    const errorMsg = await screen.findByTestId('error-message');
    await user.click(errorMsg);
    expect(screen.queryByText('申請エラー')).not.toBeInTheDocument();
  });

  it('承認エラーメッセージを閉じることができる', async () => {
    vi.spyOn(window, 'confirm').mockReturnValue(true);
    mockApproveJournalEntry.mockRejectedValue(new Error('承認エラー'));

    render(<JournalEntryList {...defaultProps} />);

    const user = userEvent.setup();
    await user.click(screen.getByText('承認'));

    const errorMsg = await screen.findByTestId('error-message');
    await user.click(errorMsg);
    expect(screen.queryByText('承認エラー')).not.toBeInTheDocument();
  });

  // --- CONFIRMED ステータス表示 ---
  it('CONFIRMED のステータスラベルが「確定」と表示される', () => {
    render(<JournalEntryList {...defaultProps} />);

    const confirmedRow = screen.getByTestId('row-4');
    expect(within(confirmedRow).getByText('確定')).toBeInTheDocument();
  });
});
