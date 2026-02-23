import { AxiosError } from 'axios';
import { axiosInstance } from './axios-instance';
import { downloadExport } from './statementShared';

export interface TrialBalanceEntry {
  accountCode: string;
  accountName: string;
  bsplCategory: string;
  accountType: string;
  debitBalance: number;
  creditBalance: number;
  [key: string]: unknown;
}

export interface CategorySubtotal {
  accountType: string;
  accountTypeDisplayName: string;
  debitSubtotal: number;
  creditSubtotal: number;
}

export interface GetTrialBalanceResult {
  date: string | null;
  totalDebit: number;
  totalCredit: number;
  balanced: boolean;
  difference: number;
  entries: TrialBalanceEntry[];
  categorySubtotals: CategorySubtotal[];
}

export interface TrialBalanceSearchParams {
  date?: string;
}

const buildSearchParams = (params: TrialBalanceSearchParams): URLSearchParams => {
  const searchParams = new URLSearchParams();
  if (params.date) {
    searchParams.append('date', params.date);
  }
  return searchParams;
};

export const getTrialBalance = async (
  params: TrialBalanceSearchParams
): Promise<GetTrialBalanceResult> => {
  const searchParams = buildSearchParams(params);
  const queryString = searchParams.toString();
  const url = queryString ? `/api/trial-balance?${queryString}` : '/api/trial-balance';
  const { data } = await axiosInstance.get<GetTrialBalanceResult>(url);
  return {
    ...data,
    entries: data.entries ?? [],
    categorySubtotals: data.categorySubtotals ?? [],
  };
};

export type TrialBalanceExportFormat = 'csv' | 'excel' | 'pdf';

export const exportTrialBalance = async (
  format: TrialBalanceExportFormat,
  date?: string
): Promise<void> => {
  const params = new URLSearchParams();
  params.append('format', format);
  if (date) {
    params.append('date', date);
  }
  const extMap: Record<TrialBalanceExportFormat, string> = {
    csv: 'csv',
    excel: 'xlsx',
    pdf: 'pdf',
  };
  await downloadExport(
    `/api/trial-balance/export?${params.toString()}`,
    `trial-balance.${extMap[format]}`
  );
};

export const getTrialBalanceErrorMessage = (error: unknown): string => {
  if (error instanceof AxiosError && error.response?.data) {
    const data = error.response.data as { errorMessage?: string };
    if (data.errorMessage) {
      return data.errorMessage;
    }
  }
  if (error instanceof Error) {
    return error.message;
  }
  return '残高試算表の取得に失敗しました';
};
