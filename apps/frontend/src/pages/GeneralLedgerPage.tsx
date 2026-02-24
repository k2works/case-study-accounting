import React, { useCallback, useEffect, useMemo, useRef, useState } from 'react';
import { Navigate } from 'react-router-dom';
import { useAuth } from '../hooks/useAuth';
import {
  getGeneralLedger,
  getGeneralLedgerErrorMessage,
  exportGeneralLedger,
} from '../api/getGeneralLedger';
import type {
  GeneralLedgerEntry,
  GeneralLedgerSearchParams,
  GetGeneralLedgerResult,
  GeneralLedgerExportFormat,
} from '../api/getGeneralLedger';
import { MainLayout, Loading, ErrorMessage, Pagination, Button } from '../views/common';
import { GeneralLedgerFilter } from '../views/ledger/GeneralLedgerFilter';
import type { GeneralLedgerFilterValues } from '../views/ledger/GeneralLedgerFilter';
import { GeneralLedgerTable } from '../views/ledger/GeneralLedgerTable';
import { GeneralLedgerSummary } from '../views/ledger/GeneralLedgerSummary';

interface GeneralLedgerState {
  entries: GeneralLedgerEntry[];
  accountId: number | null;
  accountCode: string;
  accountName: string;
  openingBalance: number;
  debitTotal: number;
  creditTotal: number;
  closingBalance: number;
  page: number;
  size: number;
  totalElements: number;
  totalPages: number;
  isLoading: boolean;
  errorMessage: string | null;
}

const initialState: GeneralLedgerState = {
  entries: [],
  accountId: null,
  accountCode: '',
  accountName: '',
  openingBalance: 0,
  debitTotal: 0,
  creditTotal: 0,
  closingBalance: 0,
  page: 0,
  size: 20,
  totalElements: 0,
  totalPages: 0,
  isLoading: false,
  errorMessage: null,
};

const initialFilterValues: GeneralLedgerFilterValues = {
  accountId: '',
  dateFrom: '',
  dateTo: '',
};

const useGeneralLedgerFetch = () => {
  const [state, setState] = useState<GeneralLedgerState>(initialState);
  const [filterValues, setFilterValues] = useState<GeneralLedgerFilterValues>(initialFilterValues);
  const lastAutoFetchAccountId = useRef<string | null>(null);

  const fetchGeneralLedger = useCallback(async (params: GeneralLedgerSearchParams) => {
    setState((prev) => ({ ...prev, isLoading: true, errorMessage: null }));
    try {
      const data: GetGeneralLedgerResult = await getGeneralLedger(params);
      setState({
        entries: data.content ?? [],
        accountId: data.accountId,
        accountCode: data.accountCode,
        accountName: data.accountName,
        openingBalance: data.openingBalance,
        debitTotal: data.debitTotal,
        creditTotal: data.creditTotal,
        closingBalance: data.closingBalance,
        page: data.page,
        size: data.size,
        totalElements: data.totalElements,
        totalPages: data.totalPages,
        isLoading: false,
        errorMessage: null,
      });
    } catch (error) {
      setState((prev) => ({
        ...prev,
        isLoading: false,
        errorMessage: getGeneralLedgerErrorMessage(error),
      }));
    }
  }, []);

  const buildSearchParams = useCallback(
    (page: number, size: number): GeneralLedgerSearchParams | null => {
      if (!filterValues.accountId) {
        return null;
      }
      const params: GeneralLedgerSearchParams = {
        accountId: Number(filterValues.accountId),
        page,
        size,
      };
      if (filterValues.dateFrom) params.dateFrom = filterValues.dateFrom;
      if (filterValues.dateTo) params.dateTo = filterValues.dateTo;
      return params;
    },
    [filterValues]
  );

  const handleSearch = useCallback(() => {
    const params = buildSearchParams(0, state.size);
    if (!params) {
      setState((prev) => ({ ...prev, errorMessage: '勘定科目を選択してください' }));
      return;
    }
    void fetchGeneralLedger(params);
  }, [buildSearchParams, fetchGeneralLedger, state.size]);

  const handlePageChange = useCallback(
    (newPage: number) => {
      const params = buildSearchParams(newPage - 1, state.size);
      if (params) {
        void fetchGeneralLedger(params);
      }
    },
    [buildSearchParams, fetchGeneralLedger, state.size]
  );

  const handleItemsPerPageChange = useCallback(
    (newSize: number) => {
      setState((prev) => ({ ...prev, size: newSize, page: 0 }));
      const params = buildSearchParams(0, newSize);
      if (params) {
        void fetchGeneralLedger(params);
      }
    },
    [buildSearchParams, fetchGeneralLedger]
  );

  useEffect(() => {
    if (!filterValues.accountId) {
      setState((prev) => ({ ...initialState, size: prev.size }));
      lastAutoFetchAccountId.current = null;
      return;
    }
    if (lastAutoFetchAccountId.current === filterValues.accountId) {
      return;
    }
    lastAutoFetchAccountId.current = filterValues.accountId;
    const params: GeneralLedgerSearchParams = {
      accountId: Number(filterValues.accountId),
      page: 0,
      size: state.size,
    };
    if (filterValues.dateFrom) params.dateFrom = filterValues.dateFrom;
    if (filterValues.dateTo) params.dateTo = filterValues.dateTo;
    void fetchGeneralLedger(params);
  }, [
    fetchGeneralLedger,
    filterValues.accountId,
    filterValues.dateFrom,
    filterValues.dateTo,
    state.size,
  ]);

  return {
    state,
    filterValues,
    setFilterValues,
    fetchGeneralLedger,
    handleSearch,
    handlePageChange,
    handleItemsPerPageChange,
  };
};

