import { describe, it, expect, vi, beforeEach } from 'vitest';
import { render, screen, waitFor } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import SubsidiaryLedgerPage from './SubsidiaryLedgerPage';
import { useRequireAuth } from '../hooks/useRequireAuth';
import { getSubsidiaryLedger } from '../api/getSubsidiaryLedger';
import type { GetSubsidiaryLedgerResult } from '../api/getSubsidiaryLedger';

vi.mock('../hooks/useRequireAuth', () => ({
  useRequireAuth: vi.fn(),
}));

vi.mock('../api/getSubsidiaryLedger', () => ({
  getSubsidiaryLedger: vi.fn(),
  getSubsidiaryLedgerErrorMessage: (error: unknown) =>
    error instanceof Error ? error.message : '補助元帳の取得に失敗しました',
}));

vi.mock(
  'react-router-dom',
  async () => (await import('../test/pageTestMocks')).reactRouterDomMocks
);
vi.mock('../views/common', async () => {
  const mocks = (await import('../test/pageTestMocks')).commonViewMocks;
  return {
    ...mocks,
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
  };
});

vi.mock('../views/ledger/SubsidiaryLedgerFilter', () => ({
  SubsidiaryLedgerFilter: ({
    values,
    onChange,
    onSearch,
  }: {
    values: { accountCode: string; subAccountCode: string; dateFrom: string; dateTo: string };
    onChange: (values: {
      accountCode: string;
      subAccountCode: string;
      dateFrom: string;
      dateTo: string;
    }) => void;
    onSearch: () => void;
  }) => (
    <div data-testid="subsidiary-ledger-filter">
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

vi.mock('../views/ledger/SubsidiaryLedgerSummary', () => ({
  SubsidiaryLedgerSummary: ({
    accountCode,
    accountName,
  }: {
    accountCode: string;
    accountName: string;
  }) => (
    <div data-testid="subsidiary-ledger-summary">
      {accountCode} {accountName}
    </div>
  ),
}));

vi.mock('../views/ledger/SubsidiaryLedgerTable', () => ({
  SubsidiaryLedgerTable: ({ entries }: { entries: unknown[] }) => (
    <div data-testid="subsidiary-ledger-table">{entries.length}</div>
  ),
}));

const mockUseRequireAuth = vi.mocked(useRequireAuth);
const mockGetSubsidiaryLedger = vi.mocked(getSubsidiaryLedger);

const createMockResult = (
  overrides: Partial<GetSubsidiaryLedgerResult> = {}
): GetSubsidiaryLedgerResult => ({
  content: [],
  accountCode: '1000',
  accountName: '現金',
  subAccountCode: '',
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

describe('SubsidiaryLedgerPage', () => {
  beforeEach(() => {
    vi.clearAllMocks();
    mockUseRequireAuth.mockReturnValue(null);
    mockGetSubsidiaryLedger.mockResolvedValue(createMockResult());
  });

  it('auth guard がリダイレクトを返す場合はそれを表示', () => {
    mockUseRequireAuth.mockReturnValue(<div data-testid="navigate" data-to="/login" />);

    render(<SubsidiaryLedgerPage />);

    expect(screen.getByTestId('navigate')).toHaveAttribute('data-to', '/login');
  });

  it('認証済みの場合は補助元帳ページを表示', () => {
    render(<SubsidiaryLedgerPage />);

    expect(screen.getByText('補助元帳照会')).toBeInTheDocument();
    expect(screen.getByTestId('subsidiary-ledger-filter')).toBeInTheDocument();
  });

  it('勘定科目選択で補助元帳を表示する', async () => {
    mockGetSubsidiaryLedger.mockResolvedValue(
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

    render(<SubsidiaryLedgerPage />);

    const user = userEvent.setup();
    await user.click(screen.getByTestId('account-select-btn'));

    await waitFor(() => {
      expect(screen.getByTestId('subsidiary-ledger-summary')).toBeInTheDocument();
    });
    expect(screen.getByTestId('subsidiary-ledger-table')).toBeInTheDocument();
  });

  it('API エラー時にエラーメッセージを表示する', async () => {
    mockGetSubsidiaryLedger.mockRejectedValue(new Error('サーバーエラー'));

    render(<SubsidiaryLedgerPage />);

    const user = userEvent.setup();
    await user.click(screen.getByTestId('account-select-btn'));

    await waitFor(() => {
      expect(screen.getByTestId('error-message')).toHaveTextContent('サーバーエラー');
    });
  });

  it('勘定科目未選択で照会ボタンをクリックするとエラー表示', async () => {
    render(<SubsidiaryLedgerPage />);

    const user = userEvent.setup();
    await user.click(screen.getByTestId('search-btn'));

    await waitFor(() => {
      expect(screen.getByTestId('error-message')).toHaveTextContent('勘定科目を選択してください');
    });
  });
});
