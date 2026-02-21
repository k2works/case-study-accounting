import React from 'react';
import { describe, it, expect, vi, beforeEach } from 'vitest';
import { render, screen, waitFor } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import CreateJournalEntryPage from './CreateJournalEntryPage';
import { useAuth } from '../hooks/useAuth';
import { useNavigate } from 'react-router-dom';
import type { AuthContextType } from '../types/auth';
import { getAccounts } from '../api/getAccounts';
import { createJournalEntry } from '../api/createJournalEntry';
import type { CreateJournalEntryRequest } from '../api/createJournalEntry';

vi.mock('../hooks/useAuth', () => ({
  useAuth: vi.fn(),
}));

vi.mock('../api/getAccounts', () => ({
  getAccounts: vi.fn(),
  getAccountsErrorMessage: (error: unknown) =>
    error instanceof Error ? error.message : '勘定科目一覧の取得に失敗しました',
}));

vi.mock('../api/createJournalEntry', () => ({
  createJournalEntry: vi.fn(),
  createJournalEntryErrorMessage: (error: unknown) =>
    error instanceof Error ? error.message : '仕訳の登録に失敗しました',
}));

vi.mock('react-router-dom', () => ({
  Navigate: ({ to }: { to: string }) => <div data-testid="navigate" data-to={to} />,
  useNavigate: vi.fn(),
}));

vi.mock('../views/common', () => ({
  MainLayout: ({ children }: { children: React.ReactNode }) => (
    <div data-testid="main-layout">{children}</div>
  ),
  Loading: ({ message }: { message?: string }) => <div data-testid="loading">{message}</div>,
  SuccessNotification: ({ message }: { message: string }) => (
    <div data-testid="success-notification">{message}</div>
  ),
  ErrorMessage: ({ message }: { message: string }) => (
    <div data-testid="error-message">{message}</div>
  ),
  Button: ({ children, ...props }: React.ButtonHTMLAttributes<HTMLButtonElement>) => (
    <button {...props}>{children}</button>
  ),
}));

vi.mock('../views/journal/AutoJournalGenerateDialog', () => ({
  AutoJournalGenerateDialog: ({ isOpen }: { isOpen: boolean }) =>
    isOpen ? <div data-testid="auto-journal-dialog">dialog open</div> : null,
}));

const mockRequest: CreateJournalEntryRequest = {
  journalDate: '2024-01-31',
  description: '売上計上',
  lines: [
    { lineNumber: 1, accountId: 1, debitAmount: 1000 },
    { lineNumber: 2, accountId: 2, creditAmount: 1000 },
  ],
};

vi.mock('../views/journal/JournalEntryForm', () => ({
  JournalEntryForm: ({
    onSubmit,
    onCancel,
    error,
  }: {
    onSubmit: (data: CreateJournalEntryRequest) => void;
    onCancel: () => void;
    error?: string;
  }) => (
    <div data-testid="journal-entry-form">
      {error && <div data-testid="journal-entry-form-error">{error}</div>}
      <button onClick={() => onSubmit(mockRequest)}>submit</button>
      <button onClick={onCancel}>cancel</button>
    </div>
  ),
}));

const mockUseAuth = vi.mocked(useAuth);
const mockUseNavigate = vi.mocked(useNavigate);
const mockGetAccounts = vi.mocked(getAccounts);
const mockCreateJournalEntry = vi.mocked(createJournalEntry);

const setupUser = () => userEvent.setup();

const createMockAuthContext = (overrides: Partial<AuthContextType> = {}): AuthContextType => ({
  user: null,
  isAuthenticated: false,
  isLoading: false,
  login: vi.fn(),
  logout: vi.fn(),
  hasRole: vi.fn(() => false),
  ...overrides,
});

