import { describe, it, expect } from 'vitest';
import { render, screen } from '@testing-library/react';
import { MoneyDisplay } from './MoneyDisplay';

describe('MoneyDisplay', () => {
  it('renders amount with default currency', () => {
    render(<MoneyDisplay amount={1000} />);
    expect(screen.getByText('¥1,000')).toBeInTheDocument();
  });

  it('renders amount with custom currency', () => {
    render(<MoneyDisplay amount={1000} currency="$" />);
    expect(screen.getByText('$1,000')).toBeInTheDocument();
  });

  it('formats large numbers with comma separators', () => {
    render(<MoneyDisplay amount={1234567} />);
    expect(screen.getByText('¥1,234,567')).toBeInTheDocument();
  });

  describe('sign display', () => {
    it('does not show sign by default', () => {
      render(<MoneyDisplay amount={1000} />);
      expect(screen.getByText('¥1,000')).toBeInTheDocument();
    });

    it('shows positive sign when showSign is true', () => {
      render(<MoneyDisplay amount={1000} showSign />);
      expect(screen.getByText('+¥1,000')).toBeInTheDocument();
    });

    it('shows negative sign when showSign is true', () => {
      render(<MoneyDisplay amount={-1000} showSign />);
      expect(screen.getByText('-¥1,000')).toBeInTheDocument();
    });

    it('does not show sign for zero', () => {
      render(<MoneyDisplay amount={0} showSign />);
      expect(screen.getByText('¥0')).toBeInTheDocument();
    });
  });

  describe('colorize', () => {
    it('applies positive class for positive amount', () => {
      const { container } = render(<MoneyDisplay amount={1000} colorize />);
      expect(container.querySelector('.money-display--positive')).toBeInTheDocument();
    });

    it('applies negative class for negative amount', () => {
      const { container } = render(<MoneyDisplay amount={-1000} colorize />);
      expect(container.querySelector('.money-display--negative')).toBeInTheDocument();
    });

    it('does not apply color class for zero', () => {
      const { container } = render(<MoneyDisplay amount={0} colorize />);
      expect(container.querySelector('.money-display--positive')).not.toBeInTheDocument();
      expect(container.querySelector('.money-display--negative')).not.toBeInTheDocument();
    });

    it('does not apply color class when colorize is false', () => {
      const { container } = render(<MoneyDisplay amount={1000} colorize={false} />);
      expect(container.querySelector('.money-display--positive')).not.toBeInTheDocument();
    });
  });

  describe('sizes', () => {
    it('renders small size', () => {
      const { container } = render(<MoneyDisplay amount={1000} size="small" />);
      expect(container.querySelector('.money-display--small')).toBeInTheDocument();
    });

    it('renders medium size by default', () => {
      const { container } = render(<MoneyDisplay amount={1000} />);
      expect(container.querySelector('.money-display--medium')).toBeInTheDocument();
    });

    it('renders large size', () => {
      const { container } = render(<MoneyDisplay amount={1000} size="large" />);
      expect(container.querySelector('.money-display--large')).toBeInTheDocument();
    });
  });

  it('handles negative amounts correctly', () => {
    render(<MoneyDisplay amount={-5000} />);
    expect(screen.getByText('¥5,000')).toBeInTheDocument();
  });
});
