import React from 'react';
import { describe, it, expect, vi, beforeEach } from 'vitest';
import { render, screen, waitFor } from '@testing-library/react';
import UserEditPage from './UserEditPage';
import { useAuth } from '../hooks/useAuth';
import { getUser } from '../api/getUser';
import type { AuthContextType } from '../types/auth';

vi.mock('../hooks/useAuth', () => ({
  useAuth: vi.fn(),
}));

vi.mock('../api/getUser', () => ({
  getUser: vi.fn(),
  getUserErrorMessage: (error: unknown) =>
    error instanceof Error ? error.message : 'ユーザー情報の取得に失敗しました',
}));

vi.mock('react-router-dom', () => ({
  Navigate: ({ to }: { to: string }) => <div data-testid="navigate" data-to={to} />,
  useParams: vi.fn(() => ({ id: 'user-1' })),
  useNavigate: vi.fn(() => vi.fn()),
}));

vi.mock('../views/common', () => ({
  MainLayout: ({ children }: { children: React.ReactNode }) => (
    <div data-testid="main-layout">{children}</div>
  ),
  Loading: ({ message }: { message?: string }) => <div data-testid="loading">{message}</div>,
  ErrorMessage: ({ message, onRetry }: { message: string; onRetry?: () => void }) => (
    <div data-testid="error-message">
      <span>{message}</span>
      {onRetry && <button onClick={onRetry}>再試行</button>}
    </div>
  ),
}));

vi.mock('../views/auth/UserEditForm', () => ({
  UserEditForm: ({
    user,
    onSuccess,
  }: {
    user: { id: string; username: string };
    onSuccess: (message: string) => void;
  }) => (
    <div data-testid="user-edit-form-mock">
      <p>ユーザー ID: {user.id}</p>
      <p>ユーザー名: {user.username}</p>
      <button onClick={() => onSuccess('更新しました')}>更新</button>
    </div>
  ),
}));

const mockUseAuth = vi.mocked(useAuth);
const mockGetUser = vi.mocked(getUser);

const createMockAuthContext = (overrides: Partial<AuthContextType> = {}): AuthContextType => ({
  user: null,
  isAuthenticated: false,
  isLoading: false,
  login: vi.fn(),
  logout: vi.fn(),
  hasRole: vi.fn(() => false),
  ...overrides,
});

const mockUser = {
  id: 'user-1',
  username: 'admin',
  email: 'admin@example.com',
  displayName: '管理者',
  role: 'ADMIN',
  lastLoginAt: '2024-01-15T10:30:00',
};

