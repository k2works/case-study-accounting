import { describe, it, expect, vi, beforeEach, afterEach } from 'vitest';
import { render, screen, act } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { SuccessNotification } from './SuccessNotification';

describe('SuccessNotification', () => {
  beforeEach(() => {
    vi.useFakeTimers();
  });

  afterEach(() => {
    vi.useRealTimers();
  });

  it('renders message', () => {
    render(<SuccessNotification message="保存しました" />);
    expect(screen.getByRole('status')).toBeInTheDocument();
    expect(screen.getByText('保存しました')).toBeInTheDocument();
  });

  it('shows check icon', () => {
    render(<SuccessNotification message="成功" />);
    expect(screen.getByText('✓')).toBeInTheDocument();
  });

  describe('dismiss button', () => {
    it('shows dismiss button when onDismiss is provided', () => {
      render(<SuccessNotification message="成功" onDismiss={() => {}} />);
      expect(screen.getByRole('button', { name: '閉じる' })).toBeInTheDocument();
    });

    it('does not show dismiss button when onDismiss is not provided', () => {
      render(<SuccessNotification message="成功" />);
      expect(screen.queryByRole('button', { name: '閉じる' })).not.toBeInTheDocument();
    });

    it('calls onDismiss when dismiss button is clicked', async () => {
      vi.useRealTimers();
      const user = userEvent.setup();
      const handleDismiss = vi.fn();
      render(<SuccessNotification message="成功" onDismiss={handleDismiss} />);

      await user.click(screen.getByRole('button', { name: '閉じる' }));
      expect(handleDismiss).toHaveBeenCalledTimes(1);
    });
  });

  describe('auto hide', () => {
    it('calls onDismiss after default duration', () => {
      const handleDismiss = vi.fn();
      render(<SuccessNotification message="成功" onDismiss={handleDismiss} />);

      expect(handleDismiss).not.toHaveBeenCalled();

      act(() => {
        vi.advanceTimersByTime(3000);
      });

      expect(handleDismiss).toHaveBeenCalledTimes(1);
    });

    it('calls onDismiss after custom duration', () => {
      const handleDismiss = vi.fn();
      render(
        <SuccessNotification message="成功" onDismiss={handleDismiss} autoHideDuration={5000} />
      );

      act(() => {
        vi.advanceTimersByTime(3000);
      });
      expect(handleDismiss).not.toHaveBeenCalled();

      act(() => {
        vi.advanceTimersByTime(2000);
      });
      expect(handleDismiss).toHaveBeenCalledTimes(1);
    });

    it('does not auto hide when autoHideDuration is 0', () => {
      const handleDismiss = vi.fn();
      render(<SuccessNotification message="成功" onDismiss={handleDismiss} autoHideDuration={0} />);

      act(() => {
        vi.advanceTimersByTime(10000);
      });
      expect(handleDismiss).not.toHaveBeenCalled();
    });

    it('does not set timer when onDismiss is not provided', () => {
      render(<SuccessNotification message="成功" />);

      act(() => {
        vi.advanceTimersByTime(10000);
      });
      expect(screen.getByText('成功')).toBeInTheDocument();
    });

    it('clears timer on unmount', () => {
      const handleDismiss = vi.fn();
      const { unmount } = render(<SuccessNotification message="成功" onDismiss={handleDismiss} />);

      unmount();

      act(() => {
        vi.advanceTimersByTime(5000);
      });
      expect(handleDismiss).not.toHaveBeenCalled();
    });
  });
});
