import { describe, it, expect, vi } from 'vitest';
import { render, screen } from '@testing-library/react';
import { MemoryRouter, Route, Routes } from 'react-router-dom';
import EditAccountStructurePage from './EditAccountStructurePage';

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
    Loading: ({ message }: { message: string }) => <div>{message}</div>,
  };
});

vi.mock('../views/account-structure/EditAccountStructureForm', () => ({
  EditAccountStructureForm: ({ code }: { code: string }) => (
    <div data-testid="edit-form">code={code}</div>
  ),
}));

const renderWithRoute = (path: string) =>
  render(
    <MemoryRouter initialEntries={[path]}>
      <Routes>
        <Route
          path="/master/account-structures/:code/edit"
          element={<EditAccountStructurePage />}
        />
      </Routes>
    </MemoryRouter>
  );

describe('EditAccountStructurePage', () => {
  it('ページタイトルとフォームを表示する', () => {
    renderWithRoute('/master/account-structures/1000/edit');

    expect(screen.getByText('勘定科目体系 編集')).toBeInTheDocument();
    expect(screen.getByTestId('edit-account-structure-page')).toBeInTheDocument();
    expect(screen.getByTestId('edit-form')).toHaveTextContent('code=1000');
  });
});
