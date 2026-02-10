import { AxiosError } from 'axios';
import { axiosInstance } from './axios-instance';

export interface MonthlyBalanceEntry {
  month: number;
  openingBalance: number;
  debitAmount: number;
  creditAmount: number;
  closingBalance: number;
  [key: string]: unknown;
}

export interface GetMonthlyBalanceResult {
  accountCode: string;
  accountName: string;
  fiscalPeriod: number;
  openingBalance: number;
  debitTotal: number;
  creditTotal: number;
  closingBalance: number;
  entries: MonthlyBalanceEntry[];
}

export interface MonthlyBalanceSearchParams {
  accountCode: string;
  fiscalPeriod?: number;
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

const buildSearchParams = (params: MonthlyBalanceSearchParams): URLSearchParams => {
  const searchParams = new URLSearchParams();
  appendIfDefined(searchParams, 'accountCode', params.accountCode);
  appendIfDefined(searchParams, 'fiscalPeriod', params.fiscalPeriod);
  return searchParams;
};

export const getMonthlyBalance = async (
  params: MonthlyBalanceSearchParams
): Promise<GetMonthlyBalanceResult> => {
  const searchParams = buildSearchParams(params);
  const queryString = searchParams.toString();
  const url = queryString ? `/api/monthly-balance?${queryString}` : '/api/monthly-balance';
  const { data } = await axiosInstance.get<GetMonthlyBalanceResult>(url);
  return {
    ...data,
    entries: data.entries ?? [],
  };
};

export const getMonthlyBalanceErrorMessage = (error: unknown): string => {
  if (error instanceof AxiosError && error.response?.data) {
    const data = error.response.data as { errorMessage?: string };
    if (data.errorMessage) {
      return data.errorMessage;
    }
  }
  if (error instanceof Error) {
    return error.message;
  }
  return '月次残高の取得に失敗しました';
};
