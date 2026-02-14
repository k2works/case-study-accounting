import React, { useCallback, useEffect, useMemo, useRef, useState } from 'react';
import { useRequireAuth } from '../hooks/useRequireAuth';
import { getDailyBalance, getDailyBalanceErrorMessage } from '../api/getDailyBalance';
import type {
  DailyBalanceEntry,
  DailyBalanceSearchParams,
  GetDailyBalanceResult,
} from '../api/getDailyBalance';
import { MainLayout, Loading, ErrorMessage } from '../views/common';
import { DailyBalanceFilter } from '../views/ledger/DailyBalanceFilter';
import type { DailyBalanceFilterValues } from '../views/ledger/DailyBalanceFilter';
import { DailyBalanceSummary } from '../views/ledger/DailyBalanceSummary';
import { DailyBalanceTable } from '../views/ledger/DailyBalanceTable';
import { DailyBalanceChart } from '../views/ledger/DailyBalanceChart';

interface DailyBalanceState {
  entries: DailyBalanceEntry[];
  accountId: number | null;
  accountCode: string;
  accountName: string;
  openingBalance: number;
  debitTotal: number;
  creditTotal: number;
  closingBalance: number;
  isLoading: boolean;
  errorMessage: string | null;
}

const initialState: DailyBalanceState = {
  entries: [],
  accountId: null,
  accountCode: '',
  accountName: '',
  openingBalance: 0,
  debitTotal: 0,
  creditTotal: 0,
  closingBalance: 0,
  isLoading: false,
  errorMessage: null,
};

const initialFilterValues: DailyBalanceFilterValues = {
  accountId: '',
  dateFrom: '',
  dateTo: '',
};

const useDailyBalanceFetch = () => {
  const [state, setState] = useState<DailyBalanceState>(initialState);
  const [filterValues, setFilterValues] = useState<DailyBalanceFilterValues>(initialFilterValues);
  const lastAutoFetchAccountId = useRef<string | null>(null);

  const fetchDailyBalance = useCallback(async (params: DailyBalanceSearchParams) => {
    setState((prev) => ({ ...prev, isLoading: true, errorMessage: null }));
    try {
      const data: GetDailyBalanceResult = await getDailyBalance(params);
      setState({
        entries: data.entries ?? [],
        accountId: data.accountId,
        accountCode: data.accountCode,
        accountName: data.accountName,
        openingBalance: data.openingBalance,
        debitTotal: data.debitTotal,
        creditTotal: data.creditTotal,
        closingBalance: data.closingBalance,
        isLoading: false,
        errorMessage: null,
      });
    } catch (error) {
      setState((prev) => ({
        ...prev,
        isLoading: false,
        errorMessage: getDailyBalanceErrorMessage(error),
      }));
    }
  }, []);

  const buildSearchParams = useCallback((): DailyBalanceSearchParams | null => {
    if (!filterValues.accountId) {
      return null;
    }
    const params: DailyBalanceSearchParams = {
      accountId: Number(filterValues.accountId),
    };
    if (filterValues.dateFrom) params.dateFrom = filterValues.dateFrom;
    if (filterValues.dateTo) params.dateTo = filterValues.dateTo;
    return params;
  }, [filterValues]);

  const handleSearch = useCallback(() => {
    const params = buildSearchParams();
    if (!params) {
      setState((prev) => ({ ...prev, errorMessage: '勘定科目を選択してください' }));
      return;
    }
    void fetchDailyBalance(params);
  }, [buildSearchParams, fetchDailyBalance]);

  useEffect(() => {
    if (!filterValues.accountId) {
      setState(initialState);
      lastAutoFetchAccountId.current = null;
      return;
    }
    if (lastAutoFetchAccountId.current === filterValues.accountId) {
      return;
    }
    lastAutoFetchAccountId.current = filterValues.accountId;
    const params: DailyBalanceSearchParams = {
      accountId: Number(filterValues.accountId),
    };
    if (filterValues.dateFrom) params.dateFrom = filterValues.dateFrom;
    if (filterValues.dateTo) params.dateTo = filterValues.dateTo;
    void fetchDailyBalance(params);
  }, [fetchDailyBalance, filterValues.accountId, filterValues.dateFrom, filterValues.dateTo]);

  return {
    state,
    filterValues,
    setFilterValues,
    handleSearch,
  };
};

interface DailyBalancePageContentProps {
  state: DailyBalanceState;
  filterValues: DailyBalanceFilterValues;
  onFilterChange: (values: DailyBalanceFilterValues) => void;
  onSearch: () => void;
}

const DailyBalancePageContent: React.FC<DailyBalancePageContentProps> = ({
  state,
  filterValues,
  onFilterChange,
  onSearch,
}) => {
  const hasAccount = Boolean(filterValues.accountId);
  const shouldShowBalance = hasAccount && (state.entries.length > 0 || !state.isLoading);

  return (
    <div data-testid="daily-balance-page">
      <h1>日次残高照会</h1>
      <DailyBalanceFilter values={filterValues} onChange={onFilterChange} onSearch={onSearch} />
      {state.isLoading && state.entries.length === 0 && (
        <Loading message="日次残高を読み込み中..." />
      )}
      {state.errorMessage ? (
        <ErrorMessage message={state.errorMessage} onRetry={onSearch} />
      ) : (
        shouldShowBalance && (
          <>
            <DailyBalanceSummary
              accountCode={state.accountCode}
              accountName={state.accountName}
              openingBalance={state.openingBalance}
              debitTotal={state.debitTotal}
              creditTotal={state.creditTotal}
              closingBalance={state.closingBalance}
            />
            <DailyBalanceChart entries={state.entries} />
            <DailyBalanceTable entries={state.entries} />
          </>
        )
      )}
    </div>
  );
};

const DailyBalancePage: React.FC = () => {
  const authGuard = useRequireAuth(['ADMIN', 'MANAGER', 'USER']);
  const { state, filterValues, setFilterValues, handleSearch } = useDailyBalanceFetch();

  const breadcrumbs = useMemo(
    () => [{ label: 'ホーム' }, { label: '元帳・残高' }, { label: '日次残高' }],
    []
  );

  if (authGuard) return authGuard;

  return (
    <MainLayout breadcrumbs={breadcrumbs}>
      <DailyBalancePageContent
        state={state}
        filterValues={filterValues}
        onFilterChange={setFilterValues}
        onSearch={handleSearch}
      />
    </MainLayout>
  );
};

export default DailyBalancePage;
