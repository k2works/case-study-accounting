import React from 'react';
import { describe, it, expect, vi, beforeEach } from 'vitest';
import { render, screen, waitFor } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import UserListPage from './UserListPage';
import { useAuth } from '../hooks/useAuth';
import { useLocation } from 'react-router-dom';
import type { AuthContextType } from '../types/auth';
import { getUsers } from '../api/getUsers';
import { deleteUser } from '../api/deleteUser';

vi.mock('../hooks/useAuth', () => ({
  useAuth: vi.fn(),
}));

vi.mock('../api/getUsers', () => ({
  getUsers: vi.fn(),
  getUsersErrorMessage: (error: unknown) =>
    error instanceof Error ? error.message : 'ユーザー一覧の取得に失敗しました',
}));

vi.mock('../api/deleteUser', () => ({
  deleteUser: vi.fn(),
  getDeleteUserErrorMessage: (error: unknown) =>
    error instanceof Error ? error.message : 'ユーザー削除に失敗しました',
}));

vi.mock('react-router-dom', () => ({
  Navigate: ({ to }: { to: string }) => <div data-testid="navigate" data-to={to} />,
  useLocation: vi.fn(),
  useNavigate: vi.fn(() => vi.fn()),
}));

vi.mock('../views/common', () => ({
  MainLayout: ({ children }: { children: React.ReactNode }) => (
    <div data-testid="main-layout">{children}</div>
  ),
  Loading: ({ message }: { message?: string }) => <div data-testid="loading">{message}</div>,
  ErrorMessage: ({ message, onDismiss }: { message: string; onDismiss?: () => void }) => (
    <div data-testid="error-message">
      <span>{message}</span>
      {onDismiss && <button onClick={onDismiss}>閉じる</button>}
    </div>
  ),
  SuccessNotification: ({ message, onDismiss }: { message: string; onDismiss?: () => void }) => (
    <div data-testid="success-notification">
      <span>{message}</span>
      <button onClick={onDismiss}>閉じる</button>
    </div>
  ),
  ConfirmModal: ({
    isOpen,
    onClose,
    onConfirm,
    title,
    message,
    confirmLabel,
  }: {
    isOpen: boolean;
    onClose: () => void;
    onConfirm: () => void;
    title: string;
    message: string;
    confirmLabel?: string;
  }) =>
    isOpen ? (
      <div data-testid="confirm-modal">
        <h2>{title}</h2>
        <p>{message}</p>
        <button onClick={onClose}>キャンセル</button>
        <button onClick={onConfirm}>{confirmLabel || '確認'}</button>
      </div>
    ) : null,
}));

vi.mock('../views/auth/UserList', () => ({
  UserList: ({
    users,
    onEdit,
    onDelete,
  }: {
    users: Array<{ id: string; displayName: string }>;
    onEdit: (user: { id: string }) => void;
    onDelete: (user: { id: string; displayName: string }) => void;
  }) => (
    <div data-testid="user-list-mock">
      {users.map((user) => (
        <div key={user.id} data-testid={`user-${user.id}`}>
          {user.displayName}
          <button onClick={() => onEdit(user)}>編集</button>
          <button onClick={() => onDelete(user)}>削除</button>
        </div>
      ))}
    </div>
  ),
}));

vi.mock('../views/auth/UserFilter', () => ({
  UserFilter: ({
    values,
    onChange,
    onSearch,
    onReset,
  }: {
    values: { role: string; keyword: string };
    onChange: (values: { role: string; keyword: string }) => void;
    onSearch: () => void;
    onReset: () => void;
  }) => (
    <div data-testid="user-filter-mock">
      <select
        data-testid="filter-role"
        value={values.role}
        onChange={(e) => onChange({ ...values, role: e.target.value })}
      >
        <option value="">すべて</option>
        <option value="ADMIN">管理者</option>
      </select>
      <input
        data-testid="filter-keyword"
        value={values.keyword}
        onChange={(e) => onChange({ ...values, keyword: e.target.value })}
      />
      <button data-testid="filter-search" onClick={onSearch}>
        検索
      </button>
      <button data-testid="filter-reset" onClick={onReset}>
        リセット
      </button>
    </div>
  ),
}));

vi.mock('../views/auth/DeleteUserConfirmDialog', () => ({
  DeleteUserConfirmDialog: ({
    user,
    isOpen,
    onCancel,
    onConfirm,
    isDeleting,
  }: {
    user: { displayName: string } | null;
    isOpen: boolean;
    onCancel: () => void;
    onConfirm: () => void;
    isDeleting?: boolean;
  }) =>
    isOpen && user ? (
      <div data-testid="delete-user-dialog">
        <p>ユーザー「{user.displayName}」を削除しますか？</p>
        <button onClick={onCancel} data-testid="dialog-cancel">
          キャンセル
        </button>
        <button onClick={onConfirm} disabled={isDeleting} data-testid="dialog-confirm">
          {isDeleting ? '削除中...' : '削除確認'}
        </button>
      </div>
    ) : null,
}));

const mockUseAuth = vi.mocked(useAuth);
const mockUseLocation = vi.mocked(useLocation);
const mockGetUsers = vi.mocked(getUsers);
const mockDeleteUser = vi.mocked(deleteUser);

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

