import React, { useCallback, useEffect, useState } from 'react';
import { Navigate, useNavigate, useParams } from 'react-router-dom';
import { useAuth } from '../hooks/useAuth';
import { getUser, getUserErrorMessage } from '../api/getUser';
import type { GetUserResponse } from '../api/getUser';
import { MainLayout, Loading, ErrorMessage } from '../views/common';
import { UserEditForm } from '../views/auth/UserEditForm';

interface UserState {
  user: GetUserResponse | null;
  isLoading: boolean;
  errorMessage: string | null;
}

const initialState: UserState = {
  user: null,
  isLoading: false,
  errorMessage: null,
};

const useUserFetch = (userId: string, isInvalidId: boolean) => {
  const [state, setState] = useState<UserState>(initialState);

  const fetchUser = useCallback(async () => {
    if (isInvalidId) {
      setState({ user: null, isLoading: false, errorMessage: 'ユーザーが見つかりませんでした' });
      return;
    }

    setState((prev) => ({ ...prev, isLoading: true, errorMessage: null }));
    try {
      const data = await getUser(userId);
      setState({ user: data, isLoading: false, errorMessage: null });
    } catch (error) {
      setState((prev) => ({
        ...prev,
        isLoading: false,
        errorMessage: getUserErrorMessage(error),
      }));
    }
  }, [isInvalidId, userId]);

  useEffect(() => {
    void fetchUser();
  }, [fetchUser]);

  return { state, fetchUser };
};

/**
 * ユーザー編集ページ
 */
const breadcrumbItems = [
  { label: 'ホーム' },
  { label: 'ユーザー管理' },
  { label: 'ユーザー一覧', path: '/users' },
  { label: 'ユーザー編集' },
];

const UserEditPageContent: React.FC<{
  state: UserState;
  fetchUser: () => Promise<void>;
  navigate: ReturnType<typeof useNavigate>;
}> = ({ state, fetchUser, navigate }) => (
  <MainLayout breadcrumbs={breadcrumbItems}>
    <div data-testid="user-edit-page">
      <h1>ユーザー編集</h1>
      {state.isLoading && <Loading message="ユーザー情報を読み込み中..." />}
      {state.errorMessage && <ErrorMessage message={state.errorMessage} onRetry={fetchUser} />}
      {state.user && (
        <UserEditForm
          user={state.user}
          onSuccess={(message) =>
            navigate('/users', { replace: true, state: { successMessage: message } })
          }
        />
      )}
    </div>
  </MainLayout>
);

const UserEditPage: React.FC = () => {
  const { isAuthenticated, isLoading, hasRole } = useAuth();
  const { id } = useParams();
  const navigate = useNavigate();

  const userId = id ?? '';
  const isInvalidId = userId.trim() === '';
  const { state, fetchUser } = useUserFetch(userId, isInvalidId);

  if (isLoading) {
    return <Loading message="認証情報を確認中..." fullScreen />;
  }

  if (!isAuthenticated) {
    return <Navigate to="/login" replace />;
  }

  if (!hasRole('ADMIN')) {
    return <Navigate to="/" replace />;
  }

  return <UserEditPageContent state={state} fetchUser={fetchUser} navigate={navigate} />;
};

export default UserEditPage;
