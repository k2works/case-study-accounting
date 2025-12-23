import React from 'react';
import { useAuth } from '../../../hooks/useAuth';
import { config } from '../../../config';
import './Header.css';

/**
 * ヘッダーコンポーネント
 *
 * アプリケーション名、ユーザー情報、ログアウトボタンを表示する。
 */
export const Header: React.FC = () => {
  const { user, logout } = useAuth();

  const handleLogout = () => {
    logout();
  };

  return (
    <header className="header" data-testid="header">
      <div className="header__brand">
        <h1 className="header__title">{config.appName}</h1>
      </div>
      <div className="header__user">
        <span className="header__username" data-testid="header-username">
          {user?.username}
        </span>
        <button className="header__logout-btn" onClick={handleLogout} data-testid="logout-button">
          ログアウト
        </button>
      </div>
    </header>
  );
};
