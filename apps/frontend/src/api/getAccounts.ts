import { AxiosError } from 'axios';
import { axiosInstance } from './axios-instance';

export interface Account extends Record<string, unknown> {
  accountId: number;
  accountCode: string;
  accountName: string;
  accountType: string;
}

export const getAccounts = async (): Promise<Account[]> => {
  const { data } = await axiosInstance.get<Account[]>('/api/accounts');
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
