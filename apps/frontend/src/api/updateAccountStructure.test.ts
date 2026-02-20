import { beforeEach, describe, expect, it, vi } from 'vitest';
import { AxiosError } from 'axios';
import { axiosInstance } from './axios-instance';
import {
  updateAccountStructure,
  getUpdateAccountStructureErrorMessage,
} from './updateAccountStructure';

vi.mock('./axios-instance', () => ({
  axiosInstance: { put: vi.fn() },
}));

const mockPut = vi.mocked(axiosInstance.put);

describe('updateAccountStructure', () => {
  beforeEach(() => {
    mockPut.mockReset();
  });

  it('PUT /api/account-structures/:code にペイロードを送信する', async () => {
    const payload = { parentAccountCode: '1000', displayOrder: 5 };
    mockPut.mockResolvedValue({
      data: { success: true, accountCode: '2000', message: '更新しました' },
    });

    const result = await updateAccountStructure('2000', payload);

    expect(mockPut).toHaveBeenCalledWith('/api/account-structures/2000', payload);
    expect(result.success).toBe(true);
    expect(result.message).toBe('更新しました');
  });
});

describe('getUpdateAccountStructureErrorMessage', () => {
  it('AxiosError のレスポンスから errorMessage を返す', () => {
    const error = new AxiosError('fail');
    error.response = {
      data: { errorMessage: '更新エラー' },
      status: 400,
      statusText: 'Bad Request',
      headers: {},
      config: {} as never,
    };
    expect(getUpdateAccountStructureErrorMessage(error)).toBe('更新エラー');
  });

  it('Error の message を返す', () => {
    expect(getUpdateAccountStructureErrorMessage(new Error('timeout'))).toBe('timeout');
  });

  it('不明なエラーの場合デフォルトメッセージを返す', () => {
    expect(getUpdateAccountStructureErrorMessage(42)).toBe('勘定科目構成の更新に失敗しました');
  });
});
