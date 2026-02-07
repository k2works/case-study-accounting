import React, { useMemo } from 'react';
import type { User } from '../../api/getUsers';
import { Button, Table, TableColumn } from '../common';

interface UserListProps {
  users: User[];
  onEdit: (user: User) => void;
  onDelete: (user: User) => void;
}

/**
 * ユーザー一覧コンポーネント
 */
export const UserList: React.FC<UserListProps> = ({ users, onEdit, onDelete }) => {
  const columns = useMemo<TableColumn<User>[]>(
    () => [
      { key: 'id', header: 'ユーザー ID', width: '160px' },
      { key: 'username', header: 'ユーザー名', width: '160px' },
      { key: 'email', header: 'メールアドレス' },
      { key: 'displayName', header: '表示名', width: '160px' },
      { key: 'role', header: 'ロール', width: '120px' },
      {
        key: 'lastLoginAt',
        header: '最終ログイン',
        width: '180px',
        render: (value) => {
          if (!value) return '-';
          return new Date(value as string).toLocaleString('ja-JP');
        },
      },
      {
        key: 'actions',
        header: '操作',
        width: '160px',
        align: 'center',
        render: (_, row) => (
          <div style={{ display: 'flex', gap: '8px', justifyContent: 'center' }}>
            <Button
              variant="text"
              size="small"
              onClick={() => onEdit(row)}
              data-testid={`user-edit-${row.id}`}
            >
              編集
            </Button>
            <Button
              variant="danger"
              size="small"
              onClick={() => onDelete(row)}
              data-testid={`user-delete-${row.id}`}
            >
              削除
            </Button>
          </div>
        ),
      },
    ],
    [onDelete, onEdit]
  );

  return (
    <div data-testid="user-list">
      <Table
        columns={columns}
        data={users}
        keyField="id"
        emptyMessage="ユーザーが登録されていません"
      />
    </div>
  );
};
