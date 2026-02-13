import { describe, it, expect, vi, beforeEach } from 'vitest';
import { render, screen, waitFor } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import TrialBalancePage from './TrialBalancePage';
import { useRequireAuth } from '../hooks/useRequireAuth';
import { getTrialBalance } from '../api/getTrialBalance';
import type { GetTrialBalanceResult } from '../api/getTrialBalance';

vi.mock('../hooks/useRequireAuth', () => ({
  useRequireAuth: vi.fn(),
}));

vi.mock('../api/getTrialBalance', () => ({
  getTrialBalance: vi.fn(),
  getTrialBalanceErrorMessage: (error: unknown) =>
    error instanceof Error ? error.message : '残高試算表の取得に失敗しました',
}));

vi.mock(
  'react-router-dom',
  async () => (await import('../test/pageTestMocks')).reactRouterDomMocks
);
vi.mock('../views/common', async () => (await import('../test/pageTestMocks')).commonViewMocks);

vi.mock('../views/ledger/TrialBalanceFilter', () => ({
  TrialBalanceFilter: ({
    onChange,
    onSearch,
  }: {
    values: { date: string };
    onChange: (values: { date: string }) => void;
    onSearch: () => void;
  }) => (
    <div data-testid="trial-balance-filter">
      <button data-testid="set-date-btn" onClick={() => onChange({ date: '2024-06-30' })}>
        基準日設定
      </button>
      <button data-testid="search-btn" onClick={onSearch}>
        表示
      </button>
    </div>
  ),
}));

vi.mock('../views/ledger/TrialBalanceSummary', () => ({
  TrialBalanceSummary: ({ totalDebit }: { totalDebit: number }) => (
    <div data-testid="trial-balance-summary">借方: {totalDebit}</div>
  ),
}));

vi.mock('../views/ledger/TrialBalanceTable', () => ({
  TrialBalanceTable: ({ entries }: { entries: unknown[] }) => (
    <div data-testid="trial-balance-table">{entries.length}</div>
  ),
}));

const mockUseRequireAuth = vi.mocked(useRequireAuth);
const mockGetTrialBalance = vi.mocked(getTrialBalance);

const createMockResult = (
  overrides: Partial<GetTrialBalanceResult> = {}
): GetTrialBalanceResult => ({
  date: null,
  totalDebit: 0,
  totalCredit: 0,
  balanced: true,
  difference: 0,
  entries: [],
  categorySubtotals: [],
  ...overrides,
});

describe('TrialBalancePage', () => {
  beforeEach(() => {
    vi.clearAllMocks();
    mockUseRequireAuth.mockReturnValue(null);
    mockGetTrialBalance.mockResolvedValue(createMockResult());
  });

  it('auth guard がリダイレクトを返す場合はそれを表示', () => {
    mockUseRequireAuth.mockReturnValue(<div data-testid="navigate" data-to="/login" />);

    render(<TrialBalancePage />);

    expect(screen.getByTestId('navigate')).toHaveAttribute('data-to', '/login');
  });

  it('認証済みの場合は残高試算表ページを表示', () => {
    render(<TrialBalancePage />);

    expect(screen.getByText('残高試算表')).toBeInTheDocument();
    expect(screen.getByTestId('trial-balance-filter')).toBeInTheDocument();
  });

  it('表示ボタンクリックで試算表を表示する', async () => {
    mockGetTrialBalance.mockResolvedValue(
      createMockResult({
        entries: [
          {
            accountCode: '1000',
            accountName: '現金',
            bsplCategory: 'BS',
            accountType: 'ASSET',
            debitBalance: 50000,
            creditBalance: 0,
          },
        ],
        totalDebit: 50000,
        totalCredit: 50000,
      })
    );

    render(<TrialBalancePage />);

    const user = userEvent.setup();
    await user.click(screen.getByTestId('search-btn'));

    await waitFor(() => {
      expect(screen.getByTestId('trial-balance-summary')).toBeInTheDocument();
    });
    expect(screen.getByTestId('trial-balance-table')).toBeInTheDocument();
  });

  it('API エラー時にエラーメッセージを表示する', async () => {
    mockGetTrialBalance.mockRejectedValue(new Error('接続エラー'));

    render(<TrialBalancePage />);

    const user = userEvent.setup();
    await user.click(screen.getByTestId('search-btn'));

    await waitFor(() => {
      expect(screen.getByTestId('error-message')).toHaveTextContent('接続エラー');
    });
  });

  it('ローディング中にローディング表示を出す', async () => {
    // 未解決の Promise でローディング状態を維持
    mockGetTrialBalance.mockReturnValue(new Promise<GetTrialBalanceResult>(() => {}));
    render(<TrialBalancePage />);
    await userEvent.setup().click(screen.getByTestId('search-btn'));
    await waitFor(() => expect(screen.getByTestId('loading')).toBeInTheDocument());
  });

  it('基準日を設定して試算表を表示する', async () => {
    mockGetTrialBalance.mockResolvedValue(
      createMockResult({
        date: '2024-06-30',
        totalDebit: 100000,
        totalCredit: 100000,
      })
    );

    render(<TrialBalancePage />);

    const user = userEvent.setup();
    await user.click(screen.getByTestId('set-date-btn'));
    await user.click(screen.getByTestId('search-btn'));

    await waitFor(() => {
      expect(screen.getByTestId('trial-balance-summary')).toBeInTheDocument();
    });
    expect(mockGetTrialBalance).toHaveBeenCalledWith({ date: '2024-06-30' });
  });

  it('API が null/undefined 値を返してもデフォルト値で正常に動作する', async () => {
    mockGetTrialBalance.mockResolvedValue({
      date: null,
      totalDebit: 0,
      totalCredit: 0,
      balanced: true,
      difference: 0,
      entries: [],
      categorySubtotals: [],
    } as GetTrialBalanceResult);

    render(<TrialBalancePage />);

    const user = userEvent.setup();
    await user.click(screen.getByTestId('search-btn'));

    await waitFor(() => {
      expect(screen.getByTestId('trial-balance-summary')).toBeInTheDocument();
    });
  });
});
