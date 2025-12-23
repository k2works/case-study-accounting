import { describe, it, expect, vi } from 'vitest';
import { render, screen } from '@testing-library/react';
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

const renderWithRouter = () => {
  return render(
    <BrowserRouter>
      <DashboardPage />
    </BrowserRouter>
  );
};

describe('DashboardPage', () => {
  it('renders dashboard', () => {
    renderWithRouter();
    expect(screen.getByTestId('dashboard')).toBeInTheDocument();
  });

  it('renders dashboard title', () => {
    renderWithRouter();
    expect(screen.getByRole('heading', { name: 'ダッシュボード', level: 1 })).toBeInTheDocument();
  });

  describe('notices section', () => {
    it('renders notices section', () => {
      renderWithRouter();
      expect(screen.getByRole('heading', { name: 'お知らせ', level: 2 })).toBeInTheDocument();
    });

    it('renders important notice', () => {
      renderWithRouter();
      expect(screen.getByText('決算期末のお知らせ')).toBeInTheDocument();
      expect(screen.getByText('重要')).toBeInTheDocument();
    });

    it('renders warning notice', () => {
      renderWithRouter();
      expect(screen.getByText('システムメンテナンス')).toBeInTheDocument();
      expect(screen.getByText('注意')).toBeInTheDocument();
    });

    it('renders info notice', () => {
      renderWithRouter();
      expect(screen.getByText('新機能リリース')).toBeInTheDocument();
      expect(screen.getAllByText('お知らせ').length).toBeGreaterThanOrEqual(1);
    });

    it('renders notice dates', () => {
      renderWithRouter();
      expect(screen.getByText('2024/03/01')).toBeInTheDocument();
      expect(screen.getByText('2024/03/05')).toBeInTheDocument();
      expect(screen.getByText('2024/03/10')).toBeInTheDocument();
    });
  });

  describe('stats section', () => {
    it('renders journal count', () => {
      renderWithRouter();
      expect(screen.getByText('本日の仕訳件数')).toBeInTheDocument();
      expect(screen.getByText('25 件')).toBeInTheDocument();
    });

    it('renders pending approval count', () => {
      renderWithRouter();
      expect(screen.getByText('承認待ち件数')).toBeInTheDocument();
      expect(screen.getByText('5 件')).toBeInTheDocument();
    });
  });

  describe('recent journals section', () => {
    it('renders recent journals title', () => {
      renderWithRouter();
      expect(screen.getByRole('heading', { name: '最近の仕訳', level: 2 })).toBeInTheDocument();
    });

    it('renders table headers', () => {
      renderWithRouter();
      expect(screen.getByRole('columnheader', { name: '日付' })).toBeInTheDocument();
      expect(screen.getByRole('columnheader', { name: '摘要' })).toBeInTheDocument();
      expect(screen.getByRole('columnheader', { name: '金額' })).toBeInTheDocument();
      expect(screen.getByRole('columnheader', { name: 'ステータス' })).toBeInTheDocument();
    });

    it('renders journal entries', () => {
      renderWithRouter();
      expect(screen.getByText('売上計上')).toBeInTheDocument();
      expect(screen.getByText('仕入計上')).toBeInTheDocument();
      expect(screen.getByText('給与支払')).toBeInTheDocument();
    });

    it('renders formatted amounts', () => {
      renderWithRouter();
      expect(screen.getByText('¥100,000')).toBeInTheDocument();
      expect(screen.getByText('¥50,000')).toBeInTheDocument();
      expect(screen.getByText('¥300,000')).toBeInTheDocument();
    });

    it('renders journal statuses', () => {
      renderWithRouter();
      expect(screen.getByText('承認待ち')).toBeInTheDocument();
      expect(screen.getByText('下書き')).toBeInTheDocument();
      expect(screen.getByText('確定')).toBeInTheDocument();
    });
  });

  describe('layout', () => {
    it('renders within MainLayout', () => {
      renderWithRouter();
      expect(screen.getByTestId('header')).toBeInTheDocument();
      // Multiple navigation elements exist (sidebar nav and breadcrumb nav)
      expect(screen.getAllByRole('navigation').length).toBeGreaterThanOrEqual(1);
    });
  });
});
