import { axiosInstance } from './axios-instance';
import { downloadExport, getStatementErrorMessage } from './statementShared';
import type { ComparativeData } from './statementShared';

export type { ComparativeData } from './statementShared';

export interface ProfitAndLossEntry {
  accountCode: string;
  accountName: string;
  accountType: string;
  amount: number;
  comparative: ComparativeData | null;
  [key: string]: unknown;
}

export interface ProfitAndLossSection {
  sectionType: string;
  sectionDisplayName: string;
  entries: ProfitAndLossEntry[];
  subtotal: number;
  comparativeSubtotal: ComparativeData | null;
}

export interface GetProfitAndLossResult {
  dateFrom: string | null;
  dateTo: string | null;
  comparativeDateFrom: string | null;
  comparativeDateTo: string | null;
  sections: ProfitAndLossSection[];
  totalRevenue: number;
  totalExpense: number;
  netIncome: number;
}

export interface ProfitAndLossSearchParams {
  dateFrom?: string;
  dateTo?: string;
  comparativeDateFrom?: string;
  comparativeDateTo?: string;
}

const buildSearchParams = (params: ProfitAndLossSearchParams): URLSearchParams => {
  const searchParams = new URLSearchParams();
  if (params.dateFrom) {
    searchParams.append('dateFrom', params.dateFrom);
  }
  if (params.dateTo) {
    searchParams.append('dateTo', params.dateTo);
  }
  if (params.comparativeDateFrom) {
    searchParams.append('comparativeDateFrom', params.comparativeDateFrom);
  }
  if (params.comparativeDateTo) {
    searchParams.append('comparativeDateTo', params.comparativeDateTo);
  }
  return searchParams;
};

export const getProfitAndLoss = async (
  params: ProfitAndLossSearchParams
): Promise<GetProfitAndLossResult> => {
  const searchParams = buildSearchParams(params);
  const queryString = searchParams.toString();
  const url = queryString ? `/api/profit-and-loss?${queryString}` : '/api/profit-and-loss';
  const { data } = await axiosInstance.get<GetProfitAndLossResult>(url);
  return {
    ...data,
    sections: data.sections ?? [],
  };
};

export const exportProfitAndLoss = async (
  format: 'pdf' | 'excel',
  dateFrom?: string,
  dateTo?: string
): Promise<void> => {
  const params = new URLSearchParams();
  params.append('format', format);
  if (dateFrom) {
    params.append('dateFrom', dateFrom);
  }
  if (dateTo) {
    params.append('dateTo', dateTo);
  }
  const filename = format === 'pdf' ? 'profit-and-loss.pdf' : 'profit-and-loss.xlsx';
  await downloadExport(`/api/profit-and-loss/export?${params.toString()}`, filename);
};

export const getProfitAndLossErrorMessage = (error: unknown): string =>
  getStatementErrorMessage(error, '損益計算書の取得に失敗しました');
