import { AxiosError } from 'axios';
import { axiosInstance } from './axios-instance';

export interface UpdateAccountRequest {
  accountName: string;
  accountType: string;
}

export interface UpdateAccountResponse {
  success: boolean;
  accountId?: number;
  accountCode?: string;
  accountName?: string;
  accountType?: string;
  message?: string;
  errorMessage?: string;
}

export const updateAccount = async (
  id: number,
  payload: UpdateAccountRequest
): Promise<UpdateAccountResponse> => {
  const { data } = await axiosInstance.put<UpdateAccountResponse>(`/api/accounts/${id}`, payload);
  return data;
};

export const getUpdateAccountErrorMessage = (error: unknown): string => {
  if (error instanceof AxiosError && error.response?.data) {
    const data = error.response.data as UpdateAccountResponse;
    if (data.errorMessage) {
      return data.errorMessage;
    }
  }
  if (error instanceof Error) {
    return error.message;
  }
  return '勘定科目の更新に失敗しました';
};
