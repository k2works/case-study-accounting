import React from 'react';
import { describe, it, expect, vi, beforeEach } from 'vitest';
import { render, screen, waitFor } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import JournalEntryListPage from './JournalEntryListPage';
import { useAuth } from '../hooks/useAuth';
import { useNavigate, useLocation } from 'react-router-dom';
import { createMockAuthContext } from '../test/testUtils';
import { searchJournalEntries } from '../api/searchJournalEntries';
import type { GetJournalEntriesResult } from '../api/getJournalEntries';

vi.mock('../hooks/useAuth', () => ({
  useAuth: vi.fn(),
}));

vi.mock('../api/searchJournalEntries', () => ({
  searchJournalEntries: vi.fn(),
  searchJournalEntriesErrorMessage: (error: unknown) =>
    error instanceof Error ? error.message : '仕訳の検索に失敗しました',
}));

vi.mock('react-router-dom', () => ({
  Navigate: ({ to }: { to: string }) => <div data-testid="navigate" data-to={to} />,
  useNavigate: vi.fn(),
  useLocation: vi.fn(),
}));

vi.mock('../views/common', () => ({
  MainLayout: ({ children }: { children: React.ReactNode }) => (
    <div data-testid="main-layout">{children}</div>
  ),
  Loading: ({ message }: { message?: string }) => <div data-testid="loading">{message}</div>,
  SuccessNotification: ({ message, onDismiss }: { message: string; onDismiss: () => void }) => (
    <button data-testid="success-notification" onClick={onDismiss}>
      {message}
    </button>
  ),
  ErrorMessage: ({ message, onRetry }: { message: string; onRetry?: () => void }) => (
    <button data-testid="error-message" onClick={onRetry}>
      {message}
    </button>
  ),
  Button: ({
    children,
    onClick,
    ...props
  }: {
    children: React.ReactNode;
    onClick?: () => void;
    variant?: string;
  }) => (
    <button onClick={onClick} {...props}>
      {children}
    </button>
  ),
}));

vi.mock('../views/journal/JournalEntryList', () => ({
  JournalEntryList: ({
    entries,
    onSearch,
    onReset,
    onPageChange,
    onItemsPerPageChange,
  }: {
    entries: unknown[];
    onSearch: () => void;
    onReset: () => void;
    onPageChange: (page: number) => void;
    onItemsPerPageChange: (size: number) => void;
  }) => (
    <div data-testid="journal-entry-list">
      <span data-testid="entry-count">{entries.length}</span>
      <button data-testid="search-btn" onClick={onSearch}>
        検索
      </button>
      <button data-testid="reset-btn" onClick={onReset}>
        リセット
      </button>
      <button data-testid="page-change-btn" onClick={() => onPageChange(2)}>
        次ページ
      </button>
      <button data-testid="items-per-page-btn" onClick={() => onItemsPerPageChange(50)}>
        50件表示
      </button>
    </div>
  ),
}));

const mockUseAuth = vi.mocked(useAuth);
const mockUseNavigate = vi.mocked(useNavigate);
const mockUseLocation = vi.mocked(useLocation);
const mockSearchJournalEntries = vi.mocked(searchJournalEntries);

const createMockResult = (
  overrides: Partial<GetJournalEntriesResult> = {}
): GetJournalEntriesResult => ({
  content: [],
  page: 0,
  size: 20,
  totalElements: 0,
  totalPages: 0,
  ...overrides,
});

