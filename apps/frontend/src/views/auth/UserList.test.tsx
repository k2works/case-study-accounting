import { describe, it, expect, vi, beforeEach } from 'vitest';
import { render, screen } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { UserList } from './UserList';
import type { User } from '../../api/getUsers';

const mockUsers: User[] = [
  {
    id: 'user-1',
    username: 'admin',
    email: 'admin@example.com',
    displayName: '管理者',
    role: 'ADMIN',
    lastLoginAt: '2024-01-15T10:30:00Z',
  },
  {
    id: 'user-2',
    username: 'user1',
    email: 'user1@example.com',
    displayName: 'ユーザー1',
    role: 'USER',
    lastLoginAt: null,
  },
];

describe('UserList', () => {
  const mockOnEdit = vi.fn();
  const mockOnDelete = vi.fn();

  beforeEach(() => {
    vi.clearAllMocks();
  });

  it('ユーザー一覧が正しくレンダリングされる', () => {
    render(<UserList users={mockUsers} onEdit={mockOnEdit} onDelete={mockOnDelete} />);

    expect(screen.getByTestId('user-list')).toBeInTheDocument();
    expect(screen.getByText('admin')).toBeInTheDocument();
    expect(screen.getByText('admin@example.com')).toBeInTheDocument();
    expect(screen.getByText('管理者')).toBeInTheDocument();
    expect(screen.getByText('ADMIN')).toBeInTheDocument();
  });

  it('複数のユーザーが表示される', () => {
    render(<UserList users={mockUsers} onEdit={mockOnEdit} onDelete={mockOnDelete} />);

    expect(screen.getByText('admin')).toBeInTheDocument();
    expect(screen.getByText('user1')).toBeInTheDocument();
    expect(screen.getByText('ユーザー1')).toBeInTheDocument();
  });

  it('最終ログイン日時が正しくフォーマットされる', () => {
    render(<UserList users={mockUsers} onEdit={mockOnEdit} onDelete={mockOnDelete} />);

    // Check that the formatted date is displayed (locale dependent)
    const dateElements = screen.getAllByText(/2024/);
    expect(dateElements.length).toBeGreaterThan(0);
  });

  it('最終ログインがnullの場合は「-」が表示される', () => {
    render(<UserList users={mockUsers} onEdit={mockOnEdit} onDelete={mockOnDelete} />);

    expect(screen.getByText('-')).toBeInTheDocument();
  });

  it('ユーザーがいない場合は空メッセージが表示される', () => {
    render(<UserList users={[]} onEdit={mockOnEdit} onDelete={mockOnDelete} />);

    expect(screen.getByText('ユーザーが登録されていません')).toBeInTheDocument();
  });

  it('編集ボタンをクリックするとonEditが呼ばれる', async () => {
    const user = userEvent.setup();
    render(<UserList users={mockUsers} onEdit={mockOnEdit} onDelete={mockOnDelete} />);

    const editButton = screen.getByTestId('user-edit-user-1');
    await user.click(editButton);

    expect(mockOnEdit).toHaveBeenCalledWith(mockUsers[0]);
  });

  it('削除ボタンをクリックするとonDeleteが呼ばれる', async () => {
    const user = userEvent.setup();
    render(<UserList users={mockUsers} onEdit={mockOnEdit} onDelete={mockOnDelete} />);

    const deleteButton = screen.getByTestId('user-delete-user-1');
    await user.click(deleteButton);

    expect(mockOnDelete).toHaveBeenCalledWith(mockUsers[0]);
  });

  it('各ユーザーに編集・削除ボタンが表示される', () => {
    render(<UserList users={mockUsers} onEdit={mockOnEdit} onDelete={mockOnDelete} />);

    expect(screen.getByTestId('user-edit-user-1')).toBeInTheDocument();
    expect(screen.getByTestId('user-delete-user-1')).toBeInTheDocument();
    expect(screen.getByTestId('user-edit-user-2')).toBeInTheDocument();
    expect(screen.getByTestId('user-delete-user-2')).toBeInTheDocument();
  });

  it('テーブルヘッダーが正しく表示される', () => {
    render(<UserList users={mockUsers} onEdit={mockOnEdit} onDelete={mockOnDelete} />);

    expect(screen.getByText('ユーザー ID')).toBeInTheDocument();
    expect(screen.getByText('ユーザー名')).toBeInTheDocument();
    expect(screen.getByText('メールアドレス')).toBeInTheDocument();
    expect(screen.getByText('表示名')).toBeInTheDocument();
    expect(screen.getByText('ロール')).toBeInTheDocument();
    expect(screen.getByText('最終ログイン')).toBeInTheDocument();
    expect(screen.getByText('操作')).toBeInTheDocument();
  });
});
