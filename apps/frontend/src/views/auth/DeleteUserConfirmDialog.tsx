import React from 'react';
import type { User } from '../../api/getUsers';
import { ConfirmModal } from '../common';

interface DeleteUserConfirmDialogProps {
  user: User | null;
  isOpen: boolean;
  onCancel: () => void;
  onConfirm: () => void;
  isDeleting?: boolean;
}

/**
 * ユーザー削除確認ダイアログ
 */
export const DeleteUserConfirmDialog: React.FC<DeleteUserConfirmDialogProps> = ({
  user,
  isOpen,
  onCancel,
  onConfirm,
  isDeleting = false,
}) => {
  const title = 'ユーザーの削除';
  const message = user
    ? `ユーザー「${user.displayName}」（${user.username}）を削除しますか？\nこの操作により、ユーザーはログインできなくなります。`
    : '';

  return (
    <ConfirmModal
      isOpen={isOpen}
      onClose={onCancel}
      onConfirm={onConfirm}
      title={title}
      message={message}
      confirmLabel={isDeleting ? '削除中...' : '削除'}
      cancelLabel="キャンセル"
      isDestructive
    />
  );
};
