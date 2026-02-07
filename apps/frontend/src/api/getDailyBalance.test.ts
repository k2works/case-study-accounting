import { beforeEach, describe, expect, it, vi } from 'vitest';
import { AxiosError } from 'axios';
import { axiosInstance } from './axios-instance';
import { getDailyBalance, getDailyBalanceErrorMessage } from './getDailyBalance';

vi.mock('./axios-instance', () => ({
  axiosInstance: {
    get: vi.fn(),
  },
}));

const mockAxiosGet = vi.mocked(axiosInstance.get);

describe('getDailyBalance', () => {
  beforeEach(() => {
    mockAxiosGet.mockReset();
  });

  it('builds query params and returns normalized entries', async () => {
    mockAxiosGet.mockResolvedValue({
      data: {
        accountId: 1,
        accountCode: '101',
        accountName: '現金',
        openingBalance: 0,
        debitTotal: 1000,
        creditTotal: 500,
        closingBalance: 500,
        entries: [
          {
            date: '2024-01-01',
            debitTotal: 1000,
            creditTotal: 500,
            balance: 500,
            transactionCount: 1,
          },
        ],
      },
    });

    const result = await getDailyBalance({
      accountId: 1,
      dateFrom: '2024-01-01',
      dateTo: '2024-01-31',
    });

    expect(mockAxiosGet).toHaveBeenCalledWith(
      '/api/daily-balance?accountId=1&dateFrom=2024-01-01&dateTo=2024-01-31'
    );
    expect(result.entries).toHaveLength(1);
    expect(result.entries[0]?.date).toBe('2024-01-01');
  });

  it('omits empty params and defaults entries to empty array', async () => {
    mockAxiosGet.mockResolvedValue({
      data: {
        accountId: 2,
        accountCode: '102',
        accountName: '普通預金',
        openingBalance: 0,
        debitTotal: 0,
        creditTotal: 0,
        closingBalance: 0,
      },
    });

    const result = await getDailyBalance({ accountId: 2, dateFrom: '' });

    expect(mockAxiosGet).toHaveBeenCalledWith('/api/daily-balance?accountId=2');
    expect(result.entries).toEqual([]);
  });
});

describe('getDailyBalanceErrorMessage', () => {
  it('returns API error message when provided', () => {
    const error = new AxiosError('Request failed');
    error.response = {
      data: { errorMessage: 'エラーです' },
      status: 400,
      statusText: 'Bad Request',
      headers: {},
      config: {} as never,
    };

    expect(getDailyBalanceErrorMessage(error)).toBe('エラーです');
  });

  it('returns error message for generic Error', () => {
    expect(getDailyBalanceErrorMessage(new Error('failure'))).toBe('failure');
  });

  it('returns default message for unknown errors', () => {
    expect(getDailyBalanceErrorMessage('unknown')).toBe('日次残高の取得に失敗しました');
  });
});
