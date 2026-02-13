import React, { useCallback, useMemo, useState } from 'react';
import { Navigate } from 'react-router-dom';
import { useAuth } from '../hooks/useAuth';
import { getTrialBalance, getTrialBalanceErrorMessage } from '../api/getTrialBalance';
import type {
  TrialBalanceEntry,
  TrialBalanceSearchParams,
  GetTrialBalanceResult,
  CategorySubtotal,
} from '../api/getTrialBalance';
import { MainLayout, Loading, ErrorMessage } from '../views/common';
import { TrialBalanceFilter } from '../views/ledger/TrialBalanceFilter';
import type { TrialBalanceFilterValues } from '../views/ledger/TrialBalanceFilter';
import { TrialBalanceSummary } from '../views/ledger/TrialBalanceSummary';
import { TrialBalanceTable } from '../views/ledger/TrialBalanceTable';

interface TrialBalanceState {
  entries: TrialBalanceEntry[];
  categorySubtotals: CategorySubtotal[];
  date: string | null;
  totalDebit: number;
  totalCredit: number;
  balanced: boolean;
  difference: number;
  isLoading: boolean;
  errorMessage: string | null;
  hasSearched: boolean;
}

const initialState: TrialBalanceState = {
  entries: [],
  categorySubtotals: [],
  date: null,
  totalDebit: 0,
  totalCredit: 0,
  balanced: true,
  difference: 0,
  isLoading: false,
  errorMessage: null,
  hasSearched: false,
};

const initialFilterValues: TrialBalanceFilterValues = {
  date: '',
};

const withDefaults = (data: GetTrialBalanceResult) => ({
  entries: data.entries ?? [],
  categorySubtotals: data.categorySubtotals ?? [],
  date: data.date ?? null,
  totalDebit: data.totalDebit ?? 0,
});

const toTrialBalanceState = (data: GetTrialBalanceResult): TrialBalanceState => ({
  ...withDefaults(data),
  totalCredit: data.totalCredit ?? 0,
  balanced: data.balanced ?? true,
  difference: data.difference ?? 0,
  isLoading: false,
  errorMessage: null,
  hasSearched: true,
});

const useTrialBalanceFetch = () => {
  const [state, setState] = useState<TrialBalanceState>(initialState);
  const [filterValues, setFilterValues] = useState<TrialBalanceFilterValues>(initialFilterValues);

  const fetchTrialBalance = useCallback(async (params: TrialBalanceSearchParams) => {
    setState((prev) => ({ ...prev, isLoading: true, errorMessage: null }));
    try {
      const data: GetTrialBalanceResult = await getTrialBalance(params);
      setState(toTrialBalanceState(data));
    } catch (error) {
      setState((prev) => ({
        ...prev,
        isLoading: false,
        errorMessage: getTrialBalanceErrorMessage(error),
      }));
    }
  }, []);

  const handleSearch = useCallback(() => {
    const params: TrialBalanceSearchParams = {};
    if (filterValues.date) {
      params.date = filterValues.date;
    }
    void fetchTrialBalance(params);
  }, [filterValues, fetchTrialBalance]);

  return {
    state,
    filterValues,
    setFilterValues,
    handleSearch,
  };
};

interface TrialBalancePageContentProps {
  state: TrialBalanceState;
  filterValues: TrialBalanceFilterValues;
  onFilterChange: (values: TrialBalanceFilterValues) => void;
  onSearch: () => void;
}

const TrialBalancePageContent: React.FC<TrialBalancePageContentProps> = ({
  state,
  filterValues,
  onFilterChange,
  onSearch,
}) => {
  return (
    <div data-testid="trial-balance-page">
      <h1>残高試算表</h1>
      <TrialBalanceFilter values={filterValues} onChange={onFilterChange} onSearch={onSearch} />
      {state.isLoading && !state.hasSearched && <Loading message="残高試算表を読み込み中..." />}
      {state.errorMessage ? (
        <ErrorMessage message={state.errorMessage} onRetry={onSearch} />
      ) : (
        state.hasSearched && (
          <>
            <TrialBalanceSummary
              date={state.date}
              totalDebit={state.totalDebit}
              totalCredit={state.totalCredit}
              balanced={state.balanced}
              difference={state.difference}
              categorySubtotals={state.categorySubtotals}
            />
            <TrialBalanceTable entries={state.entries} />
          </>
        )
      )}
    </div>
  );
};

const TrialBalancePage: React.FC = () => {
  const { isAuthenticated, isLoading, hasRole } = useAuth();
  const { state, filterValues, setFilterValues, handleSearch } = useTrialBalanceFetch();

  const breadcrumbs = useMemo(
    () => [{ label: 'ホーム' }, { label: '元帳・残高' }, { label: '残高試算表' }],
    []
  );

  if (isLoading) {
    return <Loading message="認証情報を確認中..." fullScreen />;
  }

  if (!isAuthenticated) {
    return <Navigate to="/login" replace />;
  }

  if (!hasRole('ADMIN') && !hasRole('MANAGER') && !hasRole('USER')) {
    return <Navigate to="/" replace />;
  }

  return (
    <MainLayout breadcrumbs={breadcrumbs}>
      <TrialBalancePageContent
        state={state}
        filterValues={filterValues}
        onFilterChange={setFilterValues}
        onSearch={handleSearch}
      />
    </MainLayout>
  );
};

export default TrialBalancePage;
