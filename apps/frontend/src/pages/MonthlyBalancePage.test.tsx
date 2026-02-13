import React from 'react';
import { describe, it, expect, vi, beforeEach } from 'vitest';
import { render, screen, waitFor } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import MonthlyBalancePage from './MonthlyBalancePage';
import { useRequireAuth } from '../hooks/useRequireAuth';
import { getMonthlyBalance } from '../api/getMonthlyBalance';
import type { GetMonthlyBalanceResult } from '../api/getMonthlyBalance';

vi.mock('../hooks/useRequireAuth', () => ({
  useRequireAuth: vi.fn(),
}));

vi.mock('../api/getMonthlyBalance', () => ({
  getMonthlyBalance: vi.fn(),
  getMonthlyBalanceErrorMessage: (error: unknown) =>
    error instanceof Error ? error.message : '月次残高の取得に失敗しました',
}));

vi.mock('react-router-dom', () => ({
  Navigate: ({ to }: { to: string }) => <div data-testid="navigate" data-to={to} />,
}));

vi.mock('../views/common', () => ({
  MainLayout: ({ children }: { children: React.ReactNode }) => (
    <div data-testid="main-layout">{children}</div>
  ),
  Loading: ({ message }: { message?: string }) => <div data-testid="loading">{message}</div>,
  ErrorMessage: ({ message, onRetry }: { message: string; onRetry?: () => void }) => (
    <button data-testid="error-message" onClick={onRetry}>
      {message}
    </button>
  ),
}));

vi.mock('../views/ledger/MonthlyBalanceFilter', () => ({
  MonthlyBalanceFilter: ({
    values,
    onChange,
    onSearch,
  }: {
    values: { accountCode: string; fiscalPeriod: string };
    onChange: (values: { accountCode: string; fiscalPeriod: string }) => void;
    onSearch: () => void;
  }) => (
    <div data-testid="monthly-balance-filter">
      <button
        data-testid="account-select-btn"
        onClick={() => onChange({ ...values, accountCode: '1000' })}
      >
        勘定科目選択
      </button>
      <button data-testid="search-btn" onClick={onSearch}>
        照会
      </button>
    </div>
  ),
}));

vi.mock('../views/ledger/MonthlyBalanceSummary', () => ({
  MonthlyBalanceSummary: ({
    accountCode,
    accountName,
  }: {
    accountCode: string;
    accountName: string;
  }) => (
    <div data-testid="monthly-balance-summary">
      {accountCode} {accountName}
    </div>
  ),
}));

vi.mock('../views/ledger/MonthlyBalanceTable', () => ({
  MonthlyBalanceTable: ({ entries }: { entries: unknown[] }) => (
    <div data-testid="monthly-balance-table">{entries.length}</div>
  ),
}));

vi.mock('../views/ledger/MonthlyBalanceChart', () => ({
  MonthlyBalanceChart: ({ entries }: { entries: unknown[] }) => (
    <div data-testid="monthly-balance-chart">{entries.length}</div>
  ),
}));

const mockUseRequireAuth = vi.mocked(useRequireAuth);
const mockGetMonthlyBalance = vi.mocked(getMonthlyBalance);

const createMockResult = (
  overrides: Partial<GetMonthlyBalanceResult> = {}
): GetMonthlyBalanceResult => ({
  accountCode: '1000',
  accountName: '現金',
  fiscalPeriod: 2024,
  openingBalance: 0,
  debitTotal: 0,
  creditTotal: 0,
  closingBalance: 0,
  entries: [],
  ...overrides,
});

describe('MonthlyBalancePage', () => {
  beforeEach(() => {
    vi.clearAllMocks();
    mockUseRequireAuth.mockReturnValue(null);
    mockGetMonthlyBalance.mockResolvedValue(createMockResult());
  });

  it('auth guard がリダイレクトを返す場合はそれを表示', () => {
    mockUseRequireAuth.mockReturnValue(<div data-testid="navigate" data-to="/login" />);

    render(<MonthlyBalancePage />);

    expect(screen.getByTestId('navigate')).toHaveAttribute('data-to', '/login');
  });

  it('認証済みの場合は月次残高ページを表示', () => {
    render(<MonthlyBalancePage />);

    expect(screen.getByText('月次残高照会')).toBeInTheDocument();
    expect(screen.getByTestId('monthly-balance-filter')).toBeInTheDocument();
  });

  it('勘定科目選択で月次残高を表示する', async () => {
    mockGetMonthlyBalance.mockResolvedValue(
      createMockResult({
        entries: [
          { month: 1, openingBalance: 0, debitAmount: 1000, creditAmount: 0, closingBalance: 1000 },
        ],
      })
    );

    render(<MonthlyBalancePage />);

    const user = userEvent.setup();
    await user.click(screen.getByTestId('account-select-btn'));

    await waitFor(() => {
      expect(screen.getByTestId('monthly-balance-summary')).toBeInTheDocument();
    });
    expect(screen.getByTestId('monthly-balance-table')).toBeInTheDocument();
    expect(screen.getByTestId('monthly-balance-chart')).toBeInTheDocument();
  });

  it('API エラー時にエラーメッセージを表示する', async () => {
    mockGetMonthlyBalance.mockRejectedValue(new Error('サーバーエラー'));

    render(<MonthlyBalancePage />);

    const user = userEvent.setup();
    await user.click(screen.getByTestId('account-select-btn'));

    await waitFor(() => {
      expect(screen.getByTestId('error-message')).toHaveTextContent('サーバーエラー');
    });
  });

  it('勘定科目未選択で照会ボタンをクリックするとエラー表示', async () => {
    render(<MonthlyBalancePage />);

    const user = userEvent.setup();
    await user.click(screen.getByTestId('search-btn'));

    await waitFor(() => {
      expect(screen.getByTestId('error-message')).toHaveTextContent('勘定科目を選択してください');
    });
  });
});
