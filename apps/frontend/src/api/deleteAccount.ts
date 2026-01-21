import { AxiosError } from 'axios';
import { axiosInstance } from './axios-instance';

export interface DeleteAccountResponse {
  success: boolean;
  accountId?: number;
  message?: string;
  errorMessage?: string;
}

export const deleteAccount = async (id: number): Promise<DeleteAccountResponse> => {
  const { data } = await axiosInstance.delete<DeleteAccountResponse>(`/api/accounts/${id}`);
  return data;
};

export const getDeleteAccountErrorMessage = (error: unknown): string => {
  if (error instanceof AxiosError && error.response?.data) {
    const data = error.response.data as DeleteAccountResponse;
    if (data.errorMessage) {
      return data.errorMessage;
    }
  }
  if (error instanceof Error) {
    return error.message;
  }
  return '勘定科目の削除に失敗しました';
};
