import { describe, it, expect, vi, beforeEach } from 'vitest';
import { render, screen, waitFor } from '@testing-library/react';
import { MemoryRouter } from 'react-router-dom';
import AutoJournalPatternListPage from './AutoJournalPatternListPage';
import { getAutoJournalPatterns } from '../api/getAutoJournalPatterns';

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

vi.mock('../api/getAutoJournalPatterns', () => ({
  getAutoJournalPatterns: vi.fn(),
  getAutoJournalPatternsErrorMessage: (error: unknown) =>
    error instanceof Error ? error.message : '取得エラー',
}));

vi.mock('../views/auto-journal-pattern/AutoJournalPatternList', () => ({
  AutoJournalPatternList: ({ patterns }: { patterns: unknown[] }) => (
    <div data-testid="pattern-list">count={patterns.length}</div>
  ),
}));

const mockGetPatterns = vi.mocked(getAutoJournalPatterns);

describe('AutoJournalPatternListPage', () => {
  beforeEach(() => {
    vi.clearAllMocks();
  });

  it('ページタイトルを表示する', async () => {
    mockGetPatterns.mockResolvedValue([]);
    render(
      <MemoryRouter>
        <AutoJournalPatternListPage />
      </MemoryRouter>
    );

    expect(screen.getByText('自動仕訳パターン一覧')).toBeInTheDocument();
    expect(screen.getByTestId('auto-journal-pattern-list-page')).toBeInTheDocument();
  });

  it('新規登録ボタンを表示する', async () => {
    mockGetPatterns.mockResolvedValue([]);
    render(
      <MemoryRouter>
        <AutoJournalPatternListPage />
      </MemoryRouter>
    );

    expect(screen.getByText('新規登録')).toBeInTheDocument();
  });

  it('データ取得後に一覧を表示する', async () => {
    mockGetPatterns.mockResolvedValue([
      {
        patternId: 1,
        patternCode: 'P001',
        patternName: '売上パターン',
        sourceTableName: 'sales',
        isActive: true,
        items: [],
      },
    ]);
    render(
      <MemoryRouter>
        <AutoJournalPatternListPage />
      </MemoryRouter>
    );

    await waitFor(() => {
      expect(screen.getByTestId('pattern-list')).toHaveTextContent('count=1');
    });
  });

  it('データ取得失敗時にエラーメッセージを表示する', async () => {
    mockGetPatterns.mockRejectedValue(new Error('サーバーエラー'));
    render(
      <MemoryRouter>
        <AutoJournalPatternListPage />
      </MemoryRouter>
    );

    await waitFor(() => {
      expect(screen.getByText('サーバーエラー')).toBeInTheDocument();
    });
  });
});
