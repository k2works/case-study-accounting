import React from 'react';
import { Navigate } from 'react-router-dom';
import { useAuth } from '../hooks/useAuth';
import { MainLayout, Loading } from '../views/common';
import { CreateAccountStructureForm } from '../views/account-structure/CreateAccountStructureForm';

const CreateAccountStructurePage: React.FC = () => {
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

  const breadcrumbs = [
    { label: 'ホーム' },
    { label: 'マスタ管理' },
    { label: '勘定科目体系', path: '/master/account-structures' },
    { label: '新規登録' },
  ];

  return (
    <MainLayout breadcrumbs={breadcrumbs}>
      <div data-testid="create-account-structure-page">
        <h1>勘定科目体系 新規登録</h1>
        <CreateAccountStructureForm />
      </div>
    </MainLayout>
  );
};

export default CreateAccountStructurePage;
