import { describe, it, expect, vi, beforeEach } from 'vitest';
import { render, screen, waitFor } from '@testing-library/react';
import { MemoryRouter, Route, Routes } from 'react-router-dom';
import EditAutoJournalPatternPage from './EditAutoJournalPatternPage';
import { getAutoJournalPattern } from '../api/getAutoJournalPatterns';

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
  getAutoJournalPattern: vi.fn(),
  getAutoJournalPatternsErrorMessage: (error: unknown) =>
    error instanceof Error ? error.message : '取得エラー',
}));

vi.mock('../views/auto-journal-pattern/EditAutoJournalPatternForm', () => ({
  EditAutoJournalPatternForm: ({ pattern }: { pattern: { patternCode: string } }) => (
    <div data-testid="edit-form">code={pattern.patternCode}</div>
  ),
}));

const mockGetPattern = vi.mocked(getAutoJournalPattern);

const renderWithRoute = (path: string) =>
  render(
    <MemoryRouter initialEntries={[path]}>
      <Routes>
        <Route
          path="/master/auto-journal-patterns/:id/edit"
          element={<EditAutoJournalPatternPage />}
        />
      </Routes>
    </MemoryRouter>
  );

describe('EditAutoJournalPatternPage', () => {
  beforeEach(() => {
    vi.clearAllMocks();
  });

  it('ページタイトルを表示する', async () => {
    mockGetPattern.mockResolvedValue({
      patternId: 1,
      patternCode: 'P001',
      patternName: '売上パターン',
      sourceTableName: 'sales',
      isActive: true,
      items: [],
    });
    renderWithRoute('/master/auto-journal-patterns/1/edit');

    expect(screen.getByText('自動仕訳パターン編集')).toBeInTheDocument();
    expect(screen.getByTestId('edit-auto-journal-pattern-page')).toBeInTheDocument();
  });

  it('データ取得後にフォームを表示する', async () => {
    mockGetPattern.mockResolvedValue({
      patternId: 1,
      patternCode: 'P001',
      patternName: '売上パターン',
      sourceTableName: 'sales',
      isActive: true,
      items: [],
    });
    renderWithRoute('/master/auto-journal-patterns/1/edit');

    await waitFor(() => {
      expect(screen.getByTestId('edit-form')).toHaveTextContent('code=P001');
    });
  });

  it('データ取得失敗時にエラーメッセージを表示する', async () => {
    mockGetPattern.mockRejectedValue(new Error('サーバーエラー'));
    renderWithRoute('/master/auto-journal-patterns/1/edit');

    await waitFor(() => {
      expect(screen.getByText('サーバーエラー')).toBeInTheDocument();
    });
  });

  it('読み込み中はローディングを表示する', () => {
    mockGetPattern.mockImplementation(() => new Promise(() => {}));
    renderWithRoute('/master/auto-journal-patterns/1/edit');

    expect(screen.getByText('自動仕訳パターンを読み込み中...')).toBeInTheDocument();
  });
});
