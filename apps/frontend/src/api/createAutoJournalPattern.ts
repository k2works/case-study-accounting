import { AxiosError } from 'axios';
import { axiosInstance } from './axios-instance';

export interface CreatePatternItemRequest {
  lineNumber: number;
  debitCreditType: string;
  accountCode: string;
  amountFormula: string;
  descriptionTemplate?: string;
}

export interface CreateAutoJournalPatternRequest {
  patternCode: string;
  patternName: string;
  sourceTableName: string;
  description?: string;
  items: CreatePatternItemRequest[];
}

export interface CreateAutoJournalPatternResponse {
  success: boolean;
  patternId?: number;
  patternCode?: string;
  patternName?: string;
  errorMessage?: string;
}

export const createAutoJournalPattern = async (
  payload: CreateAutoJournalPatternRequest
): Promise<CreateAutoJournalPatternResponse> => {
  const { data } = await axiosInstance.post<CreateAutoJournalPatternResponse>(
    '/api/auto-journal-patterns',
    payload
  );
  return data;
};

export const getCreateAutoJournalPatternErrorMessage = (error: unknown): string => {
  if (error instanceof AxiosError && error.response?.data) {
    const data = error.response.data as CreateAutoJournalPatternResponse;
    if (data.errorMessage) return data.errorMessage;
  }
  if (error instanceof Error) return error.message;
  return '自動仕訳パターン登録に失敗しました';
};
