import { AxiosError } from 'axios';
import { axiosInstance } from './axios-instance';
import type { Account } from './getAccounts';

export const getAccount = async (id: number): Promise<Account> => {
  const { data } = await axiosInstance.get<Account>(`/api/accounts/${id}`);
  return data;
};

export const getAccountErrorMessage = (error: unknown): string => {
  if (error instanceof AxiosError && error.response?.data) {
    const data = error.response.data as { errorMessage?: string };
    if (data.errorMessage) {
      return data.errorMessage;
    }
  }
  if (error instanceof Error) {
    return error.message;
  }
  return '勘定科目の取得に失敗しました';
};
