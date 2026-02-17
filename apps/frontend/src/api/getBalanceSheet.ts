import { AxiosError } from 'axios';
import { axiosInstance } from './axios-instance';

export interface ComparativeData {
  previousAmount: number;
  difference: number;
  changeRate: number;
}

export interface BalanceSheetEntry {
  accountCode: string;
  accountName: string;
  accountType: string;
  amount: number;
  comparative: ComparativeData | null;
  [key: string]: unknown;
}

export interface BalanceSheetSection {
  sectionType: string;
  sectionDisplayName: string;
  entries: BalanceSheetEntry[];
  subtotal: number;
  comparativeSubtotal: ComparativeData | null;
}

export interface GetBalanceSheetResult {
  date: string | null;
  comparativeDate: string | null;
  sections: BalanceSheetSection[];
  totalAssets: number;
  totalLiabilities: number;
  totalEquity: number;
  totalLiabilitiesAndEquity: number;
  balanced: boolean;
  difference: number;
}

export interface BalanceSheetSearchParams {
  date?: string;
  comparativeDate?: string;
}

const buildSearchParams = (params: BalanceSheetSearchParams): URLSearchParams => {
  const searchParams = new URLSearchParams();
  if (params.date) {
    searchParams.append('date', params.date);
  }
  if (params.comparativeDate) {
    searchParams.append('comparativeDate', params.comparativeDate);
  }
  return searchParams;
};

export const getBalanceSheet = async (
  params: BalanceSheetSearchParams
): Promise<GetBalanceSheetResult> => {
  const searchParams = buildSearchParams(params);
  const queryString = searchParams.toString();
  const url = queryString ? `/api/balance-sheet?${queryString}` : '/api/balance-sheet';
  const { data } = await axiosInstance.get<GetBalanceSheetResult>(url);
  return {
    ...data,
    sections: data.sections ?? [],
  };
};

export const exportBalanceSheet = async (format: 'pdf' | 'excel', date?: string): Promise<void> => {
  const params = new URLSearchParams();
  params.append('format', format);
  if (date) {
    params.append('date', date);
  }
  const url = `/api/balance-sheet/export?${params.toString()}`;
  const response = await axiosInstance.get(url, { responseType: 'blob' });
  const blob = new Blob([response.data as BlobPart]);
  const downloadUrl = window.URL.createObjectURL(blob);
  const link = document.createElement('a');
  link.href = downloadUrl;
  link.download = format === 'pdf' ? 'balance-sheet.pdf' : 'balance-sheet.xlsx';
  document.body.appendChild(link);
  link.click();
  link.remove();
  window.URL.revokeObjectURL(downloadUrl);
};

export const getBalanceSheetErrorMessage = (error: unknown): string => {
  if (error instanceof AxiosError && error.response?.data) {
    const data = error.response.data as { errorMessage?: string };
    if (data.errorMessage) {
      return data.errorMessage;
    }
  }
  if (error instanceof Error) {
    return error.message;
  }
  return '貸借対照表の取得に失敗しました';
};
