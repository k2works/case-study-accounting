import { beforeEach, describe, expect, it, vi } from 'vitest';
import { AxiosError } from 'axios';
import { axiosInstance } from './axios-instance';
import { getSubsidiaryLedger, getSubsidiaryLedgerErrorMessage } from './getSubsidiaryLedger';

vi.mock('./axios-instance', () => ({
  axiosInstance: {
    get: vi.fn(),
  },
}));

const mockAxiosGet = vi.mocked(axiosInstance.get);

describe('getSubsidiaryLedger', () => {
  beforeEach(() => {
    mockAxiosGet.mockReset();
  });

  it('builds query params and returns normalized content', async () => {
    mockAxiosGet.mockResolvedValue({
      data: {
        content: [
          {
            journalEntryId: 1,
            journalDate: '2024-01-01',
            description: 'テスト仕訳',
            debitAmount: 1000,
            creditAmount: 0,
            runningBalance: 1000,
          },
        ],
        accountCode: '101',
        accountName: '現金',
        subAccountCode: 'A01',
        openingBalance: 0,
        debitTotal: 1000,
        creditTotal: 0,
        closingBalance: 1000,
        page: 0,
        size: 20,
        totalElements: 1,
        totalPages: 1,
      },
    });

    const result = await getSubsidiaryLedger({
      accountCode: '101',
      subAccountCode: 'A01',
      dateFrom: '2024-01-01',
      dateTo: '2024-01-31',
      page: 0,
      size: 20,
    });

    expect(mockAxiosGet).toHaveBeenCalledWith(
      '/api/subsidiary-ledger?accountCode=101&subAccountCode=A01&dateFrom=2024-01-01&dateTo=2024-01-31&page=0&size=20'
    );
    expect(result.content).toHaveLength(1);
    expect(result.content[0]?.journalDate).toBe('2024-01-01');
  });

  it('omits empty params and defaults content to empty array', async () => {
    mockAxiosGet.mockResolvedValue({
      data: {
        accountCode: '102',
        accountName: '普通預金',
        subAccountCode: '',
        openingBalance: 0,
        debitTotal: 0,
        creditTotal: 0,
        closingBalance: 0,
        page: 0,
        size: 20,
        totalElements: 0,
        totalPages: 0,
      },
    });

    const result = await getSubsidiaryLedger({ accountCode: '102', subAccountCode: '' });

    expect(mockAxiosGet).toHaveBeenCalledWith('/api/subsidiary-ledger?accountCode=102');
    expect(result.content).toEqual([]);
  });
});

describe('getSubsidiaryLedgerErrorMessage', () => {
  it('returns API error message when provided', () => {
    const error = new AxiosError('Request failed');
    error.response = {
      data: { errorMessage: 'エラーです' },
      status: 400,
      statusText: 'Bad Request',
      headers: {},
      config: {} as never,
    };

    expect(getSubsidiaryLedgerErrorMessage(error)).toBe('エラーです');
  });

  it('returns error message for generic Error', () => {
    expect(getSubsidiaryLedgerErrorMessage(new Error('failure'))).toBe('failure');
  });

  it('returns default message for unknown errors', () => {
    expect(getSubsidiaryLedgerErrorMessage('unknown')).toBe('補助元帳の取得に失敗しました');
  });
});
