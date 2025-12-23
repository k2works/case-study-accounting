import { describe, it, expect, vi } from 'vitest';
import { render, screen } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { Pagination } from './Pagination';

describe('Pagination', () => {
  const defaultProps = {
    currentPage: 1,
    totalPages: 10,
    totalItems: 100,
    itemsPerPage: 10,
    onPageChange: vi.fn(),
  };

  beforeEach(() => {
    vi.clearAllMocks();
  });

  it('renders pagination controls', () => {
    render(<Pagination {...defaultProps} />);
    expect(screen.getByRole('button', { name: /前へ/ })).toBeInTheDocument();
    expect(screen.getByRole('button', { name: /次へ/ })).toBeInTheDocument();
  });

  it('renders total items count', () => {
    render(<Pagination {...defaultProps} />);
    expect(screen.getByText('全 100 件')).toBeInTheDocument();
  });

  it('formats total items with comma', () => {
    render(<Pagination {...defaultProps} totalItems={1234567} />);
    expect(screen.getByText('全 1,234,567 件')).toBeInTheDocument();
  });

  describe('navigation buttons', () => {
    it('disables previous button on first page', () => {
      render(<Pagination {...defaultProps} currentPage={1} />);
      expect(screen.getByRole('button', { name: /前へ/ })).toBeDisabled();
    });

    it('enables previous button when not on first page', () => {
      render(<Pagination {...defaultProps} currentPage={2} />);
      expect(screen.getByRole('button', { name: /前へ/ })).not.toBeDisabled();
    });

    it('disables next button on last page', () => {
      render(<Pagination {...defaultProps} currentPage={10} totalPages={10} />);
      expect(screen.getByRole('button', { name: /次へ/ })).toBeDisabled();
    });

    it('enables next button when not on last page', () => {
      render(<Pagination {...defaultProps} currentPage={9} totalPages={10} />);
      expect(screen.getByRole('button', { name: /次へ/ })).not.toBeDisabled();
    });

    it('calls onPageChange with previous page', async () => {
      const user = userEvent.setup();
      const onPageChange = vi.fn();
      render(<Pagination {...defaultProps} currentPage={5} onPageChange={onPageChange} />);

      await user.click(screen.getByRole('button', { name: /前へ/ }));
      expect(onPageChange).toHaveBeenCalledWith(4);
    });

    it('calls onPageChange with next page', async () => {
      const user = userEvent.setup();
      const onPageChange = vi.fn();
      render(<Pagination {...defaultProps} currentPage={5} onPageChange={onPageChange} />);

      await user.click(screen.getByRole('button', { name: /次へ/ }));
      expect(onPageChange).toHaveBeenCalledWith(6);
    });
  });

  describe('page numbers', () => {
    it('renders page numbers', () => {
      render(<Pagination {...defaultProps} totalPages={5} />);
      expect(screen.getByRole('button', { name: '1' })).toBeInTheDocument();
      expect(screen.getByRole('button', { name: '2' })).toBeInTheDocument();
      expect(screen.getByRole('button', { name: '5' })).toBeInTheDocument();
    });

    it('highlights current page', () => {
      render(<Pagination {...defaultProps} currentPage={3} totalPages={5} />);
      expect(screen.getByRole('button', { name: '3' })).toHaveClass('is-active');
    });

    it('calls onPageChange when page number is clicked', async () => {
      const user = userEvent.setup();
      const onPageChange = vi.fn();
      render(
        <Pagination {...defaultProps} currentPage={1} totalPages={5} onPageChange={onPageChange} />
      );

      await user.click(screen.getByRole('button', { name: '3' }));
      expect(onPageChange).toHaveBeenCalledWith(3);
    });

    it('shows ellipsis for many pages', () => {
      render(<Pagination {...defaultProps} currentPage={1} totalPages={20} />);
      expect(screen.getByText('...')).toBeInTheDocument();
    });

    it('shows ellipsis on both sides when in middle', () => {
      render(<Pagination {...defaultProps} currentPage={10} totalPages={20} />);
      const ellipses = screen.getAllByText('...');
      expect(ellipses).toHaveLength(2);
    });
  });

  describe('items per page', () => {
    it('does not render items per page selector by default', () => {
      render(<Pagination {...defaultProps} />);
      expect(screen.queryByText('表示件数:')).not.toBeInTheDocument();
    });

    it('renders items per page selector when onItemsPerPageChange is provided', () => {
      render(<Pagination {...defaultProps} onItemsPerPageChange={() => {}} />);
      expect(screen.getByText('表示件数:')).toBeInTheDocument();
    });

    it('renders default items per page options', () => {
      render(<Pagination {...defaultProps} onItemsPerPageChange={() => {}} />);
      const select = screen.getByRole('combobox');
      expect(select).toBeInTheDocument();
      expect(select.querySelectorAll('option')).toHaveLength(4);
    });

    it('renders custom items per page options', () => {
      render(
        <Pagination
          {...defaultProps}
          onItemsPerPageChange={() => {}}
          itemsPerPageOptions={[5, 15, 25]}
        />
      );
      const select = screen.getByRole('combobox');
      expect(select.querySelectorAll('option')).toHaveLength(3);
    });

    it('calls onItemsPerPageChange when selection changes', async () => {
      const user = userEvent.setup();
      const onItemsPerPageChange = vi.fn();
      render(
        <Pagination
          {...defaultProps}
          itemsPerPage={10}
          onItemsPerPageChange={onItemsPerPageChange}
        />
      );

      await user.selectOptions(screen.getByRole('combobox'), '20');
      expect(onItemsPerPageChange).toHaveBeenCalledWith(20);
    });
  });

  describe('edge cases', () => {
    it('returns null when single page and no items per page selector', () => {
      const { container } = render(<Pagination {...defaultProps} totalPages={1} currentPage={1} />);
      expect(container.firstChild).toBeNull();
    });

    it('renders when single page but has items per page selector', () => {
      render(
        <Pagination
          {...defaultProps}
          totalPages={1}
          currentPage={1}
          onItemsPerPageChange={() => {}}
        />
      );
      expect(screen.getByText('表示件数:')).toBeInTheDocument();
    });
  });
});
