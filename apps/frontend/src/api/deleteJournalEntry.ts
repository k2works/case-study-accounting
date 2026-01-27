import { AxiosError } from 'axios';
import { axiosInstance } from './axios-instance';

export interface DeleteJournalEntryResponse {
  success: boolean;
  message?: string;
  errorMessage?: string;
}

export const deleteJournalEntry = async (id: number): Promise<DeleteJournalEntryResponse> => {
  const { data } = await axiosInstance.delete<DeleteJournalEntryResponse>(
    `/api/journal-entries/${id}`
  );
  return data;
};

const extractAxiosErrorMessage = (error: AxiosError): string | null => {
  if (error.response?.status === 404) {
    return '仕訳が見つかりません';
  }
  const data = error.response?.data as DeleteJournalEntryResponse | undefined;
  return data?.errorMessage ?? null;
};

export const deleteJournalEntryErrorMessage = (error: unknown): string => {
  if (error instanceof AxiosError) {
    return extractAxiosErrorMessage(error) ?? '仕訳の削除に失敗しました';
  }
  if (error instanceof Error) {
    return error.message;
  }
  return '仕訳の削除に失敗しました';
};
