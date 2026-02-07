import React, { useCallback, useEffect, useState } from 'react';
import { Navigate, useLocation, useNavigate } from 'react-router-dom';
import { useAuth } from '../hooks/useAuth';
import { getUsers, getUsersErrorMessage } from '../api/getUsers';
import type { User } from '../api/getUsers';
import { deleteUser, getDeleteUserErrorMessage } from '../api/deleteUser';
import { MainLayout, Loading, ErrorMessage, SuccessNotification } from '../views/common';
import { UserList } from '../views/auth/UserList';
import { DeleteUserConfirmDialog } from '../views/auth/DeleteUserConfirmDialog';

interface UserListLocationState {
  successMessage?: string;
}

interface UserListState {
  users: User[];
  isLoading: boolean;
  errorMessage: string | null;
}

const initialState: UserListState = {
  users: [],
  isLoading: false,
  errorMessage: null,
};

const useUserListFetch = () => {
  const [state, setState] = useState<UserListState>(initialState);

  const fetchUsers = useCallback(async () => {
    setState((prev) => ({ ...prev, isLoading: true, errorMessage: null }));
    try {
      const data = await getUsers();
      setState({ users: data, isLoading: false, errorMessage: null });
    } catch (error) {
      setState((prev) => ({
        ...prev,
        isLoading: false,
        errorMessage: getUsersErrorMessage(error),
      }));
    }
  }, []);

  useEffect(() => {
    void fetchUsers();
  }, [fetchUsers]);

  return { state, fetchUsers };
};

/**
 * ユーザー一覧ページ
 */
const breadcrumbItems = [{ label: 'ホーム' }, { label: 'ユーザー管理' }, { label: 'ユーザー一覧' }];

const UserListPageContent: React.FC<{
  state: UserListState;
  fetchUsers: () => Promise<void>;
  navigate: ReturnType<typeof useNavigate>;
  successMessage: string | null;
  setSuccessMessage: React.Dispatch<React.SetStateAction<string | null>>;
  deleteSuccessMessage: string | null;
  deleteErrorMessage: string | null;
  onDismissDeleteSuccess: () => void;
  onDismissDeleteError: () => void;
  onDeleteClick: (user: User) => void;
}> = ({
  state,
  fetchUsers,
  navigate,
  successMessage,
  setSuccessMessage,
  deleteSuccessMessage,
  deleteErrorMessage,
  onDismissDeleteSuccess,
  onDismissDeleteError,
  onDeleteClick,
}) => (
  <MainLayout breadcrumbs={breadcrumbItems}>
    <div data-testid="user-list-page">
      <h1>ユーザー一覧</h1>
      {successMessage && (
        <div style={{ marginBottom: '16px' }}>
          <SuccessNotification message={successMessage} onDismiss={() => setSuccessMessage(null)} />
        </div>
      )}
      {deleteSuccessMessage && (
        <div style={{ marginBottom: '16px' }}>
          <SuccessNotification message={deleteSuccessMessage} onDismiss={onDismissDeleteSuccess} />
        </div>
      )}
      {deleteErrorMessage && (
        <div style={{ marginBottom: '16px' }}>
          <ErrorMessage message={deleteErrorMessage} onDismiss={onDismissDeleteError} />
        </div>
      )}
      {state.isLoading && state.users.length === 0 && <Loading message="ユーザーを読み込み中..." />}
      {state.errorMessage ? (
        <ErrorMessage message={state.errorMessage} onRetry={fetchUsers} />
      ) : (
        <UserList
          users={state.users}
          onEdit={(user) => navigate(`/users/${user.id}/edit`)}
          onDelete={onDeleteClick}
        />
      )}
    </div>
  </MainLayout>
);

const UserListPage: React.FC = () => {
  const { isAuthenticated, isLoading, hasRole } = useAuth();
  const { state, fetchUsers } = useUserListFetch();
  const location = useLocation();
  const navigate = useNavigate();
  const [successMessage, setSuccessMessage] = useState<string | null>(() => {
    const locState = location.state as UserListLocationState | null;
    return locState?.successMessage ?? null;
  });
  const [deleteSuccessMessage, setDeleteSuccessMessage] = useState<string | null>(null);
  const [deleteErrorMessage, setDeleteErrorMessage] = useState<string | null>(null);
  const [deleteTarget, setDeleteTarget] = useState<User | null>(null);
  const [isDeleting, setIsDeleting] = useState(false);

  const handleDeleteConfirm = useCallback(async () => {
    if (!deleteTarget) {
      return;
    }

    setIsDeleting(true);
    setDeleteErrorMessage(null);
    setDeleteSuccessMessage(null);

    try {
      await deleteUser(deleteTarget.id);
      setDeleteSuccessMessage('ユーザーを削除しました');
      setDeleteTarget(null);
      await fetchUsers();
    } catch (error) {
      setDeleteErrorMessage(getDeleteUserErrorMessage(error));
    } finally {
      setIsDeleting(false);
    }
  }, [deleteTarget, fetchUsers]);

  if (isLoading) {
    return <Loading message="認証情報を確認中..." fullScreen />;
  }

  if (!isAuthenticated) {
    return <Navigate to="/login" replace />;
  }

  if (!hasRole('ADMIN')) {
    return <Navigate to="/" replace />;
  }

  return (
    <>
      <UserListPageContent
        state={state}
        fetchUsers={fetchUsers}
        navigate={navigate}
        successMessage={successMessage}
        setSuccessMessage={setSuccessMessage}
        deleteSuccessMessage={deleteSuccessMessage}
        deleteErrorMessage={deleteErrorMessage}
        onDismissDeleteSuccess={() => setDeleteSuccessMessage(null)}
        onDismissDeleteError={() => setDeleteErrorMessage(null)}
        onDeleteClick={(user) => setDeleteTarget(user)}
      />
      <DeleteUserConfirmDialog
        user={deleteTarget}
        isOpen={deleteTarget !== null}
        onCancel={() => setDeleteTarget(null)}
        onConfirm={handleDeleteConfirm}
        isDeleting={isDeleting}
      />
    </>
  );
};

export default UserListPage;
