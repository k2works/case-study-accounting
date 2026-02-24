import { AxiosError } from 'axios';
import { axiosInstance } from './axios-instance';
import { downloadExport } from './statementShared';

export interface GeneralLedgerEntry {
  journalEntryId: number;
  journalDate: string;
  description: string;
  debitAmount: number;
  creditAmount: number;
  runningBalance: number;
  [key: string]: unknown;
}

export interface GetGeneralLedgerResult {
  content: GeneralLedgerEntry[];
  accountId: number;
  accountCode: string;
  accountName: string;
  openingBalance: number;
  debitTotal: number;
  creditTotal: number;
  closingBalance: number;
  page: number;
  size: number;
  totalElements: number;
  totalPages: number;
}

export interface GeneralLedgerSearchParams {
  accountId: number;
  dateFrom?: string;
  dateTo?: string;
  page?: number;
  size?: number;
}

const appendIfDefined = (
  searchParams: URLSearchParams,
  key: string,
  value: string | number | undefined
): void => {
  if (value !== undefined && value !== '') {
    searchParams.append(key, value.toString());
  }
};

const buildSearchParams = (params: GeneralLedgerSearchParams): URLSearchParams => {
  const searchParams = new URLSearchParams();
  appendIfDefined(searchParams, 'accountId', params.accountId);
  appendIfDefined(searchParams, 'dateFrom', params.dateFrom);
  appendIfDefined(searchParams, 'dateTo', params.dateTo);
  appendIfDefined(searchParams, 'page', params.page);
  appendIfDefined(searchParams, 'size', params.size);
  return searchParams;
};

export const getGeneralLedger = async (
  params: GeneralLedgerSearchParams
): Promise<GetGeneralLedgerResult> => {
  const searchParams = buildSearchParams(params);
  const queryString = searchParams.toString();
  const url = queryString ? `/api/general-ledger?${queryString}` : '/api/general-ledger';
  const { data } = await axiosInstance.get<GetGeneralLedgerResult>(url);
  return {
    ...data,
    content: data.content ?? [],
  };
};

export type GeneralLedgerExportFormat = 'csv' | 'excel';

export const exportGeneralLedger = async (
  format: GeneralLedgerExportFormat,
  accountId: number,
  dateFrom?: string,
  dateTo?: string
): Promise<void> => {
  const params = new URLSearchParams();
  params.append('format', format);
  params.append('accountId', accountId.toString());
  if (dateFrom) {
    params.append('dateFrom', dateFrom);
  }
  if (dateTo) {
    params.append('dateTo', dateTo);
  }
  const ext = format === 'csv' ? 'csv' : 'xlsx';
  await downloadExport(`/api/general-ledger/export?${params.toString()}`, `general-ledger.${ext}`);
};

export const getGeneralLedgerErrorMessage = (error: unknown): string => {
  if (error instanceof AxiosError && error.response?.data) {
    const data = error.response.data as { errorMessage?: string };
    if (data.errorMessage) {
      return data.errorMessage;
    }
  }
  if (error instanceof Error) {
    return error.message;
  }
  return '総勘定元帳の取得に失敗しました';
};
