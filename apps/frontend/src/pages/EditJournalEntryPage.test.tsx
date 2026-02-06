import React from 'react';
import { describe, it, expect, vi, beforeEach } from 'vitest';
import { render, screen, waitFor } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import EditJournalEntryPage from './EditJournalEntryPage';
import { useAuth } from '../hooks/useAuth';
import { useNavigate, useParams } from 'react-router-dom';
import { createMockAuthContext } from '../test/testUtils';
import { getAccounts } from '../api/getAccounts';
import { getJournalEntry } from '../api/getJournalEntry';
import type { JournalEntry } from '../api/getJournalEntry';
import { updateJournalEntry } from '../api/updateJournalEntry';
import type { UpdateJournalEntryRequest } from '../api/updateJournalEntry';
import { deleteJournalEntry } from '../api/deleteJournalEntry';

vi.mock('../hooks/useAuth', () => ({
  useAuth: vi.fn(),
}));

vi.mock('../api/getAccounts', () => ({
  getAccounts: vi.fn(),
  getAccountsErrorMessage: (error: unknown) =>
    error instanceof Error ? error.message : '勘定科目一覧の取得に失敗しました',
}));

vi.mock('../api/getJournalEntry', () => ({
  getJournalEntry: vi.fn(),
  getJournalEntryErrorMessage: (error: unknown) =>
    error instanceof Error ? error.message : '仕訳の取得に失敗しました',
}));

vi.mock('../api/updateJournalEntry', () => ({
  updateJournalEntry: vi.fn(),
  updateJournalEntryErrorMessage: (error: unknown) =>
    error instanceof Error ? error.message : '仕訳の更新に失敗しました',
}));

vi.mock('../api/deleteJournalEntry', () => ({
  deleteJournalEntry: vi.fn(),
  deleteJournalEntryErrorMessage: (error: unknown) =>
    error instanceof Error ? error.message : '仕訳の削除に失敗しました',
}));

vi.mock('react-router-dom', () => ({
  Navigate: ({ to }: { to: string }) => <div data-testid="navigate" data-to={to} />,
  useNavigate: vi.fn(),
  useParams: vi.fn(),
}));

vi.mock('../views/common', () => ({
  MainLayout: ({ children }: { children: React.ReactNode }) => (
    <div data-testid="main-layout">{children}</div>
  ),
  Loading: ({ message }: { message?: string }) => <div data-testid="loading">{message}</div>,
  ErrorMessage: ({
    message,
    onRetry,
    onDismiss,
  }: {
    message: string;
    onRetry?: () => void;
    onDismiss?: () => void;
  }) => (
    <button data-testid="error-message" onClick={onRetry || onDismiss}>
      {message}
    </button>
  ),
  ConfirmModal: ({
    isOpen,
    onConfirm,
    onClose,
    message,
  }: {
    isOpen: boolean;
    onConfirm: () => void;
    onClose: () => void;
    title: string;
    message: string;
    confirmLabel: string;
    cancelLabel: string;
    isDestructive?: boolean;
  }) =>
    isOpen ? (
      <div data-testid="confirm-modal">
        <span>{message}</span>
        <button data-testid="confirm-btn" onClick={onConfirm}>
          削除
        </button>
        <button data-testid="cancel-btn" onClick={onClose}>
          キャンセル
        </button>
      </div>
    ) : null,
}));

const mockSubmitRequest: UpdateJournalEntryRequest = {
  journalDate: '2024-01-31',
  description: '更新テスト',
  version: 1,
  lines: [
    { lineNumber: 1, accountId: 1, debitAmount: 1000 },
    { lineNumber: 2, accountId: 2, creditAmount: 1000 },
  ],
};

