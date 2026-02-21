import { beforeEach, describe, expect, it, vi } from 'vitest';
import { AxiosError } from 'axios';
import { axiosInstance } from './axios-instance';
import { getFinancialAnalysis, getFinancialAnalysisErrorMessage } from './getFinancialAnalysis';

vi.mock('./axios-instance', () => ({
  axiosInstance: {
    get: vi.fn(),
  },
}));

const mockAxiosGet = vi.mocked(axiosInstance.get);

describe('getFinancialAnalysis', () => {
  beforeEach(() => {
    mockAxiosGet.mockReset();
  });

  it('builds query params and returns data with categories defaulting to []', async () => {
    mockAxiosGet.mockResolvedValue({
      data: {
        dateFrom: '2024-01-01',
        dateTo: '2024-12-31',
        comparativeDateFrom: '2023-01-01',
        comparativeDateTo: '2023-12-31',
        categories: [
          {
            categoryName: 'PROFITABILITY',
            categoryDisplayName: '収益性',
            indicators: [],
          },
        ],
      },
    });

    const result = await getFinancialAnalysis({
      dateFrom: '2024-01-01',
      dateTo: '2024-12-31',
      comparativeDateFrom: '2023-01-01',
      comparativeDateTo: '2023-12-31',
    });

    expect(mockAxiosGet).toHaveBeenCalledWith(
      '/api/financial-analysis?dateFrom=2024-01-01&dateTo=2024-12-31&comparativeDateFrom=2023-01-01&comparativeDateTo=2023-12-31'
    );
    expect(result.categories).toHaveLength(1);
  });

  it('omits empty params and defaults categories to empty array', async () => {
    mockAxiosGet.mockResolvedValue({
      data: {
        dateFrom: null,
        dateTo: null,
        comparativeDateFrom: null,
        comparativeDateTo: null,
      },
    });

    const result = await getFinancialAnalysis({});

    expect(mockAxiosGet).toHaveBeenCalledWith('/api/financial-analysis');
    expect(result.categories).toEqual([]);
  });
});

describe('getFinancialAnalysisErrorMessage', () => {
  it('returns API error message when provided', () => {
    const error = new AxiosError('Request failed');
    error.response = {
      data: { errorMessage: 'エラーです' },
      status: 400,
      statusText: 'Bad Request',
      headers: {},
      config: {} as never,
    };

    expect(getFinancialAnalysisErrorMessage(error)).toBe('エラーです');
  });

  it('returns error message for generic Error', () => {
    expect(getFinancialAnalysisErrorMessage(new Error('failure'))).toBe('failure');
  });

  it('returns default message for unknown errors', () => {
    expect(getFinancialAnalysisErrorMessage('unknown')).toBe('財務分析の取得に失敗しました');
  });
});
