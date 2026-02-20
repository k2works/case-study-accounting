import { beforeEach, describe, expect, it, vi } from 'vitest';
import { AxiosError } from 'axios';
import { axiosInstance } from './axios-instance';
import {
  updateAutoJournalPattern,
  getUpdateAutoJournalPatternErrorMessage,
} from './updateAutoJournalPattern';

vi.mock('./axios-instance', () => ({
  axiosInstance: { put: vi.fn() },
}));

const mockPut = vi.mocked(axiosInstance.put);

describe('updateAutoJournalPattern', () => {
  beforeEach(() => {
    mockPut.mockReset();
  });

  it('PUT /api/auto-journal-patterns/:id にペイロードを送信する', async () => {
    const payload = {
      patternName: '更新パターン',
      sourceTableName: 'sales',
      isActive: true,
      items: [
        { lineNumber: 1, debitCreditType: 'DEBIT', accountCode: '1100', amountFormula: 'amount' },
      ],
    };
    mockPut.mockResolvedValue({
      data: { success: true, patternId: 5, message: '更新しました' },
    });

    const result = await updateAutoJournalPattern(5, payload);

    expect(mockPut).toHaveBeenCalledWith('/api/auto-journal-patterns/5', payload);
    expect(result.success).toBe(true);
    expect(result.message).toBe('更新しました');
  });
});

describe('getUpdateAutoJournalPatternErrorMessage', () => {
  it('AxiosError のレスポンスから errorMessage を返す', () => {
    const error = new AxiosError('fail');
    error.response = {
      data: { errorMessage: '更新エラー' },
      status: 400,
      statusText: 'Bad Request',
      headers: {},
      config: {} as never,
    };
    expect(getUpdateAutoJournalPatternErrorMessage(error)).toBe('更新エラー');
  });

  it('Error の message を返す', () => {
    expect(getUpdateAutoJournalPatternErrorMessage(new Error('timeout'))).toBe('timeout');
  });

  it('不明なエラーの場合デフォルトメッセージを返す', () => {
    expect(getUpdateAutoJournalPatternErrorMessage(42)).toBe('自動仕訳パターン更新に失敗しました');
  });
});
