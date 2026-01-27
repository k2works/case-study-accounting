import { describe, it, expect, vi, beforeEach } from 'vitest';
import { render, screen } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { BrowserRouter } from 'react-router-dom';

const mockUseAuth = vi.fn();
vi.mock('../../../hooks/useAuth', () => ({
  useAuth: () => mockUseAuth(),
}));

import { Sidebar } from './Sidebar';

// ヘルパー関数
type Role = 'ADMIN' | 'MANAGER' | 'USER' | 'VIEWER';
const setRole = (role: Role, username = role.toLowerCase()) =>
  mockUseAuth.mockReturnValue({ user: { username, role } });

const setNoUser = () => mockUseAuth.mockReturnValue({ user: null });

const renderSidebar = (props = {}) =>
  render(
    <BrowserRouter>
      <Sidebar {...props} />
    </BrowserRouter>
  );

const expandMenu = async (menuText: string) => {
  const user = userEvent.setup();
  await user.click(screen.getByText(menuText));
  return user;
};

describe('Sidebar', () => {
  beforeEach(() => {
    vi.clearAllMocks();
    setRole('USER', 'testuser');
  });

  describe('basic rendering', () => {
    it('renders navigation with common menu items', () => {
      renderSidebar();
      expect(screen.getByRole('navigation')).toBeInTheDocument();
      expect(screen.getByText('ダッシュボード')).toBeInTheDocument();
      expect(screen.getByText('仕訳')).toBeInTheDocument();
      expect(screen.getByText('元帳・残高')).toBeInTheDocument();
      expect(screen.getByText('財務諸表')).toBeInTheDocument();
      expect(screen.getByText('マスタ管理')).toBeInTheDocument();
    });

    it('applies CSS classes based on props', () => {
      const { container, rerender } = renderSidebar({ isCollapsed: true });
      expect(container.querySelector('.is-collapsed')).toBeInTheDocument();

      rerender(
        <BrowserRouter>
          <Sidebar isOpen={true} />
        </BrowserRouter>
      );
      expect(container.querySelector('.is-open')).toBeInTheDocument();
    });
  });

  describe('submenu behavior', () => {
    it('expands and collapses submenu on click', async () => {
      renderSidebar();

      expect(screen.queryByText('仕訳入力')).not.toBeInTheDocument();

      await expandMenu('仕訳');
      expect(screen.getByText('仕訳入力')).toBeInTheDocument();

      await expandMenu('仕訳');
      expect(screen.queryByText('仕訳入力')).not.toBeInTheDocument();
    });

    it('adds is-open class to arrow when expanded', async () => {
      const { container } = renderSidebar();
      await expandMenu('仕訳');
      expect(container.querySelector('.sidebar__arrow.is-open')).toBeInTheDocument();
    });
  });
});

describe('Sidebar role-based menu visibility', () => {
  beforeEach(() => vi.clearAllMocks());

  describe('ADMIN role', () => {
    beforeEach(() => setRole('ADMIN'));

    it('renders system management and all privileged menus', async () => {
      renderSidebar();
      expect(screen.getByText('システム管理')).toBeInTheDocument();
      expect(screen.getByText('仕訳')).toBeInTheDocument();

      await expandMenu('財務諸表');
      expect(screen.getByText('財務分析')).toBeInTheDocument();
    });
  });

  describe('MANAGER role', () => {
    beforeEach(() => setRole('MANAGER'));

    it('renders journal and analysis but not system management', async () => {
      renderSidebar();
      expect(screen.queryByText('システム管理')).not.toBeInTheDocument();
      expect(screen.getByText('仕訳')).toBeInTheDocument();

      await expandMenu('財務諸表');
      expect(screen.getByText('財務分析')).toBeInTheDocument();
    });
  });

  describe('USER role', () => {
    beforeEach(() => setRole('USER'));

    it('does not render privileged menus', async () => {
      renderSidebar();
      expect(screen.queryByText('システム管理')).not.toBeInTheDocument();
      expect(screen.getByText('仕訳')).toBeInTheDocument();

      await expandMenu('財務諸表');
      expect(screen.queryByText('財務分析')).not.toBeInTheDocument();
    });
  });

  describe('VIEWER role and no user', () => {
    it('VIEWER sees only basic menu items', () => {
      setRole('VIEWER');
      renderSidebar();
      expect(screen.getByText('ダッシュボード')).toBeInTheDocument();
      expect(screen.queryByText('システム管理')).not.toBeInTheDocument();
      // VIEWER は仕訳メニューを見れない（roles: ['ADMIN', 'MANAGER', 'USER']）
      expect(screen.queryByText('仕訳')).not.toBeInTheDocument();
    });

    it('null user defaults to VIEWER permissions', () => {
      setNoUser();
      renderSidebar();
      expect(screen.getByText('ダッシュボード')).toBeInTheDocument();
      expect(screen.queryByText('システム管理')).not.toBeInTheDocument();
    });
  });
});
