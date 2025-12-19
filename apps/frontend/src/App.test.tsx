import { describe, it, expect } from 'vitest';
import { render, screen } from '@testing-library/react';
import { App } from './App';

describe('App', () => {
  it('should render the application title', () => {
    render(<App />);
    expect(screen.getByText('財務会計システム')).toBeInTheDocument();
  });

  it('should show that the environment is set up correctly', () => {
    render(<App />);
    expect(screen.getByText('フロントエンド環境が正常に構築されました。')).toBeInTheDocument();
  });
});
