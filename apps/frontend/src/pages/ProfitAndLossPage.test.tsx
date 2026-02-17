import { describe, it, expect, vi, beforeEach } from 'vitest';
import { render, screen, waitFor } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import ProfitAndLossPage from './ProfitAndLossPage';
import { useRequireAuth } from '../hooks/useRequireAuth';
import { getProfitAndLoss } from '../api/getProfitAndLoss';
import type { GetProfitAndLossResult } from '../api/getProfitAndLoss';

vi.mock('../hooks/useRequireAuth', () => ({
  useRequireAuth: vi.fn(),
}));

vi.mock('../api/getProfitAndLoss', () => ({
  getProfitAndLoss: vi.fn(),
  getProfitAndLossErrorMessage: (error: unknown) =>
    error instanceof Error ? error.message : '損益計算書の取得に失敗しました',
  exportProfitAndLoss: vi.fn(),
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

vi.mock('../views/statement/ProfitAndLossFilter', () => ({
  ProfitAndLossFilter: ({
    onChange,
    onSearch,
  }: {
    values: {
      dateFrom: string;
      dateTo: string;
      comparativeDateFrom: string;
      comparativeDateTo: string;
    };
    onChange: (values: {
      dateFrom: string;
      dateTo: string;
      comparativeDateFrom: string;
      comparativeDateTo: string;
    }) => void;
    onSearch: () => void;
  }) => (
    <div data-testid="profit-and-loss-filter">
      <button
        data-testid="set-date-btn"
        onClick={() =>
          onChange({
            dateFrom: '2024-01-01',
            dateTo: '2024-12-31',
            comparativeDateFrom: '',
            comparativeDateTo: '',
          })
        }
      >
        期間設定
      </button>
      <button
        data-testid="set-comparative-btn"
        onClick={() =>
          onChange({
            dateFrom: '2024-01-01',
            dateTo: '2024-12-31',
            comparativeDateFrom: '2023-01-01',
            comparativeDateTo: '2023-12-31',
          })
        }
      >
        前期比較設定
      </button>
      <button data-testid="search-btn" onClick={onSearch}>
        表示
      </button>
    </div>
  ),
}));

vi.mock('../views/statement/ProfitAndLossSummary', () => ({
  ProfitAndLossSummary: ({ dateFrom }: { dateFrom: string | null }) => (
    <div data-testid="profit-and-loss-summary">{dateFrom ?? '全期間'}</div>
  ),
}));

vi.mock('../views/statement/ProfitAndLossTable', () => ({
  ProfitAndLossTable: ({ sections }: { sections: unknown[] }) => (
    <div data-testid="profit-and-loss-table">{sections.length}</div>
  ),
}));

const mockUseRequireAuth = vi.mocked(useRequireAuth);
const mockGetProfitAndLoss = vi.mocked(getProfitAndLoss);

const createMockResult = (
  overrides: Partial<GetProfitAndLossResult> = {}
): GetProfitAndLossResult => ({
  dateFrom: null,
  dateTo: null,
  comparativeDateFrom: null,
  comparativeDateTo: null,
  sections: [],
  totalRevenue: 0,
  totalExpense: 0,
  netIncome: 0,
  ...overrides,
});

describe('ProfitAndLossPage', () => {
  beforeEach(() => {
    vi.clearAllMocks();
    mockUseRequireAuth.mockReturnValue(null);
    mockGetProfitAndLoss.mockResolvedValue(createMockResult());
  });

  it('auth guard がリダイレクトを返す場合はそれを表示', () => {
    mockUseRequireAuth.mockReturnValue(<div data-testid="navigate" data-to="/login" />);

    render(<ProfitAndLossPage />);

    expect(screen.getByTestId('navigate')).toHaveAttribute('data-to', '/login');
  });

  it('認証済みの場合は損益計算書ページを表示', () => {
    render(<ProfitAndLossPage />);

    expect(screen.getByText('損益計算書')).toBeInTheDocument();
    expect(screen.getByTestId('profit-and-loss-filter')).toBeInTheDocument();
  });

  it('表示ボタンで損益計算書を取得する', async () => {
    mockGetProfitAndLoss.mockResolvedValue(
      createMockResult({
        dateFrom: '2024-01-01',
        dateTo: '2024-12-31',
        totalRevenue: 1000000,
        totalExpense: 700000,
        netIncome: 300000,
      })
    );

    render(<ProfitAndLossPage />);

    const user = userEvent.setup();
    await user.click(screen.getByTestId('search-btn'));

    await waitFor(() => {
      expect(screen.getByTestId('profit-and-loss-summary')).toBeInTheDocument();
    });
    expect(screen.getByTestId('profit-and-loss-table')).toBeInTheDocument();
  });

  it('API エラー時にエラーメッセージを表示する', async () => {
    mockGetProfitAndLoss.mockRejectedValue(new Error('サーバーエラー'));

    render(<ProfitAndLossPage />);

    const user = userEvent.setup();
    await user.click(screen.getByTestId('search-btn'));

    await waitFor(() => {
      expect(screen.getByTestId('error-message')).toHaveTextContent('サーバーエラー');
    });
  });

  it('検索後にエクスポートボタンが表示される', async () => {
    mockGetProfitAndLoss.mockResolvedValue(
      createMockResult({
        dateFrom: '2024-01-01',
        dateTo: '2024-12-31',
        totalRevenue: 1000000,
      })
    );

    render(<ProfitAndLossPage />);

    const user = userEvent.setup();
    await user.click(screen.getByTestId('search-btn'));

    await waitFor(() => {
      expect(screen.getByTestId('profit-and-loss-summary')).toBeInTheDocument();
    });
    expect(screen.getByText('Excel')).toBeInTheDocument();
    expect(screen.getByText('PDF')).toBeInTheDocument();
  });

  it('フィルター値を設定してから検索できる', async () => {
    mockGetProfitAndLoss.mockResolvedValue(
      createMockResult({
        dateFrom: '2024-01-01',
        dateTo: '2024-12-31',
        totalRevenue: 500000,
      })
    );

    render(<ProfitAndLossPage />);

    const user = userEvent.setup();
    await user.click(screen.getByTestId('set-date-btn'));
    await user.click(screen.getByTestId('search-btn'));

    await waitFor(() => {
      expect(mockGetProfitAndLoss).toHaveBeenCalledWith({
        dateFrom: '2024-01-01',
        dateTo: '2024-12-31',
      });
    });
  });

  it('前期比較パラメータ付きで検索できる', async () => {
    mockGetProfitAndLoss.mockResolvedValue(
      createMockResult({
        dateFrom: '2024-01-01',
        dateTo: '2024-12-31',
        comparativeDateFrom: '2023-01-01',
        comparativeDateTo: '2023-12-31',
        totalRevenue: 500000,
      })
    );

    render(<ProfitAndLossPage />);

    const user = userEvent.setup();
    await user.click(screen.getByTestId('set-comparative-btn'));
    await user.click(screen.getByTestId('search-btn'));

    await waitFor(() => {
      expect(mockGetProfitAndLoss).toHaveBeenCalledWith({
        dateFrom: '2024-01-01',
        dateTo: '2024-12-31',
        comparativeDateFrom: '2023-01-01',
        comparativeDateTo: '2023-12-31',
      });
    });
  });
});
