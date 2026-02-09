import { AxiosError } from 'axios';
import { axiosInstance } from './axios-instance';

export interface SubmitForApprovalResponse {
  success: boolean;
  journalEntryId?: number;
  status?: string;
  message?: string;
  errorMessage?: string;
}

export const submitJournalEntryForApproval = async (
  id: number
): Promise<SubmitForApprovalResponse> => {
  const { data } = await axiosInstance.post<SubmitForApprovalResponse>(
    `/api/journal-entries/${id}/submit`
  );
  return data;
};

const extractAxiosErrorMessage = (error: AxiosError): string | null => {
  if (error.response?.status === 404) {
    return '仕訳が見つかりません';
  }
  const data = error.response?.data as SubmitForApprovalResponse | undefined;
  return data?.errorMessage ?? null;
};

export const submitForApprovalErrorMessage = (error: unknown): string => {
  if (error instanceof AxiosError) {
    return extractAxiosErrorMessage(error) ?? '承認申請に失敗しました';
  }
  if (error instanceof Error) {
    return error.message;
  }
  return '承認申請に失敗しました';
};
