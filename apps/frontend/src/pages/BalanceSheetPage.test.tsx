import { describe, it, expect, vi, beforeEach } from 'vitest';
import { render, screen, waitFor } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import BalanceSheetPage from './BalanceSheetPage';
import { useRequireAuth } from '../hooks/useRequireAuth';
import { getBalanceSheet } from '../api/getBalanceSheet';
import type { GetBalanceSheetResult } from '../api/getBalanceSheet';

vi.mock('../hooks/useRequireAuth', () => ({
  useRequireAuth: vi.fn(),
}));

vi.mock('../api/getBalanceSheet', () => ({
  getBalanceSheet: vi.fn(),
  getBalanceSheetErrorMessage: (error: unknown) =>
    error instanceof Error ? error.message : '貸借対照表の取得に失敗しました',
  exportBalanceSheet: vi.fn(),
}));

vi.mock(
  'react-router-dom',
  async () => (await import('../test/pageTestMocks')).reactRouterDomMocks
);
vi.mock('../views/common', async () => {
  const mocks = (await import('../test/pageTestMocks')).commonViewMocks;
  return {
    ...mocks,
    Button: ({
      children,
      onClick,
    }: {
      children: React.ReactNode;
      onClick?: () => void;
      variant?: string;
    }) => <button onClick={onClick}>{children}</button>,
  };
});

vi.mock('../views/statement/BalanceSheetFilter', () => ({
  BalanceSheetFilter: ({
    onChange,
    onSearch,
  }: {
    values: { date: string; comparativeDate: string };
    onChange: (values: { date: string; comparativeDate: string }) => void;
    onSearch: () => void;
  }) => (
    <div data-testid="balance-sheet-filter">
      <button
        data-testid="set-date-btn"
        onClick={() => onChange({ date: '2024-12-31', comparativeDate: '' })}
      >
        基準日設定
      </button>
      <button data-testid="search-btn" onClick={onSearch}>
        表示
      </button>
    </div>
  ),
}));

vi.mock('../views/statement/BalanceSheetSummary', () => ({
  BalanceSheetSummary: ({ date }: { date: string | null }) => (
    <div data-testid="balance-sheet-summary">{date ?? '全期間'}</div>
  ),
}));

vi.mock('../views/statement/BalanceSheetTable', () => ({
  BalanceSheetTable: ({ sections }: { sections: unknown[] }) => (
    <div data-testid="balance-sheet-table">{sections.length}</div>
  ),
}));

const mockUseRequireAuth = vi.mocked(useRequireAuth);
const mockGetBalanceSheet = vi.mocked(getBalanceSheet);

const createMockResult = (
  overrides: Partial<GetBalanceSheetResult> = {}
): GetBalanceSheetResult => ({
  date: null,
  comparativeDate: null,
  sections: [],
  totalAssets: 0,
  totalLiabilities: 0,
  totalEquity: 0,
  totalLiabilitiesAndEquity: 0,
  balanced: true,
  difference: 0,
  ...overrides,
});

describe('BalanceSheetPage', () => {
  beforeEach(() => {
    vi.clearAllMocks();
    mockUseRequireAuth.mockReturnValue(null);
    mockGetBalanceSheet.mockResolvedValue(createMockResult());
  });

  it('auth guard がリダイレクトを返す場合はそれを表示', () => {
    mockUseRequireAuth.mockReturnValue(<div data-testid="navigate" data-to="/login" />);

    render(<BalanceSheetPage />);

    expect(screen.getByTestId('navigate')).toHaveAttribute('data-to', '/login');
  });

  it('認証済みの場合は貸借対照表ページを表示', () => {
    render(<BalanceSheetPage />);

    expect(screen.getByText('貸借対照表')).toBeInTheDocument();
    expect(screen.getByTestId('balance-sheet-filter')).toBeInTheDocument();
  });

  it('表示ボタンで貸借対照表を取得する', async () => {
    mockGetBalanceSheet.mockResolvedValue(
      createMockResult({
        date: '2024-12-31',
        totalAssets: 100000,
        totalLiabilities: 50000,
        totalEquity: 50000,
        totalLiabilitiesAndEquity: 100000,
        balanced: true,
        difference: 0,
      })
    );

    render(<BalanceSheetPage />);

    const user = userEvent.setup();
    await user.click(screen.getByTestId('search-btn'));

    await waitFor(() => {
      expect(screen.getByTestId('balance-sheet-summary')).toBeInTheDocument();
    });
    expect(screen.getByTestId('balance-sheet-table')).toBeInTheDocument();
  });

  it('API エラー時にエラーメッセージを表示する', async () => {
    mockGetBalanceSheet.mockRejectedValue(new Error('サーバーエラー'));

    render(<BalanceSheetPage />);

    const user = userEvent.setup();
    await user.click(screen.getByTestId('search-btn'));

    await waitFor(() => {
      expect(screen.getByTestId('error-message')).toHaveTextContent('サーバーエラー');
    });
  });

  it('検索後にエクスポートボタンが表示される', async () => {
    mockGetBalanceSheet.mockResolvedValue(
      createMockResult({
        date: '2024-12-31',
        totalAssets: 100000,
      })
    );

    render(<BalanceSheetPage />);

    const user = userEvent.setup();
    await user.click(screen.getByTestId('search-btn'));

    await waitFor(() => {
      expect(screen.getByTestId('balance-sheet-summary')).toBeInTheDocument();
    });
    expect(screen.getByText('Excel')).toBeInTheDocument();
    expect(screen.getByText('PDF')).toBeInTheDocument();
  });
});
