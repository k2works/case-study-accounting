import { describe, it, expect, vi, beforeEach } from 'vitest';
import { render, screen, waitFor } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import DailyBalancePage from './DailyBalancePage';
import { useAuth } from '../hooks/useAuth';
import { createMockAuthContext } from '../test/testUtils';
import { getDailyBalance } from '../api/getDailyBalance';
import type { GetDailyBalanceResult } from '../api/getDailyBalance';

vi.mock('../hooks/useAuth', () => ({
  useAuth: vi.fn(),
}));

vi.mock('../api/getDailyBalance', () => ({
  getDailyBalance: vi.fn(),
  getDailyBalanceErrorMessage: (error: unknown) =>
    error instanceof Error ? error.message : '日次残高の取得に失敗しました',
}));

vi.mock(
  'react-router-dom',
  async () => (await import('../test/pageTestMocks')).reactRouterDomMocks
);
vi.mock('../views/common', async () => (await import('../test/pageTestMocks')).commonViewMocks);

vi.mock('../views/ledger/DailyBalanceFilter', () => ({
  DailyBalanceFilter: ({
    values,
    onChange,
    onSearch,
  }: {
    values: { accountId: string; dateFrom: string; dateTo: string };
    onChange: (values: { accountId: string; dateFrom: string; dateTo: string }) => void;
    onSearch: () => void;
  }) => (
    <div data-testid="daily-balance-filter">
      <button
        data-testid="account-select-btn"
        onClick={() => onChange({ ...values, accountId: '1' })}
      >
        勘定科目選択
      </button>
      <button
        data-testid="date-range-btn"
        onClick={() => onChange({ ...values, dateFrom: '2024-01-01', dateTo: '2024-01-31' })}
      >
        期間指定
      </button>
      <button data-testid="search-btn" onClick={onSearch}>
        照会
      </button>
    </div>
  ),
}));

vi.mock('../views/ledger/DailyBalanceSummary', () => ({
  DailyBalanceSummary: ({
    accountCode,
    accountName,
  }: {
    accountCode: string;
    accountName: string;
  }) => (
    <div data-testid="daily-balance-summary">
      {accountCode} {accountName}
    </div>
  ),
}));

vi.mock('../views/ledger/DailyBalanceTable', () => ({
  DailyBalanceTable: ({ entries }: { entries: unknown[] }) => (
    <div data-testid="daily-balance-table">{entries.length}</div>
  ),
}));

vi.mock('../views/ledger/DailyBalanceChart', () => ({
  DailyBalanceChart: ({ entries }: { entries: unknown[] }) => (
    <div data-testid="daily-balance-chart">{entries.length}</div>
  ),
}));

const mockUseAuth = vi.mocked(useAuth);
const mockGetDailyBalance = vi.mocked(getDailyBalance);

const createMockResult = (
  overrides: Partial<GetDailyBalanceResult> = {}
): GetDailyBalanceResult => ({
  accountId: 1,
  accountCode: '1000',
  accountName: '現金預金',
  openingBalance: 0,
  debitTotal: 0,
  creditTotal: 0,
  closingBalance: 0,
  entries: [],
  ...overrides,
});

describe('DailyBalancePage', () => {
  beforeEach(() => {
    vi.clearAllMocks();
    mockGetDailyBalance.mockResolvedValue(createMockResult());
  });

  it('認証されていない場合はログインページにリダイレクト', () => {
    mockUseAuth.mockReturnValue(
      createMockAuthContext({ isAuthenticated: false, isLoading: false })
    );

    render(<DailyBalancePage />);

    const navigate = screen.getByTestId('navigate');
    expect(navigate).toHaveAttribute('data-to', '/login');
  });

  it('ローディング中の表示', () => {
    mockUseAuth.mockReturnValue(createMockAuthContext({ isAuthenticated: false, isLoading: true }));

    render(<DailyBalancePage />);

    expect(screen.getByTestId('loading')).toHaveTextContent('認証情報を確認中...');
  });

  it('権限がない場合はホームにリダイレクト', () => {
    mockUseAuth.mockReturnValue(
      createMockAuthContext({
        isAuthenticated: true,
        isLoading: false,
        hasRole: vi.fn(() => false),
      })
    );

    render(<DailyBalancePage />);

    const navigate = screen.getByTestId('navigate');
    expect(navigate).toHaveAttribute('data-to', '/');
  });

  it('勘定科目選択で日次残高を表示する', async () => {
    mockUseAuth.mockReturnValue(
      createMockAuthContext({
        isAuthenticated: true,
        isLoading: false,
        hasRole: vi.fn((role) => role === 'USER'),
      })
    );
    mockGetDailyBalance.mockResolvedValue(
      createMockResult({
        entries: [
          {
            date: '2024-01-01',
            debitTotal: 1000,
            creditTotal: 0,
            balance: 1000,
            transactionCount: 1,
          },
        ],
      })
    );

    render(<DailyBalancePage />);

    const user = userEvent.setup();
    await user.click(screen.getByTestId('account-select-btn'));

    await waitFor(() => {
      expect(screen.getByTestId('daily-balance-summary')).toBeInTheDocument();
    });
    expect(screen.getByTestId('daily-balance-table')).toBeInTheDocument();
    expect(screen.getByTestId('daily-balance-chart')).toBeInTheDocument();
    expect(screen.getByText('日次残高照会')).toBeInTheDocument();
  });

  it('期間指定で検索条件を反映する', async () => {
    mockUseAuth.mockReturnValue(
      createMockAuthContext({
        isAuthenticated: true,
        isLoading: false,
        hasRole: vi.fn((role) => role === 'USER'),
      })
    );

    render(<DailyBalancePage />);

    const user = userEvent.setup();
    await user.click(screen.getByTestId('account-select-btn'));
    await user.click(screen.getByTestId('date-range-btn'));
    await user.click(screen.getByTestId('search-btn'));

    await waitFor(() => {
      const lastCall = mockGetDailyBalance.mock.calls.at(-1);
      expect(lastCall?.[0]).toMatchObject({
        accountId: 1,
        dateFrom: '2024-01-01',
        dateTo: '2024-01-31',
      });
    });
  });

  it('API エラー時にエラーメッセージを表示する', async () => {
    mockUseAuth.mockReturnValue(
      createMockAuthContext({
        isAuthenticated: true,
        isLoading: false,
        hasRole: vi.fn((role) => role === 'USER'),
      })
    );
    mockGetDailyBalance.mockRejectedValue(new Error('ネットワークエラー'));

    render(<DailyBalancePage />);

    const user = userEvent.setup();
    await user.click(screen.getByTestId('account-select-btn'));

    await waitFor(() => {
      expect(screen.getByTestId('error-message')).toHaveTextContent('ネットワークエラー');
    });
  });
});
