import { axiosInstance } from './axios-instance';
import { downloadExport, getStatementErrorMessage } from './statementShared';
import type { ComparativeData } from './statementShared';

export type { ComparativeData } from './statementShared';

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

export type BalanceSheetExportFormat = 'csv' | 'excel' | 'pdf';

export const exportBalanceSheet = async (
  format: BalanceSheetExportFormat,
  date?: string
): Promise<void> => {
  const params = new URLSearchParams();
  params.append('format', format);
  if (date) {
    params.append('date', date);
  }
  const extMap: Record<BalanceSheetExportFormat, string> = {
    csv: 'csv',
    excel: 'xlsx',
    pdf: 'pdf',
  };
  await downloadExport(
    `/api/balance-sheet/export?${params.toString()}`,
    `balance-sheet.${extMap[format]}`
  );
};

export const getBalanceSheetErrorMessage = (error: unknown): string =>
  getStatementErrorMessage(error, '貸借対照表の取得に失敗しました');
