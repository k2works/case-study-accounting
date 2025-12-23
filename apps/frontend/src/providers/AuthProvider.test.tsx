import { describe, it, expect, beforeEach, vi } from 'vitest';
import { render, screen, waitFor, act } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { QueryClient, QueryClientProvider } from '@tanstack/react-query';
import { AuthProvider, AuthContext } from './AuthProvider';
import { useContext } from 'react';
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
      <button data-testid="login-fail" onClick={() => auth.login('admin', 'wrong')}>
        Login Fail
      </button>
      <button data-testid="logout" onClick={() => auth.logout()}>
        Logout
      </button>
      <div data-testid="has-admin">{auth.hasRole('ADMIN').toString()}</div>
      <div data-testid="has-user">{auth.hasRole('USER').toString()}</div>
    </div>
  );
};

// ログインフォームのテスト用コンポーネント
const LoginForm: React.FC = () => {
  const auth = useContext(AuthContext) as AuthContextType;
  const [error, setError] = React.useState<string | null>(null);

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

import React from 'react';

const createTestQueryClient = () =>
  new QueryClient({
    defaultOptions: {
      queries: {
        retry: false,
      },
    },
  });

const renderWithProviders = (ui: React.ReactElement) => {
  const queryClient = createTestQueryClient();
  return render(
    <QueryClientProvider client={queryClient}>
      <AuthProvider>{ui}</AuthProvider>
    </QueryClientProvider>
  );
};

describe('AuthProvider', () => {
  beforeEach(() => {
    localStorageMock.clear();
    vi.clearAllMocks();
  });

  describe('初期状態', () => {
    it('未認証状態で開始する', async () => {
      renderWithProviders(<TestConsumer />);

      await waitFor(() => {
        expect(screen.getByTestId('is-loading').textContent).toBe('false');
      });

      expect(screen.getByTestId('is-authenticated').textContent).toBe('false');
      expect(screen.getByTestId('username').textContent).toBe('null');
    });

    it('localStorage に有効なトークンがある場合は認証済み状態になる', async () => {
      // 有効なトークンを設定（exp は将来の時刻）
      const futureExp = Math.floor(Date.now() / 1000) + 3600; // 1時間後
      const payload = { exp: futureExp, sub: 'admin' };
      const base64Payload = btoa(JSON.stringify(payload));
      const mockToken = `header.${base64Payload}.signature`;

      localStorageMock.setItem('accessToken', mockToken);
      localStorageMock.setItem('user', JSON.stringify({ username: 'admin', role: 'ADMIN' }));

      renderWithProviders(<TestConsumer />);

      await waitFor(() => {
        expect(screen.getByTestId('is-loading').textContent).toBe('false');
      });

      expect(screen.getByTestId('is-authenticated').textContent).toBe('true');
      expect(screen.getByTestId('username').textContent).toBe('admin');
      expect(screen.getByTestId('role').textContent).toBe('ADMIN');
    });

    it('localStorage に期限切れトークンがある場合は未認証状態になる', async () => {
      // 期限切れトークンを設定
      const pastExp = Math.floor(Date.now() / 1000) - 3600; // 1時間前
      const payload = { exp: pastExp, sub: 'admin' };
      const base64Payload = btoa(JSON.stringify(payload));
      const mockToken = `header.${base64Payload}.signature`;

      localStorageMock.setItem('accessToken', mockToken);
      localStorageMock.setItem('user', JSON.stringify({ username: 'admin', role: 'ADMIN' }));

      renderWithProviders(<TestConsumer />);

      await waitFor(() => {
        expect(screen.getByTestId('is-loading').textContent).toBe('false');
      });

      expect(screen.getByTestId('is-authenticated').textContent).toBe('false');
      expect(localStorageMock.getItem('accessToken')).toBeNull();
    });
  });

  describe('ログイン', () => {
    it('管理者としてログインに成功する', async () => {
      const user = userEvent.setup();
      renderWithProviders(<TestConsumer />);

      await waitFor(() => {
        expect(screen.getByTestId('is-loading').textContent).toBe('false');
      });

      await act(async () => {
        await user.click(screen.getByTestId('login-admin'));
      });

      await waitFor(() => {
        expect(screen.getByTestId('is-authenticated').textContent).toBe('true');
      });

      expect(screen.getByTestId('username').textContent).toBe('admin');
      expect(screen.getByTestId('role').textContent).toBe('ADMIN');
      // JWT形式のトークンが保存されていることを確認
      const accessToken = localStorageMock.getItem('accessToken');
      const refreshToken = localStorageMock.getItem('refreshToken');
      expect(accessToken).not.toBeNull();
      expect(refreshToken).not.toBeNull();
      expect(accessToken?.split('.')).toHaveLength(3);
      expect(refreshToken?.split('.')).toHaveLength(3);
    });

    it('一般ユーザーとしてログインに成功する', async () => {
      const user = userEvent.setup();
      renderWithProviders(<TestConsumer />);

      await waitFor(() => {
        expect(screen.getByTestId('is-loading').textContent).toBe('false');
      });

      await act(async () => {
        await user.click(screen.getByTestId('login-user'));
      });

      await waitFor(() => {
        expect(screen.getByTestId('is-authenticated').textContent).toBe('true');
      });

      expect(screen.getByTestId('username').textContent).toBe('user');
      expect(screen.getByTestId('role').textContent).toBe('USER');
    });

    it('間違った認証情報でログインに失敗する', async () => {
      const user = userEvent.setup();
      renderWithProviders(<LoginForm />);

      await act(async () => {
        await user.click(screen.getByTestId('login-with-error'));
      });

      await waitFor(() => {
        expect(screen.getByTestId('error-message')).toBeInTheDocument();
      });

      expect(screen.getByTestId('error-message').textContent).toBe(
        'ユーザーIDまたはパスワードが正しくありません'
      );
    });
  });

  describe('ログアウト', () => {
    it('ログアウトすると未認証状態になる', async () => {
      const user = userEvent.setup();
      renderWithProviders(<TestConsumer />);

      await waitFor(() => {
        expect(screen.getByTestId('is-loading').textContent).toBe('false');
      });

      // まずログイン
      await act(async () => {
        await user.click(screen.getByTestId('login-admin'));
      });

      await waitFor(() => {
        expect(screen.getByTestId('is-authenticated').textContent).toBe('true');
      });

      // ログアウト
      await act(async () => {
        await user.click(screen.getByTestId('logout'));
      });

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

      await waitFor(() => {
        expect(screen.getByTestId('is-loading').textContent).toBe('false');
      });

      await act(async () => {
        await user.click(screen.getByTestId('login-admin'));
      });

      await waitFor(() => {
        expect(screen.getByTestId('is-authenticated').textContent).toBe('true');
      });

      expect(screen.getByTestId('has-admin').textContent).toBe('true');
      expect(screen.getByTestId('has-user').textContent).toBe('true');
    });

    it('USER ユーザーは USER ロールのみ持つ', async () => {
      const user = userEvent.setup();
      renderWithProviders(<TestConsumer />);

      await waitFor(() => {
        expect(screen.getByTestId('is-loading').textContent).toBe('false');
      });

      await act(async () => {
        await user.click(screen.getByTestId('login-user'));
      });

      await waitFor(() => {
        expect(screen.getByTestId('is-authenticated').textContent).toBe('true');
      });

      expect(screen.getByTestId('has-admin').textContent).toBe('false');
      expect(screen.getByTestId('has-user').textContent).toBe('true');
    });

    it('未認証ユーザーはロールを持たない', async () => {
      renderWithProviders(<TestConsumer />);

      await waitFor(() => {
        expect(screen.getByTestId('is-loading').textContent).toBe('false');
      });

      expect(screen.getByTestId('has-admin').textContent).toBe('false');
      expect(screen.getByTestId('has-user').textContent).toBe('false');
    });
  });
});
