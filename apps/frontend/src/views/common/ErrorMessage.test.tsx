import { describe, it, expect, vi } from 'vitest';
import { render, screen } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { ErrorMessage } from './ErrorMessage';

describe('ErrorMessage', () => {
  it('renders error message', () => {
    render(<ErrorMessage message="Something went wrong" />);
    expect(screen.getByRole('alert')).toBeInTheDocument();
    expect(screen.getByText('Something went wrong')).toBeInTheDocument();
  });

  it('shows warning icon', () => {
    render(<ErrorMessage message="Error" />);
    expect(screen.getByText('⚠')).toBeInTheDocument();
  });

  describe('retry button', () => {
    it('shows retry button when onRetry is provided', () => {
      render(<ErrorMessage message="Error" onRetry={() => {}} />);
      expect(screen.getByRole('button', { name: '再試行' })).toBeInTheDocument();
    });

    it('does not show retry button when onRetry is not provided', () => {
      render(<ErrorMessage message="Error" />);
      expect(screen.queryByRole('button', { name: '再試行' })).not.toBeInTheDocument();
    });

    it('calls onRetry when retry button is clicked', async () => {
      const user = userEvent.setup();
      const handleRetry = vi.fn();
      render(<ErrorMessage message="Error" onRetry={handleRetry} />);

      await user.click(screen.getByRole('button', { name: '再試行' }));
      expect(handleRetry).toHaveBeenCalledTimes(1);
    });
  });

  describe('dismiss button', () => {
    it('shows dismiss button when onDismiss is provided', () => {
      render(<ErrorMessage message="Error" onDismiss={() => {}} />);
      expect(screen.getByRole('button', { name: '閉じる' })).toBeInTheDocument();
    });

    it('does not show dismiss button when onDismiss is not provided', () => {
      render(<ErrorMessage message="Error" />);
      expect(screen.queryByRole('button', { name: '閉じる' })).not.toBeInTheDocument();
    });

    it('calls onDismiss when dismiss button is clicked', async () => {
      const user = userEvent.setup();
      const handleDismiss = vi.fn();
      render(<ErrorMessage message="Error" onDismiss={handleDismiss} />);

      await user.click(screen.getByRole('button', { name: '閉じる' }));
      expect(handleDismiss).toHaveBeenCalledTimes(1);
    });
  });

  it('shows both retry and dismiss buttons', () => {
    render(<ErrorMessage message="Error" onRetry={() => {}} onDismiss={() => {}} />);
    expect(screen.getByRole('button', { name: '再試行' })).toBeInTheDocument();
    expect(screen.getByRole('button', { name: '閉じる' })).toBeInTheDocument();
  });
});
