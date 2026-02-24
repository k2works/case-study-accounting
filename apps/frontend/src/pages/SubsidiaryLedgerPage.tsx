import React, { useCallback, useEffect, useMemo, useRef, useState } from 'react';
import { useRequireAuth } from '../hooks/useRequireAuth';
import { getSubsidiaryLedger, getSubsidiaryLedgerErrorMessage } from '../api/getSubsidiaryLedger';
import type {
  SubsidiaryLedgerEntry,
  SubsidiaryLedgerSearchParams,
  GetSubsidiaryLedgerResult,
} from '../api/getSubsidiaryLedger';
import { MainLayout, Loading, ErrorMessage, Pagination } from '../views/common';
import {
  SubsidiaryLedgerFilter,
  SubsidiaryLedgerFilterValues,
} from '../views/ledger/SubsidiaryLedgerFilter';
import { SubsidiaryLedgerTable } from '../views/ledger/SubsidiaryLedgerTable';
import { SubsidiaryLedgerSummary } from '../views/ledger/SubsidiaryLedgerSummary';

interface SubsidiaryLedgerState {
  entries: SubsidiaryLedgerEntry[];
  accountCode: string;
  accountName: string;
  subAccountCode: string;
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

const initialState: SubsidiaryLedgerState = {
  entries: [],
  accountCode: '',
  accountName: '',
  subAccountCode: '',
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

const initialFilterValues: SubsidiaryLedgerFilterValues = {
  accountCode: '',
  subAccountCode: '',
  dateFrom: '',
  dateTo: '',
};

const useSubsidiaryLedgerFetch = () => {
  const [state, setState] = useState<SubsidiaryLedgerState>(initialState);
  const [filterValues, setFilterValues] =
    useState<SubsidiaryLedgerFilterValues>(initialFilterValues);
  const lastAutoFetchAccountCode = useRef<string | null>(null);

  const fetchSubsidiaryLedger = useCallback(async (params: SubsidiaryLedgerSearchParams) => {
    setState((prev) => ({ ...prev, isLoading: true, errorMessage: null }));
    try {
      const data: GetSubsidiaryLedgerResult = await getSubsidiaryLedger(params);
      setState({
        entries: data.content ?? [],
        accountCode: data.accountCode,
        accountName: data.accountName,
        subAccountCode: data.subAccountCode,
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
        errorMessage: getSubsidiaryLedgerErrorMessage(error),
      }));
    }
  }, []);

  const buildSearchParams = useCallback(
    (page: number, size: number): SubsidiaryLedgerSearchParams | null => {
      if (!filterValues.accountCode) {
        return null;
      }
      const params: SubsidiaryLedgerSearchParams = {
        accountCode: filterValues.accountCode,
        page,
        size,
      };
      if (filterValues.subAccountCode) params.subAccountCode = filterValues.subAccountCode;
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
    void fetchSubsidiaryLedger(params);
  }, [buildSearchParams, fetchSubsidiaryLedger, state.size]);

  const handlePageChange = useCallback(
    (newPage: number) => {
      const params = buildSearchParams(newPage - 1, state.size);
      if (params) {
        void fetchSubsidiaryLedger(params);
      }
    },
    [buildSearchParams, fetchSubsidiaryLedger, state.size]
  );

  const handleItemsPerPageChange = useCallback(
    (newSize: number) => {
      setState((prev) => ({ ...prev, size: newSize, page: 0 }));
      const params = buildSearchParams(0, newSize);
      if (params) {
        void fetchSubsidiaryLedger(params);
      }
    },
    [buildSearchParams, fetchSubsidiaryLedger]
  );

  useEffect(() => {
    if (!filterValues.accountCode) {
      setState((prev) => ({ ...initialState, size: prev.size }));
      lastAutoFetchAccountCode.current = null;
      return;
    }
    if (lastAutoFetchAccountCode.current === filterValues.accountCode) {
      return;
    }
    lastAutoFetchAccountCode.current = filterValues.accountCode;
    const params: SubsidiaryLedgerSearchParams = {
      accountCode: filterValues.accountCode,
      page: 0,
      size: state.size,
    };
    if (filterValues.subAccountCode) params.subAccountCode = filterValues.subAccountCode;
    if (filterValues.dateFrom) params.dateFrom = filterValues.dateFrom;
    if (filterValues.dateTo) params.dateTo = filterValues.dateTo;
    void fetchSubsidiaryLedger(params);
  }, [
    fetchSubsidiaryLedger,
    filterValues.accountCode,
    filterValues.subAccountCode,
    filterValues.dateFrom,
    filterValues.dateTo,
    state.size,
  ]);

  return {
    state,
    filterValues,
    setFilterValues,
    fetchSubsidiaryLedger,
    handleSearch,
    handlePageChange,
    handleItemsPerPageChange,
  };
};

interface SubsidiaryLedgerPageContentProps {
  state: SubsidiaryLedgerState;
  filterValues: SubsidiaryLedgerFilterValues;
  onFilterChange: (values: SubsidiaryLedgerFilterValues) => void;
  onSearch: () => void;
  onPageChange: (page: number) => void;
  onItemsPerPageChange: (size: number) => void;
}

const SubsidiaryLedgerPageContent: React.FC<SubsidiaryLedgerPageContentProps> = ({
  state,
  filterValues,
  onFilterChange,
  onSearch,
  onPageChange,
  onItemsPerPageChange,
}) => {
  const hasAccount = Boolean(filterValues.accountCode);
  const shouldShowLedger = hasAccount && (state.entries.length > 0 || !state.isLoading);

  return (
    <div data-testid="subsidiary-ledger-page">
      <h1>補助元帳照会</h1>
      <SubsidiaryLedgerFilter values={filterValues} onChange={onFilterChange} onSearch={onSearch} />
      {state.isLoading && state.entries.length === 0 && <Loading message="元帳を読み込み中..." />}
      {state.errorMessage ? (
        <ErrorMessage message={state.errorMessage} onRetry={onSearch} />
      ) : (
        shouldShowLedger && (
          <>
            <SubsidiaryLedgerSummary
              accountCode={state.accountCode}
              accountName={state.accountName}
              subAccountCode={state.subAccountCode}
              openingBalance={state.openingBalance}
              debitTotal={state.debitTotal}
              creditTotal={state.creditTotal}
              closingBalance={state.closingBalance}
            />
            <SubsidiaryLedgerTable entries={state.entries} />
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

const SubsidiaryLedgerPage: React.FC = () => {
  const authGuard = useRequireAuth(['ADMIN', 'MANAGER', 'USER']);
  const {
    state,
    filterValues,
    setFilterValues,
    handleSearch,
    handlePageChange,
    handleItemsPerPageChange,
  } = useSubsidiaryLedgerFetch();

  const breadcrumbs = useMemo(
    () => [{ label: 'ホーム' }, { label: '元帳・残高' }, { label: '補助元帳' }],
    []
  );

  if (authGuard) return authGuard;

  return (
    <MainLayout breadcrumbs={breadcrumbs}>
      <SubsidiaryLedgerPageContent
        state={state}
        filterValues={filterValues}
        onFilterChange={setFilterValues}
        onSearch={handleSearch}
        onPageChange={handlePageChange}
        onItemsPerPageChange={handleItemsPerPageChange}
      />
    </MainLayout>
  );
};

export default SubsidiaryLedgerPage;
