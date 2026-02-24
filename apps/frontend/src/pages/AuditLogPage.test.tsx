import { beforeEach, describe, expect, it, vi } from 'vitest';
import { render, screen, waitFor } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import AuditLogPage from './AuditLogPage';
import { useRequireAuth } from '../hooks/useRequireAuth';
import { getAuditLogs } from '../api/getAuditLogs';
import type { GetAuditLogsResult } from '../api/getAuditLogs';

vi.mock('../hooks/useRequireAuth', () => ({
  useRequireAuth: vi.fn(),
}));

vi.mock('../api/getAuditLogs', () => ({
  getAuditLogs: vi.fn(),
  getAuditLogsErrorMessage: (error: unknown) =>
    error instanceof Error ? error.message : '監査ログの取得に失敗しました',
}));

vi.mock(
  'react-router-dom',
  async () => (await import('../test/pageTestMocks')).reactRouterDomMocks
);
vi.mock('../views/common', async () => (await import('../test/pageTestMocks')).commonViewMocks);

vi.mock('../views/system/AuditLogFilter', () => ({
  AuditLogFilter: ({
    onChange,
    onSearch,
  }: {
    values: {
      userId: string;
      actionType: string;
      dateFrom: string;
      dateTo: string;
    };
    onChange: (values: {
      userId: string;
      actionType: string;
      dateFrom: string;
      dateTo: string;
    }) => void;
    onSearch: () => void;
  }) => (
    <div data-testid="audit-log-filter">
      <button
        data-testid="set-filter-btn"
        onClick={() =>
          onChange({
            userId: 'admin',
            actionType: 'LOGIN',
            dateFrom: '2026-01-01',
            dateTo: '2026-01-31',
          })
        }
      >
        フィルター設定
      </button>
      <button data-testid="search-btn" onClick={onSearch}>
        検索
      </button>
    </div>
  ),
}));

vi.mock('../views/system/AuditLogTable', () => ({
  AuditLogTable: ({ auditLogs }: { auditLogs: unknown[] }) => (
    <div data-testid="audit-log-table">{auditLogs.length}</div>
  ),
}));

const mockUseRequireAuth = vi.mocked(useRequireAuth);
const mockGetAuditLogs = vi.mocked(getAuditLogs);

const createMockResult = (overrides: Partial<GetAuditLogsResult> = {}): GetAuditLogsResult => ({
  auditLogs: [],
  totalCount: 0,
  totalPages: 0,
  currentPage: 0,
  ...overrides,
});

describe('AuditLogPage', () => {
  beforeEach(() => {
    vi.clearAllMocks();
    mockUseRequireAuth.mockReturnValue(null);
    mockGetAuditLogs.mockResolvedValue(createMockResult());
  });

  it('auth guard が JSX を返す場合はそれを表示', () => {
    mockUseRequireAuth.mockReturnValue(<div data-testid="navigate" data-to="/login" />);

    render(<AuditLogPage />);

    expect(screen.getByTestId('navigate')).toHaveAttribute('data-to', '/login');
  });

  it('認証済みの場合はフィルターと検索ボタンを表示', () => {
    render(<AuditLogPage />);

    expect(screen.getByRole('heading', { level: 1, name: '監査ログ' })).toBeInTheDocument();
    expect(screen.getByTestId('audit-log-filter')).toBeInTheDocument();
    expect(screen.getByTestId('search-btn')).toBeInTheDocument();
  });

  it('検索ボタンクリックで監査ログを取得して結果を表示', async () => {
    mockGetAuditLogs.mockResolvedValue(
      createMockResult({
        auditLogs: [
          {
            id: 1,
            userId: 'admin',
            actionType: 'LOGIN',
            actionTypeDisplayName: 'ログイン',
            entityType: null,
            entityTypeDisplayName: null,
            entityId: null,
            description: 'ログイン成功',
            ipAddress: '127.0.0.1',
            createdAt: '2026-01-15T10:30:00',
          },
        ],
        totalCount: 1,
        totalPages: 1,
        currentPage: 0,
      })
    );

    render(<AuditLogPage />);

    const user = userEvent.setup();
    await user.click(screen.getByTestId('set-filter-btn'));
    await user.click(screen.getByTestId('search-btn'));

    await waitFor(() => {
      expect(mockGetAuditLogs).toHaveBeenCalledWith({
        userId: 'admin',
        actionType: 'LOGIN',
        dateFrom: '2026-01-01',
        dateTo: '2026-01-31',
      });
    });

    expect(screen.getByText('全 1 件（1 / 1 ページ）')).toBeInTheDocument();
    expect(screen.getByTestId('audit-log-table')).toBeInTheDocument();
  });

  it('API エラー時にエラーメッセージを表示する', async () => {
    mockGetAuditLogs.mockRejectedValue(new Error('サーバーエラー'));

    render(<AuditLogPage />);

    await userEvent.setup().click(screen.getByTestId('search-btn'));

    await waitFor(() => {
      expect(screen.getByTestId('error-message')).toHaveTextContent('サーバーエラー');
    });
  });

  it('フィルター未設定で検索すると空パラメータで API を呼ぶ', async () => {
    render(<AuditLogPage />);

    await userEvent.setup().click(screen.getByTestId('search-btn'));

    await waitFor(() => {
      expect(mockGetAuditLogs).toHaveBeenCalledWith({});
    });
  });

  it('totalPages > 1 の場合にページネーションを表示して次ページへ遷移', async () => {
    mockGetAuditLogs
      .mockResolvedValueOnce(
        createMockResult({
          auditLogs: [
            {
              id: 1,
              userId: 'admin',
              actionType: 'LOGIN',
              actionTypeDisplayName: 'ログイン',
              entityType: null,
              entityTypeDisplayName: null,
              entityId: null,
              description: 'ログイン成功',
              ipAddress: '127.0.0.1',
              createdAt: '2026-01-15T10:30:00',
            },
          ],
          totalCount: 40,
          totalPages: 2,
          currentPage: 0,
        })
      )
      .mockResolvedValueOnce(
        createMockResult({
          auditLogs: [
            {
              id: 2,
              userId: 'admin',
              actionType: 'LOGOUT',
              actionTypeDisplayName: 'ログアウト',
              entityType: null,
              entityTypeDisplayName: null,
              entityId: null,
              description: 'ログアウト',
              ipAddress: '127.0.0.1',
              createdAt: '2026-01-15T18:00:00',
            },
          ],
          totalCount: 40,
          totalPages: 2,
          currentPage: 1,
        })
      );

    render(<AuditLogPage />);

    const user = userEvent.setup();
    await user.click(screen.getByTestId('search-btn'));

    await waitFor(() => {
      expect(screen.getByText('前へ')).toBeInTheDocument();
      expect(screen.getByText('次へ')).toBeInTheDocument();
    });

    await user.click(screen.getByText('次へ'));

    await waitFor(() => {
      expect(mockGetAuditLogs).toHaveBeenLastCalledWith({ page: 1 });
    });
  });
});
