import { beforeEach, describe, expect, it, vi } from 'vitest';
import { AxiosError } from 'axios';
import { axiosInstance } from './axios-instance';
import {
  deleteAccountStructure,
  getDeleteAccountStructureErrorMessage,
} from './deleteAccountStructure';

vi.mock('./axios-instance', () => ({
  axiosInstance: { delete: vi.fn() },
}));

const mockDelete = vi.mocked(axiosInstance.delete);

describe('deleteAccountStructure', () => {
  beforeEach(() => {
    mockDelete.mockReset();
  });

  it('DELETE /api/account-structures/:code を呼び出す', async () => {
    mockDelete.mockResolvedValue({
      data: { success: true, accountCode: '1000', message: '削除しました' },
    });

    const result = await deleteAccountStructure('1000');

    expect(mockDelete).toHaveBeenCalledWith('/api/account-structures/1000');
    expect(result.success).toBe(true);
    expect(result.message).toBe('削除しました');
  });
});

describe('getDeleteAccountStructureErrorMessage', () => {
  it('AxiosError のレスポンスから errorMessage を返す', () => {
    const error = new AxiosError('fail');
    error.response = {
      data: { errorMessage: '子構成が存在します' },
      status: 400,
      statusText: 'Bad Request',
      headers: {},
      config: {} as never,
    };
    expect(getDeleteAccountStructureErrorMessage(error)).toBe('子構成が存在します');
  });

  it('Error の message を返す', () => {
    expect(getDeleteAccountStructureErrorMessage(new Error('server error'))).toBe('server error');
  });

  it('不明なエラーの場合デフォルトメッセージを返す', () => {
    expect(getDeleteAccountStructureErrorMessage(null)).toBe('勘定科目構成の削除に失敗しました');
  });
});
