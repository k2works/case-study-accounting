import { describe, it, expect, vi, beforeEach } from 'vitest';
import { render, screen, waitFor } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { AccountList } from './AccountList';
import { deleteAccount } from '../../api/deleteAccount';
import type { Account } from '../../api/getAccounts';

vi.mock('../../hooks/useAuth', () => ({
  useAuth: () => ({
    user: { username: 'admin', role: 'ADMIN' },
    hasRole: () => true,
  }),
}));

vi.mock('../../api/deleteAccount', () => ({
  deleteAccount: vi.fn(),
  getDeleteAccountErrorMessage: (error: unknown) =>
    error instanceof Error ? error.message : '勘定科目の削除に失敗しました',
}));

const mockDeleteAccount = vi.mocked(deleteAccount);

const setupUser = () => userEvent.setup();

const mockAccounts: Account[] = [
  { accountId: 1, accountCode: '1000', accountName: '現金', accountType: 'ASSET' },
  { accountId: 2, accountCode: '2000', accountName: '売掛金', accountType: 'ASSET' },
];

const mockFilterValues = { type: '', keyword: '' };

describe('AccountList', () => {
  beforeEach(() => {
    vi.clearAllMocks();
  });

  it('勘定科目一覧が正常に表示される', async () => {
    const onEdit = vi.fn();
    const onDelete = vi.fn();
    const onFilterChange = vi.fn();
    const onSearch = vi.fn();
    const onReset = vi.fn();

    render(
      <AccountList
        accounts={mockAccounts}
        filterValues={mockFilterValues}
        onFilterChange={onFilterChange}
        onSearch={onSearch}
        onReset={onReset}
        onEdit={onEdit}
        onDelete={onDelete}
      />
    );

    await waitFor(() => {
      expect(screen.getByText('現金')).toBeInTheDocument();
      expect(screen.getByText('売掛金')).toBeInTheDocument();
    });
    expect(screen.getByText('1000')).toBeInTheDocument();
    expect(screen.getByText('2000')).toBeInTheDocument();
    expect(screen.getAllByRole('button', { name: '編集' })).toHaveLength(2);
  });

  it('勘定科目が空の場合の表示', async () => {
    const onEdit = vi.fn();
    const onDelete = vi.fn();
    const onFilterChange = vi.fn();
    const onSearch = vi.fn();
    const onReset = vi.fn();

    render(
      <AccountList
        accounts={[]}
        filterValues={mockFilterValues}
        onFilterChange={onFilterChange}
        onSearch={onSearch}
        onReset={onReset}
        onEdit={onEdit}
        onDelete={onDelete}
      />
    );

    await waitFor(() => {
      expect(screen.getByText('勘定科目が登録されていません')).toBeInTheDocument();
    });
  });

  it('編集ボタンのクリックで編集コールバックが呼ばれる', async () => {
    const onEdit = vi.fn();
    const onDelete = vi.fn();
    const onFilterChange = vi.fn();
    const onSearch = vi.fn();
    const onReset = vi.fn();
    const user = setupUser();

    render(
      <AccountList
        accounts={mockAccounts}
        filterValues={mockFilterValues}
        onFilterChange={onFilterChange}
        onSearch={onSearch}
        onReset={onReset}
        onEdit={onEdit}
        onDelete={onDelete}
      />
    );

    await waitFor(() => expect(screen.getByText('現金')).toBeInTheDocument());

    await user.click(screen.getAllByRole('button', { name: '編集' })[0]);

    expect(onEdit).toHaveBeenCalledWith(mockAccounts[0]);
  });

  it('削除ボタンが表示される', async () => {
    const onEdit = vi.fn();
    const onDelete = vi.fn();
    const onFilterChange = vi.fn();
    const onSearch = vi.fn();
    const onReset = vi.fn();

    render(
      <AccountList
        accounts={mockAccounts}
        filterValues={mockFilterValues}
        onFilterChange={onFilterChange}
        onSearch={onSearch}
        onReset={onReset}
        onEdit={onEdit}
        onDelete={onDelete}
      />
    );

    await waitFor(() => expect(screen.getByText('現金')).toBeInTheDocument());

    expect(screen.getAllByRole('button', { name: '削除' })).toHaveLength(2);
  });

  it('削除ボタンクリックで確認ダイアログが表示される', async () => {
    const onEdit = vi.fn();
    const onDelete = vi.fn();
    const onFilterChange = vi.fn();
    const onSearch = vi.fn();
    const onReset = vi.fn();
    const user = setupUser();
    const confirmSpy = vi.spyOn(window, 'confirm').mockReturnValue(false);

    render(
      <AccountList
        accounts={mockAccounts}
        filterValues={mockFilterValues}
        onFilterChange={onFilterChange}
        onSearch={onSearch}
        onReset={onReset}
        onEdit={onEdit}
        onDelete={onDelete}
      />
    );

    await waitFor(() => expect(screen.getByText('現金')).toBeInTheDocument());

    await user.click(screen.getAllByRole('button', { name: '削除' })[0]);

    expect(confirmSpy).toHaveBeenCalledWith('勘定科目「現金」を削除しますか？');
    confirmSpy.mockRestore();
  });

  it('確認後に削除処理が実行される', async () => {
    const onEdit = vi.fn();
    const onDelete = vi.fn();
    const onFilterChange = vi.fn();
    const onSearch = vi.fn();
    const onReset = vi.fn();
    const user = setupUser();
    const confirmSpy = vi.spyOn(window, 'confirm').mockReturnValue(true);
    mockDeleteAccount.mockResolvedValue({
      success: true,
      accountId: 1,
      message: '勘定科目を削除しました',
    });

    render(
      <AccountList
        accounts={mockAccounts}
        filterValues={mockFilterValues}
        onFilterChange={onFilterChange}
        onSearch={onSearch}
        onReset={onReset}
        onEdit={onEdit}
        onDelete={onDelete}
      />
    );

    await waitFor(() => expect(screen.getByText('現金')).toBeInTheDocument());

    await user.click(screen.getAllByRole('button', { name: '削除' })[0]);

    await waitFor(() => {
      expect(mockDeleteAccount).toHaveBeenCalledWith(1);
      expect(onDelete).toHaveBeenCalled();
    });

    expect(screen.getByText('勘定科目を削除しました')).toBeInTheDocument();
    confirmSpy.mockRestore();
  });
});
