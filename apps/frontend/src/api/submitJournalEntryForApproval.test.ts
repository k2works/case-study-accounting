import { beforeEach, describe, expect, it, vi } from 'vitest';
import { AxiosError } from 'axios';
import { axiosInstance } from './axios-instance';
import {
  submitJournalEntryForApproval,
  submitForApprovalErrorMessage,
} from './submitJournalEntryForApproval';

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
  it('404 の場合は仕訳が見つかりませんを返す', () => {
    const error = new AxiosError('Not found');
    error.response = {
      data: {},
      status: 404,
      statusText: 'Not Found',
      headers: {},
      config: {} as never,
    };

    expect(submitForApprovalErrorMessage(error)).toBe('仕訳が見つかりません');
  });

  it('API の errorMessage を優先して返す', () => {
    const error = new AxiosError('Bad Request');
    error.response = {
      data: { errorMessage: '承認申請できません' },
      status: 400,
      statusText: 'Bad Request',
      headers: {},
      config: {} as never,
    };

    expect(submitForApprovalErrorMessage(error)).toBe('承認申請できません');
  });

  it('汎用 Error の message を返す', () => {
    expect(submitForApprovalErrorMessage(new Error('failure'))).toBe('failure');
  });

  it('未知のエラーはデフォルトメッセージを返す', () => {
    expect(submitForApprovalErrorMessage('unknown')).toBe('承認申請に失敗しました');
  });
});
