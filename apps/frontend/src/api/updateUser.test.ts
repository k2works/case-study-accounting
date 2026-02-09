import { beforeEach, describe, expect, it, vi } from 'vitest';
import { axiosInstance } from './axios-instance';
import { updateUser, getUpdateErrorMessage } from './updateUser';
import { createErrorWithMessage } from './api-test-helpers';

vi.mock('./axios-instance', () => ({
  axiosInstance: {
    put: vi.fn(),
  },
}));

const mockAxiosPut = vi.mocked(axiosInstance.put);

describe('updateUser', () => {
  beforeEach(() => {
    mockAxiosPut.mockReset();
  });

  it('ユーザー更新 API を呼び出してレスポンスを返す', async () => {
    const mockResponse = {
      success: true,
      id: 'user-1',
      username: 'admin',
      email: 'admin@example.com',
      displayName: '更新後の名前',
      role: 'ADMIN',
    };
    mockAxiosPut.mockResolvedValue({ data: mockResponse });

    const payload = {
      displayName: '更新後の名前',
      role: 'ADMIN',
    };
    const result = await updateUser('user-1', payload);

    expect(mockAxiosPut).toHaveBeenCalledWith('/api/users/user-1', payload);
    expect(result.success).toBe(true);
    expect(result.displayName).toBe('更新後の名前');
  });

  it('パスワード付きでユーザー更新 API を呼び出す', async () => {
    const mockResponse = { success: true };
    mockAxiosPut.mockResolvedValue({ data: mockResponse });

    const payload = {
      displayName: '更新後の名前',
      password: 'newpassword123',
      role: 'USER',
    };
    const result = await updateUser('user-1', payload);

    expect(mockAxiosPut).toHaveBeenCalledWith('/api/users/user-1', payload);
    expect(result.success).toBe(true);
  });
});

describe('getUpdateErrorMessage', () => {
  it('API の errorMessage を優先して返す', () => {
    const error = createErrorWithMessage('ユーザー名は既に使用されています');
    const result = getUpdateErrorMessage(error);
    expect(result).toBe('ユーザー名は既に使用されています');
  });

  it('汎用 Error の message を返す', () => {
    const error = new Error('更新に失敗しました');
    const result = getUpdateErrorMessage(error);
    expect(result).toBe('更新に失敗しました');
  });

  it('未知のエラーはデフォルトメッセージを返す', () => {
    const result = getUpdateErrorMessage('unknown');
    expect(result).toBe('ユーザー更新に失敗しました');
  });
});
