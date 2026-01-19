import React from 'react';
import { Navigate } from 'react-router-dom';
import { useAuth } from '../hooks/useAuth';
import { MainLayout } from '../views/common';
import { CreateAccountForm } from '../views/account/CreateAccountForm';
import { Loading } from '../views/common';

/**
 * 勘定科目登録ページ
 */
const CreateAccountPage: React.FC = () => {
  const { isAuthenticated, isLoading, hasRole } = useAuth();

  if (isLoading) {
    return <Loading message="認証情報を確認中..." fullScreen />;
  }

  if (!isAuthenticated) {
    return <Navigate to="/login" replace />;
  }

  if (!hasRole('ADMIN') && !hasRole('MANAGER')) {
    return <Navigate to="/" replace />;
  }

  const breadcrumbs = [{ label: 'ホーム' }, { label: 'マスタ管理' }, { label: '勘定科目登録' }];

  return (
    <MainLayout breadcrumbs={breadcrumbs}>
      <div data-testid="create-account-page">
        <h1>勘定科目登録</h1>
        <CreateAccountForm />
      </div>
    </MainLayout>
  );
};

export default CreateAccountPage;
