import { describe, it, expect, vi, beforeEach } from 'vitest';
import { render, screen, waitFor } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { UserEditForm } from './UserEditForm';
import { updateUser } from '../../api/updateUser';

vi.mock('../../api/updateUser', () => ({
  updateUser: vi.fn(),
  getUpdateErrorMessage: (error: unknown) =>
    error instanceof Error ? error.message : 'ユーザー更新に失敗しました',
}));

const mockUpdateUser = vi.mocked(updateUser);

const mockUser = {
  id: 'user-1',
  username: 'testuser',
  email: 'test@example.com',
  displayName: 'テストユーザー',
  role: 'USER',
  lastLoginAt: '2024-01-01T00:00:00Z',
};

const setupUser = () => userEvent.setup();
const displayNameInput = () => screen.getByTestId('user-edit-display-name-input');
const passwordInput = () => screen.getByTestId('user-edit-password-input');
const roleSelect = () => screen.getByTestId('user-edit-role-select');
const submitButton = () => screen.getByTestId('user-edit-submit');
const delay = (ms: number) =>
  new Promise<{ success: boolean }>((resolve) => setTimeout(() => resolve({ success: true }), ms));

describe('UserEditForm', () => {
  const mockOnSuccess = vi.fn();

  beforeEach(() => {
    vi.clearAllMocks();
  });

  it('フォームが正しくレンダリングされる', () => {
    render(<UserEditForm user={mockUser} onSuccess={mockOnSuccess} />);

    expect(screen.getByTestId('user-edit-form')).toBeInTheDocument();
    expect(screen.getByTestId('user-edit-id-input')).toBeInTheDocument();
    expect(screen.getByTestId('user-edit-username-input')).toBeInTheDocument();
    expect(screen.getByTestId('user-edit-email-input')).toBeInTheDocument();
    expect(displayNameInput()).toBeInTheDocument();
    expect(passwordInput()).toBeInTheDocument();
    expect(roleSelect()).toBeInTheDocument();
    expect(submitButton()).toBeInTheDocument();
  });

  it('ユーザー情報が初期値として表示される', () => {
    render(<UserEditForm user={mockUser} onSuccess={mockOnSuccess} />);

    expect(screen.getByTestId('user-edit-id-input')).toHaveValue('user-1');
    expect(screen.getByTestId('user-edit-username-input')).toHaveValue('testuser');
    expect(screen.getByTestId('user-edit-email-input')).toHaveValue('test@example.com');
    expect(displayNameInput()).toHaveValue('テストユーザー');
    expect(roleSelect()).toHaveValue('USER');
  });

  it('読み取り専用フィールドが無効化されている', () => {
    render(<UserEditForm user={mockUser} onSuccess={mockOnSuccess} />);

    expect(screen.getByTestId('user-edit-id-input')).toBeDisabled();
    expect(screen.getByTestId('user-edit-username-input')).toBeDisabled();
    expect(screen.getByTestId('user-edit-email-input')).toBeDisabled();
  });

  it('表示名が空の場合にバリデーションエラーが表示される', async () => {
    const user = setupUser();
    render(<UserEditForm user={mockUser} onSuccess={mockOnSuccess} />);

    await user.clear(displayNameInput());
    await user.click(submitButton());

    expect(screen.getByTestId('user-edit-display-name-error')).toBeInTheDocument();
    expect(screen.getByText('表示名を入力してください')).toBeInTheDocument();
  });

  it('パスワードが8文字未満の場合にバリデーションエラーが表示される', async () => {
    const user = setupUser();
    render(<UserEditForm user={mockUser} onSuccess={mockOnSuccess} />);

    await user.type(passwordInput(), 'short');
    await user.click(submitButton());

    expect(screen.getByTestId('user-edit-password-error')).toBeInTheDocument();
    expect(screen.getByText('パスワードは8文字以上です')).toBeInTheDocument();
  });

  it('パスワードが空でも送信できる（任意項目）', async () => {
    const user = setupUser();
    mockUpdateUser.mockResolvedValue({ success: true });
    render(<UserEditForm user={mockUser} onSuccess={mockOnSuccess} />);

    await user.click(submitButton());

    await waitFor(() => {
      expect(mockUpdateUser).toHaveBeenCalledWith('user-1', {
        displayName: 'テストユーザー',
        role: 'USER',
      });
    });
  });

  it('パスワード付きで更新できる', async () => {
    const user = setupUser();
    mockUpdateUser.mockResolvedValue({ success: true });
    render(<UserEditForm user={mockUser} onSuccess={mockOnSuccess} />);

    await user.type(passwordInput(), 'newpassword123');
    await user.click(submitButton());

    await waitFor(() => {
      expect(mockUpdateUser).toHaveBeenCalledWith('user-1', {
        displayName: 'テストユーザー',
        password: 'newpassword123',
        role: 'USER',
      });
    });
  });

  it('送信中はフォームが無効化される', async () => {
    const user = setupUser();
    mockUpdateUser.mockImplementation(() => delay(100));
    render(<UserEditForm user={mockUser} onSuccess={mockOnSuccess} />);

    await user.click(submitButton());

    expect(displayNameInput()).toBeDisabled();
    expect(passwordInput()).toBeDisabled();
    expect(roleSelect()).toBeDisabled();
    expect(submitButton()).toBeDisabled();
    expect(screen.getByText('更新中...')).toBeInTheDocument();
  });

  it('更新成功時にサクセスメッセージが表示される', async () => {
    const user = setupUser();
    mockUpdateUser.mockResolvedValue({ success: true });
    render(<UserEditForm user={mockUser} onSuccess={mockOnSuccess} />);

    await user.click(submitButton());

    await waitFor(() => {
      expect(screen.getByTestId('user-edit-success')).toBeInTheDocument();
      expect(screen.getByText('ユーザー更新が完了しました')).toBeInTheDocument();
    });
    expect(mockOnSuccess).toHaveBeenCalledWith('ユーザー更新が完了しました');
  });

  it('更新失敗時にエラーメッセージが表示される', async () => {
    const user = setupUser();
    mockUpdateUser.mockResolvedValue({
      success: false,
      errorMessage: '更新権限がありません',
    });
    render(<UserEditForm user={mockUser} onSuccess={mockOnSuccess} />);

    await user.click(submitButton());

    await waitFor(() => {
      expect(screen.getByTestId('user-edit-error')).toBeInTheDocument();
      expect(screen.getByText('更新権限がありません')).toBeInTheDocument();
    });
  });

  it('例外発生時にエラーメッセージが表示される', async () => {
    const user = setupUser();
    mockUpdateUser.mockRejectedValue(new Error('ネットワークエラー'));
    render(<UserEditForm user={mockUser} onSuccess={mockOnSuccess} />);

    await user.click(submitButton());

    await waitFor(() => {
      expect(screen.getByTestId('user-edit-error')).toBeInTheDocument();
      expect(screen.getByText('ネットワークエラー')).toBeInTheDocument();
    });
  });

  it('入力値の変更でバリデーションエラーがクリアされる', async () => {
    const user = setupUser();
    render(<UserEditForm user={mockUser} onSuccess={mockOnSuccess} />);

    await user.clear(displayNameInput());
    await user.click(submitButton());
    expect(screen.getByTestId('user-edit-display-name-error')).toBeInTheDocument();

    await user.type(displayNameInput(), '新しい名前');
    expect(screen.queryByTestId('user-edit-display-name-error')).not.toBeInTheDocument();
  });

  it('ユーザーが変更されると初期値がリセットされる', () => {
    const { rerender } = render(<UserEditForm user={mockUser} onSuccess={mockOnSuccess} />);

    expect(displayNameInput()).toHaveValue('テストユーザー');

    const newUser = {
      ...mockUser,
      id: 'user-2',
      displayName: '別のユーザー',
    };
    rerender(<UserEditForm user={newUser} onSuccess={mockOnSuccess} />);

    expect(displayNameInput()).toHaveValue('別のユーザー');
  });

  it('ロールを変更できる', async () => {
    const user = setupUser();
    mockUpdateUser.mockResolvedValue({ success: true });
    render(<UserEditForm user={mockUser} onSuccess={mockOnSuccess} />);

    await user.selectOptions(roleSelect(), 'ADMIN');
    await user.click(submitButton());

    await waitFor(() => {
      expect(mockUpdateUser).toHaveBeenCalledWith('user-1', {
        displayName: 'テストユーザー',
        role: 'ADMIN',
      });
    });
  });
});
