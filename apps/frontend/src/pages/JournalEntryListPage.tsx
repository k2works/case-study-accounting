import React, { useCallback, useEffect, useMemo, useState } from 'react';
import { Navigate, useLocation, useNavigate } from 'react-router-dom';
import { useAuth } from '../hooks/useAuth';
import {
  searchJournalEntries,
  searchJournalEntriesErrorMessage,
} from '../api/searchJournalEntries';
import type { SearchJournalEntriesParams } from '../api/searchJournalEntries';
import type { JournalEntrySummary, GetJournalEntriesResult } from '../api/getJournalEntries';
import { MainLayout, Loading, SuccessNotification, ErrorMessage, Button } from '../views/common';
import { JournalEntryList } from '../views/journal/JournalEntryList';
import type { JournalEntryFilterValues } from '../views/journal/JournalEntryFilter';

interface JournalEntryListLocationState {
  successMessage?: string;
}

interface JournalEntryListState {
  entries: JournalEntrySummary[];
  isLoading: boolean;
  errorMessage: string | null;
  page: number;
  size: number;
  totalElements: number;
  totalPages: number;
}

const initialState: JournalEntryListState = {
  entries: [],
  isLoading: false,
  errorMessage: null,
  page: 0,
  size: 20,
  totalElements: 0,
  totalPages: 0,
};

const initialFilterValues: JournalEntryFilterValues = {
  status: '',
  dateFrom: '',
  dateTo: '',
  accountId: '',
  amountFrom: '',
  amountTo: '',
  description: '',
};

const useJournalEntryListFetch = () => {
  const [state, setState] = useState<JournalEntryListState>(initialState);
  const [filterValues, setFilterValues] = useState<JournalEntryFilterValues>(initialFilterValues);

  const fetchEntries = useCallback(async (params?: SearchJournalEntriesParams) => {
    setState((prev) => ({ ...prev, isLoading: true, errorMessage: null }));
    try {
      const data: GetJournalEntriesResult = await searchJournalEntries(params);
      setState({
        entries: data.content,
        isLoading: false,
        errorMessage: null,
        page: data.page,
        size: data.size,
        totalElements: data.totalElements,
        totalPages: data.totalPages,
      });
    } catch (error) {
      setState((prev) => ({
        ...prev,
        isLoading: false,
        errorMessage: searchJournalEntriesErrorMessage(error),
      }));
    }
  }, []);

  const buildSearchParams = useCallback(
    (page: number, size: number): SearchJournalEntriesParams => {
      const params: SearchJournalEntriesParams = { page, size };
      if (filterValues.status) params.status = [filterValues.status];

      const stringFields = ['dateFrom', 'dateTo', 'description'] as const;
      stringFields.forEach((key) => {
        if (filterValues[key]) (params as Record<string, unknown>)[key] = filterValues[key];
      });

      const numberFields = ['accountId', 'amountFrom', 'amountTo'] as const;
      numberFields.forEach((key) => {
        if (filterValues[key]) (params as Record<string, unknown>)[key] = Number(filterValues[key]);
      });

      return params;
    },
    [filterValues]
  );

  const handleSearch = useCallback(() => {
    void fetchEntries(buildSearchParams(0, state.size));
  }, [fetchEntries, buildSearchParams, state.size]);

  const handleReset = useCallback(() => {
    setFilterValues(initialFilterValues);
    void fetchEntries({ page: 0, size: state.size });
  }, [fetchEntries, state.size]);

  const handlePageChange = useCallback(
    (newPage: number) => {
      void fetchEntries(buildSearchParams(newPage - 1, state.size));
    },
    [fetchEntries, buildSearchParams, state.size]
  );

  const handleItemsPerPageChange = useCallback(
    (newSize: number) => {
      void fetchEntries(buildSearchParams(0, newSize));
    },
    [fetchEntries, buildSearchParams]
  );

  useEffect(() => {
    void fetchEntries({ page: 0, size: initialState.size });
  }, [fetchEntries]);

  return {
    state,
    filterValues,
    setFilterValues,
    fetchEntries,
    handleSearch,
    handleReset,
    handlePageChange,
    handleItemsPerPageChange,
  };
};

interface JournalEntryListBodyProps {
  state: JournalEntryListState;
  fetchEntries: (params?: SearchJournalEntriesParams) => Promise<void>;
  filterValues: JournalEntryFilterValues;
  onFilterChange: (values: JournalEntryFilterValues) => void;
  onSearch: () => void;
  onReset: () => void;
  onPageChange: (page: number) => void;
  onItemsPerPageChange: (size: number) => void;
  shouldShowList: boolean;
}

