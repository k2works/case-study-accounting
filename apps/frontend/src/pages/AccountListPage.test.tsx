import React from 'react';
import { describe, it, expect, vi, beforeEach } from 'vitest';
import { render, screen, waitFor } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import AccountListPage from './AccountListPage';
import { useAuth } from '../hooks/useAuth';
import { useLocation } from 'react-router-dom';
import type { AuthContextType } from '../types/auth';
import { getAccounts } from '../api/getAccounts';

vi.mock('../hooks/useAuth', () => ({
  useAuth: vi.fn(),
}));

vi.mock('../api/getAccounts', () => ({
  getAccounts: vi.fn(),
  getAccountsErrorMessage: (error: unknown) =>
    error instanceof Error ? error.message : '勘定科目一覧の取得に失敗しました',
}));

vi.mock('react-router-dom', () => ({
  Navigate: ({ to }: { to: string }) => <div data-testid="navigate" data-to={to} />,
  useLocation: vi.fn(),
  useNavigate: vi.fn(),
}));

vi.mock('../views/common', () => ({
  MainLayout: ({ children }: { children: React.ReactNode }) => (
    <div data-testid="main-layout">{children}</div>
  ),
  Loading: ({ message }: { message?: string }) => <div data-testid="loading">{message}</div>,
  SuccessNotification: ({ message, onDismiss }: { message: string; onDismiss?: () => void }) => (
    <div data-testid="success-notification">
      <span>{message}</span>
      <button onClick={onDismiss}>閉じる</button>
    </div>
  ),
}));

vi.mock('../views/account/AccountList', () => ({
  AccountList: () => <div data-testid="account-list-mock">AccountList</div>,
}));

const mockUseAuth = vi.mocked(useAuth);
const mockUseLocation = vi.mocked(useLocation);
const mockGetAccounts = vi.mocked(getAccounts);

const setupUser = () => userEvent.setup();

const createMockAuthContext = (overrides: Partial<AuthContextType> = {}): AuthContextType => ({
  user: null,
  isAuthenticated: false,
  isLoading: false,
  login: vi.fn(),
  logout: vi.fn(),
  hasRole: vi.fn(() => false),
  ...overrides,
});

describe('AccountListPage', () => {
  beforeEach(() => {
    vi.clearAllMocks();
    mockUseLocation.mockReturnValue({ state: null } as ReturnType<typeof useLocation>);
    mockGetAccounts.mockResolvedValue([]);
  });

  it('認証されていない場合はログインページにリダイレクト', () => {
    mockUseAuth.mockReturnValue(
      createMockAuthContext({
        isAuthenticated: false,
        isLoading: false,
      })
    );

    render(<AccountListPage />);

    const navigate = screen.getByTestId('navigate');
    expect(navigate).toHaveAttribute('data-to', '/login');
  });

  it('ローディング中の表示', () => {
    mockUseAuth.mockReturnValue(
      createMockAuthContext({
        isAuthenticated: false,
        isLoading: true,
      })
    );

    render(<AccountListPage />);

    expect(screen.getByTestId('loading')).toHaveTextContent('認証情報を確認中...');
  });

  it('ADMIN/MANAGER ロールの場合は正常表示', async () => {
    mockUseAuth.mockReturnValue(
      createMockAuthContext({
        isAuthenticated: true,
        isLoading: false,
        hasRole: vi.fn((role) => role === 'ADMIN'),
      })
    );

    render(<AccountListPage />);

    expect(screen.getByTestId('account-list-page')).toBeInTheDocument();
    expect(screen.getByText('勘定科目一覧')).toBeInTheDocument();
    await waitFor(() => expect(screen.getByTestId('account-list-mock')).toBeInTheDocument());
  });

  it('権限がない場合はホームにリダイレクト', () => {
    mockUseAuth.mockReturnValue(
      createMockAuthContext({
        isAuthenticated: true,
        isLoading: false,
        hasRole: vi.fn(() => false),
      })
    );

    render(<AccountListPage />);

    const navigate = screen.getByTestId('navigate');
    expect(navigate).toHaveAttribute('data-to', '/');
  });

  it('サクセスメッセージの表示と消去', async () => {
    mockUseAuth.mockReturnValue(
      createMockAuthContext({
        isAuthenticated: true,
        isLoading: false,
        hasRole: vi.fn((role) => role === 'ADMIN'),
      })
    );
    mockUseLocation.mockReturnValue({
      state: { successMessage: '勘定科目を更新しました' },
    } as ReturnType<typeof useLocation>);
    const user = setupUser();

    render(<AccountListPage />);

    expect(screen.getByTestId('success-notification')).toBeInTheDocument();
    expect(screen.getByText('勘定科目を更新しました')).toBeInTheDocument();

    await user.click(screen.getByRole('button', { name: '閉じる' }));

    expect(screen.queryByTestId('success-notification')).not.toBeInTheDocument();
  });
});
