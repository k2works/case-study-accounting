import { AxiosError } from 'axios';
import { axiosInstance } from './axios-instance';

export interface DailyBalanceEntry {
  date: string;
  debitTotal: number;
  creditTotal: number;
  balance: number;
  transactionCount: number;
  [key: string]: unknown;
}

export interface GetDailyBalanceResult {
  accountId: number;
  accountCode: string;
  accountName: string;
  openingBalance: number;
  debitTotal: number;
  creditTotal: number;
  closingBalance: number;
  entries: DailyBalanceEntry[];
}

export interface DailyBalanceSearchParams {
  accountId: number;
  dateFrom?: string;
  dateTo?: string;
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

const buildSearchParams = (params: DailyBalanceSearchParams): URLSearchParams => {
  const searchParams = new URLSearchParams();
  appendIfDefined(searchParams, 'accountId', params.accountId);
  appendIfDefined(searchParams, 'dateFrom', params.dateFrom);
  appendIfDefined(searchParams, 'dateTo', params.dateTo);
  return searchParams;
};

export const getDailyBalance = async (
  params: DailyBalanceSearchParams
): Promise<GetDailyBalanceResult> => {
  const searchParams = buildSearchParams(params);
  const queryString = searchParams.toString();
  const url = queryString ? `/api/daily-balance?${queryString}` : '/api/daily-balance';
  const { data } = await axiosInstance.get<GetDailyBalanceResult>(url);
  return {
    ...data,
    entries: data.entries ?? [],
  };
};

export const getDailyBalanceErrorMessage = (error: unknown): string => {
  if (error instanceof AxiosError && error.response?.data) {
    const data = error.response.data as { errorMessage?: string };
    if (data.errorMessage) {
      return data.errorMessage;
    }
  }
  if (error instanceof Error) {
    return error.message;
  }
  return '日次残高の取得に失敗しました';
};
