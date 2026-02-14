/* eslint-disable react-refresh/only-export-components */
import React from 'react';

export const Navigate = ({ to }: { to: string }) => <div data-testid="navigate" data-to={to} />;

export const MainLayout = ({ children }: { children: React.ReactNode }) => (
  <div data-testid="main-layout">{children}</div>
);

export const Loading = ({ message }: { message?: string }) => (
  <div data-testid="loading">{message}</div>
);

export const ErrorMessage = ({ message, onRetry }: { message: string; onRetry?: () => void }) => (
  <button data-testid="error-message" onClick={onRetry}>
    {message}
  </button>
);

/** vi.mock 用のグループ化エクスポート */
export const reactRouterDomMocks = { Navigate };
export const commonViewMocks = { MainLayout, Loading, ErrorMessage };
