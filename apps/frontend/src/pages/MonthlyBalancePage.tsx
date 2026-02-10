import React, { useCallback, useEffect, useMemo, useRef, useState } from 'react';
import { Navigate } from 'react-router-dom';
import { useAuth } from '../hooks/useAuth';
import { getMonthlyBalance, getMonthlyBalanceErrorMessage } from '../api/getMonthlyBalance';
import type {
  MonthlyBalanceEntry,
  MonthlyBalanceSearchParams,
  GetMonthlyBalanceResult,
} from '../api/getMonthlyBalance';
import { MainLayout, Loading, ErrorMessage } from '../views/common';
import { MonthlyBalanceFilter } from '../views/ledger/MonthlyBalanceFilter';
import type { MonthlyBalanceFilterValues } from '../views/ledger/MonthlyBalanceFilter';
import { MonthlyBalanceSummary } from '../views/ledger/MonthlyBalanceSummary';
import { MonthlyBalanceTable } from '../views/ledger/MonthlyBalanceTable';
import { MonthlyBalanceChart } from '../views/ledger/MonthlyBalanceChart';

interface MonthlyBalanceState {
  entries: MonthlyBalanceEntry[];
  accountCode: string;
  accountName: string;
  fiscalPeriod: number | null;
  openingBalance: number;
  debitTotal: number;
  creditTotal: number;
  closingBalance: number;
  isLoading: boolean;
  errorMessage: string | null;
}

const initialState: MonthlyBalanceState = {
  entries: [],
  accountCode: '',
  accountName: '',
  fiscalPeriod: null,
  openingBalance: 0,
  debitTotal: 0,
  creditTotal: 0,
  closingBalance: 0,
  isLoading: false,
  errorMessage: null,
};

const initialFilterValues: MonthlyBalanceFilterValues = {
  accountCode: '',
  fiscalPeriod: '',
};

const useMonthlyBalanceFetch = () => {
  const [state, setState] = useState<MonthlyBalanceState>(initialState);
  const [filterValues, setFilterValues] = useState<MonthlyBalanceFilterValues>(initialFilterValues);
  const lastAutoFetchAccountCode = useRef<string | null>(null);

  const fetchMonthlyBalance = useCallback(async (params: MonthlyBalanceSearchParams) => {
    setState((prev) => ({ ...prev, isLoading: true, errorMessage: null }));
    try {
      const data: GetMonthlyBalanceResult = await getMonthlyBalance(params);
      setState({
        entries: data.entries ?? [],
        accountCode: data.accountCode,
        accountName: data.accountName,
        fiscalPeriod: data.fiscalPeriod,
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
        errorMessage: getMonthlyBalanceErrorMessage(error),
      }));
    }
  }, []);

  const buildSearchParams = useCallback((): MonthlyBalanceSearchParams | null => {
    if (!filterValues.accountCode) {
      return null;
    }
    const params: MonthlyBalanceSearchParams = {
      accountCode: filterValues.accountCode,
    };
    if (filterValues.fiscalPeriod) params.fiscalPeriod = Number(filterValues.fiscalPeriod);
    return params;
  }, [filterValues]);

  const handleSearch = useCallback(() => {
    const params = buildSearchParams();
    if (!params) {
      setState((prev) => ({ ...prev, errorMessage: '勘定科目を選択してください' }));
      return;
    }
    void fetchMonthlyBalance(params);
  }, [buildSearchParams, fetchMonthlyBalance]);

  useEffect(() => {
    if (!filterValues.accountCode) {
      setState(initialState);
      lastAutoFetchAccountCode.current = null;
      return;
    }
    if (lastAutoFetchAccountCode.current === filterValues.accountCode) {
      return;
    }
    lastAutoFetchAccountCode.current = filterValues.accountCode;
    const params: MonthlyBalanceSearchParams = {
      accountCode: filterValues.accountCode,
    };
    if (filterValues.fiscalPeriod) params.fiscalPeriod = Number(filterValues.fiscalPeriod);
    void fetchMonthlyBalance(params);
  }, [fetchMonthlyBalance, filterValues.accountCode, filterValues.fiscalPeriod]);

  return {
    state,
    filterValues,
    setFilterValues,
    handleSearch,
  };
};

interface MonthlyBalancePageContentProps {
  state: MonthlyBalanceState;
  filterValues: MonthlyBalanceFilterValues;
  onFilterChange: (values: MonthlyBalanceFilterValues) => void;
  onSearch: () => void;
}

const MonthlyBalancePageContent: React.FC<MonthlyBalancePageContentProps> = ({
  state,
  filterValues,
  onFilterChange,
  onSearch,
}) => {
  const hasAccount = Boolean(filterValues.accountCode);
  const shouldShowBalance = hasAccount && (state.entries.length > 0 || !state.isLoading);

  return (
    <div data-testid="monthly-balance-page">
      <h1>月次残高照会</h1>
      <MonthlyBalanceFilter values={filterValues} onChange={onFilterChange} onSearch={onSearch} />
      {state.isLoading && state.entries.length === 0 && (
        <Loading message="月次残高を読み込み中..." />
      )}
      {state.errorMessage ? (
        <ErrorMessage message={state.errorMessage} onRetry={onSearch} />
      ) : (
        shouldShowBalance && (
          <>
            <MonthlyBalanceSummary
              accountCode={state.accountCode}
              accountName={state.accountName}
              fiscalPeriod={state.fiscalPeriod}
              openingBalance={state.openingBalance}
              debitTotal={state.debitTotal}
              creditTotal={state.creditTotal}
              closingBalance={state.closingBalance}
            />
            <MonthlyBalanceChart entries={state.entries} />
            <MonthlyBalanceTable entries={state.entries} />
          </>
        )
      )}
    </div>
  );
};

const MonthlyBalancePage: React.FC = () => {
  const { isAuthenticated, isLoading, hasRole } = useAuth();
  const { state, filterValues, setFilterValues, handleSearch } = useMonthlyBalanceFetch();

  const breadcrumbs = useMemo(
    () => [{ label: 'ホーム' }, { label: '元帳・残高' }, { label: '月次残高' }],
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
      <MonthlyBalancePageContent
        state={state}
        filterValues={filterValues}
        onFilterChange={setFilterValues}
        onSearch={handleSearch}
      />
    </MainLayout>
  );
};

export default MonthlyBalancePage;