const mockUsers = [
  {
    id: 'user-1',
    username: 'admin',
    email: 'admin@example.com',
    displayName: '管理者',
    role: 'ADMIN',
    lastLoginAt: '2024-01-15T10:30:00',
  },
  {
    id: 'user-2',
    username: 'user',
    email: 'user@example.com',
    displayName: 'ユーザー',
    role: 'USER',
    lastLoginAt: null,
  },
];

describe('UserListPage', () => {
  beforeEach(() => {
    vi.clearAllMocks();
    mockUseLocation.mockReturnValue({ state: null } as ReturnType<typeof useLocation>);
    mockGetUsers.mockResolvedValue(mockUsers);
    mockDeleteUser.mockResolvedValue({ success: true });
  });

  it('認証されていない場合はログインページにリダイレクト', () => {
    mockUseAuth.mockReturnValue(
      createMockAuthContext({
        isAuthenticated: false,
        isLoading: false,
      })
    );

    render(<UserListPage />);

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

    render(<UserListPage />);

    expect(screen.getByTestId('loading')).toHaveTextContent('認証情報を確認中...');
  });

  it('ADMIN ロールの場合は正常表示', async () => {
    mockUseAuth.mockReturnValue(
      createMockAuthContext({
        isAuthenticated: true,
        isLoading: false,
        hasRole: vi.fn((role) => role === 'ADMIN'),
      })
    );

    render(<UserListPage />);

    expect(screen.getByTestId('user-list-page')).toBeInTheDocument();
    expect(screen.getByText('ユーザー一覧')).toBeInTheDocument();
    await waitFor(() => expect(screen.getByTestId('user-list-mock')).toBeInTheDocument());
  });

  it('権限がない場合はホームにリダイレクト', () => {
    mockUseAuth.mockReturnValue(
      createMockAuthContext({
        isAuthenticated: true,
        isLoading: false,
        hasRole: vi.fn(() => false),
      })
    );

    render(<UserListPage />);

    const navigate = screen.getByTestId('navigate');
    expect(navigate).toHaveAttribute('data-to', '/');
  });

  describe('ユーザー削除', () => {
    beforeEach(() => {
      mockUseAuth.mockReturnValue(
        createMockAuthContext({
          isAuthenticated: true,
          isLoading: false,
          hasRole: vi.fn((role) => role === 'ADMIN'),
        })
      );
    });

    it('削除ボタンクリックで確認ダイアログが表示される', async () => {
      const user = setupUser();
      render(<UserListPage />);

      await waitFor(() => expect(screen.getByTestId('user-list-mock')).toBeInTheDocument());

      const deleteButtons = await screen.findAllByRole('button', { name: '削除' });
      await user.click(deleteButtons[0]);

      expect(screen.getByTestId('delete-user-dialog')).toBeInTheDocument();
      expect(screen.getByText('ユーザー「管理者」を削除しますか？')).toBeInTheDocument();
    });

    it('削除確認後、APIが呼ばれて成功メッセージが表示される', async () => {
      const user = setupUser();
      render(<UserListPage />);

      await waitFor(() => expect(screen.getByTestId('user-list-mock')).toBeInTheDocument());

      const deleteButtons = await screen.findAllByRole('button', { name: '削除' });
      await user.click(deleteButtons[0]);

      const confirmButton = screen.getByTestId('dialog-confirm');
      await user.click(confirmButton);

      await waitFor(() => {
        expect(mockDeleteUser).toHaveBeenCalledWith('user-1');
      });

      await waitFor(() => {
        expect(screen.getByTestId('success-notification')).toBeInTheDocument();
        expect(screen.getByText('ユーザーを削除しました')).toBeInTheDocument();
      });
    });

    it('削除キャンセルでダイアログが閉じる', async () => {
      const user = setupUser();
      render(<UserListPage />);

      await waitFor(() => expect(screen.getByTestId('user-list-mock')).toBeInTheDocument());

      const deleteButtons = await screen.findAllByRole('button', { name: '削除' });
      await user.click(deleteButtons[0]);

      expect(screen.getByTestId('delete-user-dialog')).toBeInTheDocument();

      const cancelButton = screen.getByRole('button', { name: 'キャンセル' });
      await user.click(cancelButton);

      expect(screen.queryByTestId('delete-user-dialog')).not.toBeInTheDocument();
    });

    it('削除失敗時にエラーメッセージが表示される', async () => {
      mockDeleteUser.mockRejectedValue(new Error('削除に失敗しました'));
      const user = setupUser();
      render(<UserListPage />);

      await waitFor(() => expect(screen.getByTestId('user-list-mock')).toBeInTheDocument());

      const deleteButtons = await screen.findAllByRole('button', { name: '削除' });
      await user.click(deleteButtons[0]);

      const confirmButton = screen.getByTestId('dialog-confirm');
      await user.click(confirmButton);

      await waitFor(() => {
        expect(screen.getByTestId('error-message')).toBeInTheDocument();
        expect(screen.getByText('削除に失敗しました')).toBeInTheDocument();
      });
    });
  });
});
