import React from 'react';
import { Navigate } from 'react-router-dom';
import { useAuth } from '../hooks/useAuth';
import { MainLayout } from '../views/common';
import { Loading } from '../views/common';
import { CreateAutoJournalPatternForm } from '../views/auto-journal-pattern/CreateAutoJournalPatternForm';

const CreateAutoJournalPatternPage: React.FC = () => {
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
    { label: '自動仕訳パターン一覧', path: '/master/auto-journal-patterns' },
    { label: '自動仕訳パターン登録' },
  ];

  return (
    <MainLayout breadcrumbs={breadcrumbs}>
      <div data-testid="create-auto-journal-pattern-page">
        <h1>自動仕訳パターン登録</h1>
        <CreateAutoJournalPatternForm />
      </div>
    </MainLayout>
  );
};

export default CreateAutoJournalPatternPage;
