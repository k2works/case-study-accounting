import { AxiosError } from 'axios';
import { axiosInstance } from './axios-instance';

export interface UpdatePatternItemRequest {
  lineNumber: number;
  debitCreditType: string;
  accountCode: string;
  amountFormula: string;
  descriptionTemplate?: string;
}

export interface UpdateAutoJournalPatternRequest {
  patternName: string;
  sourceTableName: string;
  description?: string;
  isActive: boolean;
  items: UpdatePatternItemRequest[];
}

export interface UpdateAutoJournalPatternResponse {
  success: boolean;
  patternId?: number;
  patternCode?: string;
  patternName?: string;
  message?: string;
  errorMessage?: string;
}

export const updateAutoJournalPattern = async (
  id: number,
  payload: UpdateAutoJournalPatternRequest
): Promise<UpdateAutoJournalPatternResponse> => {
  const { data } = await axiosInstance.put<UpdateAutoJournalPatternResponse>(
    `/api/auto-journal-patterns/${id}`,
    payload
  );
  return data;
};

export const getUpdateAutoJournalPatternErrorMessage = (error: unknown): string => {
  if (error instanceof AxiosError && error.response?.data) {
    const data = error.response.data as UpdateAutoJournalPatternResponse;
    if (data.errorMessage) return data.errorMessage;
  }
  if (error instanceof Error) return error.message;
  return '自動仕訳パターン更新に失敗しました';
};
