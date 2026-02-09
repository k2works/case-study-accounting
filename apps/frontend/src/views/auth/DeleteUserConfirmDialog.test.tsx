import { describe, it, expect, vi, beforeEach } from 'vitest';
import { render, screen } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { DeleteUserConfirmDialog } from './DeleteUserConfirmDialog';
import type { User } from '../../api/getUsers';

const mockUser: User = {
  id: 'user-1',
  username: 'testuser',
  email: 'test@example.com',
  displayName: 'テストユーザー',
  role: 'USER',
  lastLoginAt: '2024-01-01T00:00:00Z',
};

describe('DeleteUserConfirmDialog', () => {
  const mockOnCancel = vi.fn();
  const mockOnConfirm = vi.fn();

  beforeEach(() => {
    vi.clearAllMocks();
  });

  it('ダイアログが開いている場合に表示される', () => {
    render(
      <DeleteUserConfirmDialog
        user={mockUser}
        isOpen={true}
        onCancel={mockOnCancel}
        onConfirm={mockOnConfirm}
      />
    );

    expect(screen.getByText('ユーザーの削除')).toBeInTheDocument();
    expect(
      screen.getByText(/ユーザー「テストユーザー」（testuser）を削除しますか？/)
    ).toBeInTheDocument();
  });

  it('ダイアログが閉じている場合は非表示', () => {
    render(
      <DeleteUserConfirmDialog
        user={mockUser}
        isOpen={false}
        onCancel={mockOnCancel}
        onConfirm={mockOnConfirm}
      />
    );

    expect(screen.queryByText('ユーザーの削除')).not.toBeInTheDocument();
  });

  it('削除ボタンをクリックするとonConfirmが呼ばれる', async () => {
    const user = userEvent.setup();
    render(
      <DeleteUserConfirmDialog
        user={mockUser}
        isOpen={true}
        onCancel={mockOnCancel}
        onConfirm={mockOnConfirm}
      />
    );

    const confirmButton = screen.getByText('削除');
    await user.click(confirmButton);

    expect(mockOnConfirm).toHaveBeenCalled();
  });

  it('キャンセルボタンをクリックするとonCancelが呼ばれる', async () => {
    const user = userEvent.setup();
    render(
      <DeleteUserConfirmDialog
        user={mockUser}
        isOpen={true}
        onCancel={mockOnCancel}
        onConfirm={mockOnConfirm}
      />
    );

    const cancelButton = screen.getByText('キャンセル');
    await user.click(cancelButton);

    expect(mockOnCancel).toHaveBeenCalled();
  });

  it('削除中はボタンラベルが「削除中...」に変わる', () => {
    render(
      <DeleteUserConfirmDialog
        user={mockUser}
        isOpen={true}
        onCancel={mockOnCancel}
        onConfirm={mockOnConfirm}
        isDeleting={true}
      />
    );

    expect(screen.getByText('削除中...')).toBeInTheDocument();
  });

  it('userがnullの場合はメッセージが空になる', () => {
    render(
      <DeleteUserConfirmDialog
        user={null}
        isOpen={true}
        onCancel={mockOnCancel}
        onConfirm={mockOnConfirm}
      />
    );

    expect(screen.getByText('ユーザーの削除')).toBeInTheDocument();
    // The message should be empty when user is null
    expect(screen.queryByText(/を削除しますか/)).not.toBeInTheDocument();
  });

  it('警告メッセージが表示される', () => {
    render(
      <DeleteUserConfirmDialog
        user={mockUser}
        isOpen={true}
        onCancel={mockOnCancel}
        onConfirm={mockOnConfirm}
      />
    );

    expect(
      screen.getByText(/この操作により、ユーザーはログインできなくなります。/)
    ).toBeInTheDocument();
  });
});
