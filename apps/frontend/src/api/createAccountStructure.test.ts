import { beforeEach, describe, expect, it, vi } from 'vitest';
import { AxiosError } from 'axios';
import { axiosInstance } from './axios-instance';
import {
  createAccountStructure,
  getCreateAccountStructureErrorMessage,
} from './createAccountStructure';

vi.mock('./axios-instance', () => ({
  axiosInstance: { post: vi.fn() },
}));

const mockPost = vi.mocked(axiosInstance.post);

describe('createAccountStructure', () => {
  beforeEach(() => {
    mockPost.mockReset();
  });

  it('POST /api/account-structures にペイロードを送信する', async () => {
    const payload = { accountCode: '1000', parentAccountCode: null, displayOrder: 1 };
    mockPost.mockResolvedValue({ data: { success: true, accountCode: '1000' } });

    const result = await createAccountStructure(payload);

    expect(mockPost).toHaveBeenCalledWith('/api/account-structures', payload);
    expect(result.success).toBe(true);
    expect(result.accountCode).toBe('1000');
  });
});

describe('getCreateAccountStructureErrorMessage', () => {
  it('AxiosError のレスポンスから errorMessage を返す', () => {
    const error = new AxiosError('fail');
    error.response = {
      data: { errorMessage: '重複エラー' },
      status: 400,
      statusText: 'Bad Request',
      headers: {},
      config: {} as never,
    };
    expect(getCreateAccountStructureErrorMessage(error)).toBe('重複エラー');
  });

  it('Error の message を返す', () => {
    expect(getCreateAccountStructureErrorMessage(new Error('ネットワークエラー'))).toBe(
      'ネットワークエラー'
    );
  });

  it('不明なエラーの場合デフォルトメッセージを返す', () => {
    expect(getCreateAccountStructureErrorMessage('unknown')).toBe(
      '勘定科目構成の登録に失敗しました'
    );
  });
});
