import { describe, it, expect, vi } from 'vitest';
import { render, screen } from '@testing-library/react';
import { MemoryRouter } from 'react-router-dom';
import CreateAccountStructurePage from './CreateAccountStructurePage';

vi.mock('../hooks/useAuth', () => ({
  useAuth: () => ({
    isAuthenticated: true,
    isLoading: false,
    hasRole: (role: string) => role === 'MANAGER' || role === 'ADMIN',
  }),
}));

vi.mock('../views/common', async (importOriginal) => {
  const actual = (await importOriginal()) as Record<string, unknown>;
  return {
    ...actual,
    MainLayout: ({ children }: { children: React.ReactNode }) => <div>{children}</div>,
  };
});

vi.mock('../views/account-structure/CreateAccountStructureForm', () => ({
  CreateAccountStructureForm: () => (
    <div data-testid="create-account-structure-form">form mock</div>
  ),
}));

describe('CreateAccountStructurePage', () => {
  it('ページタイトルとフォームを表示する', () => {
    render(
      <MemoryRouter>
        <CreateAccountStructurePage />
      </MemoryRouter>
    );

    expect(screen.getByText('勘定科目体系 新規登録')).toBeInTheDocument();
    expect(screen.getByTestId('create-account-structure-page')).toBeInTheDocument();
    expect(screen.getByTestId('create-account-structure-form')).toBeInTheDocument();
  });
});
