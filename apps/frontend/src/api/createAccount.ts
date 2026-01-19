import { AxiosError } from 'axios';
import { axiosInstance } from './axios-instance';

export interface CreateAccountRequest {
  accountCode: string;
  accountName: string;
  accountType: string;
}

export interface CreateAccountResponse {
  success: boolean;
  accountId?: number;
  accountCode?: string;
  accountName?: string;
  accountType?: string;
  errorMessage?: string;
}

export const createAccount = async (
  payload: CreateAccountRequest
): Promise<CreateAccountResponse> => {
  const { data } = await axiosInstance.post<CreateAccountResponse>('/api/accounts', payload);
  return data;
};

export const getCreateAccountErrorMessage = (error: unknown): string => {
  if (error instanceof AxiosError && error.response?.data) {
    const data = error.response.data as CreateAccountResponse;
    if (data.errorMessage) {
      return data.errorMessage;
    }
  }
  if (error instanceof Error) {
    return error.message;
  }
  return '勘定科目登録に失敗しました';
};
