import { beforeEach, describe, expect, it, vi } from 'vitest';
import { render, screen, waitFor } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import FinancialAnalysisPage from './FinancialAnalysisPage';
import { useRequireAuth } from '../hooks/useRequireAuth';
import { getFinancialAnalysis } from '../api/getFinancialAnalysis';
import type { GetFinancialAnalysisResult } from '../api/getFinancialAnalysis';

vi.mock('../hooks/useRequireAuth', () => ({
  useRequireAuth: vi.fn(),
}));

vi.mock('../api/getFinancialAnalysis', () => ({
  getFinancialAnalysis: vi.fn(),
  getFinancialAnalysisErrorMessage: (error: unknown) =>
    error instanceof Error ? error.message : '財務分析の取得に失敗しました',
}));

vi.mock(
  'react-router-dom',
  async () => (await import('../test/pageTestMocks')).reactRouterDomMocks
);
vi.mock('../views/common', async () => (await import('../test/pageTestMocks')).commonViewMocks);

vi.mock('../views/statement/FinancialAnalysisFilter', () => ({
  FinancialAnalysisFilter: ({
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
    <div data-testid="financial-analysis-filter">
      <button
        data-testid="set-filter-btn"
        onClick={() =>
          onChange({
            dateFrom: '2024-01-01',
            dateTo: '2024-12-31',
            comparativeDateFrom: '2023-01-01',
            comparativeDateTo: '2023-12-31',
          })
        }
      >
        期間設定
      </button>
      <button data-testid="search-btn" onClick={onSearch}>
        表示
      </button>
    </div>
  ),
}));

vi.mock('../views/statement/FinancialAnalysisIndicators', () => ({
  FinancialAnalysisIndicators: ({ categories }: { categories: unknown[] }) => (
    <div data-testid="financial-analysis-indicators">{categories.length}</div>
  ),
}));

vi.mock('../views/statement/FinancialAnalysisTrend', () => ({
  FinancialAnalysisTrend: ({ categories }: { categories: unknown[] }) => (
    <div data-testid="financial-analysis-trend">{categories.length}</div>
  ),
}));

const mockUseRequireAuth = vi.mocked(useRequireAuth);
const mockGetFinancialAnalysis = vi.mocked(getFinancialAnalysis);

const createMockResult = (
  overrides: Partial<GetFinancialAnalysisResult> = {}
): GetFinancialAnalysisResult => ({
  dateFrom: null,
  dateTo: null,
  comparativeDateFrom: null,
  comparativeDateTo: null,
  categories: [],
  ...overrides,
});

describe('FinancialAnalysisPage', () => {
  beforeEach(() => {
    vi.clearAllMocks();
    mockUseRequireAuth.mockReturnValue(null);
    mockGetFinancialAnalysis.mockResolvedValue(createMockResult());
  });

  it('auth guard が JSX を返す場合はそれを表示', () => {
    mockUseRequireAuth.mockReturnValue(<div data-testid="navigate" data-to="/login" />);

    render(<FinancialAnalysisPage />);

    expect(screen.getByTestId('navigate')).toHaveAttribute('data-to', '/login');
  });

  it('認証済みの場合はフィルターと表示ボタンを表示', () => {
    render(<FinancialAnalysisPage />);

    expect(screen.getByText('財務分析')).toBeInTheDocument();
    expect(screen.getByTestId('financial-analysis-filter')).toBeInTheDocument();
    expect(screen.getByTestId('search-btn')).toBeInTheDocument();
  });

  it('表示ボタンクリックで財務分析を取得して結果を表示', async () => {
    mockGetFinancialAnalysis.mockResolvedValue(
      createMockResult({
        dateFrom: '2024-01-01',
        dateTo: '2024-12-31',
        comparativeDateFrom: '2023-01-01',
        comparativeDateTo: '2023-12-31',
        categories: [
          {
            categoryName: 'PROFITABILITY',
            categoryDisplayName: '収益性',
            indicators: [
              {
                name: 'ROE',
                unit: '%',
                value: 80,
                previousValue: 64.29,
                difference: 15.71,
                changeRate: 24.44,
                formula: '当期純利益 ÷ 自己資本 × 100',
                industryAverage: 8,
              },
            ],
          },
        ],
      })
    );

    render(<FinancialAnalysisPage />);

    const user = userEvent.setup();
    await user.click(screen.getByTestId('set-filter-btn'));
    await user.click(screen.getByTestId('search-btn'));

    await waitFor(() => {
      expect(mockGetFinancialAnalysis).toHaveBeenCalledWith({
        dateFrom: '2024-01-01',
        dateTo: '2024-12-31',
        comparativeDateFrom: '2023-01-01',
        comparativeDateTo: '2023-12-31',
      });
    });
    expect(screen.getByTestId('financial-analysis-indicators')).toBeInTheDocument();
    expect(screen.getByTestId('financial-analysis-trend')).toBeInTheDocument();
  });

  it('API エラー時にエラーメッセージを表示する', async () => {
    mockGetFinancialAnalysis.mockRejectedValue(new Error('サーバーエラー'));

    render(<FinancialAnalysisPage />);

    const user = userEvent.setup();
    await user.click(screen.getByTestId('search-btn'));

    await waitFor(() => {
      expect(screen.getByTestId('error-message')).toHaveTextContent('サーバーエラー');
    });
  });
});
