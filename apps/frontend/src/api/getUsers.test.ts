import { beforeEach, describe, expect, it, vi } from 'vitest';
import { axiosInstance } from './axios-instance';
import { getUsers, getUsersErrorMessage } from './getUsers';
import { createErrorWithMessage } from './api-test-helpers';

vi.mock('./axios-instance', () => ({
  axiosInstance: {
    get: vi.fn(),
  },
}));

const mockAxiosGet = vi.mocked(axiosInstance.get);

const mockUsers = [
  {
    id: 'user-1',
    username: 'admin',
    email: 'admin@example.com',
    displayName: '管理者',
    role: 'ADMIN',
    lastLoginAt: '2024-01-01T00:00:00Z',
  },
  {
    id: 'user-2',
    username: 'user1',
    email: 'user1@example.com',
    displayName: 'ユーザー1',
    role: 'USER',
    lastLoginAt: null,
  },
];

describe('getUsers', () => {
  beforeEach(() => {
    mockAxiosGet.mockReset();
  });

  it('パラメータなしでユーザー一覧を取得する', async () => {
    mockAxiosGet.mockResolvedValue({ data: mockUsers });

    const result = await getUsers();

    expect(mockAxiosGet).toHaveBeenCalledWith('/api/users');
    expect(result).toEqual(mockUsers);
  });

  it('ロールでフィルタしてユーザー一覧を取得する', async () => {
    mockAxiosGet.mockResolvedValue({ data: [mockUsers[0]] });

    const result = await getUsers({ role: 'ADMIN' });

    expect(mockAxiosGet).toHaveBeenCalledWith('/api/users?role=ADMIN');
    expect(result).toHaveLength(1);
  });

  it('キーワードでフィルタしてユーザー一覧を取得する', async () => {
    mockAxiosGet.mockResolvedValue({ data: mockUsers });

    const result = await getUsers({ keyword: 'admin' });

    expect(mockAxiosGet).toHaveBeenCalledWith('/api/users?keyword=admin');
    expect(result).toEqual(mockUsers);
  });

  it('ロールとキーワード両方でフィルタしてユーザー一覧を取得する', async () => {
    mockAxiosGet.mockResolvedValue({ data: [mockUsers[0]] });

    const result = await getUsers({ role: 'ADMIN', keyword: '管理' });

    expect(mockAxiosGet).toHaveBeenCalledWith('/api/users?role=ADMIN&keyword=%E7%AE%A1%E7%90%86');
    expect(result).toHaveLength(1);
  });
});

describe('getUsersErrorMessage', () => {
  it('API の errorMessage を優先して返す', () => {
    const error = createErrorWithMessage('カスタムエラーメッセージ');
    const result = getUsersErrorMessage(error);
    expect(result).toBe('カスタムエラーメッセージ');
  });

  it('汎用 Error の message を返す', () => {
    const error = new Error('エラーが発生しました');
    const result = getUsersErrorMessage(error);
    expect(result).toBe('エラーが発生しました');
  });

  it('未知のエラーはデフォルトメッセージを返す', () => {
    const result = getUsersErrorMessage('unknown');
    expect(result).toBe('ユーザー一覧の取得に失敗しました');
  });
});
