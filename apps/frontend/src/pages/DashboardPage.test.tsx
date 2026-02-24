import { describe, it, expect, vi, beforeEach } from 'vitest';
import { render, screen, waitFor } from '@testing-library/react';
import { BrowserRouter } from 'react-router-dom';
import DashboardPage from './DashboardPage';

vi.mock('../hooks/useAuth', () => ({
  useAuth: () => ({
    user: { username: 'testuser', role: 'USER' },
    logout: vi.fn(),
  }),
}));

vi.mock('../config', () => ({
  config: {
    appName: 'テストアプリ',
  },
}));

const mockGetDashboardStats = vi.fn();
vi.mock('../api/getDashboardStats', () => ({
  getDashboardStats: (...args: unknown[]) => mockGetDashboardStats(...args),
}));

const mockGetJournalEntries = vi.fn();
vi.mock('../api/getJournalEntries', () => ({
  getJournalEntries: (...args: unknown[]) => mockGetJournalEntries(...args),
}));

const renderDashboard = () =>
  render(
    <BrowserRouter>
      <DashboardPage />
    </BrowserRouter>
  );

describe('DashboardPage', () => {
  beforeEach(() => {
    vi.clearAllMocks();
    mockGetDashboardStats.mockResolvedValue({
      todayJournalCount: 12,
      pendingApprovalCount: 3,
    });
    mockGetJournalEntries.mockResolvedValue({
      content: [
        {
          journalEntryId: 1,
          journalDate: '2024-04-01',
          description: '現金売上',
          totalDebitAmount: 10000,
          totalCreditAmount: 10000,
          status: 'DRAFT',
          version: 1,
        },
        {
          journalEntryId: 2,
          journalDate: '2024-04-05',
          description: '仕入支払',
          totalDebitAmount: 5000,
          totalCreditAmount: 5000,
          status: 'PENDING',
          version: 1,
        },
        {
          journalEntryId: 3,
          journalDate: '2024-04-10',
          description: '給与支払',
          totalDebitAmount: 200000,
          totalCreditAmount: 200000,
          status: 'CONFIRMED',
          version: 1,
        },
      ],
      page: 0,
      size: 5,
      totalElements: 3,
      totalPages: 1,
    });
  });

  it('renders dashboard', async () => {
    renderDashboard();
    await waitFor(() => {
      expect(screen.getByTestId('dashboard')).toBeInTheDocument();
    });
  });

  it('renders dashboard title', async () => {
    renderDashboard();
    await waitFor(() => {
      expect(screen.getByRole('heading', { name: 'ダッシュボード', level: 1 })).toBeInTheDocument();
    });
  });

  describe('notices section', () => {
    it('renders notices section with all notices', async () => {
      renderDashboard();
      await waitFor(() => {
        expect(screen.getByRole('heading', { name: 'お知らせ', level: 2 })).toBeInTheDocument();
      });
      expect(screen.getByText('決算期末のお知らせ')).toBeInTheDocument();
      expect(screen.getByText('重要')).toBeInTheDocument();
      expect(screen.getByText('システムメンテナンス')).toBeInTheDocument();
      expect(screen.getByText('注意')).toBeInTheDocument();
      expect(screen.getByText('新機能リリース')).toBeInTheDocument();
    });

    it('renders notice dates', async () => {
      renderDashboard();
      await waitFor(() => {
        expect(screen.getByText('2024/03/01')).toBeInTheDocument();
      });
      expect(screen.getByText('2024/03/05')).toBeInTheDocument();
      expect(screen.getByText('2024/03/10')).toBeInTheDocument();
    });
  });

  describe('stats section', () => {
    it('renders journal and approval counts from API', async () => {
      renderDashboard();
      await waitFor(() => {
        expect(screen.getByText('12 件')).toBeInTheDocument();
      });
      expect(screen.getByText('本日の仕訳件数')).toBeInTheDocument();
      expect(screen.getByText('承認待ち件数')).toBeInTheDocument();
      expect(screen.getByText('3 件')).toBeInTheDocument();
    });
  });

  describe('recent journals section', () => {
    it('renders table with headers', async () => {
      renderDashboard();
      await waitFor(() => {
        expect(screen.getByRole('heading', { name: '最近の仕訳', level: 2 })).toBeInTheDocument();
      });
      expect(screen.getByRole('columnheader', { name: '日付' })).toBeInTheDocument();
      expect(screen.getByRole('columnheader', { name: '摘要' })).toBeInTheDocument();
      expect(screen.getByRole('columnheader', { name: '金額' })).toBeInTheDocument();
      expect(screen.getByRole('columnheader', { name: 'ステータス' })).toBeInTheDocument();
    });

    it('renders journal entries with amounts and statuses', async () => {
      renderDashboard();
      await waitFor(() => {
        expect(screen.getByText('現金売上')).toBeInTheDocument();
      });
      expect(screen.getByText('¥10,000')).toBeInTheDocument();
      expect(screen.getByText('仕入支払')).toBeInTheDocument();
      expect(screen.getByText('¥5,000')).toBeInTheDocument();
      expect(screen.getByText('給与支払')).toBeInTheDocument();
      expect(screen.getByText('¥200,000')).toBeInTheDocument();
      expect(screen.getByText('承認待ち')).toBeInTheDocument();
      expect(screen.getByText('下書き')).toBeInTheDocument();
      expect(screen.getByText('確定')).toBeInTheDocument();
    });
  });

  describe('loading state', () => {
    it('shows loading indicator while fetching data', () => {
      mockGetDashboardStats.mockReturnValue(new Promise(() => {}));
      mockGetJournalEntries.mockReturnValue(new Promise(() => {}));
      renderDashboard();
      expect(screen.getByTestId('dashboard-loading')).toBeInTheDocument();
    });
  });

  describe('error state', () => {
    it('shows error message when API fails', async () => {
      mockGetDashboardStats.mockRejectedValue(new Error('API Error'));
      renderDashboard();
      await waitFor(() => {
        expect(screen.getByTestId('dashboard-error')).toBeInTheDocument();
      });
      expect(screen.getByText('データの取得に失敗しました')).toBeInTheDocument();
    });
  });

  describe('layout', () => {
    it('renders within MainLayout', async () => {
      renderDashboard();
      await waitFor(() => {
        expect(screen.getByTestId('header')).toBeInTheDocument();
      });
      expect(screen.getAllByRole('navigation').length).toBeGreaterThanOrEqual(1);
    });
  });
});
