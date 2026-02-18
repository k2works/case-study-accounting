import { AxiosError } from 'axios';
import { axiosInstance } from './axios-instance';

export interface UpdateAccountStructureRequest {
  parentAccountCode: string | null;
  displayOrder: number;
}

export interface UpdateAccountStructureResponse {
  success: boolean;
  accountCode?: string;
  accountPath?: string;
  hierarchyLevel?: number;
  parentAccountCode?: string;
  displayOrder?: number;
  message?: string;
  errorMessage?: string;
}

export const updateAccountStructure = async (
  code: string,
  payload: UpdateAccountStructureRequest
): Promise<UpdateAccountStructureResponse> => {
  const { data } = await axiosInstance.put<UpdateAccountStructureResponse>(
    `/api/account-structures/${code}`,
    payload
  );
  return data;
};

export const getUpdateAccountStructureErrorMessage = (error: unknown): string => {
  if (error instanceof AxiosError && error.response?.data) {
    const data = error.response.data as UpdateAccountStructureResponse;
    if (data.errorMessage) {
      return data.errorMessage;
    }
  }
  if (error instanceof Error) {
    return error.message;
  }
  return '勘定科目構成の更新に失敗しました';
};