vi.mock('../views/journal/JournalEntryEditForm', () => ({
  JournalEntryEditForm: ({
    onSubmit,
    onCancel,
    onDelete,
  }: {
    onSubmit: (data: UpdateJournalEntryRequest) => void;
    onCancel: () => void;
    onDelete?: () => void;
  }) => (
    <div data-testid="journal-entry-edit-form">
      <button onClick={() => onSubmit(mockSubmitRequest)}>保存</button>
      <button onClick={onCancel}>キャンセル</button>
      {onDelete && <button onClick={onDelete}>削除</button>}
    </div>
  ),
}));

const mockUseAuth = vi.mocked(useAuth);
const mockUseNavigate = vi.mocked(useNavigate);
const mockUseParams = vi.mocked(useParams);
const mockGetAccounts = vi.mocked(getAccounts);
const mockGetJournalEntry = vi.mocked(getJournalEntry);
const mockUpdateJournalEntry = vi.mocked(updateJournalEntry);
const mockDeleteJournalEntry = vi.mocked(deleteJournalEntry);

const mockJournalEntry: JournalEntry = {
  journalEntryId: 1,
  journalDate: '2024-01-31',
  description: 'テスト仕訳',
  status: 'DRAFT',
  version: 1,
  lines: [
    {
      lineNumber: 1,
      accountId: 1,
      accountCode: '100',
      accountName: '現金',
      debitAmount: 1000,
    },
    {
      lineNumber: 2,
      accountId: 2,
      accountCode: '400',
      accountName: '売上',
      creditAmount: 1000,
    },
  ],
};

