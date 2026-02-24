import { beforeEach, describe, expect, it, vi } from 'vitest';
import { AxiosError } from 'axios';
import { axiosInstance } from './axios-instance';
import { getAuditLogs, getAuditLogsErrorMessage } from './getAuditLogs';

vi.mock('./axios-instance', () => ({
  axiosInstance: {
    get: vi.fn(),
  },
}));

const mockAxiosGet = vi.mocked(axiosInstance.get);

describe('getAuditLogs', () => {
  beforeEach(() => {
    mockAxiosGet.mockReset();
  });

  it('builds query params and returns auditLogs with default []', async () => {
    mockAxiosGet.mockResolvedValue({
      data: {
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
        totalCount: 100,
        totalPages: 5,
        currentPage: 0,
      },
    });

    const result = await getAuditLogs({
      userId: 'admin',
      actionType: 'LOGIN',
      dateFrom: '2026-01-01',
      dateTo: '2026-01-31',
      page: 0,
      size: 20,
    });

    expect(mockAxiosGet).toHaveBeenCalledWith(
      '/api/audit-logs?userId=admin&actionType=LOGIN&dateFrom=2026-01-01&dateTo=2026-01-31&page=0&size=20'
    );
    expect(result.auditLogs).toHaveLength(1);
  });

  it('omits empty params and defaults auditLogs to empty array', async () => {
    mockAxiosGet.mockResolvedValue({
      data: {
        totalCount: 0,
        totalPages: 0,
        currentPage: 0,
      },
    });

    const result = await getAuditLogs({});

    expect(mockAxiosGet).toHaveBeenCalledWith('/api/audit-logs');
    expect(result.auditLogs).toEqual([]);
  });
});

describe('getAuditLogsErrorMessage', () => {
  it('returns API error message when provided', () => {
    const error = new AxiosError('Request failed');
    error.response = {
      data: { errorMessage: 'エラーです' },
      status: 400,
      statusText: 'Bad Request',
      headers: {},
      config: {} as never,
    };

    expect(getAuditLogsErrorMessage(error)).toBe('エラーです');
  });

  it('returns error message for generic Error', () => {
    expect(getAuditLogsErrorMessage(new Error('failure'))).toBe('failure');
  });

  it('returns default message for unknown errors', () => {
    expect(getAuditLogsErrorMessage('unknown')).toBe('監査ログの取得に失敗しました');
  });
});
