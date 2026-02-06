import React from 'react';
import { describe, it, expect, vi, beforeEach } from 'vitest';
import { render, screen, waitFor } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import GeneralLedgerPage from './GeneralLedgerPage';
import { useAuth } from '../hooks/useAuth';
import { createMockAuthContext } from '../test/testUtils';
import { getGeneralLedger } from '../api/getGeneralLedger';
import type { GetGeneralLedgerResult } from '../api/getGeneralLedger';

vi.mock('../hooks/useAuth', () => ({
  useAuth: vi.fn(),
}));

vi.mock('../api/getGeneralLedger', () => ({
  getGeneralLedger: vi.fn(),
  getGeneralLedgerErrorMessage: (error: unknown) =>
    error instanceof Error ? error.message : '総勘定元帳の取得に失敗しました',
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
  Pagination: ({
    onPageChange,
    onItemsPerPageChange,
  }: {
    onPageChange: (page: number) => void;
    onItemsPerPageChange?: (size: number) => void;
  }) => (
    <div data-testid="pagination">
      <button data-testid="page-change-btn" onClick={() => onPageChange(2)}>
        次ページ
      </button>
      {onItemsPerPageChange && (
        <button data-testid="items-change-btn" onClick={() => onItemsPerPageChange(50)}>
          50 件表示
        </button>
      )}
    </div>
  ),
}));

vi.mock('../views/ledger/GeneralLedgerFilter', () => ({
  GeneralLedgerFilter: ({
    values,
    onChange,
    onSearch,
  }: {
    values: { accountId: string; dateFrom: string; dateTo: string };
    onChange: (values: { accountId: string; dateFrom: string; dateTo: string }) => void;
    onSearch: () => void;
  }) => (
    <div data-testid="general-ledger-filter">
      <button
        data-testid="account-select-btn"
        onClick={() => onChange({ ...values, accountId: '1' })}
      >
        勘定科目選択
      </button>
      <button data-testid="search-btn" onClick={onSearch}>
        照会
      </button>
    </div>
  ),
}));

vi.mock('../views/ledger/GeneralLedgerSummary', () => ({
  GeneralLedgerSummary: ({
    accountCode,
    accountName,
  }: {
    accountCode: string;
    accountName: string;
  }) => (
    <div data-testid="general-ledger-summary">
      {accountCode} {accountName}
    </div>
  ),
}));

vi.mock('../views/ledger/GeneralLedgerTable', () => ({
  GeneralLedgerTable: ({ entries }: { entries: unknown[] }) => (
    <div data-testid="general-ledger-table">{entries.length}</div>
  ),
}));

const mockUseAuth = vi.mocked(useAuth);
const mockGetGeneralLedger = vi.mocked(getGeneralLedger);

const createMockResult = (
  overrides: Partial<GetGeneralLedgerResult> = {}
): GetGeneralLedgerResult => ({
  content: [],
  accountId: 1,
  accountCode: '100',
  accountName: '現金',
  openingBalance: 0,
  debitTotal: 0,
  creditTotal: 0,
  closingBalance: 0,
  page: 0,
  size: 20,
  totalElements: 0,
  totalPages: 0,
  ...overrides,
});

describe('GeneralLedgerPage', () => {
  beforeEach(() => {
    vi.clearAllMocks();
    mockGetGeneralLedger.mockResolvedValue(createMockResult());
  });

  it('認証されていない場合はログインページにリダイレクト', () => {
    mockUseAuth.mockReturnValue(
      createMockAuthContext({ isAuthenticated: false, isLoading: false })
    );

    render(<GeneralLedgerPage />);

    const navigate = screen.getByTestId('navigate');
    expect(navigate).toHaveAttribute('data-to', '/login');
  });

  it('ローディング中の表示', () => {
    mockUseAuth.mockReturnValue(createMockAuthContext({ isAuthenticated: false, isLoading: true }));

    render(<GeneralLedgerPage />);

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

    render(<GeneralLedgerPage />);

    const navigate = screen.getByTestId('navigate');
    expect(navigate).toHaveAttribute('data-to', '/');
  });

  it('勘定科目選択で元帳を表示する', async () => {
    mockUseAuth.mockReturnValue(
      createMockAuthContext({
        isAuthenticated: true,
        isLoading: false,
        hasRole: vi.fn((role) => role === 'USER'),
      })
    );
    mockGetGeneralLedger.mockResolvedValue(
      createMockResult({
        content: [
          {
            journalEntryId: 1,
            journalDate: '2024-01-01',
            description: 'テスト仕訳',
            debitAmount: 1000,
            creditAmount: 0,
            runningBalance: 1000,
          },
        ],
        totalElements: 1,
        totalPages: 1,
      })
    );

    render(<GeneralLedgerPage />);

    const user = userEvent.setup();
    await user.click(screen.getByTestId('account-select-btn'));

    await waitFor(() => {
      expect(screen.getByTestId('general-ledger-summary')).toBeInTheDocument();
    });
    expect(screen.getByTestId('general-ledger-table')).toBeInTheDocument();
    expect(screen.getByText('総勘定元帳照会')).toBeInTheDocument();
  });

  it('照会ボタンで再検索する', async () => {
    mockUseAuth.mockReturnValue(
      createMockAuthContext({
        isAuthenticated: true,
        isLoading: false,
        hasRole: vi.fn((role) => role === 'USER'),
      })
    );

    render(<GeneralLedgerPage />);

    const user = userEvent.setup();
    await user.click(screen.getByTestId('account-select-btn'));

    await waitFor(() => {
      expect(mockGetGeneralLedger).toHaveBeenCalledTimes(1);
    });

    await user.click(screen.getByTestId('search-btn'));

    expect(mockGetGeneralLedger).toHaveBeenCalledTimes(2);
  });

  it('ページ変更で再取得する', async () => {
    mockUseAuth.mockReturnValue(
      createMockAuthContext({
        isAuthenticated: true,
        isLoading: false,
        hasRole: vi.fn((role) => role === 'USER'),
      })
    );

    render(<GeneralLedgerPage />);

    const user = userEvent.setup();
    await user.click(screen.getByTestId('account-select-btn'));

    await waitFor(() => {
      expect(screen.getByTestId('pagination')).toBeInTheDocument();
    });

    await user.click(screen.getByTestId('page-change-btn'));

    expect(mockGetGeneralLedger).toHaveBeenCalledTimes(2);
  });

  it('API エラー時にエラーメッセージを表示する', async () => {
    mockUseAuth.mockReturnValue(
      createMockAuthContext({
        isAuthenticated: true,
        isLoading: false,
        hasRole: vi.fn((role) => role === 'USER'),
      })
    );
    mockGetGeneralLedger.mockRejectedValue(new Error('ネットワークエラー'));

    render(<GeneralLedgerPage />);

    const user = userEvent.setup();
    await user.click(screen.getByTestId('account-select-btn'));

    await waitFor(() => {
      expect(screen.getByTestId('error-message')).toHaveTextContent('ネットワークエラー');
    });
  });
});