describe('EditJournalEntryPage', () => {
  beforeEach(() => {
    vi.clearAllMocks();
    mockUseNavigate.mockReturnValue(vi.fn());
    mockUseParams.mockReturnValue({ id: '1' });
    mockGetAccounts.mockResolvedValue([
      { accountId: 1, accountCode: '100', accountName: '現金', accountType: 'ASSET' },
      { accountId: 2, accountCode: '400', accountName: '売上', accountType: 'REVENUE' },
    ]);
    mockGetJournalEntry.mockResolvedValue(mockJournalEntry);
    mockUpdateJournalEntry.mockResolvedValue({ success: true });
    mockDeleteJournalEntry.mockResolvedValue({ success: true, message: '仕訳を削除しました' });
  });

  it('認証されていない場合はログインページにリダイレクト', () => {
    mockUseAuth.mockReturnValue(
      createMockAuthContext({ isAuthenticated: false, isLoading: false })
    );

    render(<EditJournalEntryPage />);

    const navigate = screen.getByTestId('navigate');
    expect(navigate).toHaveAttribute('data-to', '/login');
  });

  it('ローディング中の表示', () => {
    mockUseAuth.mockReturnValue(createMockAuthContext({ isAuthenticated: false, isLoading: true }));

    render(<EditJournalEntryPage />);

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

    render(<EditJournalEntryPage />);

    const navigate = screen.getByTestId('navigate');
    expect(navigate).toHaveAttribute('data-to', '/');
  });

  it('仕訳編集フォームを表示する', async () => {
    mockUseAuth.mockReturnValue(
      createMockAuthContext({
        isAuthenticated: true,
        isLoading: false,
        hasRole: vi.fn((role) => role === 'USER'),
      })
    );

    render(<EditJournalEntryPage />);

    await waitFor(() => {
      expect(screen.getByTestId('journal-entry-edit-form')).toBeInTheDocument();
    });
    expect(screen.getByText('仕訳編集')).toBeInTheDocument();
  });

  it('仕訳を更新できる', async () => {
    const navigate = vi.fn();
    mockUseNavigate.mockReturnValue(navigate);
    mockUseAuth.mockReturnValue(
      createMockAuthContext({
        isAuthenticated: true,
        isLoading: false,
        hasRole: vi.fn((role) => role === 'USER'),
      })
    );

    render(<EditJournalEntryPage />);

    await waitFor(() => {
      expect(screen.getByTestId('journal-entry-edit-form')).toBeInTheDocument();
    });

    const user = userEvent.setup();
    await user.click(screen.getByText('保存'));

    await waitFor(() => {
      expect(mockUpdateJournalEntry).toHaveBeenCalledWith(1, mockSubmitRequest);
    });
    await waitFor(() => {
      expect(navigate).toHaveBeenCalledWith('/', {
        replace: true,
        state: { successMessage: '仕訳を更新しました' },
      });
    });
  });

  it('削除確認ダイアログを表示し、削除を実行する', async () => {
    const navigate = vi.fn();
    mockUseNavigate.mockReturnValue(navigate);
    mockUseAuth.mockReturnValue(
      createMockAuthContext({
        isAuthenticated: true,
        isLoading: false,
        hasRole: vi.fn((role) => role === 'USER'),
      })
    );

    render(<EditJournalEntryPage />);

    await waitFor(() => {
      expect(screen.getByTestId('journal-entry-edit-form')).toBeInTheDocument();
    });

    const user = userEvent.setup();
    await user.click(screen.getByText('削除'));

    await waitFor(() => {
      expect(screen.getByTestId('confirm-modal')).toBeInTheDocument();
    });

    await user.click(screen.getByTestId('confirm-btn'));

    await waitFor(() => {
      expect(mockDeleteJournalEntry).toHaveBeenCalledWith(1);
    });
    await waitFor(() => {
      expect(navigate).toHaveBeenCalledWith('/', {
        replace: true,
        state: { successMessage: '仕訳を削除しました' },
      });
    });
  });

  it('削除確認ダイアログをキャンセルする', async () => {
    mockUseAuth.mockReturnValue(
      createMockAuthContext({
        isAuthenticated: true,
        isLoading: false,
        hasRole: vi.fn((role) => role === 'USER'),
      })
    );

    render(<EditJournalEntryPage />);

    await waitFor(() => {
      expect(screen.getByTestId('journal-entry-edit-form')).toBeInTheDocument();
    });

    const user = userEvent.setup();
    await user.click(screen.getByText('削除'));

    await waitFor(() => {
      expect(screen.getByTestId('confirm-modal')).toBeInTheDocument();
    });

    await user.click(screen.getByTestId('cancel-btn'));

    await waitFor(() => {
      expect(screen.queryByTestId('confirm-modal')).not.toBeInTheDocument();
    });
  });

  it('仕訳取得エラー時にエラーメッセージを表示する', async () => {
    mockUseAuth.mockReturnValue(
      createMockAuthContext({
        isAuthenticated: true,
        isLoading: false,
        hasRole: vi.fn((role) => role === 'USER'),
      })
    );
    mockGetJournalEntry.mockRejectedValue(new Error('仕訳が見つかりません'));

    render(<EditJournalEntryPage />);

    await waitFor(() => {
      expect(screen.getByTestId('error-message')).toHaveTextContent('仕訳が見つかりません');
    });
  });

  it('無効な ID の場合にエラーメッセージを表示する', async () => {
    mockUseParams.mockReturnValue({ id: 'invalid' });
    mockUseAuth.mockReturnValue(
      createMockAuthContext({
        isAuthenticated: true,
        isLoading: false,
        hasRole: vi.fn((role) => role === 'USER'),
      })
    );

    render(<EditJournalEntryPage />);

    await waitFor(() => {
      expect(screen.getByTestId('error-message')).toHaveTextContent('仕訳が見つかりません');
    });
  });

  it('非下書き仕訳の場合は編集不可メッセージを表示する', async () => {
    mockUseAuth.mockReturnValue(
      createMockAuthContext({
        isAuthenticated: true,
        isLoading: false,
        hasRole: vi.fn((role) => role === 'USER'),
      })
    );
    mockGetJournalEntry.mockResolvedValue({
      ...mockJournalEntry,
      status: 'APPROVED',
    });

    render(<EditJournalEntryPage />);

    await waitFor(() => {
      expect(screen.getByTestId('error-message')).toHaveTextContent(
        '下書き状態の仕訳のみ編集できます'
      );
    });
  });
});
