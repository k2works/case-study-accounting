import React, { useState } from 'react';
import { Header } from './Header';
import { Sidebar } from './Sidebar';
import { Breadcrumb, BreadcrumbItem } from './Breadcrumb';
import './MainLayout.css';

interface MainLayoutProps {
  children: React.ReactNode;
  breadcrumbs?: BreadcrumbItem[];
}

/**
 * メインレイアウトコンポーネント
 *
 * ヘッダー、サイドバー、メインコンテンツエリアを含むレイアウト。
 */
export const MainLayout: React.FC<MainLayoutProps> = ({ children, breadcrumbs }) => {
  const [isSidebarOpen, setIsSidebarOpen] = useState(false);

  const handleToggleSidebar = () => {
    setIsSidebarOpen((prev) => !prev);
  };

  const handleCloseSidebar = () => {
    setIsSidebarOpen(false);
  };

  return (
    <div className="main-layout">
      <Header />
      <div className="main-layout__body">
        <Sidebar isCollapsed={false} isOpen={isSidebarOpen} />
        <main className="main-layout__content">
          {breadcrumbs && breadcrumbs.length > 0 && <Breadcrumb items={breadcrumbs} />}
          <div className="main-layout__page">{children}</div>
        </main>
      </div>

      {/* モバイル用オーバーレイ */}
      {isSidebarOpen && (
        <div
          className="main-layout__overlay is-visible"
          onClick={handleCloseSidebar}
          onKeyDown={(e) => {
            if (e.key === 'Enter' || e.key === ' ') {
              handleCloseSidebar();
            }
          }}
          role="button"
          tabIndex={0}
          aria-label="メニューを閉じる"
        />
      )}

      {/* モバイル用ハンバーガーメニュー */}
      <button
        className={`main-layout__menu-toggle ${isSidebarOpen ? 'is-open' : ''}`}
        onClick={handleToggleSidebar}
        aria-label="メニュー"
      >
        <span className="main-layout__menu-icon" />
      </button>
    </div>
  );
};
