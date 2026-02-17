import { AxiosError } from 'axios';
import { axiosInstance } from './axios-instance';

export interface ComparativeData {
  previousAmount: number;
  difference: number;
  changeRate: number;
}

export const downloadExport = async (url: string, filename: string): Promise<void> => {
  const response = await axiosInstance.get(url, { responseType: 'blob' });
  const blob = new Blob([response.data as BlobPart]);
  const downloadUrl = window.URL.createObjectURL(blob);
  const link = document.createElement('a');
  link.href = downloadUrl;
  link.download = filename;
  document.body.appendChild(link);
  link.click();
  link.remove();
  window.URL.revokeObjectURL(downloadUrl);
};

export const getStatementErrorMessage = (error: unknown, defaultMessage: string): string => {
  if (error instanceof AxiosError && error.response?.data) {
    const data = error.response.data as { errorMessage?: string };
    if (data.errorMessage) {
      return data.errorMessage;
    }
  }
  if (error instanceof Error) {
    return error.message;
  }
  return defaultMessage;
};
