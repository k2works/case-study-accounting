import { AxiosError } from 'axios';
import { axiosInstance } from './axios-instance';

export interface AccountStructure extends Record<string, unknown> {
  accountCode: string;
  accountName: string | null;
  accountPath: string;
  hierarchyLevel: number;
  parentAccountCode: string | null;
  displayOrder: number;
}

export const getAccountStructures = async (): Promise<AccountStructure[]> => {
  const { data } = await axiosInstance.get<AccountStructure[]>('/api/account-structures');
  return data;
};

export const getAccountStructure = async (code: string): Promise<AccountStructure> => {
  const { data } = await axiosInstance.get<AccountStructure>(`/api/account-structures/${code}`);
  return data;
};

export const getAccountStructuresErrorMessage = (error: unknown): string => {
  if (error instanceof AxiosError && error.response?.data) {
    const data = error.response.data as { errorMessage?: string };
    if (data.errorMessage) {
      return data.errorMessage;
    }
  }
  if (error instanceof Error) {
    return error.message;
  }
  return '勘定科目構成の取得に失敗しました';
};
