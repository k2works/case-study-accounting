import { AxiosError } from 'axios';
import { axiosInstance } from './axios-instance';

export interface JournalEntryLineRequest {
  lineNumber: number;
  accountId: number;
  debitAmount?: number;
  creditAmount?: number;
}

export interface CreateJournalEntryRequest {
  journalDate: string;
  description: string;
  lines: JournalEntryLineRequest[];
}

export interface CreateJournalEntryResponse {
  success: boolean;
  journalEntryId?: number;
  journalDate?: string;
  description?: string;
  status?: string;
  errorMessage?: string;
}

export const createJournalEntry = async (
  request: CreateJournalEntryRequest
): Promise<CreateJournalEntryResponse> => {
  const { data } = await axiosInstance.post<CreateJournalEntryResponse>(
    '/api/journal-entries',
    request
  );
  return data;
};

export const createJournalEntryErrorMessage = (error: unknown): string => {
  if (error instanceof AxiosError && error.response?.data) {
    const data = error.response.data as CreateJournalEntryResponse;
    if (data.errorMessage) {
      return data.errorMessage;
    }
  }
  if (error instanceof Error) {
    return error.message;
  }
  return '仕訳の登録に失敗しました';
};
