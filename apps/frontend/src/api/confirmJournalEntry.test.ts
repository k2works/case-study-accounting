import { beforeEach, describe, expect, it, vi } from 'vitest';
import { axiosInstance } from './axios-instance';
import { confirmJournalEntry, confirmJournalEntryErrorMessage } from './confirmJournalEntry';
import { createErrorMessageTestCases } from './api-test-helpers';

vi.mock('./axios-instance', () => ({
  axiosInstance: {
    post: vi.fn(),
  },
}));

const mockAxiosPost = vi.mocked(axiosInstance.post);

describe('confirmJournalEntry', () => {
  beforeEach(() => {
    mockAxiosPost.mockReset();
  });

  it('仕訳確定 API を呼び出してレスポンスを返す', async () => {
    mockAxiosPost.mockResolvedValue({
      data: {
        success: true,
        journalEntryId: 1,
        status: 'CONFIRMED',
        message: '仕訳を確定しました',
      },
    });

    const result = await confirmJournalEntry(1);

    expect(mockAxiosPost).toHaveBeenCalledWith('/api/journal-entries/1/confirm');
    expect(result.success).toBe(true);
    expect(result.status).toBe('CONFIRMED');
  });
});

describe('confirmJournalEntryErrorMessage', () => {
  const testCases = createErrorMessageTestCases(confirmJournalEntryErrorMessage, {
    notFoundMessage: '仕訳が見つかりません',
    customErrorMessage: '確定できません',
    defaultErrorMessage: '確定に失敗しました',
  });

  testCases.forEach(({ name, run }) => {
    it(name, run);
  });
});