describe('UserEditPage', () => {
  beforeEach(() => {
    vi.clearAllMocks();
    mockGetUser.mockResolvedValue(mockUser);
  });

  it('認証されていない場合はログインページにリダイレクト', () => {
    mockUseAuth.mockReturnValue(
      createMockAuthContext({
        isAuthenticated: false,
        isLoading: false,
      })
    );

    render(<UserEditPage />);

    const navigate = screen.getByTestId('navigate');
    expect(navigate).toHaveAttribute('data-to', '/login');
  });

  it('認証ローディング中の表示', () => {
    mockUseAuth.mockReturnValue(
      createMockAuthContext({
        isAuthenticated: false,
        isLoading: true,
      })
    );

    render(<UserEditPage />);

    expect(screen.getByTestId('loading')).toHaveTextContent('認証情報を確認中...');
  });

  it('ADMIN ロールでない場合はホームにリダイレクト', () => {
    mockUseAuth.mockReturnValue(
      createMockAuthContext({
        isAuthenticated: true,
        isLoading: false,
        hasRole: vi.fn(() => false),
      })
    );

    render(<UserEditPage />);

    const navigate = screen.getByTestId('navigate');
    expect(navigate).toHaveAttribute('data-to', '/');
  });

  it('ADMIN ロールの場合は編集ページが表示される', async () => {
    mockUseAuth.mockReturnValue(
      createMockAuthContext({
        isAuthenticated: true,
        isLoading: false,
        hasRole: vi.fn((role) => role === 'ADMIN'),
      })
    );

    render(<UserEditPage />);

    await waitFor(() => {
      expect(screen.getByTestId('user-edit-page')).toBeInTheDocument();
    });
    expect(screen.getByText('ユーザー編集')).toBeInTheDocument();
  });

  it('ユーザー情報取得中はローディングが表示される', async () => {
    mockUseAuth.mockReturnValue(
      createMockAuthContext({
        isAuthenticated: true,
        isLoading: false,
        hasRole: vi.fn((role) => role === 'ADMIN'),
      })
    );

    mockGetUser.mockImplementation(
      () => new Promise((resolve) => setTimeout(() => resolve(mockUser), 100))
    );

    render(<UserEditPage />);

    expect(screen.getByTestId('loading')).toHaveTextContent('ユーザー情報を読み込み中...');

    await waitFor(() => {
      expect(screen.getByTestId('user-edit-form-mock')).toBeInTheDocument();
    });
  });

  it('ユーザー情報取得成功時にフォームが表示される', async () => {
    mockUseAuth.mockReturnValue(
      createMockAuthContext({
        isAuthenticated: true,
        isLoading: false,
        hasRole: vi.fn((role) => role === 'ADMIN'),
      })
    );

    render(<UserEditPage />);

    await waitFor(() => {
      expect(screen.getByTestId('user-edit-form-mock')).toBeInTheDocument();
    });
    expect(screen.getByText('ユーザー ID: user-1')).toBeInTheDocument();
    expect(screen.getByText('ユーザー名: admin')).toBeInTheDocument();
  });

  it('ユーザー情報取得失敗時にエラーメッセージが表示される', async () => {
    mockUseAuth.mockReturnValue(
      createMockAuthContext({
        isAuthenticated: true,
        isLoading: false,
        hasRole: vi.fn((role) => role === 'ADMIN'),
      })
    );

    mockGetUser.mockRejectedValue(new Error('ユーザー情報の取得に失敗しました'));

    render(<UserEditPage />);

    await waitFor(() => {
      expect(screen.getByTestId('error-message')).toBeInTheDocument();
    });
    expect(screen.getByText('ユーザー情報の取得に失敗しました')).toBeInTheDocument();
  });

  it('getUser が呼ばれる', async () => {
    mockUseAuth.mockReturnValue(
      createMockAuthContext({
        isAuthenticated: true,
        isLoading: false,
        hasRole: vi.fn((role) => role === 'ADMIN'),
      })
    );

    render(<UserEditPage />);

    await waitFor(() => {
      expect(mockGetUser).toHaveBeenCalledWith('user-1');
    });
  });
});

describe('UserEditPage - 無効なIDの場合', () => {
  it('IDが空の場合はエラーメッセージが表示される', async () => {
    vi.doMock('react-router-dom', () => ({
      Navigate: ({ to }: { to: string }) => <div data-testid="navigate" data-to={to} />,
      useParams: vi.fn(() => ({ id: '' })),
      useNavigate: vi.fn(() => vi.fn()),
    }));

    // Re-import the module to pick up the new mock
    vi.resetModules();
    const { default: UserEditPageWithEmptyId } = await import('./UserEditPage');
    const { useAuth: mockedUseAuth } = await import('../hooks/useAuth');

    vi.mocked(mockedUseAuth).mockReturnValue(
      createMockAuthContext({
        isAuthenticated: true,
        isLoading: false,
        hasRole: vi.fn((role) => role === 'ADMIN'),
      })
    );

    render(<UserEditPageWithEmptyId />);

    await waitFor(() => {
      expect(screen.getByTestId('error-message')).toBeInTheDocument();
    });
    expect(screen.getByText('ユーザーが見つかりませんでした')).toBeInTheDocument();
  });
});
