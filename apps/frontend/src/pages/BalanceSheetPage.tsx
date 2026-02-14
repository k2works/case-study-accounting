import React, { useCallback, useMemo, useState } from 'react';
import { useRequireAuth } from '../hooks/useRequireAuth';
import {
  getBalanceSheet,
  getBalanceSheetErrorMessage,
  exportBalanceSheet,
} from '../api/getBalanceSheet';
import type {
  BalanceSheetSection,
  BalanceSheetSearchParams,
  GetBalanceSheetResult,
} from '../api/getBalanceSheet';
import { MainLayout, Loading, ErrorMessage, Button } from '../views/common';
import { BalanceSheetFilter } from '../views/statement/BalanceSheetFilter';
import type { BalanceSheetFilterValues } from '../views/statement/BalanceSheetFilter';
import { BalanceSheetSummary } from '../views/statement/BalanceSheetSummary';
import { BalanceSheetTable } from '../views/statement/BalanceSheetTable';

interface BalanceSheetState {
  sections: BalanceSheetSection[];
  date: string | null;
  comparativeDate: string | null;
  totalAssets: number;
  totalLiabilities: number;
  totalEquity: number;
  totalLiabilitiesAndEquity: number;
  balanced: boolean;
  difference: number;
  isLoading: boolean;
  errorMessage: string | null;
  hasSearched: boolean;
}

const initialState: BalanceSheetState = {
  sections: [],
  date: null,
  comparativeDate: null,
  totalAssets: 0,
  totalLiabilities: 0,
  totalEquity: 0,
  totalLiabilitiesAndEquity: 0,
  balanced: true,
  difference: 0,
  isLoading: false,
  errorMessage: null,
  hasSearched: false,
};

const initialFilterValues: BalanceSheetFilterValues = {
  date: '',
  comparativeDate: '',
};

const withDefaults = (data: GetBalanceSheetResult) => ({
  sections: data.sections ?? [],
  date: data.date ?? null,
  comparativeDate: data.comparativeDate ?? null,
  totalAssets: data.totalAssets ?? 0,
  totalLiabilities: data.totalLiabilities ?? 0,
});

const withTotals = (data: GetBalanceSheetResult) => ({
  totalEquity: data.totalEquity ?? 0,
  totalLiabilitiesAndEquity: data.totalLiabilitiesAndEquity ?? 0,
  balanced: data.balanced ?? true,
  difference: data.difference ?? 0,
});

const toBalanceSheetState = (data: GetBalanceSheetResult): BalanceSheetState => ({
  ...withDefaults(data),
  ...withTotals(data),
  isLoading: false,
  errorMessage: null,
  hasSearched: true,
});

const useBalanceSheetFetch = () => {
  const [state, setState] = useState<BalanceSheetState>(initialState);
  const [filterValues, setFilterValues] = useState<BalanceSheetFilterValues>(initialFilterValues);

  const fetchBalanceSheet = useCallback(async (params: BalanceSheetSearchParams) => {
    setState((prev) => ({ ...prev, isLoading: true, errorMessage: null }));
    try {
      const data: GetBalanceSheetResult = await getBalanceSheet(params);
      setState(toBalanceSheetState(data));
    } catch (error) {
      setState((prev) => ({
        ...prev,
        isLoading: false,
        errorMessage: getBalanceSheetErrorMessage(error),
      }));
    }
  }, []);

  const handleSearch = useCallback(() => {
    const params: BalanceSheetSearchParams = {};
    if (filterValues.date) {
      params.date = filterValues.date;
    }
    if (filterValues.comparativeDate) {
      params.comparativeDate = filterValues.comparativeDate;
    }
    void fetchBalanceSheet(params);
  }, [filterValues, fetchBalanceSheet]);

  return {
    state,
    filterValues,
    setFilterValues,
    handleSearch,
  };
};

const handleExport = (format: 'pdf' | 'excel', date: string | null) => {
  void exportBalanceSheet(format, date ?? undefined);
};

interface BalanceSheetPageContentProps {
  state: BalanceSheetState;
  filterValues: BalanceSheetFilterValues;
  onFilterChange: (values: BalanceSheetFilterValues) => void;
  onSearch: () => void;
}

const ExportButtons: React.FC<{ date: string | null }> = ({ date }) => (
  <div style={{ display: 'flex', gap: '8px', marginTop: '12px' }}>
    <Button variant="secondary" onClick={() => handleExport('excel', date)}>
      Excel
    </Button>
    <Button variant="secondary" onClick={() => handleExport('pdf', date)}>
      PDF
    </Button>
  </div>
);

const BalanceSheetPageContent: React.FC<BalanceSheetPageContentProps> = ({
  state,
  filterValues,
  onFilterChange,
  onSearch,
}) => {
  return (
    <div data-testid="balance-sheet-page">
      <h1>貸借対照表</h1>
      <BalanceSheetFilter values={filterValues} onChange={onFilterChange} onSearch={onSearch} />
      {state.isLoading && !state.hasSearched && <Loading message="貸借対照表を読み込み中..." />}
      {state.errorMessage ? (
        <ErrorMessage message={state.errorMessage} onRetry={onSearch} />
      ) : (
        state.hasSearched && (
          <>
            <BalanceSheetSummary
              date={state.date}
              comparativeDate={state.comparativeDate}
              totalAssets={state.totalAssets}
              totalLiabilities={state.totalLiabilities}
              totalEquity={state.totalEquity}
              totalLiabilitiesAndEquity={state.totalLiabilitiesAndEquity}
              balanced={state.balanced}
              difference={state.difference}
            />
            <ExportButtons date={state.date} />
            <BalanceSheetTable sections={state.sections} hasComparative={!!state.comparativeDate} />
          </>
        )
      )}
    </div>
  );
};

const BalanceSheetPage: React.FC = () => {
  const authGuard = useRequireAuth(['ADMIN', 'MANAGER', 'USER']);
  const { state, filterValues, setFilterValues, handleSearch } = useBalanceSheetFetch();

  const breadcrumbs = useMemo(
    () => [{ label: 'ホーム' }, { label: '財務諸表' }, { label: '貸借対照表' }],
    []
  );

  if (authGuard) return authGuard;

  return (
    <MainLayout breadcrumbs={breadcrumbs}>
      <BalanceSheetPageContent
        state={state}
        filterValues={filterValues}
        onFilterChange={setFilterValues}
        onSearch={handleSearch}
      />
    </MainLayout>
  );
};

export default BalanceSheetPage;
