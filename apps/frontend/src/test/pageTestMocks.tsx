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

export const Button = ({
  children,
  onClick,
  variant,
}: {
  children: React.ReactNode;
  onClick?: () => void;
  variant?: string;
}) => (
  <button data-testid={`button-${variant ?? 'default'}`} onClick={onClick}>
    {children}
  </button>
);

export const Pagination = ({
  onPageChange,
  onItemsPerPageChange,
}: {
  currentPage?: number;
  totalPages?: number;
  totalItems?: number;
  itemsPerPage?: number;
  onPageChange: (page: number) => void;
  onItemsPerPageChange?: (size: number) => void;
}) => (
  <div data-testid="pagination">
    <button data-testid="page-change-btn" onClick={() => onPageChange(2)}>
      次ページ
    </button>
    {onItemsPerPageChange && (
      <button data-testid="items-change-btn" onClick={() => onItemsPerPageChange(50)}>
        50 件表示
      </button>
    )}
  </div>
);

/** vi.mock 用のグループ化エクスポート */
export const reactRouterDomMocks = { Navigate };
export const commonViewMocks = { MainLayout, Loading, ErrorMessage, Button, Pagination };
