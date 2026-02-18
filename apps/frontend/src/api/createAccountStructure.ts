import { AxiosError } from 'axios';
import { axiosInstance } from './axios-instance';

export interface CreateAccountStructureRequest {
  accountCode: string;
  parentAccountCode: string | null;
  displayOrder: number;
}

export interface CreateAccountStructureResponse {
  success: boolean;
  accountCode?: string;
  accountPath?: string;
  hierarchyLevel?: number;
  parentAccountCode?: string;
  displayOrder?: number;
  errorMessage?: string;
}

export const createAccountStructure = async (
  payload: CreateAccountStructureRequest
): Promise<CreateAccountStructureResponse> => {
  const { data } = await axiosInstance.post<CreateAccountStructureResponse>(
    '/api/account-structures',
    payload
  );
  return data;
};

export const getCreateAccountStructureErrorMessage = (error: unknown): string => {
  if (error instanceof AxiosError && error.response?.data) {
    const data = error.response.data as CreateAccountStructureResponse;
    if (data.errorMessage) {
      return data.errorMessage;
    }
  }
  if (error instanceof Error) {
    return error.message;
  }
  return '勘定科目構成の登録に失敗しました';
};
