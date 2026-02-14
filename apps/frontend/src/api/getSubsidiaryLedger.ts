import { AxiosError } from 'axios';
import { axiosInstance } from './axios-instance';

export interface SubsidiaryLedgerEntry {
  journalEntryId: number;
  journalDate: string;
  description: string;
  debitAmount: number;
  creditAmount: number;
  runningBalance: number;
  [key: string]: unknown;
}

export interface GetSubsidiaryLedgerResult {
  content: SubsidiaryLedgerEntry[];
  accountCode: string;
  accountName: string;
  subAccountCode: string;
  openingBalance: number;
  debitTotal: number;
  creditTotal: number;
  closingBalance: number;
  page: number;
  size: number;
  totalElements: number;
  totalPages: number;
}

export interface SubsidiaryLedgerSearchParams {
  accountCode: string;
  subAccountCode?: string;
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

const buildSearchParams = (params: SubsidiaryLedgerSearchParams): URLSearchParams => {
  const searchParams = new URLSearchParams();
  appendIfDefined(searchParams, 'accountCode', params.accountCode);
  appendIfDefined(searchParams, 'subAccountCode', params.subAccountCode);
  appendIfDefined(searchParams, 'dateFrom', params.dateFrom);
  appendIfDefined(searchParams, 'dateTo', params.dateTo);
  appendIfDefined(searchParams, 'page', params.page);
  appendIfDefined(searchParams, 'size', params.size);
  return searchParams;
};

export const getSubsidiaryLedger = async (
  params: SubsidiaryLedgerSearchParams
): Promise<GetSubsidiaryLedgerResult> => {
  const searchParams = buildSearchParams(params);
  const queryString = searchParams.toString();
  const url = queryString ? `/api/subsidiary-ledger?${queryString}` : '/api/subsidiary-ledger';
  const { data } = await axiosInstance.get<GetSubsidiaryLedgerResult>(url);
  return {
    ...data,
    content: data.content ?? [],
  };
};

export const getSubsidiaryLedgerErrorMessage = (error: unknown): string => {
  if (error instanceof AxiosError && error.response?.data) {
    const data = error.response.data as { errorMessage?: string };
    if (data.errorMessage) {
      return data.errorMessage;
    }
  }
  if (error instanceof Error) {
    return error.message;
  }
  return '補助元帳の取得に失敗しました';
};
