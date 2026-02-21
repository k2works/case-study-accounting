import React, { useCallback, useMemo, useState } from 'react';
import { useRequireAuth } from '../hooks/useRequireAuth';
import {
  getFinancialAnalysis,
  getFinancialAnalysisErrorMessage,
} from '../api/getFinancialAnalysis';
import type {
  IndicatorCategory,
  FinancialAnalysisSearchParams,
  GetFinancialAnalysisResult,
} from '../api/getFinancialAnalysis';
import { MainLayout, Loading, ErrorMessage } from '../views/common';
import { FinancialAnalysisFilter } from '../views/statement/FinancialAnalysisFilter';
import type { FinancialAnalysisFilterValues } from '../views/statement/FinancialAnalysisFilter';
import { FinancialAnalysisIndicators } from '../views/statement/FinancialAnalysisIndicators';
import { FinancialAnalysisTrend } from '../views/statement/FinancialAnalysisTrend';

interface FinancialAnalysisState {
  categories: IndicatorCategory[];
  dateFrom: string | null;
  dateTo: string | null;
  comparativeDateFrom: string | null;
  comparativeDateTo: string | null;
  isLoading: boolean;
  errorMessage: string | null;
  hasSearched: boolean;
}

const initialState: FinancialAnalysisState = {
  categories: [],
  dateFrom: null,
  dateTo: null,
  comparativeDateFrom: null,
  comparativeDateTo: null,
  isLoading: false,
  errorMessage: null,
  hasSearched: false,
};

const initialFilterValues: FinancialAnalysisFilterValues = {
  dateFrom: '',
  dateTo: '',
  comparativeDateFrom: '',
  comparativeDateTo: '',
};

const toResultState = (data: GetFinancialAnalysisResult): FinancialAnalysisState => ({
  categories: data.categories ?? [],
  dateFrom: data.dateFrom ?? null,
  dateTo: data.dateTo ?? null,
  comparativeDateFrom: data.comparativeDateFrom ?? null,
  comparativeDateTo: data.comparativeDateTo ?? null,
  isLoading: false,
  errorMessage: null,
  hasSearched: true,
});

const hasComparativePeriod = (state: FinancialAnalysisState): boolean =>
  Boolean(state.comparativeDateFrom || state.comparativeDateTo);

const useFinancialAnalysisFetch = () => {
  const [state, setState] = useState<FinancialAnalysisState>(initialState);
  const [filterValues, setFilterValues] =
    useState<FinancialAnalysisFilterValues>(initialFilterValues);

  const fetchData = useCallback(async (params: FinancialAnalysisSearchParams) => {
    setState((prev) => ({ ...prev, isLoading: true, errorMessage: null }));
    try {
      const data = await getFinancialAnalysis(params);
      setState(toResultState(data));
    } catch (error) {
      setState((prev) => ({
        ...prev,
        isLoading: false,
        errorMessage: getFinancialAnalysisErrorMessage(error),
      }));
    }
  }, []);

  const handleSearch = useCallback(() => {
    const params: FinancialAnalysisSearchParams = {};
    if (filterValues.dateFrom) params.dateFrom = filterValues.dateFrom;
    if (filterValues.dateTo) params.dateTo = filterValues.dateTo;
    if (filterValues.comparativeDateFrom)
      params.comparativeDateFrom = filterValues.comparativeDateFrom;
    if (filterValues.comparativeDateTo) params.comparativeDateTo = filterValues.comparativeDateTo;
    void fetchData(params);
  }, [filterValues, fetchData]);

  return { state, filterValues, setFilterValues, handleSearch };
};

const FinancialAnalysisPage: React.FC = () => {
  const authGuard = useRequireAuth(['ADMIN', 'MANAGER']);
  const { state, filterValues, setFilterValues, handleSearch } = useFinancialAnalysisFetch();
  const hasComparative = hasComparativePeriod(state);

  const breadcrumbs = useMemo(
    () => [{ label: 'ホーム' }, { label: '財務諸表' }, { label: '財務分析' }],
    []
  );

  if (authGuard) return authGuard;

  return (
    <MainLayout breadcrumbs={breadcrumbs}>
      <div data-testid="financial-analysis-page">
        <h1>財務分析</h1>
        <FinancialAnalysisFilter
          values={filterValues}
          onChange={setFilterValues}
          onSearch={handleSearch}
        />
        {state.isLoading && !state.hasSearched && <Loading message="財務分析を読み込み中..." />}
        {state.errorMessage ? (
          <ErrorMessage message={state.errorMessage} onRetry={handleSearch} />
        ) : (
          state.hasSearched && (
            <>
              <FinancialAnalysisIndicators
                categories={state.categories}
                hasComparative={hasComparative}
              />
              <FinancialAnalysisTrend categories={state.categories} />
            </>
          )
        )}
      </div>
    </MainLayout>
  );
};

export default FinancialAnalysisPage;
