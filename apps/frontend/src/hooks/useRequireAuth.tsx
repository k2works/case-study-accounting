import React from 'react';
import { Navigate } from 'react-router-dom';
import { useAuth } from './useAuth';
import { Loading } from '../views/common';
import type { Role } from '../types/auth';

export const useRequireAuth = (roles: Role[]): React.ReactElement | null => {
  const { isAuthenticated, isLoading, hasRole } = useAuth();

  if (isLoading) {
    return <Loading message="認証情報を確認中..." fullScreen />;
  }
  if (!isAuthenticated) {
    return <Navigate to="/login" replace />;
  }
  if (roles.length > 0 && !roles.some((r) => hasRole(r))) {
    return <Navigate to="/" replace />;
  }
  return null;
};
