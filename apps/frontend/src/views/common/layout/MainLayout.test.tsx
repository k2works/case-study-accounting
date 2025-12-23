import { describe, it, expect, vi } from 'vitest';
import { render, screen } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { BrowserRouter } from 'react-router-dom';
import { MainLayout } from './MainLayout';

vi.mock('../../../hooks/useAuth', () => ({
  useAuth: () => ({
    user: { username: 'testuser', role: 'USER' },
    logout: vi.fn(),
  }),
}));

vi.mock('../../../config', () => ({
  config: {
    appName: 'テストアプリ',
  },
}));

const renderWithRouter = (ui: React.ReactElement) => {
  return render(<BrowserRouter>{ui}</BrowserRouter>);
};

describe('MainLayout', () => {
  it('renders children', () => {
    renderWithRouter(
      <MainLayout>
        <div>コンテンツ</div>
      </MainLayout>
    );
    expect(screen.getByText('コンテンツ')).toBeInTheDocument();
  });

  it('renders header', () => {
    renderWithRouter(
      <MainLayout>
        <div>コンテンツ</div>
      </MainLayout>
    );
    expect(screen.getByTestId('header')).toBeInTheDocument();
  });

  it('renders sidebar', () => {
    renderWithRouter(
      <MainLayout>
        <div>コンテンツ</div>
      </MainLayout>
    );
    expect(screen.getByRole('navigation')).toBeInTheDocument();
  });

  it('renders breadcrumbs when provided', () => {
    renderWithRouter(
      <MainLayout breadcrumbs={[{ label: 'ホーム', path: '/' }, { label: '設定ページ' }]}>
        <div>コンテンツ</div>
      </MainLayout>
    );
    expect(screen.getByText('ホーム')).toBeInTheDocument();
    expect(screen.getByText('設定ページ')).toBeInTheDocument();
  });

  it('does not render breadcrumbs when not provided', () => {
    renderWithRouter(
      <MainLayout>
        <div>コンテンツ</div>
      </MainLayout>
    );
    expect(screen.queryByRole('navigation', { name: 'パンくずリスト' })).not.toBeInTheDocument();
  });

  it('does not render breadcrumbs when empty array', () => {
    renderWithRouter(
      <MainLayout breadcrumbs={[]}>
        <div>コンテンツ</div>
      </MainLayout>
    );
    expect(screen.queryByRole('navigation', { name: 'パンくずリスト' })).not.toBeInTheDocument();
  });

  it('renders menu toggle button', () => {
    renderWithRouter(
      <MainLayout>
        <div>コンテンツ</div>
      </MainLayout>
    );
    expect(screen.getByRole('button', { name: 'メニュー' })).toBeInTheDocument();
  });

  it('toggles sidebar when menu button is clicked', async () => {
    const user = userEvent.setup();
    const { container } = renderWithRouter(
      <MainLayout>
        <div>コンテンツ</div>
      </MainLayout>
    );

    expect(container.querySelector('.is-open')).not.toBeInTheDocument();

    await user.click(screen.getByRole('button', { name: 'メニュー' }));
    expect(container.querySelector('.sidebar.is-open')).toBeInTheDocument();
    expect(container.querySelector('.main-layout__overlay.is-visible')).toBeInTheDocument();
    expect(container.querySelector('.main-layout__menu-toggle.is-open')).toBeInTheDocument();
  });

  it('closes sidebar when overlay is clicked', async () => {
    const user = userEvent.setup();
    const { container } = renderWithRouter(
      <MainLayout>
        <div>コンテンツ</div>
      </MainLayout>
    );

    await user.click(screen.getByRole('button', { name: 'メニュー' }));
    expect(container.querySelector('.sidebar.is-open')).toBeInTheDocument();

    const overlay = container.querySelector('.main-layout__overlay');
    await user.click(overlay!);
    expect(container.querySelector('.sidebar.is-open')).not.toBeInTheDocument();
  });

  it('toggles sidebar open and close', async () => {
    const user = userEvent.setup();
    const { container } = renderWithRouter(
      <MainLayout>
        <div>コンテンツ</div>
      </MainLayout>
    );

    const menuButton = screen.getByRole('button', { name: 'メニュー' });

    await user.click(menuButton);
    expect(container.querySelector('.sidebar.is-open')).toBeInTheDocument();

    await user.click(menuButton);
    expect(container.querySelector('.sidebar.is-open')).not.toBeInTheDocument();
  });
});
