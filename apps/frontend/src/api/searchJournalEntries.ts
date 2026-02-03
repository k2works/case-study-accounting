import { AxiosError } from 'axios';
import { axiosInstance } from './axios-instance';
import type { GetJournalEntriesResult } from './getJournalEntries';

export interface SearchJournalEntriesParams {
  page?: number;
  size?: number;
  status?: string[];
  dateFrom?: string;
  dateTo?: string;
  accountId?: number;
  amountFrom?: number;
  amountTo?: number;
  description?: string;
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

const appendStatusParams = (
  searchParams: URLSearchParams,
  statuses: string[] | undefined
): void => {
  if (statuses && statuses.length > 0) {
    statuses.forEach((s) => searchParams.append('status', s));
  }
};

const SEARCH_PARAM_KEYS: readonly (keyof Omit<SearchJournalEntriesParams, 'status'>)[] = [
  'page',
  'size',
  'dateFrom',
  'dateTo',
  'accountId',
  'amountFrom',
  'amountTo',
  'description',
] as const;

const buildSearchParams = (params?: SearchJournalEntriesParams): URLSearchParams => {
  const searchParams = new URLSearchParams();
  if (!params) return searchParams;
  SEARCH_PARAM_KEYS.forEach((key) => appendIfDefined(searchParams, key, params[key]));
  appendStatusParams(searchParams, params.status);
  return searchParams;
};

export const searchJournalEntries = async (
  params?: SearchJournalEntriesParams
): Promise<GetJournalEntriesResult> => {
  const searchParams = buildSearchParams(params);
  const queryString = searchParams.toString();
  const url = queryString
    ? `/api/journal-entries/search?${queryString}`
    : '/api/journal-entries/search';
  const { data } = await axiosInstance.get<GetJournalEntriesResult>(url);
  return {
    ...data,
    content: data.content ?? [],
  };
};

export const searchJournalEntriesErrorMessage = (error: unknown): string => {
  if (error instanceof AxiosError && error.response?.data) {
    const data = error.response.data as { errorMessage?: string };
    if (data.errorMessage) {
      return data.errorMessage;
    }
  }
  if (error instanceof Error) {
    return error.message;
  }
  return '仕訳の検索に失敗しました';
};
