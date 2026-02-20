import { AxiosError } from 'axios';
import { axiosInstance } from './axios-instance';

export interface PatternItem {
  lineNumber: number;
  debitCreditType: string;
  accountCode: string;
  amountFormula: string;
  descriptionTemplate?: string;
}

export interface AutoJournalPattern extends Record<string, unknown> {
  patternId: number;
  patternCode: string;
  patternName: string;
  sourceTableName: string;
  description?: string;
  isActive: boolean;
  items: PatternItem[];
}

export const getAutoJournalPatterns = async (): Promise<AutoJournalPattern[]> => {
  const { data } = await axiosInstance.get<AutoJournalPattern[]>('/api/auto-journal-patterns');
  return data;
};

export const getAutoJournalPattern = async (id: number): Promise<AutoJournalPattern> => {
  const { data } = await axiosInstance.get<AutoJournalPattern>(`/api/auto-journal-patterns/${id}`);
  return data;
};

export const getAutoJournalPatternsErrorMessage = (error: unknown): string => {
  if (error instanceof AxiosError && error.response?.data) {
    const data = error.response.data as { errorMessage?: string };
    if (data.errorMessage) return data.errorMessage;
  }
  if (error instanceof Error) return error.message;
  return '自動仕訳パターン一覧の取得に失敗しました';
};
