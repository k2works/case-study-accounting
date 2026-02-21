import { axiosInstance } from './axios-instance';
import { getStatementErrorMessage } from './statementShared';

export interface FinancialIndicator {
  name: string;
  unit: string;
  value: number;
  previousValue: number | null;
  difference: number | null;
  changeRate: number | null;
  formula: string;
  industryAverage: number;
}

export interface IndicatorCategory {
  categoryName: string;
  categoryDisplayName: string;
  indicators: FinancialIndicator[];
}

export interface GetFinancialAnalysisResult {
  dateFrom: string | null;
  dateTo: string | null;
  comparativeDateFrom: string | null;
  comparativeDateTo: string | null;
  categories: IndicatorCategory[];
}

export interface FinancialAnalysisSearchParams {
  dateFrom?: string;
  dateTo?: string;
  comparativeDateFrom?: string;
  comparativeDateTo?: string;
}

const buildSearchParams = (params: FinancialAnalysisSearchParams): URLSearchParams => {
  const searchParams = new URLSearchParams();
  if (params.dateFrom) searchParams.append('dateFrom', params.dateFrom);
  if (params.dateTo) searchParams.append('dateTo', params.dateTo);
  if (params.comparativeDateFrom)
    searchParams.append('comparativeDateFrom', params.comparativeDateFrom);
  if (params.comparativeDateTo) searchParams.append('comparativeDateTo', params.comparativeDateTo);
  return searchParams;
};

export const getFinancialAnalysis = async (
  params: FinancialAnalysisSearchParams
): Promise<GetFinancialAnalysisResult> => {
  const searchParams = buildSearchParams(params);
  const queryString = searchParams.toString();
  const url = queryString ? `/api/financial-analysis?${queryString}` : '/api/financial-analysis';
  const { data } = await axiosInstance.get<GetFinancialAnalysisResult>(url);
  return {
    ...data,
    categories: data.categories ?? [],
  };
};

export const getFinancialAnalysisErrorMessage = (error: unknown): string =>
  getStatementErrorMessage(error, '財務分析の取得に失敗しました');
