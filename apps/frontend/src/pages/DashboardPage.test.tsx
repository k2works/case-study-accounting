import { describe, it, expect, vi, beforeEach } from 'vitest';
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

const renderDashboard = () =>
  render(
    <BrowserRouter>
      <DashboardPage />
    </BrowserRouter>
  );

describe('DashboardPage', () => {
  beforeEach(() => {
    renderDashboard();
  });

  it('renders dashboard', () => {
    expect(screen.getByTestId('dashboard')).toBeInTheDocument();
  });

  it('renders dashboard title', () => {
    expect(screen.getByRole('heading', { name: 'ダッシュボード', level: 1 })).toBeInTheDocument();
  });

  describe('notices section', () => {
    it('renders notices section with all notices', () => {
      expect(screen.getByRole('heading', { name: 'お知らせ', level: 2 })).toBeInTheDocument();
      expect(screen.getByText('決算期末のお知らせ')).toBeInTheDocument();
      expect(screen.getByText('重要')).toBeInTheDocument();
      expect(screen.getByText('システムメンテナンス')).toBeInTheDocument();
      expect(screen.getByText('注意')).toBeInTheDocument();
      expect(screen.getByText('新機能リリース')).toBeInTheDocument();
    });

    it('renders notice dates', () => {
      expect(screen.getByText('2024/03/01')).toBeInTheDocument();
      expect(screen.getByText('2024/03/05')).toBeInTheDocument();
      expect(screen.getByText('2024/03/10')).toBeInTheDocument();
    });
  });

  describe('stats section', () => {
    it('renders journal and approval counts', () => {
      expect(screen.getByText('本日の仕訳件数')).toBeInTheDocument();
      expect(screen.getByText('25 件')).toBeInTheDocument();
      expect(screen.getByText('承認待ち件数')).toBeInTheDocument();
      expect(screen.getByText('5 件')).toBeInTheDocument();
    });
  });

  describe('recent journals section', () => {
    it('renders table with headers', () => {
      expect(screen.getByRole('heading', { name: '最近の仕訳', level: 2 })).toBeInTheDocument();
      expect(screen.getByRole('columnheader', { name: '日付' })).toBeInTheDocument();
      expect(screen.getByRole('columnheader', { name: '摘要' })).toBeInTheDocument();
      expect(screen.getByRole('columnheader', { name: '金額' })).toBeInTheDocument();
      expect(screen.getByRole('columnheader', { name: 'ステータス' })).toBeInTheDocument();
    });

    it('renders journal entries with amounts and statuses', () => {
      expect(screen.getByText('売上計上')).toBeInTheDocument();
      expect(screen.getByText('¥100,000')).toBeInTheDocument();
      expect(screen.getByText('仕入計上')).toBeInTheDocument();
      expect(screen.getByText('¥50,000')).toBeInTheDocument();
      expect(screen.getByText('給与支払')).toBeInTheDocument();
      expect(screen.getByText('¥300,000')).toBeInTheDocument();
      expect(screen.getByText('承認待ち')).toBeInTheDocument();
      expect(screen.getByText('下書き')).toBeInTheDocument();
      expect(screen.getByText('確定')).toBeInTheDocument();
    });
  });

  describe('layout', () => {
    it('renders within MainLayout', () => {
      expect(screen.getByTestId('header')).toBeInTheDocument();
      expect(screen.getAllByRole('navigation').length).toBeGreaterThanOrEqual(1);
    });
  });
});
