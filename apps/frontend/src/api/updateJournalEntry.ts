import { AxiosError } from 'axios';
import { axiosInstance } from './axios-instance';

export interface UpdateJournalEntryLineRequest {
  lineNumber: number;
  accountId: number;
  debitAmount?: number;
  creditAmount?: number;
}

export interface UpdateJournalEntryRequest {
  journalDate: string;
  description: string;
  lines: UpdateJournalEntryLineRequest[];
  version: number;
}

export interface UpdateJournalEntryResponse {
  success: boolean;
  journalEntryId?: number;
  journalDate?: string;
  description?: string;
  status?: string;
  version?: number;
  message?: string;
  errorMessage?: string;
}

export const updateJournalEntry = async (
  id: number,
  request: UpdateJournalEntryRequest
): Promise<UpdateJournalEntryResponse> => {
  const { data } = await axiosInstance.put<UpdateJournalEntryResponse>(
    `/api/journal-entries/${id}`,
    request
  );
  return data;
};

const extractAxiosErrorMessage = (error: AxiosError): string | null => {
  if (error.response?.status === 409) {
    return '他のユーザーによって更新されています。再読み込みしてください。';
  }
  const data = error.response?.data as UpdateJournalEntryResponse | undefined;
  return data?.errorMessage ?? null;
};

export const updateJournalEntryErrorMessage = (error: unknown): string => {
  if (error instanceof AxiosError) {
    return extractAxiosErrorMessage(error) ?? '仕訳の更新に失敗しました';
  }
  if (error instanceof Error) {
    return error.message;
  }
  return '仕訳の更新に失敗しました';
};
