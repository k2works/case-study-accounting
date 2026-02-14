import { beforeEach, describe, expect, it, vi } from 'vitest';
import { AxiosError } from 'axios';
import { axiosInstance } from './axios-instance';
import { rejectJournalEntry, rejectJournalEntryErrorMessage } from './rejectJournalEntry';
import { createErrorMessageTestCases } from './api-test-helpers';

vi.mock('./axios-instance', () => ({
  axiosInstance: {
    post: vi.fn(),
  },
}));

const mockAxiosPost = vi.mocked(axiosInstance.post);

describe('rejectJournalEntry', () => {
  beforeEach(() => {
    mockAxiosPost.mockReset();
  });

  it('差し戻し API を呼び出してレスポンスを返す', async () => {
    mockAxiosPost.mockResolvedValue({
      data: {
        success: true,
        journalEntryId: 1,
        status: 'DRAFT',
        rejectedBy: 'manager',
        rejectionReason: '金額に誤りがあります',
        message: '仕訳を差し戻しました',
      },
    });

    const result = await rejectJournalEntry(1, '金額に誤りがあります');

    expect(mockAxiosPost).toHaveBeenCalledWith('/api/journal-entries/1/reject', {
      rejectionReason: '金額に誤りがあります',
    });
    expect(result.success).toBe(true);
    expect(result.status).toBe('DRAFT');
    expect(result.rejectionReason).toBe('金額に誤りがあります');
  });
});

describe('rejectJournalEntryErrorMessage', () => {
  const testCases = createErrorMessageTestCases(rejectJournalEntryErrorMessage, {
    notFoundMessage: '仕訳が見つかりません',
    customErrorMessage: '承認待ち状態の仕訳のみ差し戻し可能です',
    defaultErrorMessage: '差し戻しに失敗しました',
  });

  testCases.forEach(({ name, run }) => {
    it(name, run);
  });

  it('AxiosError でレスポンスが無い場合はデフォルトメッセージを返す', () => {
    const error = new AxiosError('Network Error');
    expect(rejectJournalEntryErrorMessage(error)).toBe('差し戻しに失敗しました');
  });

  it('非 404 でエラーメッセージが無い場合はデフォルトメッセージを返す', () => {
    const error = new AxiosError('Bad Request');
    error.response = {
      data: {},
      status: 400,
      statusText: 'Bad Request',
      headers: {},
      config: {} as never,
    };
    expect(rejectJournalEntryErrorMessage(error)).toBe('差し戻しに失敗しました');
  });
});
