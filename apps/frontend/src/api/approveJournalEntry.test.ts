import { beforeEach, describe, expect, it, vi } from 'vitest';
import { AxiosError } from 'axios';
import { axiosInstance } from './axios-instance';
import { approveJournalEntry, approveJournalEntryErrorMessage } from './approveJournalEntry';

vi.mock('./axios-instance', () => ({
  axiosInstance: {
    post: vi.fn(),
  },
}));

const mockAxiosPost = vi.mocked(axiosInstance.post);

describe('approveJournalEntry', () => {
  beforeEach(() => {
    mockAxiosPost.mockReset();
  });

  it('仕訳承認 API を呼び出してレスポンスを返す', async () => {
    mockAxiosPost.mockResolvedValue({
      data: { success: true, journalEntryId: 1, status: 'APPROVED', message: '承認しました' },
    });

    const result = await approveJournalEntry(1);

    expect(mockAxiosPost).toHaveBeenCalledWith('/api/journal-entries/1/approve');
    expect(result.success).toBe(true);
    expect(result.status).toBe('APPROVED');
  });
});

describe('approveJournalEntryErrorMessage', () => {
  it('404 の場合は仕訳が見つかりませんを返す', () => {
    const error = new AxiosError('Not found');
    error.response = {
      data: {},
      status: 404,
      statusText: 'Not Found',
      headers: {},
      config: {} as never,
    };

    expect(approveJournalEntryErrorMessage(error)).toBe('仕訳が見つかりません');
  });

  it('API の errorMessage を優先して返す', () => {
    const error = new AxiosError('Bad Request');
    error.response = {
      data: { errorMessage: '承認できません' },
      status: 400,
      statusText: 'Bad Request',
      headers: {},
      config: {} as never,
    };

    expect(approveJournalEntryErrorMessage(error)).toBe('承認できません');
  });

  it('汎用 Error の message を返す', () => {
    expect(approveJournalEntryErrorMessage(new Error('failure'))).toBe('failure');
  });

  it('未知のエラーはデフォルトメッセージを返す', () => {
    expect(approveJournalEntryErrorMessage('unknown')).toBe('承認に失敗しました');
  });
});
