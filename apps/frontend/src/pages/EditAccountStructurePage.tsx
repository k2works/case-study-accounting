import React from 'react';
import { Navigate, useParams } from 'react-router-dom';
import { useAuth } from '../hooks/useAuth';
import { MainLayout, Loading } from '../views/common';
import { EditAccountStructureForm } from '../views/account-structure/EditAccountStructureForm';

const EditAccountStructurePage: React.FC = () => {
  const { isAuthenticated, isLoading, hasRole } = useAuth();
  const { code } = useParams<{ code: string }>();

  if (isLoading) {
    return <Loading message="認証情報を確認中..." fullScreen />;
  }

  if (!isAuthenticated) {
    return <Navigate to="/login" replace />;
  }

  if (!hasRole('ADMIN') && !hasRole('MANAGER')) {
    return <Navigate to="/" replace />;
  }

  if (!code) {
    return <Navigate to="/master/account-structures" replace />;
  }

  const breadcrumbs = [
    { label: 'ホーム' },
    { label: 'マスタ管理' },
    { label: '勘定科目体系', path: '/master/account-structures' },
    { label: '編集' },
  ];

  return (
    <MainLayout breadcrumbs={breadcrumbs}>
      <div data-testid="edit-account-structure-page">
        <h1>勘定科目体系 編集</h1>
        <EditAccountStructureForm code={code} />
      </div>
    </MainLayout>
  );
};

export default EditAccountStructurePage;