interface GeneralLedgerPageContentProps {
  state: GeneralLedgerState;
  filterValues: GeneralLedgerFilterValues;
  onFilterChange: (values: GeneralLedgerFilterValues) => void;
  onSearch: () => void;
  onPageChange: (page: number) => void;
  onItemsPerPageChange: (size: number) => void;
  onExport: (format: GeneralLedgerExportFormat) => void;
}

const GeneralLedgerPageContent: React.FC<GeneralLedgerPageContentProps> = ({
  state,
  filterValues,
  onFilterChange,
  onSearch,
  onPageChange,
  onItemsPerPageChange,
  onExport,
}) => {
  const hasAccount = Boolean(filterValues.accountId);
  const shouldShowLedger = hasAccount && (state.entries.length > 0 || !state.isLoading);

  return (
    <div data-testid="general-ledger-page">
      <h1>総勘定元帳照会</h1>
      <GeneralLedgerFilter values={filterValues} onChange={onFilterChange} onSearch={onSearch} />
      {state.isLoading && state.entries.length === 0 && <Loading message="元帳を読み込み中..." />}
      {state.errorMessage ? (
        <ErrorMessage message={state.errorMessage} onRetry={onSearch} />
      ) : (
        shouldShowLedger && (
          <>
            <GeneralLedgerSummary
              accountCode={state.accountCode}
              accountName={state.accountName}
              openingBalance={state.openingBalance}
              debitTotal={state.debitTotal}
              creditTotal={state.creditTotal}
              closingBalance={state.closingBalance}
            />
            <div style={{ display: 'flex', gap: '8px', marginTop: '12px' }}>
              <Button variant="secondary" onClick={() => onExport('csv')}>
                CSV
              </Button>
              <Button variant="secondary" onClick={() => onExport('excel')}>
                Excel
              </Button>
            </div>
            <GeneralLedgerTable entries={state.entries} />
            <Pagination
              currentPage={state.page + 1}
              totalPages={state.totalPages}
              totalItems={state.totalElements}
              itemsPerPage={state.size}
              onPageChange={onPageChange}
              onItemsPerPageChange={onItemsPerPageChange}
            />
          </>
        )
      )}
    </div>
  );
};

const GeneralLedgerPage: React.FC = () => {
  const { isAuthenticated, isLoading, hasRole } = useAuth();
  const {
    state,
    filterValues,
    setFilterValues,
    handleSearch,
    handlePageChange,
    handleItemsPerPageChange,
  } = useGeneralLedgerFetch();

  const handleExport = useCallback(
    (format: GeneralLedgerExportFormat) => {
      if (!filterValues.accountId) return;
      void exportGeneralLedger(
        format,
        Number(filterValues.accountId),
        filterValues.dateFrom || undefined,
        filterValues.dateTo || undefined
      );
    },
    [filterValues]
  );

  const breadcrumbs = useMemo(
    () => [{ label: 'ホーム' }, { label: '元帳・残高' }, { label: '総勘定元帳' }],
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
      <GeneralLedgerPageContent
        state={state}
        filterValues={filterValues}
        onFilterChange={setFilterValues}
        onSearch={handleSearch}
        onPageChange={handlePageChange}
        onItemsPerPageChange={handleItemsPerPageChange}
        onExport={handleExport}
      />
    </MainLayout>
  );
};

export default GeneralLedgerPage;
