import React, { useMemo } from 'react';
import type { User } from '../../api/getUsers';
import { Button, Table, TableColumn } from '../common';

interface UserListProps {
  users: User[];
  onEdit: (user: User) => void;
}

/**
 * ユーザー一覧コンポーネント
 */
export const UserList: React.FC<UserListProps> = ({ users, onEdit }) => {
  const columns = useMemo<TableColumn<User>[]>(
    () => [
      { key: 'id', header: 'ユーザー ID', width: '160px' },
      { key: 'username', header: 'ユーザー名', width: '160px' },
      { key: 'email', header: 'メールアドレス' },
      { key: 'displayName', header: '表示名', width: '160px' },
      { key: 'role', header: 'ロール', width: '120px' },
      {
        key: 'actions',
        header: '操作',
        width: '120px',
        align: 'center',
        render: (_, row) => (
          <Button
            variant="text"
            size="small"
            onClick={() => onEdit(row)}
            data-testid={`user-edit-${row.id}`}
          >
            編集
          </Button>
        ),
      },
    ],
    [onEdit]
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
