import { beforeEach, describe, expect, it, vi } from 'vitest';
import { AxiosError } from 'axios';
import { axiosInstance } from './axios-instance';
import {
  deleteAutoJournalPattern,
  getDeleteAutoJournalPatternErrorMessage,
} from './deleteAutoJournalPattern';

vi.mock('./axios-instance', () => ({
  axiosInstance: { delete: vi.fn() },
}));

const mockDelete = vi.mocked(axiosInstance.delete);

describe('deleteAutoJournalPattern', () => {
  beforeEach(() => {
    mockDelete.mockReset();
  });

  it('DELETE /api/auto-journal-patterns/:id を呼び出す', async () => {
    mockDelete.mockResolvedValue({
      data: { success: true, patternId: 3, message: '削除しました' },
    });

    const result = await deleteAutoJournalPattern(3);

    expect(mockDelete).toHaveBeenCalledWith('/api/auto-journal-patterns/3');
    expect(result.success).toBe(true);
    expect(result.message).toBe('削除しました');
  });
});

describe('getDeleteAutoJournalPatternErrorMessage', () => {
  it('AxiosError のレスポンスから errorMessage を返す', () => {
    const error = new AxiosError('fail');
    error.response = {
      data: { errorMessage: '削除エラー' },
      status: 400,
      statusText: 'Bad Request',
      headers: {},
      config: {} as never,
    };
    expect(getDeleteAutoJournalPatternErrorMessage(error)).toBe('削除エラー');
  });

  it('Error の message を返す', () => {
    expect(getDeleteAutoJournalPatternErrorMessage(new Error('server error'))).toBe('server error');
  });

  it('不明なエラーの場合デフォルトメッセージを返す', () => {
    expect(getDeleteAutoJournalPatternErrorMessage(null)).toBe(
      '自動仕訳パターンの削除に失敗しました'
    );
  });
});
