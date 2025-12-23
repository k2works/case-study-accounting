import React, { useContext, useState } from 'react';
import { describe, it, expect, beforeEach, vi } from 'vitest';
import { render, screen, waitFor, act } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { QueryClient, QueryClientProvider } from '@tanstack/react-query';
import { AuthProvider, AuthContext } from './AuthProvider';
import type { AuthContextType } from '../types/auth';

// localStorage モック
const localStorageMock = (() => {
  let store: Record<string, string> = {};
  return {
    getItem: (key: string) => store[key] || null,
    setItem: (key: string, value: string) => {
      store[key] = value;
    },
    removeItem: (key: string) => {
      delete store[key];
    },
    clear: () => {
      store = {};
    },
  };
})();

Object.defineProperty(window, 'localStorage', { value: localStorageMock });

// テスト用コンポーネント
const TestConsumer: React.FC = () => {
  const auth = useContext(AuthContext);
  if (!auth) return <div>No Auth Context</div>;

  return (
    <div>
      <div data-testid="is-authenticated">{auth.isAuthenticated.toString()}</div>
      <div data-testid="is-loading">{auth.isLoading.toString()}</div>
      <div data-testid="username">{auth.user?.username || 'null'}</div>
      <div data-testid="role">{auth.user?.role || 'null'}</div>
      <button data-testid="login-admin" onClick={() => auth.login('admin', 'Password123!')}>
        Login Admin
      </button>
      <button data-testid="login-user" onClick={() => auth.login('user', 'Password123!')}>
        Login User
      </button>
      <button data-testid="logout" onClick={() => auth.logout()}>
        Logout
      </button>
      <div data-testid="has-admin">{auth.hasRole('ADMIN').toString()}</div>
      <div data-testid="has-user">{auth.hasRole('USER').toString()}</div>
    </div>
  );
};

// エラーテスト用コンポーネント
const LoginFormWithError: React.FC = () => {
  const auth = useContext(AuthContext) as AuthContextType;
  const [error, setError] = useState<string | null>(null);

  const handleLogin = async () => {
    try {
      await auth.login('admin', 'wrong');
    } catch (e) {
      setError((e as Error).message);
    }
  };

  return (
    <div>
      <button data-testid="login-with-error" onClick={handleLogin}>
        Login
      </button>
      {error && <div data-testid="error-message">{error}</div>}
    </div>
  );
};

const createTestQueryClient = () =>
  new QueryClient({ defaultOptions: { queries: { retry: false } } });

const renderWithProviders = (ui: React.ReactElement) => {
  const queryClient = createTestQueryClient();
  return render(
    <QueryClientProvider client={queryClient}>
      <AuthProvider>{ui}</AuthProvider>
    </QueryClientProvider>
  );
};

// ヘルパー関数
const waitForLoadingComplete = () =>
  waitFor(() => expect(screen.getByTestId('is-loading').textContent).toBe('false'));

const waitForAuthenticated = () =>
  waitFor(() => expect(screen.getByTestId('is-authenticated').textContent).toBe('true'));

const clickButton = async (user: ReturnType<typeof userEvent.setup>, testId: string) =>
  act(async () => {
    await user.click(screen.getByTestId(testId));
  });

const createMockToken = (expOffset: number) => {
  const exp = Math.floor(Date.now() / 1000) + expOffset;
  const payload = { exp, sub: 'admin' };
  const base64Payload = btoa(JSON.stringify(payload));
  return `header.${base64Payload}.signature`;
};

