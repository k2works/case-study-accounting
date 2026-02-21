import { AxiosError } from 'axios';
import { axiosInstance } from './axios-instance';

export interface GenerateAutoJournalRequest {
  patternId: number;
  amounts: Record<string, number>;
  journalDate: string;
  description?: string;
}

export interface GenerateAutoJournalResponse {
  success: boolean;
  journalEntryId?: number;
  journalDate?: string;
  description?: string;
  status?: string;
  errorMessage?: string;
}

export const generateAutoJournal = async (
  request: GenerateAutoJournalRequest
): Promise<GenerateAutoJournalResponse> => {
  const { data } = await axiosInstance.post<GenerateAutoJournalResponse>(
    '/api/journal-entries/generate',
    request
  );
  return data;
};

export const generateAutoJournalErrorMessage = (error: unknown): string => {
  if (error instanceof AxiosError && error.response?.data) {
    const data = error.response.data as GenerateAutoJournalResponse;
    if (data.errorMessage) {
      return data.errorMessage;
    }
  }
  if (error instanceof Error) {
    return error.message;
  }
  return '自動仕訳の生成に失敗しました';
};
