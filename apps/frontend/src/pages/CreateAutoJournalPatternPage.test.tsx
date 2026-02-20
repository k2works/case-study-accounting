import { describe, it, expect, vi } from 'vitest';
import { render, screen } from '@testing-library/react';
import { MemoryRouter } from 'react-router-dom';
import CreateAutoJournalPatternPage from './CreateAutoJournalPatternPage';

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

vi.mock('../views/auto-journal-pattern/CreateAutoJournalPatternForm', () => ({
  CreateAutoJournalPatternForm: () => (
    <div data-testid="create-auto-journal-pattern-form">form mock</div>
  ),
}));

describe('CreateAutoJournalPatternPage', () => {
  it('ページタイトルとフォームを表示する', () => {
    render(
      <MemoryRouter>
        <CreateAutoJournalPatternPage />
      </MemoryRouter>
    );

    expect(screen.getByText('自動仕訳パターン登録')).toBeInTheDocument();
    expect(screen.getByTestId('create-auto-journal-pattern-page')).toBeInTheDocument();
    expect(screen.getByTestId('create-auto-journal-pattern-form')).toBeInTheDocument();
  });
});
