import React, { useCallback, useMemo, useState } from 'react';
import { useRequireAuth } from '../hooks/useRequireAuth';
import {
  getProfitAndLoss,
  getProfitAndLossErrorMessage,
  exportProfitAndLoss,
} from '../api/getProfitAndLoss';
import type {
  ProfitAndLossSection,
  ProfitAndLossSearchParams,
  GetProfitAndLossResult,
} from '../api/getProfitAndLoss';
import { MainLayout, Loading, ErrorMessage, Button } from '../views/common';
import { ProfitAndLossFilter } from '../views/statement/ProfitAndLossFilter';
import type { ProfitAndLossFilterValues } from '../views/statement/ProfitAndLossFilter';
import { ProfitAndLossSummary } from '../views/statement/ProfitAndLossSummary';
import { ProfitAndLossTable } from '../views/statement/ProfitAndLossTable';

interface ProfitAndLossState {
  sections: ProfitAndLossSection[];
  dateFrom: string | null;
  dateTo: string | null;
  comparativeDateFrom: string | null;
  comparativeDateTo: string | null;
  totalRevenue: number;
  totalExpense: number;
  netIncome: number;
  isLoading: boolean;
  errorMessage: string | null;
  hasSearched: boolean;
}

const initialState: ProfitAndLossState = {
  sections: [],
  dateFrom: null,
  dateTo: null,
  comparativeDateFrom: null,
  comparativeDateTo: null,
  totalRevenue: 0,
  totalExpense: 0,
  netIncome: 0,
  isLoading: false,
  errorMessage: null,
  hasSearched: false,
};

const initialFilterValues: ProfitAndLossFilterValues = {
  dateFrom: '',
  dateTo: '',
  comparativeDateFrom: '',
  comparativeDateTo: '',
};

const withDates = (data: GetProfitAndLossResult) => ({
  sections: data.sections ?? [],
  dateFrom: data.dateFrom ?? null,
  dateTo: data.dateTo ?? null,
  comparativeDateFrom: data.comparativeDateFrom ?? null,
  comparativeDateTo: data.comparativeDateTo ?? null,
});

const withTotals = (data: GetProfitAndLossResult) => ({
  totalRevenue: data.totalRevenue ?? 0,
  totalExpense: data.totalExpense ?? 0,
  netIncome: data.netIncome ?? 0,
});

const toProfitAndLossState = (data: GetProfitAndLossResult): ProfitAndLossState => ({
  ...withDates(data),
  ...withTotals(data),
  isLoading: false,
  errorMessage: null,
  hasSearched: true,
});

const useProfitAndLossFetch = () => {
  const [state, setState] = useState<ProfitAndLossState>(initialState);
  const [filterValues, setFilterValues] = useState<ProfitAndLossFilterValues>(initialFilterValues);

  const fetchProfitAndLoss = useCallback(async (params: ProfitAndLossSearchParams) => {
    setState((prev) => ({ ...prev, isLoading: true, errorMessage: null }));
    try {
      const data: GetProfitAndLossResult = await getProfitAndLoss(params);
      setState(toProfitAndLossState(data));
    } catch (error) {
      setState((prev) => ({
        ...prev,
        isLoading: false,
        errorMessage: getProfitAndLossErrorMessage(error),
      }));
    }
  }, []);

  const handleSearch = useCallback(() => {
    const params: ProfitAndLossSearchParams = {};
    if (filterValues.dateFrom) {
      params.dateFrom = filterValues.dateFrom;
    }
    if (filterValues.dateTo) {
      params.dateTo = filterValues.dateTo;
    }
    if (filterValues.comparativeDateFrom) {
      params.comparativeDateFrom = filterValues.comparativeDateFrom;
    }
    if (filterValues.comparativeDateTo) {
      params.comparativeDateTo = filterValues.comparativeDateTo;
    }
    void fetchProfitAndLoss(params);
  }, [filterValues, fetchProfitAndLoss]);

  return {
    state,
    filterValues,
    setFilterValues,
    handleSearch,
  };
};

