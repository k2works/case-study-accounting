import React from 'react';
import { Navigate, useLocation } from 'react-router-dom';
import { useAuth } from '../hooks/useAuth';
import { LoginForm } from '../views/auth/LoginForm';
import './LoginPage.css';

/**
 * ログインページ
 */
const LoginPage: React.FC = () => {
  const { isAuthenticated, isLoading } = useAuth();
  const location = useLocation();

  // ログイン済みの場合、元のページまたはダッシュボードへリダイレクト
  if (isAuthenticated) {
    const from = (location.state as { from?: Location })?.from?.pathname || '/';
    return <Navigate to={from} replace />;
  }

  if (isLoading) {
    return (
      <div className="login-page">
        <div className="login-page__loading">認証情報を確認中...</div>
      </div>
    );
  }

  return (
    <div className="login-page" data-testid="login-page">
      <div className="login-page__container">
        <div className="login-page__header">
          <h1 className="login-page__title">財務会計システム</h1>
          <p className="login-page__subtitle">ログイン</p>
        </div>
        <LoginForm />
      </div>
    </div>
  );
};

export default LoginPage;
