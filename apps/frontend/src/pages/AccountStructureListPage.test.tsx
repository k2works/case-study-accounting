import { describe, it, expect, vi, beforeEach } from 'vitest';
import { render, screen, waitFor } from '@testing-library/react';
import { MemoryRouter } from 'react-router-dom';
import AccountStructureListPage from './AccountStructureListPage';
import { getAccountStructures } from '../api/getAccountStructures';

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

vi.mock('../api/getAccountStructures', () => ({
  getAccountStructures: vi.fn(),
  getAccountStructuresErrorMessage: (error: unknown) =>
    error instanceof Error ? error.message : '取得エラー',
}));

vi.mock('../views/account-structure/AccountStructureList', () => ({
  AccountStructureList: ({ structures }: { structures: unknown[] }) => (
    <div data-testid="structure-list">count={structures.length}</div>
  ),
}));

const mockGetStructures = vi.mocked(getAccountStructures);

describe('AccountStructureListPage', () => {
  beforeEach(() => {
    vi.clearAllMocks();
  });

  it('ページタイトルを表示する', async () => {
    mockGetStructures.mockResolvedValue([]);
    render(
      <MemoryRouter>
        <AccountStructureListPage />
      </MemoryRouter>
    );

    expect(screen.getByText('勘定科目体系')).toBeInTheDocument();
    expect(screen.getByTestId('account-structure-list-page')).toBeInTheDocument();
  });

  it('新規登録ボタンを表示する', async () => {
    mockGetStructures.mockResolvedValue([]);
    render(
      <MemoryRouter>
        <AccountStructureListPage />
      </MemoryRouter>
    );

    expect(screen.getByText('新規登録')).toBeInTheDocument();
  });

  it('データ取得後に一覧を表示する', async () => {
    mockGetStructures.mockResolvedValue([
      {
        accountCode: '1000',
        accountName: '現金',
        accountPath: '/1000',
        hierarchyLevel: 1,
        parentAccountCode: null,
        displayOrder: 1,
      },
    ]);
    render(
      <MemoryRouter>
        <AccountStructureListPage />
      </MemoryRouter>
    );

    await waitFor(() => {
      expect(screen.getByTestId('structure-list')).toHaveTextContent('count=1');
    });
  });

  it('データ取得失敗時にエラーメッセージを表示する', async () => {
    mockGetStructures.mockRejectedValue(new Error('サーバーエラー'));
    render(
      <MemoryRouter>
        <AccountStructureListPage />
      </MemoryRouter>
    );

    await waitFor(() => {
      expect(screen.getByText('サーバーエラー')).toBeInTheDocument();
    });
  });
});
