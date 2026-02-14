import { beforeEach, describe, expect, it, vi } from 'vitest';
import { AxiosError } from 'axios';
import { axiosInstance } from './axios-instance';
import { getTrialBalance, getTrialBalanceErrorMessage } from './getTrialBalance';

vi.mock('./axios-instance', () => ({
  axiosInstance: {
    get: vi.fn(),
  },
}));

const mockAxiosGet = vi.mocked(axiosInstance.get);

describe('getTrialBalance', () => {
  beforeEach(() => {
    mockAxiosGet.mockReset();
  });

  it('builds query with date param', async () => {
    mockAxiosGet.mockResolvedValue({
      data: {
        date: '2024-06-30',
        totalDebit: 100000,
        totalCredit: 100000,
        balanced: true,
        difference: 0,
        entries: [
          {
            accountCode: '1000',
            accountName: '現金',
            bsplCategory: 'BS',
            accountType: 'ASSET',
            debitBalance: 50000,
            creditBalance: 0,
          },
        ],
        categorySubtotals: [
          {
            accountType: 'ASSET',
            accountTypeDisplayName: '資産',
            debitSubtotal: 50000,
            creditSubtotal: 0,
          },
        ],
      },
    });

    const result = await getTrialBalance({ date: '2024-06-30' });

    expect(mockAxiosGet).toHaveBeenCalledWith('/api/trial-balance?date=2024-06-30');
    expect(result.entries).toHaveLength(1);
    expect(result.categorySubtotals).toHaveLength(1);
    expect(result.balanced).toBe(true);
  });

  it('omits date param when not provided', async () => {
    mockAxiosGet.mockResolvedValue({
      data: {
        date: null,
        totalDebit: 0,
        totalCredit: 0,
        balanced: true,
        difference: 0,
      },
    });

    const result = await getTrialBalance({});

    expect(mockAxiosGet).toHaveBeenCalledWith('/api/trial-balance');
    expect(result.entries).toEqual([]);
    expect(result.categorySubtotals).toEqual([]);
  });
});

describe('getTrialBalanceErrorMessage', () => {
  it('returns API error message when provided', () => {
    const error = new AxiosError('Request failed');
    error.response = {
      data: { errorMessage: 'サーバーエラー' },
      status: 500,
      statusText: 'Internal Server Error',
      headers: {},
      config: {} as never,
    };

    expect(getTrialBalanceErrorMessage(error)).toBe('サーバーエラー');
  });

  it('returns error message for generic Error', () => {
    expect(getTrialBalanceErrorMessage(new Error('タイムアウト'))).toBe('タイムアウト');
  });

  it('returns default message for unknown errors', () => {
    expect(getTrialBalanceErrorMessage(42)).toBe('残高試算表の取得に失敗しました');
  });
});
