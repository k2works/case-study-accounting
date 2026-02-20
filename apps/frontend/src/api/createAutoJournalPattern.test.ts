import { beforeEach, describe, expect, it, vi } from 'vitest';
import { AxiosError } from 'axios';
import { axiosInstance } from './axios-instance';
import {
  createAutoJournalPattern,
  getCreateAutoJournalPatternErrorMessage,
} from './createAutoJournalPattern';

vi.mock('./axios-instance', () => ({
  axiosInstance: { post: vi.fn() },
}));

const mockPost = vi.mocked(axiosInstance.post);

describe('createAutoJournalPattern', () => {
  beforeEach(() => {
    mockPost.mockReset();
  });

  it('POST /api/auto-journal-patterns にペイロードを送信する', async () => {
    const payload = {
      patternCode: 'P001',
      patternName: '売上パターン',
      sourceTableName: 'sales',
      description: 'テスト',
      items: [
        { lineNumber: 1, debitCreditType: 'DEBIT', accountCode: '1100', amountFormula: 'amount' },
      ],
    };
    mockPost.mockResolvedValue({
      data: { success: true, patternId: 1, patternCode: 'P001' },
    });

    const result = await createAutoJournalPattern(payload);

    expect(mockPost).toHaveBeenCalledWith('/api/auto-journal-patterns', payload);
    expect(result.success).toBe(true);
    expect(result.patternId).toBe(1);
  });
});

describe('getCreateAutoJournalPatternErrorMessage', () => {
  it('AxiosError のレスポンスから errorMessage を返す', () => {
    const error = new AxiosError('fail');
    error.response = {
      data: { errorMessage: 'コードが既に使用されています' },
      status: 409,
      statusText: 'Conflict',
      headers: {},
      config: {} as never,
    };
    expect(getCreateAutoJournalPatternErrorMessage(error)).toBe('コードが既に使用されています');
  });

  it('Error の message を返す', () => {
    expect(getCreateAutoJournalPatternErrorMessage(new Error('接続エラー'))).toBe('接続エラー');
  });

  it('不明なエラーの場合デフォルトメッセージを返す', () => {
    expect(getCreateAutoJournalPatternErrorMessage(null)).toBe(
      '自動仕訳パターン登録に失敗しました'
    );
  });
});
