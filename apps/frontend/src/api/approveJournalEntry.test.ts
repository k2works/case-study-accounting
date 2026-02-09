import { beforeEach, describe, expect, it, vi } from 'vitest';
import { axiosInstance } from './axios-instance';
import { approveJournalEntry, approveJournalEntryErrorMessage } from './approveJournalEntry';
import { createErrorMessageTestCases } from './api-test-helpers';

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
  const testCases = createErrorMessageTestCases(approveJournalEntryErrorMessage, {
    notFoundMessage: '仕訳が見つかりません',
    customErrorMessage: '承認できません',
    defaultErrorMessage: '承認に失敗しました',
  });

  testCases.forEach(({ name, run }) => {
    it(name, run);
  });
});
