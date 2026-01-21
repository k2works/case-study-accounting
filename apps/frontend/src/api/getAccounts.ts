import { AxiosError } from 'axios';
import { axiosInstance } from './axios-instance';

export interface Account extends Record<string, unknown> {
  accountId: number;
  accountCode: string;
  accountName: string;
  accountType: string;
}

export interface AccountSearchParams {
  type?: string;
  keyword?: string;
}

export const getAccounts = async (params?: AccountSearchParams): Promise<Account[]> => {
  const searchParams = new URLSearchParams();
  if (params?.type) {
    searchParams.append('type', params.type);
  }
  if (params?.keyword) {
    searchParams.append('keyword', params.keyword);
  }
  const queryString = searchParams.toString();
  const url = queryString ? `/api/accounts?${queryString}` : '/api/accounts';
  const { data } = await axiosInstance.get<Account[]>(url);
  return data;
};

export const getAccountsErrorMessage = (error: unknown): string => {
  if (error instanceof AxiosError && error.response?.data) {
    const data = error.response.data as { errorMessage?: string };
    if (data.errorMessage) {
      return data.errorMessage;
    }
  }
  if (error instanceof Error) {
    return error.message;
  }
  return '勘定科目一覧の取得に失敗しました';
};