describe('AuthProvider', () => {
  beforeEach(() => {
    localStorageMock.clear();
    vi.clearAllMocks();
  });

  describe('初期状態', () => {
    it('未認証状態で開始する', async () => {
      renderWithProviders(<TestConsumer />);
      await waitForLoadingComplete();
      expect(screen.getByTestId('is-authenticated').textContent).toBe('false');
      expect(screen.getByTestId('username').textContent).toBe('null');
    });

    it('localStorage に有効なトークンがある場合は認証済み状態になる', async () => {
      localStorageMock.setItem('accessToken', createMockToken(3600));
      localStorageMock.setItem('user', JSON.stringify({ username: 'admin', role: 'ADMIN' }));

      renderWithProviders(<TestConsumer />);
      await waitForLoadingComplete();

      expect(screen.getByTestId('is-authenticated').textContent).toBe('true');
      expect(screen.getByTestId('username').textContent).toBe('admin');
      expect(screen.getByTestId('role').textContent).toBe('ADMIN');
    });

    it('localStorage に期限切れトークンがある場合は未認証状態になる', async () => {
      localStorageMock.setItem('accessToken', createMockToken(-3600));
      localStorageMock.setItem('user', JSON.stringify({ username: 'admin', role: 'ADMIN' }));

      renderWithProviders(<TestConsumer />);
      await waitForLoadingComplete();

      expect(screen.getByTestId('is-authenticated').textContent).toBe('false');
      expect(localStorageMock.getItem('accessToken')).toBeNull();
    });
  });

  describe('ログイン', () => {
    it('管理者としてログインに成功する', async () => {
      const user = userEvent.setup();
      renderWithProviders(<TestConsumer />);
      await waitForLoadingComplete();

      await clickButton(user, 'login-admin');
      await waitForAuthenticated();

      expect(screen.getByTestId('username').textContent).toBe('admin');
      expect(screen.getByTestId('role').textContent).toBe('ADMIN');
      expect(localStorageMock.getItem('accessToken')?.split('.')).toHaveLength(3);
      expect(localStorageMock.getItem('refreshToken')?.split('.')).toHaveLength(3);
    });

    it('一般ユーザーとしてログインに成功する', async () => {
      const user = userEvent.setup();
      renderWithProviders(<TestConsumer />);
      await waitForLoadingComplete();

      await clickButton(user, 'login-user');
      await waitForAuthenticated();

      expect(screen.getByTestId('username').textContent).toBe('user');
      expect(screen.getByTestId('role').textContent).toBe('USER');
    });

    it('間違った認証情報でログインに失敗する', async () => {
      const user = userEvent.setup();
      renderWithProviders(<LoginFormWithError />);

      await clickButton(user, 'login-with-error');

      await waitFor(() => expect(screen.getByTestId('error-message')).toBeInTheDocument());
      expect(screen.getByTestId('error-message').textContent).toBe(
        'ユーザーIDまたはパスワードが正しくありません'
      );
    });
  });

  describe('ログアウト', () => {
    it('ログアウトすると未認証状態になる', async () => {
      const user = userEvent.setup();
      renderWithProviders(<TestConsumer />);
      await waitForLoadingComplete();

      await clickButton(user, 'login-admin');
      await waitForAuthenticated();

      await clickButton(user, 'logout');

      expect(screen.getByTestId('is-authenticated').textContent).toBe('false');
      expect(screen.getByTestId('username').textContent).toBe('null');
      expect(localStorageMock.getItem('accessToken')).toBeNull();
      expect(localStorageMock.getItem('refreshToken')).toBeNull();
      expect(localStorageMock.getItem('user')).toBeNull();
    });
  });

  describe('ロールチェック', () => {
    it('ADMIN ユーザーはすべてのロールを持つ', async () => {
      const user = userEvent.setup();
      renderWithProviders(<TestConsumer />);
      await waitForLoadingComplete();

      await clickButton(user, 'login-admin');
      await waitForAuthenticated();

      expect(screen.getByTestId('has-admin').textContent).toBe('true');
      expect(screen.getByTestId('has-user').textContent).toBe('true');
    });

    it('USER ユーザーは USER ロールのみ持つ', async () => {
      const user = userEvent.setup();
      renderWithProviders(<TestConsumer />);
      await waitForLoadingComplete();

      await clickButton(user, 'login-user');
      await waitForAuthenticated();

      expect(screen.getByTestId('has-admin').textContent).toBe('false');
      expect(screen.getByTestId('has-user').textContent).toBe('true');
    });

    it('未認証ユーザーはロールを持たない', async () => {
      renderWithProviders(<TestConsumer />);
      await waitForLoadingComplete();

      expect(screen.getByTestId('has-admin').textContent).toBe('false');
      expect(screen.getByTestId('has-user').textContent).toBe('false');
    });
  });
});
