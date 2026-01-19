import React from 'react';
import { Navigate } from 'react-router-dom';
import { useAuth } from '../hooks/useAuth';
import { MainLayout } from '../views/common';
import { RegisterUserForm } from '../views/auth/RegisterUserForm';
import { Loading } from '../views/common';

/**
 * ユーザー登録ページ
 */
const RegisterUserPage: React.FC = () => {
  const { isAuthenticated, isLoading, hasRole } = useAuth();

  if (isLoading) {
    return <Loading message="認証情報を確認中..." fullScreen />;
  }

  if (!isAuthenticated) {
    return <Navigate to="/login" replace />;
  }

  if (!hasRole('ADMIN')) {
    return <Navigate to="/" replace />;
  }

  const breadcrumbs = [{ label: 'システム管理' }, { label: 'ユーザー登録' }];

  return (
    <MainLayout breadcrumbs={breadcrumbs}>
      <div data-testid="register-user-page">
        <h1>ユーザー登録</h1>
        <RegisterUserForm />
      </div>
    </MainLayout>
  );
};

export default RegisterUserPage;