const handleExport = (format: 'pdf' | 'excel', dateFrom: string | null, dateTo: string | null) => {
  void exportProfitAndLoss(format, dateFrom ?? undefined, dateTo ?? undefined);
};

interface ProfitAndLossPageContentProps {
  state: ProfitAndLossState;
  filterValues: ProfitAndLossFilterValues;
  onFilterChange: (values: ProfitAndLossFilterValues) => void;
  onSearch: () => void;
}

const ExportButtons: React.FC<{ dateFrom: string | null; dateTo: string | null }> = ({
  dateFrom,
  dateTo,
}) => (
  <div style={{ display: 'flex', gap: '8px', marginTop: '12px' }}>
    <Button variant="secondary" onClick={() => handleExport('excel', dateFrom, dateTo)}>
      Excel
    </Button>
    <Button variant="secondary" onClick={() => handleExport('pdf', dateFrom, dateTo)}>
      PDF
    </Button>
  </div>
);

const getPreviousAmount = (section: ProfitAndLossSection | undefined): number =>
  section?.comparativeSubtotal?.previousAmount ?? 0;

const hasComparativePeriod = (state: ProfitAndLossState): boolean =>
  !!(state.comparativeDateFrom || state.comparativeDateTo);

const computeComparativeNetIncome = (state: ProfitAndLossState): number | null => {
  if (!hasComparativePeriod(state)) return null;
  const revSection = state.sections.find((s) => s.sectionType === 'REVENUE');
  const expSection = state.sections.find((s) => s.sectionType === 'EXPENSE');
  return getPreviousAmount(revSection) - getPreviousAmount(expSection);
};

const ProfitAndLossPageContent: React.FC<ProfitAndLossPageContentProps> = ({
  state,
  filterValues,
  onFilterChange,
  onSearch,
}) => {
  const hasComparative = !!(state.comparativeDateFrom || state.comparativeDateTo);
  const comparativeNetIncome = computeComparativeNetIncome(state);

  return (
    <div data-testid="profit-and-loss-page">
      <h1>損益計算書</h1>
      <ProfitAndLossFilter values={filterValues} onChange={onFilterChange} onSearch={onSearch} />
      {state.isLoading && !state.hasSearched && <Loading message="損益計算書を読み込み中..." />}
      {state.errorMessage ? (
        <ErrorMessage message={state.errorMessage} onRetry={onSearch} />
      ) : (
        state.hasSearched && (
          <>
            <ProfitAndLossSummary
              dateFrom={state.dateFrom}
              dateTo={state.dateTo}
              comparativeDateFrom={state.comparativeDateFrom}
              comparativeDateTo={state.comparativeDateTo}
              totalRevenue={state.totalRevenue}
              totalExpense={state.totalExpense}
              netIncome={state.netIncome}
            />
            <ExportButtons dateFrom={state.dateFrom} dateTo={state.dateTo} />
            <ProfitAndLossTable
              sections={state.sections}
              hasComparative={hasComparative}
              netIncome={state.netIncome}
              comparativeNetIncome={comparativeNetIncome}
            />
          </>
        )
      )}
    </div>
  );
};

const ProfitAndLossPage: React.FC = () => {
  const authGuard = useRequireAuth(['ADMIN', 'MANAGER', 'USER']);
  const { state, filterValues, setFilterValues, handleSearch } = useProfitAndLossFetch();

  const breadcrumbs = useMemo(
    () => [{ label: 'ホーム' }, { label: '財務諸表' }, { label: '損益計算書' }],
    []
  );

  if (authGuard) return authGuard;

  return (
    <MainLayout breadcrumbs={breadcrumbs}>
      <ProfitAndLossPageContent
        state={state}
        filterValues={filterValues}
        onFilterChange={setFilterValues}
        onSearch={handleSearch}
      />
    </MainLayout>
  );
};

export default ProfitAndLossPage;
