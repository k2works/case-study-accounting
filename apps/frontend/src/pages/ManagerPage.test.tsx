import { describe, it, expect, vi } from 'vitest';
import { render, screen } from '@testing-library/react';
import { MemoryRouter } from 'react-router-dom';
import { ManagerPage } from './ManagerPage';

const mockUseAuth = vi.fn();
vi.mock('../hooks/useAuth', () => ({
  useAuth: () => mockUseAuth(),
}));

vi.mock('../views/common', () => ({
  MainLayout: ({ children }: { children: React.ReactNode }) => (
    <div data-testid="main-layout">{children}</div>
  ),
  Loading: ({ message }: { message: string }) => <div data-testid="loading">{message}</div>,
}));

const renderWithRouter = (ui: React.ReactElement) => render(<MemoryRouter>{ui}</MemoryRouter>);

const breadcrumbs = [{ label: 'ホーム' }, { label: 'テスト' }];

describe('ManagerPage', () => {
  it('認証読み込み中はローディングを表示する', () => {
    mockUseAuth.mockReturnValue({ isAuthenticated: false, isLoading: true, hasRole: () => false });
    renderWithRouter(
      <ManagerPage breadcrumbs={breadcrumbs}>
        <div>content</div>
      </ManagerPage>
    );

    expect(screen.getByTestId('loading')).toBeInTheDocument();
  });

  it('未認証の場合 /login にリダイレクトする', () => {
    mockUseAuth.mockReturnValue({ isAuthenticated: false, isLoading: false, hasRole: () => false });
    renderWithRouter(
      <ManagerPage breadcrumbs={breadcrumbs}>
        <div>content</div>
      </ManagerPage>
    );

    expect(screen.queryByText('content')).not.toBeInTheDocument();
  });

  it('権限がない場合コンテンツを表示しない', () => {
    mockUseAuth.mockReturnValue({ isAuthenticated: true, isLoading: false, hasRole: () => false });
    renderWithRouter(
      <ManagerPage breadcrumbs={breadcrumbs}>
        <div>content</div>
      </ManagerPage>
    );

    expect(screen.queryByText('content')).not.toBeInTheDocument();
  });

  it('ADMIN 権限がある場合コンテンツを表示する', () => {
    mockUseAuth.mockReturnValue({
      isAuthenticated: true,
      isLoading: false,
      hasRole: (role: string) => role === 'ADMIN',
    });
    renderWithRouter(
      <ManagerPage breadcrumbs={breadcrumbs}>
        <div>content</div>
      </ManagerPage>
    );

    expect(screen.getByText('content')).toBeInTheDocument();
  });

  it('MANAGER 権限がある場合コンテンツを表示する', () => {
    mockUseAuth.mockReturnValue({
      isAuthenticated: true,
      isLoading: false,
      hasRole: (role: string) => role === 'MANAGER',
    });
    renderWithRouter(
      <ManagerPage breadcrumbs={breadcrumbs}>
        <div>content</div>
      </ManagerPage>
    );

    expect(screen.getByText('content')).toBeInTheDocument();
  });
});