describe('JournalEntryListPage', () => {
  beforeEach(() => {
    vi.clearAllMocks();
    mockUseNavigate.mockReturnValue(vi.fn());
    mockUseLocation.mockReturnValue({
      pathname: '/journal/entries',
      search: '',
      hash: '',
      state: null,
      key: 'default',
    });
    mockSearchJournalEntries.mockResolvedValue(createMockResult());
  });

  it('認証されていない場合はログインページにリダイレクト', () => {
    mockUseAuth.mockReturnValue(
      createMockAuthContext({ isAuthenticated: false, isLoading: false })
    );

    render(<JournalEntryListPage />);

    const navigate = screen.getByTestId('navigate');
    expect(navigate).toHaveAttribute('data-to', '/login');
  });

  it('ローディング中の表示', () => {
    mockUseAuth.mockReturnValue(createMockAuthContext({ isAuthenticated: false, isLoading: true }));

    render(<JournalEntryListPage />);

    expect(screen.getByTestId('loading')).toHaveTextContent('認証情報を確認中...');
  });

  it('権限がない場合はホームにリダイレクト', () => {
    mockUseAuth.mockReturnValue(
      createMockAuthContext({
        isAuthenticated: true,
        isLoading: false,
        hasRole: vi.fn(() => false),
      })
    );

    render(<JournalEntryListPage />);

    const navigate = screen.getByTestId('navigate');
    expect(navigate).toHaveAttribute('data-to', '/');
  });

  it('仕訳一覧ページを表示する', async () => {
    mockUseAuth.mockReturnValue(
      createMockAuthContext({
        isAuthenticated: true,
        isLoading: false,
        hasRole: vi.fn((role) => role === 'USER'),
      })
    );
    mockSearchJournalEntries.mockResolvedValue(
      createMockResult({
        content: [
          {
            journalEntryId: 1,
            journalDate: '2024-01-01',
            description: 'テスト仕訳',
            totalDebitAmount: 1000,
            totalCreditAmount: 1000,
            status: 'DRAFT',
            version: 1,
          },
        ],
        totalElements: 1,
        totalPages: 1,
      })
    );

    render(<JournalEntryListPage />);

    await waitFor(() => {
      expect(screen.getByTestId('journal-entry-list')).toBeInTheDocument();
    });
    expect(screen.getByText('仕訳一覧')).toBeInTheDocument();
    expect(screen.getByText('新規作成')).toBeInTheDocument();
  });

  it('新規作成ボタンで仕訳作成ページに遷移する', async () => {
    const navigate = vi.fn();
    mockUseNavigate.mockReturnValue(navigate);
    mockUseAuth.mockReturnValue(
      createMockAuthContext({
        isAuthenticated: true,
        isLoading: false,
        hasRole: vi.fn((role) => role === 'USER'),
      })
    );

    render(<JournalEntryListPage />);

    await waitFor(() => {
      expect(screen.getByText('新規作成')).toBeInTheDocument();
    });

    const user = userEvent.setup();
    await user.click(screen.getByText('新規作成'));

    expect(navigate).toHaveBeenCalledWith('/journal/entries/new');
  });

  it('location state からの成功メッセージを表示する', async () => {
    mockUseAuth.mockReturnValue(
      createMockAuthContext({
        isAuthenticated: true,
        isLoading: false,
        hasRole: vi.fn((role) => role === 'USER'),
      })
    );
    mockUseLocation.mockReturnValue({
      pathname: '/journal/entries',
      search: '',
      hash: '',
      state: { successMessage: '仕訳を登録しました' },
      key: 'default',
    });

    render(<JournalEntryListPage />);

    await waitFor(() => {
      expect(screen.getByTestId('success-notification')).toHaveTextContent('仕訳を登録しました');
    });
  });

  it('API エラー時にエラーメッセージを表示する', async () => {
    mockUseAuth.mockReturnValue(
      createMockAuthContext({
        isAuthenticated: true,
        isLoading: false,
        hasRole: vi.fn((role) => role === 'USER'),
      })
    );
    mockSearchJournalEntries.mockRejectedValue(new Error('ネットワークエラー'));

    render(<JournalEntryListPage />);

    await waitFor(() => {
      expect(screen.getByTestId('error-message')).toHaveTextContent('ネットワークエラー');
    });
  });

  it('検索を実行する', async () => {
    mockUseAuth.mockReturnValue(
      createMockAuthContext({
        isAuthenticated: true,
        isLoading: false,
        hasRole: vi.fn((role) => role === 'USER'),
      })
    );

    render(<JournalEntryListPage />);

    await waitFor(() => {
      expect(screen.getByTestId('journal-entry-list')).toBeInTheDocument();
    });

    const user = userEvent.setup();
    await user.click(screen.getByTestId('search-btn'));

    expect(mockSearchJournalEntries).toHaveBeenCalledTimes(2); // initial + search
  });

  it('リセットを実行する', async () => {
    mockUseAuth.mockReturnValue(
      createMockAuthContext({
        isAuthenticated: true,
        isLoading: false,
        hasRole: vi.fn((role) => role === 'USER'),
      })
    );

    render(<JournalEntryListPage />);

    await waitFor(() => {
      expect(screen.getByTestId('journal-entry-list')).toBeInTheDocument();
    });

    const user = userEvent.setup();
    await user.click(screen.getByTestId('reset-btn'));

    expect(mockSearchJournalEntries).toHaveBeenCalledTimes(2); // initial + reset
  });

  it('ページ変更を実行する', async () => {
    mockUseAuth.mockReturnValue(
      createMockAuthContext({
        isAuthenticated: true,
        isLoading: false,
        hasRole: vi.fn((role) => role === 'USER'),
      })
    );

    render(<JournalEntryListPage />);

    await waitFor(() => {
      expect(screen.getByTestId('journal-entry-list')).toBeInTheDocument();
    });

    const user = userEvent.setup();
    await user.click(screen.getByTestId('page-change-btn'));

    expect(mockSearchJournalEntries).toHaveBeenCalledTimes(2);
  });
});
