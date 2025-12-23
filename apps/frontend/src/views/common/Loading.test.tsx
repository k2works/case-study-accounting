import { describe, it, expect } from 'vitest';
import { render, screen } from '@testing-library/react';
import { Loading } from './Loading';

describe('Loading', () => {
  it('renders with default message', () => {
    render(<Loading />);
    expect(screen.getByText('読み込み中...')).toBeInTheDocument();
  });

  it('renders with custom message', () => {
    render(<Loading message="データを取得中..." />);
    expect(screen.getByText('データを取得中...')).toBeInTheDocument();
  });

  it('does not render message when empty', () => {
    render(<Loading message="" />);
    expect(screen.queryByRole('paragraph')).not.toBeInTheDocument();
  });

  describe('sizes', () => {
    it('renders small spinner', () => {
      const { container } = render(<Loading size="small" />);
      expect(container.querySelector('.loading__spinner--small')).toBeInTheDocument();
    });

    it('renders medium spinner by default', () => {
      const { container } = render(<Loading />);
      expect(container.querySelector('.loading__spinner--medium')).toBeInTheDocument();
    });

    it('renders large spinner', () => {
      const { container } = render(<Loading size="large" />);
      expect(container.querySelector('.loading__spinner--large')).toBeInTheDocument();
    });
  });

  describe('fullScreen mode', () => {
    it('renders in fullscreen mode', () => {
      const { container } = render(<Loading fullScreen />);
      expect(container.querySelector('.loading--fullscreen')).toBeInTheDocument();
    });

    it('does not render fullscreen by default', () => {
      const { container } = render(<Loading />);
      expect(container.querySelector('.loading--fullscreen')).not.toBeInTheDocument();
    });
  });
});
