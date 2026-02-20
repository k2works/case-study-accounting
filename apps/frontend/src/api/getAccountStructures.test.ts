import { beforeEach, describe, expect, it, vi } from 'vitest';
import { AxiosError } from 'axios';
import { axiosInstance } from './axios-instance';
import {
  getAccountStructures,
  getAccountStructure,
  getAccountStructuresErrorMessage,
} from './getAccountStructures';

vi.mock('./axios-instance', () => ({
  axiosInstance: { get: vi.fn() },
}));

const mockGet = vi.mocked(axiosInstance.get);

describe('getAccountStructures', () => {
  beforeEach(() => {
    mockGet.mockReset();
  });

  it('GET /api/account-structures で一覧を取得する', async () => {
    const mockData = [
      {
        accountCode: '1000',
        accountName: '現金',
        accountPath: '/1000',
        hierarchyLevel: 1,
        parentAccountCode: null,
        displayOrder: 1,
      },
    ];
    mockGet.mockResolvedValue({ data: mockData });

    const result = await getAccountStructures();

    expect(mockGet).toHaveBeenCalledWith('/api/account-structures');
    expect(result).toEqual(mockData);
  });
});

describe('getAccountStructure', () => {
  beforeEach(() => {
    mockGet.mockReset();
  });

  it('GET /api/account-structures/:code で単一データを取得する', async () => {
    const mockData = {
      accountCode: '1000',
      accountName: '現金',
      accountPath: '/1000',
      hierarchyLevel: 1,
      parentAccountCode: null,
      displayOrder: 1,
    };
    mockGet.mockResolvedValue({ data: mockData });

    const result = await getAccountStructure('1000');

    expect(mockGet).toHaveBeenCalledWith('/api/account-structures/1000');
    expect(result).toEqual(mockData);
  });
});

describe('getAccountStructuresErrorMessage', () => {
  it('AxiosError のレスポンスから errorMessage を返す', () => {
    const error = new AxiosError('fail');
    error.response = {
      data: { errorMessage: '取得エラー' },
      status: 500,
      statusText: 'Internal Server Error',
      headers: {},
      config: {} as never,
    };
    expect(getAccountStructuresErrorMessage(error)).toBe('取得エラー');
  });

  it('Error の message を返す', () => {
    expect(getAccountStructuresErrorMessage(new Error('network'))).toBe('network');
  });

  it('不明なエラーの場合デフォルトメッセージを返す', () => {
    expect(getAccountStructuresErrorMessage(undefined)).toBe('勘定科目構成の取得に失敗しました');
  });
});
