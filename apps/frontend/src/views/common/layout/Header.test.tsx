import { describe, it, expect, vi, beforeEach } from 'vitest';
import { render, screen } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { Header } from './Header';

const mockLogout = vi.fn();
const mockUser = { username: 'testuser', role: 'USER' as const };

vi.mock('../../../hooks/useAuth', () => ({
  useAuth: () => ({
    user: mockUser,
    logout: mockLogout,
  }),
}));

vi.mock('../../../config', () => ({
  config: {
    appName: 'テストアプリ',
  },
}));

describe('Header', () => {
  beforeEach(() => {
    vi.clearAllMocks();
  });

  it('renders app name', () => {
    render(<Header />);
    expect(screen.getByText('テストアプリ')).toBeInTheDocument();
  });

  it('renders username', () => {
    render(<Header />);
    expect(screen.getByTestId('header-username')).toHaveTextContent('testuser');
  });

  it('renders logout button', () => {
    render(<Header />);
    expect(screen.getByTestId('logout-button')).toBeInTheDocument();
    expect(screen.getByText('ログアウト')).toBeInTheDocument();
  });

  it('calls logout when logout button is clicked', async () => {
    const user = userEvent.setup();
    render(<Header />);

    await user.click(screen.getByTestId('logout-button'));
    expect(mockLogout).toHaveBeenCalledTimes(1);
  });

  it('has correct data-testid', () => {
    render(<Header />);
    expect(screen.getByTestId('header')).toBeInTheDocument();
  });
});
