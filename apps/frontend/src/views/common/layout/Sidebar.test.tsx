import { describe, it, expect, vi, beforeEach } from 'vitest';
import { render, screen } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { BrowserRouter } from 'react-router-dom';

// Mock useAuth with a factory that we can control
const mockUseAuth = vi.fn();
vi.mock('../../../hooks/useAuth', () => ({
  useAuth: () => mockUseAuth(),
}));

// Import after mock
import { Sidebar } from './Sidebar';

const renderWithRouter = (props = {}) => {
  return render(
    <BrowserRouter>
      <Sidebar {...props} />
    </BrowserRouter>
  );
};

describe('Sidebar', () => {
  beforeEach(() => {
    vi.clearAllMocks();
    // Default: regular USER role
    mockUseAuth.mockReturnValue({
      user: { username: 'testuser', role: 'USER' },
    });
  });

  it('renders navigation', () => {
    renderWithRouter();
    expect(screen.getByRole('navigation')).toBeInTheDocument();
  });

  it('renders dashboard menu item', () => {
    renderWithRouter();
    expect(screen.getByText('ダッシュボード')).toBeInTheDocument();
  });

  it('renders common menu items', () => {
    renderWithRouter();
    expect(screen.getByText('仕訳管理')).toBeInTheDocument();
    expect(screen.getByText('元帳・残高')).toBeInTheDocument();
    expect(screen.getByText('財務諸表')).toBeInTheDocument();
    expect(screen.getByText('マスタ管理')).toBeInTheDocument();
  });

  it('expands submenu when parent is clicked', async () => {
    const user = userEvent.setup();
    renderWithRouter();

    expect(screen.queryByText('仕訳一覧')).not.toBeInTheDocument();

    await user.click(screen.getByText('仕訳管理'));

    expect(screen.getByText('仕訳一覧')).toBeInTheDocument();
    expect(screen.getByText('仕訳入力')).toBeInTheDocument();
  });

  it('collapses submenu when parent is clicked again', async () => {
    const user = userEvent.setup();
    renderWithRouter();

    await user.click(screen.getByText('仕訳管理'));
    expect(screen.getByText('仕訳一覧')).toBeInTheDocument();

    await user.click(screen.getByText('仕訳管理'));
    expect(screen.queryByText('仕訳一覧')).not.toBeInTheDocument();
  });

  it('applies collapsed class when isCollapsed is true', () => {
    const { container } = renderWithRouter({ isCollapsed: true });
    expect(container.querySelector('.is-collapsed')).toBeInTheDocument();
  });

  it('applies open class when isOpen is true', () => {
    const { container } = renderWithRouter({ isOpen: true });
    expect(container.querySelector('.is-open')).toBeInTheDocument();
  });

  it('adds is-open class to arrow when submenu is expanded', async () => {
    const user = userEvent.setup();
    const { container } = renderWithRouter();

    await user.click(screen.getByText('仕訳管理'));
    expect(container.querySelector('.sidebar__arrow.is-open')).toBeInTheDocument();
  });
});

describe('Sidebar with ADMIN role', () => {
  beforeEach(() => {
    vi.clearAllMocks();
    mockUseAuth.mockReturnValue({
      user: { username: 'admin', role: 'ADMIN' },
    });
  });

  it('renders system management menu for admin', () => {
    renderWithRouter();
    expect(screen.getByText('システム管理')).toBeInTheDocument();
  });

  it('renders approval menu for admin', async () => {
    const user = userEvent.setup();
    renderWithRouter();

    await user.click(screen.getByText('仕訳管理'));
    expect(screen.getByText('承認待ち')).toBeInTheDocument();
  });

  it('renders financial analysis for admin', async () => {
    const user = userEvent.setup();
    renderWithRouter();

    await user.click(screen.getByText('財務諸表'));
    expect(screen.getByText('財務分析')).toBeInTheDocument();
  });
});

describe('Sidebar with MANAGER role', () => {
  beforeEach(() => {
    vi.clearAllMocks();
    mockUseAuth.mockReturnValue({
      user: { username: 'manager', role: 'MANAGER' },
    });
  });

  it('does not render system management for manager', () => {
    renderWithRouter();
    expect(screen.queryByText('システム管理')).not.toBeInTheDocument();
  });

  it('renders approval menu for manager', async () => {
    const user = userEvent.setup();
    renderWithRouter();

    await user.click(screen.getByText('仕訳管理'));
    expect(screen.getByText('承認待ち')).toBeInTheDocument();
  });

  it('renders financial analysis for manager', async () => {
    const user = userEvent.setup();
    renderWithRouter();

    await user.click(screen.getByText('財務諸表'));
    expect(screen.getByText('財務分析')).toBeInTheDocument();
  });
});

describe('Sidebar with USER role', () => {
  beforeEach(() => {
    vi.clearAllMocks();
    mockUseAuth.mockReturnValue({
      user: { username: 'testuser', role: 'USER' },
    });
  });

  it('does not render system management for regular user', () => {
    renderWithRouter();
    expect(screen.queryByText('システム管理')).not.toBeInTheDocument();
  });

  it('does not render approval menu for regular user', async () => {
    const user = userEvent.setup();
    renderWithRouter();

    await user.click(screen.getByText('仕訳管理'));
    expect(screen.queryByText('承認待ち')).not.toBeInTheDocument();
  });

  it('does not render financial analysis for regular user', async () => {
    const user = userEvent.setup();
    renderWithRouter();

    await user.click(screen.getByText('財務諸表'));
    expect(screen.queryByText('財務分析')).not.toBeInTheDocument();
  });
});

describe('Sidebar with VIEWER role', () => {
  beforeEach(() => {
    vi.clearAllMocks();
    mockUseAuth.mockReturnValue({
      user: { username: 'viewer', role: 'VIEWER' },
    });
  });

  it('renders only basic menu items for viewer', () => {
    renderWithRouter();
    expect(screen.getByText('ダッシュボード')).toBeInTheDocument();
    expect(screen.queryByText('システム管理')).not.toBeInTheDocument();
  });
});

describe('Sidebar with no user', () => {
  beforeEach(() => {
    vi.clearAllMocks();
    mockUseAuth.mockReturnValue({
      user: null,
    });
  });

  it('uses VIEWER role when user is null', () => {
    renderWithRouter();
    expect(screen.getByText('ダッシュボード')).toBeInTheDocument();
    expect(screen.queryByText('システム管理')).not.toBeInTheDocument();
  });
});
