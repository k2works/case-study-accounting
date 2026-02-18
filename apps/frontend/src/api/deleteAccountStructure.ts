import { AxiosError } from 'axios';
import { axiosInstance } from './axios-instance';

export interface DeleteAccountStructureResponse {
  success: boolean;
  accountCode?: string;
  message?: string;
  errorMessage?: string;
}

export const deleteAccountStructure = async (
  code: string
): Promise<DeleteAccountStructureResponse> => {
  const { data } = await axiosInstance.delete<DeleteAccountStructureResponse>(
    `/api/account-structures/${code}`
  );
  return data;
};

export const getDeleteAccountStructureErrorMessage = (error: unknown): string => {
  if (error instanceof AxiosError && error.response?.data) {
    const data = error.response.data as DeleteAccountStructureResponse;
    if (data.errorMessage) {
      return data.errorMessage;
    }
  }
  if (error instanceof Error) {
    return error.message;
  }
  return '勘定科目構成の削除に失敗しました';
};
