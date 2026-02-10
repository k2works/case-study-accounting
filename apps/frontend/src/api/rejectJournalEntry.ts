import { AxiosError } from 'axios';
import { axiosInstance } from './axios-instance';

export interface RejectJournalEntryResponse {
  success: boolean;
  journalEntryId?: number;
  status?: string;
  rejectedBy?: string;
  rejectedAt?: string;
  rejectionReason?: string;
  message?: string;
  errorMessage?: string;
}

export const rejectJournalEntry = async (
  id: number,
  rejectionReason: string
): Promise<RejectJournalEntryResponse> => {
  const { data } = await axiosInstance.post<RejectJournalEntryResponse>(
    `/api/journal-entries/${id}/reject`,
    { rejectionReason }
  );
  return data;
};

const extractAxiosErrorMessage = (error: AxiosError): string | null => {
  if (error.response?.status === 404) {
    return '仕訳が見つかりません';
  }
  const data = error.response?.data as RejectJournalEntryResponse | undefined;
  return data?.errorMessage ?? null;
};

export const rejectJournalEntryErrorMessage = (error: unknown): string => {
  if (error instanceof AxiosError) {
    return extractAxiosErrorMessage(error) ?? '差し戻しに失敗しました';
  }
  if (error instanceof Error) {
    return error.message;
  }
  return '差し戻しに失敗しました';
};
