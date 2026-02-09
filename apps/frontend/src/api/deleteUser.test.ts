import { beforeEach, describe, expect, it, vi } from 'vitest';
import { axiosInstance } from './axios-instance';
import { deleteUser, getDeleteUserErrorMessage } from './deleteUser';
import { createErrorWithMessage } from './api-test-helpers';

vi.mock('./axios-instance', () => ({
  axiosInstance: {
    delete: vi.fn(),
  },
}));

const mockAxiosDelete = vi.mocked(axiosInstance.delete);

describe('deleteUser', () => {
  beforeEach(() => {
    mockAxiosDelete.mockReset();
  });

  it('ユーザー削除 API を呼び出してレスポンスを返す', async () => {
    const mockResponse = { success: true };
    mockAxiosDelete.mockResolvedValue({ data: mockResponse });

    const result = await deleteUser('user-1');

    expect(mockAxiosDelete).toHaveBeenCalledWith('/api/users/user-1');
    expect(result.success).toBe(true);
  });

  it('削除失敗時のレスポンスを返す', async () => {
    const mockResponse = {
      success: false,
      errorMessage: '削除権限がありません',
    };
    mockAxiosDelete.mockResolvedValue({ data: mockResponse });

    const result = await deleteUser('user-1');

    expect(result.success).toBe(false);
    expect(result.errorMessage).toBe('削除権限がありません');
  });
});

describe('getDeleteUserErrorMessage', () => {
  it('API の errorMessage を優先して返す', () => {
    const error = createErrorWithMessage('このユーザーは削除できません');
    const result = getDeleteUserErrorMessage(error);
    expect(result).toBe('このユーザーは削除できません');
  });

  it('汎用 Error の message を返す', () => {
    const error = new Error('削除に失敗しました');
    const result = getDeleteUserErrorMessage(error);
    expect(result).toBe('削除に失敗しました');
  });

  it('未知のエラーはデフォルトメッセージを返す', () => {
    const result = getDeleteUserErrorMessage('unknown');
    expect(result).toBe('ユーザー削除に失敗しました');
  });
});
