import { beforeEach, describe, expect, it, vi } from 'vitest';
import { axiosInstance } from './axios-instance';
import {
  submitJournalEntryForApproval,
  submitForApprovalErrorMessage,
} from './submitJournalEntryForApproval';
import { createErrorMessageTestCases } from './api-test-helpers';

vi.mock('./axios-instance', () => ({
  axiosInstance: {
    post: vi.fn(),
  },
}));

const mockAxiosPost = vi.mocked(axiosInstance.post);

describe('submitJournalEntryForApproval', () => {
  beforeEach(() => {
    mockAxiosPost.mockReset();
  });

  it('承認申請 API を呼び出してレスポンスを返す', async () => {
    mockAxiosPost.mockResolvedValue({
      data: { success: true, journalEntryId: 1, status: 'PENDING', message: '申請しました' },
    });

    const result = await submitJournalEntryForApproval(1);

    expect(mockAxiosPost).toHaveBeenCalledWith('/api/journal-entries/1/submit');
    expect(result.success).toBe(true);
    expect(result.status).toBe('PENDING');
  });
});

describe('submitForApprovalErrorMessage', () => {
  const testCases = createErrorMessageTestCases(submitForApprovalErrorMessage, {
    notFoundMessage: '仕訳が見つかりません',
    customErrorMessage: '承認申請できません',
    defaultErrorMessage: '承認申請に失敗しました',
  });

  testCases.forEach(({ name, run }) => {
    it(name, run);
  });
});
