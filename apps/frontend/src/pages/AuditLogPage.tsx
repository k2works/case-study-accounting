import React, { useCallback, useMemo, useState } from 'react';
import { useRequireAuth } from '../hooks/useRequireAuth';
import { getAuditLogs, getAuditLogsErrorMessage } from '../api/getAuditLogs';
import type { AuditLogEntry, AuditLogSearchParams } from '../api/getAuditLogs';
import { MainLayout, Loading, ErrorMessage } from '../views/common';
import { AuditLogFilter } from '../views/system/AuditLogFilter';
import type { AuditLogFilterValues } from '../views/system/AuditLogFilter';
import { AuditLogTable } from '../views/system/AuditLogTable';

interface AuditLogState {
  auditLogs: AuditLogEntry[];
  totalCount: number;
  totalPages: number;
  currentPage: number;
  isLoading: boolean;
  errorMessage: string | null;
  hasSearched: boolean;
}

const initialState: AuditLogState = {
  auditLogs: [],
  totalCount: 0,
  totalPages: 0,
  currentPage: 0,
  isLoading: false,
  errorMessage: null,
  hasSearched: false,
};

const initialFilterValues: AuditLogFilterValues = {
  userId: '',
  actionType: '',
  dateFrom: '',
  dateTo: '',
};

const buildSearchParams = (
  filterValues: AuditLogFilterValues,
  page?: number
): AuditLogSearchParams => {
  const params: AuditLogSearchParams = {};
  if (filterValues.userId) params.userId = filterValues.userId;
  if (filterValues.actionType) params.actionType = filterValues.actionType;
  if (filterValues.dateFrom) params.dateFrom = filterValues.dateFrom;
  if (filterValues.dateTo) params.dateTo = filterValues.dateTo;
  if (page !== undefined) params.page = page;
  return params;
};

const useAuditLogFetch = () => {
  const [state, setState] = useState<AuditLogState>(initialState);
  const [filterValues, setFilterValues] = useState<AuditLogFilterValues>(initialFilterValues);

  const fetchData = useCallback(async (params: AuditLogSearchParams) => {
    setState((prev) => ({ ...prev, isLoading: true, errorMessage: null }));
    try {
      const data = await getAuditLogs(params);
      setState({
        auditLogs: data.auditLogs,
        totalCount: data.totalCount,
        totalPages: data.totalPages,
        currentPage: data.currentPage,
        isLoading: false,
        errorMessage: null,
        hasSearched: true,
      });
    } catch (error) {
      setState((prev) => ({
        ...prev,
        isLoading: false,
        errorMessage: getAuditLogsErrorMessage(error),
      }));
    }
  }, []);

  const handleSearch = useCallback(
    (page?: number) => {
      void fetchData(buildSearchParams(filterValues, page));
    },
    [fetchData, filterValues]
  );

  const handlePageChange = useCallback(
    (page: number) => {
      handleSearch(page);
    },
    [handleSearch]
  );

  return {
    state,
    filterValues,
    setFilterValues,
    handleSearch: () => handleSearch(),
    handlePageChange,
  };
};

const AuditLogPage: React.FC = () => {
  const authGuard = useRequireAuth(['ADMIN']);
  const { state, filterValues, setFilterValues, handleSearch, handlePageChange } =
    useAuditLogFetch();

  const breadcrumbs = useMemo(
    () => [{ label: 'ホーム' }, { label: 'システム管理' }, { label: '監査ログ' }],
    []
  );

  if (authGuard) return authGuard;

  return (
    <MainLayout breadcrumbs={breadcrumbs}>
      <div data-testid="audit-log-page">
        <h1>監査ログ</h1>
        <AuditLogFilter values={filterValues} onChange={setFilterValues} onSearch={handleSearch} />
        {state.isLoading && <Loading message="監査ログを読み込み中..." />}
        {state.errorMessage ? (
          <ErrorMessage message={state.errorMessage} onRetry={handleSearch} />
        ) : (
          state.hasSearched && (
            <>
              <p style={{ margin: '16px 0 8px' }}>
                全 {state.totalCount} 件（{state.currentPage + 1} / {state.totalPages} ページ）
              </p>
              <AuditLogTable auditLogs={state.auditLogs} />
              {state.totalPages > 1 && (
                <div
                  style={{
                    display: 'flex',
                    gap: '8px',
                    marginTop: '16px',
                    justifyContent: 'center',
                  }}
                >
                  <button
                    onClick={() => handlePageChange(state.currentPage - 1)}
                    disabled={state.currentPage <= 0}
                  >
                    前へ
                  </button>
                  <span>
                    {state.currentPage + 1} / {state.totalPages}
                  </span>
                  <button
                    onClick={() => handlePageChange(state.currentPage + 1)}
                    disabled={state.currentPage >= state.totalPages - 1}
                  >
                    次へ
                  </button>
                </div>
              )}
            </>
          )
        )}
      </div>
    </MainLayout>
  );
};

export default AuditLogPage;
