import { AxiosError } from 'axios';
import { axiosInstance } from './axios-instance';

export interface JournalEntrySummary extends Record<string, unknown> {
  journalEntryId: number;
  journalDate: string;
  description: string;
  totalDebitAmount: number;
  totalCreditAmount: number;
  status: string;
  version: number;
}

export interface GetJournalEntriesResult {
  content: JournalEntrySummary[];
  page: number;
  size: number;
  totalElements: number;
  totalPages: number;
}

export interface JournalEntriesSearchParams {
  page?: number;
  size?: number;
  status?: string[];
  dateFrom?: string;
  dateTo?: string;
}

const appendIfDefined = (
  searchParams: URLSearchParams,
  key: string,
  value: string | number | undefined
): void => {
  if (value !== undefined) {
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

const buildSearchParams = (params?: JournalEntriesSearchParams): URLSearchParams => {
  const searchParams = new URLSearchParams();
  appendIfDefined(searchParams, 'page', params?.page);
  appendIfDefined(searchParams, 'size', params?.size);
  appendStatusParams(searchParams, params?.status);
  appendIfDefined(searchParams, 'dateFrom', params?.dateFrom);
  appendIfDefined(searchParams, 'dateTo', params?.dateTo);
  return searchParams;
};

export const getJournalEntries = async (
  params?: JournalEntriesSearchParams
): Promise<GetJournalEntriesResult> => {
  const searchParams = buildSearchParams(params);
  const queryString = searchParams.toString();
  const url = queryString ? `/api/journal-entries?${queryString}` : '/api/journal-entries';
  const { data } = await axiosInstance.get<GetJournalEntriesResult>(url);
  return {
    ...data,
    content: data.content ?? [],
  };
};

export const getJournalEntriesErrorMessage = (error: unknown): string => {
  if (error instanceof AxiosError && error.response?.data) {
    const data = error.response.data as { errorMessage?: string };
    if (data.errorMessage) {
      return data.errorMessage;
    }
  }
  if (error instanceof Error) {
    return error.message;
  }
  return '仕訳一覧の取得に失敗しました';
};