const JournalEntryListBody: React.FC<JournalEntryListBodyProps> = ({
  state,
  fetchEntries,
  filterValues,
  onFilterChange,
  onSearch,
  onReset,
  onPageChange,
  onItemsPerPageChange,
  shouldShowList,
}) => {
  if (state.errorMessage) {
    return <ErrorMessage message={state.errorMessage} onRetry={() => void fetchEntries()} />;
  }
  if (!shouldShowList) return null;
  return (
    <JournalEntryList
      entries={state.entries}
      filterValues={filterValues}
      onFilterChange={onFilterChange}
      onSearch={onSearch}
      onReset={onReset}
      onDelete={onSearch}
      currentPage={state.page + 1}
      totalPages={state.totalPages}
      totalItems={state.totalElements}
      itemsPerPage={state.size}
      onPageChange={onPageChange}
      onItemsPerPageChange={onItemsPerPageChange}
    />
  );
};

interface JournalEntryListContentProps {
  state: JournalEntryListState;
  fetchEntries: (params?: SearchJournalEntriesParams) => Promise<void>;
  filterValues: JournalEntryFilterValues;
  onFilterChange: (values: JournalEntryFilterValues) => void;
  onSearch: () => void;
  onReset: () => void;
  onPageChange: (page: number) => void;
  onItemsPerPageChange: (size: number) => void;
  successMessage: string | null;
  onDismissSuccess: () => void;
  onCreateNew: () => void;
  canCreate: boolean;
}

const JournalEntryListContent: React.FC<JournalEntryListContentProps> = ({
  state,
  fetchEntries,
  filterValues,
  onFilterChange,
  onSearch,
  onReset,
  onPageChange,
  onItemsPerPageChange,
  successMessage,
  onDismissSuccess,
  onCreateNew,
  canCreate,
}) => {
  const shouldShowList = !state.isLoading || state.entries.length > 0;
  const showInitialLoading = state.isLoading && state.entries.length === 0;
  const hasSuccess = Boolean(successMessage);

  return (
    <div data-testid="journal-entry-list-page">
      <div
        style={{
          display: 'flex',
          justifyContent: 'space-between',
          alignItems: 'center',
          marginBottom: '1rem',
        }}
      >
        <h1>仕訳一覧</h1>
        {canCreate && (
          <Button variant="primary" onClick={onCreateNew}>
            新規作成
          </Button>
        )}
      </div>
      {hasSuccess && (
        <div style={{ marginBottom: '16px' }}>
          <SuccessNotification message={successMessage!} onDismiss={onDismissSuccess} />
        </div>
      )}
      {showInitialLoading && <Loading message="仕訳を読み込み中..." />}
      <JournalEntryListBody
        state={state}
        fetchEntries={fetchEntries}
        filterValues={filterValues}
        onFilterChange={onFilterChange}
        onSearch={onSearch}
        onReset={onReset}
        onPageChange={onPageChange}
        onItemsPerPageChange={onItemsPerPageChange}
        shouldShowList={shouldShowList}
      />
    </div>
  );
};

/**
 * 仕訳一覧ページ
 */
const JournalEntryListPage: React.FC = () => {
  const { isAuthenticated, isLoading, hasRole } = useAuth();
  const location = useLocation();
  const navigate = useNavigate();
  const {
    state,
    filterValues,
    setFilterValues,
    fetchEntries,
    handleSearch,
    handleReset,
    handlePageChange,
    handleItemsPerPageChange,
  } = useJournalEntryListFetch();
  const [successMessage, setSuccessMessage] = useState<string | null>(() => {
    const state = location.state as JournalEntryListLocationState | null;
    return state?.successMessage ?? null;
  });

  const breadcrumbs = useMemo(
    () => [{ label: 'ホーム' }, { label: '仕訳管理' }, { label: '仕訳一覧' }],
    []
  );

  if (isLoading) {
    return <Loading message="認証情報を確認中..." fullScreen />;
  }

  if (!isAuthenticated) {
    return <Navigate to="/login" replace />;
  }

  const canAccessPage = hasRole('ADMIN') || hasRole('MANAGER') || hasRole('USER');
  if (!canAccessPage) {
    return <Navigate to="/" replace />;
  }

  return (
    <MainLayout breadcrumbs={breadcrumbs}>
      <JournalEntryListContent
        state={state}
        fetchEntries={fetchEntries}
        filterValues={filterValues}
        onFilterChange={setFilterValues}
        onSearch={handleSearch}
        onReset={handleReset}
        onPageChange={handlePageChange}
        onItemsPerPageChange={handleItemsPerPageChange}
        successMessage={successMessage}
        onDismissSuccess={() => setSuccessMessage(null)}
        onCreateNew={() => navigate('/journal/entries/new')}
        canCreate={hasRole('USER')}
      />
    </MainLayout>
  );
};

export default JournalEntryListPage;
