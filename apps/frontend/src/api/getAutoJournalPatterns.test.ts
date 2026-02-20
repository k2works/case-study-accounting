import { beforeEach, describe, expect, it, vi } from 'vitest';
import { AxiosError } from 'axios';
import { axiosInstance } from './axios-instance';
import {
  getAutoJournalPatterns,
  getAutoJournalPattern,
  getAutoJournalPatternsErrorMessage,
} from './getAutoJournalPatterns';

vi.mock('./axios-instance', () => ({
  axiosInstance: { get: vi.fn() },
}));

const mockGet = vi.mocked(axiosInstance.get);

describe('getAutoJournalPatterns', () => {
  beforeEach(() => {
    mockGet.mockReset();
  });

  it('GET /api/auto-journal-patterns で一覧を取得する', async () => {
    const mockData = [
      {
        patternId: 1,
        patternCode: 'P001',
        patternName: '売上パターン',
        sourceTableName: 'sales',
        isActive: true,
        items: [],
      },
    ];
    mockGet.mockResolvedValue({ data: mockData });

    const result = await getAutoJournalPatterns();

    expect(mockGet).toHaveBeenCalledWith('/api/auto-journal-patterns');
    expect(result).toEqual(mockData);
  });
});

describe('getAutoJournalPattern', () => {
  beforeEach(() => {
    mockGet.mockReset();
  });

  it('GET /api/auto-journal-patterns/:id で単一データを取得する', async () => {
    const mockData = {
      patternId: 1,
      patternCode: 'P001',
      patternName: '売上パターン',
      sourceTableName: 'sales',
      isActive: true,
      items: [
        { lineNumber: 1, debitCreditType: 'DEBIT', accountCode: '1100', amountFormula: 'amount' },
      ],
    };
    mockGet.mockResolvedValue({ data: mockData });

    const result = await getAutoJournalPattern(1);

    expect(mockGet).toHaveBeenCalledWith('/api/auto-journal-patterns/1');
    expect(result).toEqual(mockData);
  });
});

describe('getAutoJournalPatternsErrorMessage', () => {
  it('AxiosError のレスポンスから errorMessage を返す', () => {
    const error = new AxiosError('fail');
    error.response = {
      data: { errorMessage: '取得エラー' },
      status: 500,
      statusText: 'Internal Server Error',
      headers: {},
      config: {} as never,
    };
    expect(getAutoJournalPatternsErrorMessage(error)).toBe('取得エラー');
  });

  it('Error の message を返す', () => {
    expect(getAutoJournalPatternsErrorMessage(new Error('network'))).toBe('network');
  });

  it('不明なエラーの場合デフォルトメッセージを返す', () => {
    expect(getAutoJournalPatternsErrorMessage(undefined)).toBe(
      '自動仕訳パターン一覧の取得に失敗しました'
    );
  });
});
