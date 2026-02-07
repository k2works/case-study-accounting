import React, { useState } from 'react';
import { NavLink } from 'react-router-dom';
import { useAuth } from '../../../hooks/useAuth';
import './Sidebar.css';

interface MenuItem {
  id: string;
  label: string;
  path?: string;
  icon?: string;
  children?: MenuItem[];
  roles?: string[];
}

const MENU_ITEMS: MenuItem[] = [
  {
    id: 'dashboard',
    label: 'ダッシュボード',
    path: '/',
    icon: 'dashboard',
  },
  {
    id: 'journal-entry',
    label: '仕訳',
    icon: 'book',
    roles: ['ADMIN', 'MANAGER', 'USER'],
    children: [
      { id: 'journal-entry-list', label: '仕訳一覧', path: '/journal/entries' },
      { id: 'journal-entry-new', label: '仕訳入力', path: '/journal/entries/new' },
    ],
  },
  {
    id: 'ledger',
    label: '元帳・残高',
    icon: 'ledger',
    children: [
      { id: 'ledger-general', label: '総勘定元帳', path: '/general-ledger' },
      { id: 'ledger-daily-balance', label: '日次残高', path: '/ledger/daily-balance' },
      { id: 'ledger-subsidiary', label: '補助元帳', path: '/ledger/subsidiary' },
      { id: 'ledger-trial-balance', label: '残高試算表', path: '/ledger/trial-balance' },
    ],
  },
  {
    id: 'financial-statements',
    label: '財務諸表',
    icon: 'chart',
    children: [
      { id: 'fs-balance-sheet', label: '貸借対照表', path: '/financial-statements/balance-sheet' },
      {
        id: 'fs-income-statement',
        label: '損益計算書',
        path: '/financial-statements/income-statement',
      },
      {
        id: 'fs-analysis',
        label: '財務分析',
        path: '/financial-statements/analysis',
        roles: ['ADMIN', 'MANAGER'],
      },
    ],
  },
  {
    id: 'master',
    label: 'マスタ管理',
    icon: 'settings',
    children: [{ id: 'master-accounts', label: '勘定科目', path: '/master/accounts' }],
  },
  {
    id: 'system',
    label: 'システム管理',
    icon: 'admin',
    roles: ['ADMIN'],
    children: [
      { id: 'system-users', label: 'ユーザー', path: '/system/users' },
      { id: 'system-audit', label: '監査ログ', path: '/system/audit' },
    ],
  },
];

/**
 * ユーザーのロールに基づいてメニュー項目をフィルタリング
 */
const filterMenuItemsByRole = (items: MenuItem[], userRole: string): MenuItem[] => {
  return items
    .filter((item) => !item.roles || item.roles.includes(userRole))
    .map((item) => ({
      ...item,
      children: item.children ? filterMenuItemsByRole(item.children, userRole) : undefined,
    }))
    .filter((item) => !item.children || item.children.length > 0);
};

interface SidebarItemProps {
  item: MenuItem;
  isOpen: boolean;
  onToggle: () => void;
}

const SidebarItem: React.FC<SidebarItemProps> = ({ item, isOpen, onToggle }) => {
  const hasChildren = item.children && item.children.length > 0;

  if (hasChildren) {
    return (
      <li className="sidebar__item">
        <button
          className={`sidebar__link sidebar__link--parent ${isOpen ? 'is-open' : ''}`}
          onClick={onToggle}
        >
          <span className="sidebar__link-text">{item.label}</span>
          <span className={`sidebar__arrow ${isOpen ? 'is-open' : ''}`}>▼</span>
        </button>
        {isOpen && (
          <ul className="sidebar__submenu">
            {item.children!.map((child) => (
              <li key={child.id} className="sidebar__subitem">
                <NavLink
                  to={child.path!}
                  className={({ isActive }) => `sidebar__sublink ${isActive ? 'is-active' : ''}`}
                >
                  {child.label}
                </NavLink>
              </li>
            ))}
          </ul>
        )}
      </li>
    );
  }

  return (
    <li className="sidebar__item">
      <NavLink
        to={item.path!}
        className={({ isActive }) => `sidebar__link ${isActive ? 'is-active' : ''}`}
      >
        <span className="sidebar__link-text">{item.label}</span>
      </NavLink>
    </li>
  );
};

interface SidebarProps {
  isCollapsed?: boolean;
  isOpen?: boolean;
}

/**
 * サイドバーコンポーネント
 *
 * ナビゲーションメニューを表示する。
 * ユーザーのロールに基づいてメニュー項目をフィルタリングする。
 */
export const Sidebar: React.FC<SidebarProps> = ({ isCollapsed = false, isOpen = false }) => {
  const { user } = useAuth();
  const [openMenus, setOpenMenus] = useState<Set<string>>(new Set());

  const userRole = user?.role || 'VIEWER';
  const filteredMenuItems = filterMenuItemsByRole(MENU_ITEMS, userRole);

  const handleToggleMenu = (menuId: string) => {
    setOpenMenus((prev) => {
      const next = new Set(prev);
      if (next.has(menuId)) {
        next.delete(menuId);
      } else {
        next.add(menuId);
      }
      return next;
    });
  };

  const sidebarClasses = ['sidebar', isCollapsed ? 'is-collapsed' : '', isOpen ? 'is-open' : '']
    .filter(Boolean)
    .join(' ');

  return (
    <aside className={sidebarClasses}>
      <nav className="sidebar__nav">
        <ul className="sidebar__menu">
          {filteredMenuItems.map((item) => (
            <SidebarItem
              key={item.id}
              item={item}
              isOpen={openMenus.has(item.id)}
              onToggle={() => handleToggleMenu(item.id)}
            />
          ))}
        </ul>
      </nav>
    </aside>
  );
};
