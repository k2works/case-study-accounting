import { beforeEach, describe, expect, it, vi } from 'vitest';
import { AxiosError } from 'axios';
import { axiosInstance } from './axios-instance';
import { getMonthlyBalance, getMonthlyBalanceErrorMessage } from './getMonthlyBalance';

vi.mock('./axios-instance', () => ({
  axiosInstance: {
    get: vi.fn(),
  },
}));

const mockAxiosGet = vi.mocked(axiosInstance.get);

describe('getMonthlyBalance', () => {
  beforeEach(() => {
    mockAxiosGet.mockReset();
  });

  it('builds query params and returns result with entries', async () => {
    mockAxiosGet.mockResolvedValue({
      data: {
        accountCode: '1000',
        accountName: '現金',
        fiscalPeriod: 2024,
        openingBalance: 10000,
        debitTotal: 5000,
        creditTotal: 3000,
        closingBalance: 12000,
        entries: [
          {
            month: 1,
            openingBalance: 10000,
            debitAmount: 5000,
            creditAmount: 3000,
            closingBalance: 12000,
          },
        ],
      },
    });

    const result = await getMonthlyBalance({ accountCode: '1000', fiscalPeriod: 2024 });

    expect(mockAxiosGet).toHaveBeenCalledWith(
      '/api/monthly-balance?accountCode=1000&fiscalPeriod=2024'
    );
    expect(result.entries).toHaveLength(1);
    expect(result.accountCode).toBe('1000');
    expect(result.fiscalPeriod).toBe(2024);
  });

  it('sends request without query params when accountCode is empty', async () => {
    mockAxiosGet.mockResolvedValue({
      data: {
        accountCode: '',
        accountName: '',
        fiscalPeriod: 0,
        openingBalance: 0,
        debitTotal: 0,
        creditTotal: 0,
        closingBalance: 0,
      },
    });

    await getMonthlyBalance({ accountCode: '' });

    expect(mockAxiosGet).toHaveBeenCalledWith('/api/monthly-balance');
  });

  it('omits fiscalPeriod when not provided', async () => {
    mockAxiosGet.mockResolvedValue({
      data: {
        accountCode: '1000',
        accountName: '現金',
        fiscalPeriod: 2024,
        openingBalance: 0,
        debitTotal: 0,
        creditTotal: 0,
        closingBalance: 0,
      },
    });

    const result = await getMonthlyBalance({ accountCode: '1000' });

    expect(mockAxiosGet).toHaveBeenCalledWith('/api/monthly-balance?accountCode=1000');
    expect(result.entries).toEqual([]);
  });
});

describe('getMonthlyBalanceErrorMessage', () => {
  it('returns API error message when provided', () => {
    const error = new AxiosError('Request failed');
    error.response = {
      data: { errorMessage: '勘定科目が見つかりません' },
      status: 404,
      statusText: 'Not Found',
      headers: {},
      config: {} as never,
    };

    expect(getMonthlyBalanceErrorMessage(error)).toBe('勘定科目が見つかりません');
  });

  it('returns error message for generic Error', () => {
    expect(getMonthlyBalanceErrorMessage(new Error('ネットワークエラー'))).toBe(
      'ネットワークエラー'
    );
  });

  it('returns default message for unknown errors', () => {
    expect(getMonthlyBalanceErrorMessage('unknown')).toBe('月次残高の取得に失敗しました');
  });
});
