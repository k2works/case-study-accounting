import { beforeEach, describe, expect, it, vi } from 'vitest';
import { axiosInstance } from './axios-instance';
import { getUser, getUserErrorMessage } from './getUser';
import { create404Error, createErrorWithMessage } from './api-test-helpers';

vi.mock('./axios-instance', () => ({
  axiosInstance: {
    get: vi.fn(),
  },
}));

const mockAxiosGet = vi.mocked(axiosInstance.get);

const mockUser = {
  id: 'user-1',
  username: 'admin',
  email: 'admin@example.com',
  displayName: '管理者',
  role: 'ADMIN',
  lastLoginAt: '2024-01-01T00:00:00Z',
};

describe('getUser', () => {
  beforeEach(() => {
    mockAxiosGet.mockReset();
  });

  it('ユーザー取得 API を呼び出してレスポンスを返す', async () => {
    mockAxiosGet.mockResolvedValue({ data: mockUser });

    const result = await getUser('user-1');

    expect(mockAxiosGet).toHaveBeenCalledWith('/api/users/user-1');
    expect(result).toEqual(mockUser);
    expect(result.id).toBe('user-1');
    expect(result.username).toBe('admin');
  });
});

describe('getUserErrorMessage', () => {
  it('404 の場合はユーザーが見つかりませんでしたを返す', () => {
    const error = create404Error();
    const result = getUserErrorMessage(error);
    expect(result).toBe('ユーザーが見つかりませんでした');
  });

  it('API の errorMessage を優先して返す', () => {
    const error = createErrorWithMessage('アクセス権限がありません');
    const result = getUserErrorMessage(error);
    expect(result).toBe('アクセス権限がありません');
  });

  it('汎用 Error の message を返す', () => {
    const error = new Error('取得に失敗しました');
    const result = getUserErrorMessage(error);
    expect(result).toBe('取得に失敗しました');
  });

  it('未知のエラーはデフォルトメッセージを返す', () => {
    const result = getUserErrorMessage('unknown');
    expect(result).toBe('ユーザー情報の取得に失敗しました');
  });
});
