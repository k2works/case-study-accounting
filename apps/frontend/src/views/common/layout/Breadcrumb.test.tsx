import { describe, it, expect } from 'vitest';
import { render, screen } from '@testing-library/react';
import { BrowserRouter } from 'react-router-dom';
import { Breadcrumb, BreadcrumbItem } from './Breadcrumb';

const renderWithRouter = (items: BreadcrumbItem[]) => {
  return render(
    <BrowserRouter>
      <Breadcrumb items={items} />
    </BrowserRouter>
  );
};

describe('Breadcrumb', () => {
  it('renders null when items is empty', () => {
    const { container } = renderWithRouter([]);
    expect(container.firstChild).toBeNull();
  });

  it('renders single item as text', () => {
    renderWithRouter([{ label: 'ホーム' }]);
    expect(screen.getByText('ホーム')).toBeInTheDocument();
  });

  it('renders multiple items with separators', () => {
    renderWithRouter([
      { label: 'ホーム', path: '/' },
      { label: '仕訳管理', path: '/journals' },
      { label: '仕訳入力' },
    ]);

    expect(screen.getByText('ホーム')).toBeInTheDocument();
    expect(screen.getByText('仕訳管理')).toBeInTheDocument();
    expect(screen.getByText('仕訳入力')).toBeInTheDocument();
    expect(screen.getAllByText('>')).toHaveLength(2);
  });

  it('renders links for items with path', () => {
    renderWithRouter([
      { label: 'ホーム', path: '/' },
      { label: '仕訳管理', path: '/journals' },
      { label: '仕訳入力' },
    ]);

    expect(screen.getByRole('link', { name: 'ホーム' })).toHaveAttribute('href', '/');
    expect(screen.getByRole('link', { name: '仕訳管理' })).toHaveAttribute('href', '/journals');
  });

  it('renders last item as text not link', () => {
    renderWithRouter([
      { label: 'ホーム', path: '/' },
      { label: '仕訳入力', path: '/journals/new' },
    ]);

    // The last item should be text, not a link
    expect(screen.queryAllByRole('link')).toHaveLength(1);
    expect(screen.getByText('仕訳入力')).toBeInTheDocument();
    expect(screen.queryByRole('link', { name: '仕訳入力' })).not.toBeInTheDocument();
  });

  it('renders item without path as text', () => {
    renderWithRouter([
      { label: 'ホーム', path: '/' },
      { label: 'テキストのみ' },
      { label: '最後' },
    ]);

    expect(screen.queryAllByRole('link')).toHaveLength(1);
  });

  it('applies current class to last item', () => {
    const { container } = renderWithRouter([
      { label: 'ホーム', path: '/' },
      { label: '現在のページ' },
    ]);

    expect(container.querySelector('.is-current')).toBeInTheDocument();
  });

  it('has correct aria label', () => {
    renderWithRouter([{ label: 'ホーム' }]);
    expect(screen.getByRole('navigation')).toHaveAttribute('aria-label', 'パンくずリスト');
  });
});