describe('CreateJournalEntryPage', () => {
  beforeEach(() => {
    vi.clearAllMocks();
    mockGetAccounts.mockResolvedValue([]);
    mockCreateJournalEntry.mockResolvedValue({ success: true });
    mockUseNavigate.mockReturnValue(vi.fn());
  });

  it('認証されていない場合はログインページにリダイレクト', () => {
    mockUseAuth.mockReturnValue(
      createMockAuthContext({
        isAuthenticated: false,
        isLoading: false,
      })
    );

    render(<CreateJournalEntryPage />);

    const navigate = screen.getByTestId('navigate');
    expect(navigate).toHaveAttribute('data-to', '/login');
  });

  it('ローディング中の表示', () => {
    mockUseAuth.mockReturnValue(
      createMockAuthContext({
        isAuthenticated: false,
        isLoading: true,
      })
    );

    render(<CreateJournalEntryPage />);

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

    render(<CreateJournalEntryPage />);

    const navigate = screen.getByTestId('navigate');
    expect(navigate).toHaveAttribute('data-to', '/');
  });

  it('仕訳入力フォームを表示し送信できる', async () => {
    const navigate = vi.fn();
    mockUseNavigate.mockReturnValue(navigate);
    mockUseAuth.mockReturnValue(
      createMockAuthContext({
        isAuthenticated: true,
        isLoading: false,
        hasRole: vi.fn((role) => role === 'USER'),
      })
    );

    render(<CreateJournalEntryPage />);

    expect(screen.getByTestId('create-journal-entry-page')).toBeInTheDocument();
    expect(screen.getByText('仕訳入力')).toBeInTheDocument();

    const user = setupUser();
    await user.click(screen.getByText('submit'));

    await waitFor(() => {
      expect(mockCreateJournalEntry).toHaveBeenCalledWith(mockRequest);
    });
    // 成功メッセージが表示されることを確認（遷移は setTimeout で行われるため直接テストしない）
    await waitFor(() => {
      expect(screen.getByTestId('success-notification')).toHaveTextContent(
        '仕訳登録が完了しました'
      );
    });
  });

  it('ADMIN ユーザーは 自動仕訳 ボタンを表示しダイアログを開ける', async () => {
    mockUseAuth.mockReturnValue(
      createMockAuthContext({
        user: { username: 'admin', role: 'ADMIN' },
        isAuthenticated: true,
        isLoading: false,
        hasRole: vi.fn((role) => role === 'ADMIN'),
      })
    );

    render(<CreateJournalEntryPage />);

    const user = setupUser();
    expect(screen.getByTestId('auto-journal-button')).toBeInTheDocument();
    await user.click(screen.getByTestId('auto-journal-button'));

    expect(screen.getByTestId('auto-journal-dialog')).toBeInTheDocument();
  });

  it('USER ロールは 自動仕訳 ボタンを表示しない', async () => {
    mockUseAuth.mockReturnValue(
      createMockAuthContext({
        user: { username: 'user', role: 'USER' },
        isAuthenticated: true,
        isLoading: false,
        hasRole: vi.fn((role) => role === 'USER'),
      })
    );

    render(<CreateJournalEntryPage />);

    expect(screen.queryByTestId('auto-journal-button')).not.toBeInTheDocument();
    await waitFor(() => {
      expect(mockGetAccounts).toHaveBeenCalled();
    });
  });

  it('勘定科目取得エラー時に ErrorMessage を表示', async () => {
    mockGetAccounts.mockRejectedValue(new Error('勘定科目 API エラー'));
    mockUseAuth.mockReturnValue(
      createMockAuthContext({
        user: { username: 'user', role: 'USER' },
        isAuthenticated: true,
        isLoading: false,
        hasRole: vi.fn((role) => role === 'USER'),
      })
    );

    render(<CreateJournalEntryPage />);

    await waitFor(() => {
      expect(screen.getByTestId('error-message')).toHaveTextContent('勘定科目 API エラー');
    });
  });

  it('仕訳送信エラー時にフォームへエラーを表示', async () => {
    mockCreateJournalEntry.mockResolvedValue({
      success: false,
      errorMessage: '仕訳の登録に失敗しました',
    });
    mockUseAuth.mockReturnValue(
      createMockAuthContext({
        user: { username: 'user', role: 'USER' },
        isAuthenticated: true,
        isLoading: false,
        hasRole: vi.fn((role) => role === 'USER'),
      })
    );

    render(<CreateJournalEntryPage />);

    const user = setupUser();
    await user.click(screen.getByText('submit'));

    await waitFor(() => {
      expect(screen.getByTestId('journal-entry-form-error')).toHaveTextContent(
        '仕訳の登録に失敗しました'
      );
    });
  });

  it('キャンセルボタン押下で前画面へ戻る', async () => {
    const navigate = vi.fn();
    mockUseNavigate.mockReturnValue(navigate);
    mockUseAuth.mockReturnValue(
      createMockAuthContext({
        user: { username: 'user', role: 'USER' },
        isAuthenticated: true,
        isLoading: false,
        hasRole: vi.fn((role) => role === 'USER'),
      })
    );

    render(<CreateJournalEntryPage />);

    const user = setupUser();
    await user.click(screen.getByText('cancel'));

    expect(navigate).toHaveBeenCalledWith(-1);
  });
});
