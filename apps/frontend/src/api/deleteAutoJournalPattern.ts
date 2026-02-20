import { AxiosError } from 'axios';
import { axiosInstance } from './axios-instance';

export interface DeleteAutoJournalPatternResponse {
  success: boolean;
  patternId?: number;
  message?: string;
  errorMessage?: string;
}

export const deleteAutoJournalPattern = async (
  id: number
): Promise<DeleteAutoJournalPatternResponse> => {
  const { data } = await axiosInstance.delete<DeleteAutoJournalPatternResponse>(
    `/api/auto-journal-patterns/${id}`
  );
  return data;
};

export const getDeleteAutoJournalPatternErrorMessage = (error: unknown): string => {
  if (error instanceof AxiosError && error.response?.data) {
    const data = error.response.data as DeleteAutoJournalPatternResponse;
    if (data.errorMessage) return data.errorMessage;
  }
  if (error instanceof Error) return error.message;
  return '自動仕訳パターンの削除に失敗しました';
};
