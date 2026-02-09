import { AxiosError } from 'axios';
import { axiosInstance } from './axios-instance';

export interface ApproveJournalEntryResponse {
  success: boolean;
  journalEntryId?: number;
  status?: string;
  approvedBy?: string;
  approvedAt?: string;
  message?: string;
  errorMessage?: string;
}

export const approveJournalEntry = async (id: number): Promise<ApproveJournalEntryResponse> => {
  const { data } = await axiosInstance.post<ApproveJournalEntryResponse>(
    `/api/journal-entries/${id}/approve`
  );
  return data;
};

const extractAxiosErrorMessage = (error: AxiosError): string | null => {
  if (error.response?.status === 404) {
    return '仕訳が見つかりません';
  }
  const data = error.response?.data as ApproveJournalEntryResponse | undefined;
  return data?.errorMessage ?? null;
};

export const approveJournalEntryErrorMessage = (error: unknown): string => {
  if (error instanceof AxiosError) {
    return extractAxiosErrorMessage(error) ?? '承認に失敗しました';
  }
  if (error instanceof Error) {
    return error.message;
  }
  return '承認に失敗しました';
};
