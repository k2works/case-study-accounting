import { beforeEach, describe, expect, it, vi } from 'vitest';
import { AxiosError } from 'axios';
import { axiosInstance } from './axios-instance';
import { generateAutoJournal, generateAutoJournalErrorMessage } from './generateAutoJournal';

vi.mock('./axios-instance', () => ({
  axiosInstance: {
    post: vi.fn(),
  },
}));

const mockAxiosPost = vi.mocked(axiosInstance.post);

describe('generateAutoJournal', () => {
  beforeEach(() => {
    mockAxiosPost.mockReset();
  });

  it('自動仕訳生成 API を呼び出してレスポンスを返す', async () => {
    mockAxiosPost.mockResolvedValue({
      data: {
        success: true,
        journalEntryId: 100,
        journalDate: '2024-12-31',
        description: '売上自動計上',
        status: 'DRAFT',
      },
    });

    const result = await generateAutoJournal({
      patternId: 1,
      amounts: { amount: 1000 },
      journalDate: '2024-12-31',
      description: '売上自動計上',
    });

    expect(mockAxiosPost).toHaveBeenCalledWith('/api/journal-entries/generate', {
      patternId: 1,
      amounts: { amount: 1000 },
      journalDate: '2024-12-31',
      description: '売上自動計上',
    });
    expect(result.success).toBe(true);
    expect(result.journalEntryId).toBe(100);
  });
});

describe('generateAutoJournalErrorMessage', () => {
  it('returns API error message when provided', () => {
    const error = new AxiosError('Request failed');
    error.response = {
      data: { errorMessage: '生成できません' },
      status: 400,
      statusText: 'Bad Request',
      headers: {},
      config: {} as never,
    };

    expect(generateAutoJournalErrorMessage(error)).toBe('生成できません');
  });

  it('returns error message for generic Error', () => {
    expect(generateAutoJournalErrorMessage(new Error('failure'))).toBe('failure');
  });

  it('returns default message for unknown errors', () => {
    expect(generateAutoJournalErrorMessage('unknown')).toBe('自動仕訳の生成に失敗しました');
  });
});
