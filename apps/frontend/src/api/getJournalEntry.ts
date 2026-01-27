import { AxiosError } from 'axios';
import { axiosInstance } from './axios-instance';

export interface JournalEntryLine {
  lineNumber: number;
  accountId: number;
  accountCode: string;
  accountName: string;
  debitAmount?: number;
  creditAmount?: number;
}

export interface JournalEntry {
  journalEntryId: number;
  journalDate: string;
  description: string;
  status: string;
  version: number;
  lines: JournalEntryLine[];
}

export const getJournalEntry = async (id: number): Promise<JournalEntry> => {
  const { data } = await axiosInstance.get<JournalEntry>(`/api/journal-entries/${id}`);
  return data;
};

const extractAxiosErrorMessage = (error: AxiosError): string | null => {
  if (error.response?.status === 404) {
    return '仕訳が見つかりません';
  }
  const data = error.response?.data as { errorMessage?: string } | undefined;
  return data?.errorMessage ?? null;
};

export const getJournalEntryErrorMessage = (error: unknown): string => {
  if (error instanceof AxiosError) {
    return extractAxiosErrorMessage(error) ?? '仕訳の取得に失敗しました';
  }
  if (error instanceof Error) {
    return error.message;
  }
  return '仕訳の取得に失敗しました';
};
