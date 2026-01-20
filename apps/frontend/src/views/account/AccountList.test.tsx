import { describe, it, expect, vi, beforeEach } from 'vitest';
import { render, screen, waitFor } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { AccountList } from './AccountList';
import { getAccounts, type Account } from '../../api/getAccounts';
import { useNavigate } from 'react-router-dom';

vi.mock('react-router-dom', () => ({
  useNavigate: vi.fn(),
}));

vi.mock('../../api/getAccounts', () => ({
  getAccounts: vi.fn(),
  getAccountsErrorMessage: (error: unknown) =>
    error instanceof Error ? error.message : '勘定科目一覧の取得に失敗しました',
}));

const mockGetAccounts = vi.mocked(getAccounts);
const mockUseNavigate = vi.mocked(useNavigate);

const setupUser = () => userEvent.setup();

const mockAccounts: Account[] = [
  { accountId: 1, accountCode: '1000', accountName: '現金', accountType: 'ASSET' },
  { accountId: 2, accountCode: '2000', accountName: '売掛金', accountType: 'ASSET' },
];

describe('AccountList', () => {
  beforeEach(() => {
    vi.clearAllMocks();
    mockUseNavigate.mockReturnValue(vi.fn());
  });

  it('勘定科目一覧が正常に表示される', async () => {
    mockGetAccounts.mockResolvedValue(mockAccounts);

    render(<AccountList />);

    await waitFor(() => {
      expect(screen.getByText('現金')).toBeInTheDocument();
      expect(screen.getByText('売掛金')).toBeInTheDocument();
    });
    expect(screen.getByText('1000')).toBeInTheDocument();
    expect(screen.getByText('2000')).toBeInTheDocument();
    expect(screen.getAllByRole('button', { name: '編集' })).toHaveLength(2);
  });

  it('ローディング中の表示', async () => {
    mockGetAccounts.mockReturnValue(new Promise<Account[]>(() => {}));

    render(<AccountList />);

    await waitFor(() => {
      expect(screen.getByText('勘定科目を読み込み中...')).toBeInTheDocument();
    });
  });

  it('エラー発生時のエラーメッセージ表示', async () => {
    mockGetAccounts.mockRejectedValue(new Error('取得に失敗しました'));

    render(<AccountList />);

    await waitFor(() => {
      expect(screen.getByRole('alert')).toBeInTheDocument();
      expect(screen.getByText('取得に失敗しました')).toBeInTheDocument();
    });
  });

  it('勘定科目が空の場合の表示', async () => {
    mockGetAccounts.mockResolvedValue([]);

    render(<AccountList />);

    await waitFor(() => {
      expect(screen.getByText('勘定科目が登録されていません')).toBeInTheDocument();
    });
  });

  it('編集ボタンのクリックで編集画面に遷移する', async () => {
    const navigate = vi.fn();
    mockUseNavigate.mockReturnValue(navigate);
    mockGetAccounts.mockResolvedValue(mockAccounts);
    const user = setupUser();

    render(<AccountList />);

    await waitFor(() => expect(screen.getByText('現金')).toBeInTheDocument());

    await user.click(screen.getAllByRole('button', { name: '編集' })[0]);

    expect(navigate).toHaveBeenCalledWith('/master/accounts/1/edit');
  });
});
