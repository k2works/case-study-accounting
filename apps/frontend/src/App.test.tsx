import { describe, it, expect, vi, beforeEach } from 'vitest';
import { render, screen, waitFor } from '@testing-library/react';
import { MemoryRouter } from 'react-router-dom';
import { QueryClient, QueryClientProvider } from '@tanstack/react-query';
import { App } from './App';
import { AuthProvider } from './providers/AuthProvider';

// localStorage モック
const localStorageMock = (() => {
  let store: Record<string, string> = {};
  return {
    getItem: vi.fn((key: string) => store[key] || null),
    setItem: vi.fn((key: string, value: string) => {
      store[key] = value;
    }),
    removeItem: vi.fn((key: string) => {
      delete store[key];
    }),
    clear: vi.fn(() => {
      store = {};
    }),
  };
})();

Object.defineProperty(window, 'localStorage', {
  value: localStorageMock,
});

const queryClient = new QueryClient({
  defaultOptions: {
    queries: {
      retry: false,
    },
  },
});

const renderApp = (initialRoute = '/') => {
  return render(
    <QueryClientProvider client={queryClient}>
      <MemoryRouter initialEntries={[initialRoute]}>
        <AuthProvider>
          <App />
        </AuthProvider>
      </MemoryRouter>
    </QueryClientProvider>
  );
};

describe('App', () => {
  beforeEach(() => {
    localStorageMock.clear();
    queryClient.clear();
  });

  it('should redirect to login page when not authenticated', async () => {
    renderApp('/');
    await waitFor(() => {
      expect(screen.getByRole('heading', { name: '財務会計システム' })).toBeInTheDocument();
      expect(
        screen.getByText('ログイン', { selector: '.login-page__subtitle' })
      ).toBeInTheDocument();
    });
  });

  it('should render login page at /login route', async () => {
    renderApp('/login');
    await waitFor(() => {
      expect(screen.getByRole('heading', { name: '財務会計システム' })).toBeInTheDocument();
    });
  });

  it('should show login form fields', async () => {
    renderApp('/login');
    await waitFor(() => {
      expect(screen.getByLabelText(/ユーザー名/)).toBeInTheDocument();
      expect(screen.getByLabelText(/パスワード/)).toBeInTheDocument();
      expect(screen.getByRole('button', { name: 'ログイン' })).toBeInTheDocument();
    });
  });
});
