import { AxiosError } from 'axios';
import { axiosInstance } from './axios-instance';

export interface ConfirmJournalEntryResponse {
  success: boolean;
  journalEntryId?: number;
  status?: string;
  confirmedBy?: string;
  confirmedAt?: string;
  message?: string;
  errorMessage?: string;
}

export const confirmJournalEntry = async (id: number): Promise<ConfirmJournalEntryResponse> => {
  const { data } = await axiosInstance.post<ConfirmJournalEntryResponse>(
    `/api/journal-entries/${id}/confirm`
  );
  return data;
};

const extractAxiosErrorMessage = (error: AxiosError): string | null => {
  if (error.response?.status === 404) {
    return '仕訳が見つかりません';
  }
  const data = error.response?.data as ConfirmJournalEntryResponse | undefined;
  return data?.errorMessage ?? null;
};

export const confirmJournalEntryErrorMessage = (error: unknown): string => {
  if (error instanceof AxiosError) {
    return extractAxiosErrorMessage(error) ?? '確定に失敗しました';
  }
  if (error instanceof Error) {
    return error.message;
  }
  return '確定に失敗しました';
};
