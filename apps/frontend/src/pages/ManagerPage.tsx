import React from 'react';
import { Navigate } from 'react-router-dom';
import { useAuth } from '../hooks/useAuth';
import { MainLayout, Loading } from '../views/common';

interface Breadcrumb {
  label: string;
  path?: string;
}

interface ManagerPageProps {
  breadcrumbs: Breadcrumb[];
  children: React.ReactNode;
}

export const ManagerPage: React.FC<ManagerPageProps> = ({ breadcrumbs, children }) => {
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

  return <MainLayout breadcrumbs={breadcrumbs}>{children}</MainLayout>;
};
