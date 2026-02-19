import React, { useCallback, useEffect, useMemo, useState } from 'react';
import { Navigate, useLocation, useNavigate } from 'react-router-dom';
import { useAuth } from '../hooks/useAuth';
import {
  getAutoJournalPatterns,
  getAutoJournalPatternsErrorMessage,
  type AutoJournalPattern,
} from '../api/getAutoJournalPatterns';
import { MainLayout, Loading, ErrorMessage, SuccessNotification, Button } from '../views/common';
import { AutoJournalPatternList } from '../views/auto-journal-pattern/AutoJournalPatternList';

interface AutoJournalPatternListLocationState {
  successMessage?: string;
}

interface AutoJournalPatternListState {
  patterns: AutoJournalPattern[];
  isLoading: boolean;
  errorMessage: string | null;
}

const initialState: AutoJournalPatternListState = {
  patterns: [],
  isLoading: false,
  errorMessage: null,
};

const useAutoJournalPatternListFetch = () => {
  const [state, setState] = useState<AutoJournalPatternListState>(initialState);

  const fetchPatterns = useCallback(async () => {
    setState((prev) => ({ ...prev, isLoading: true, errorMessage: null }));
    try {
      const data = await getAutoJournalPatterns();
      setState({ patterns: data, isLoading: false, errorMessage: null });
    } catch (error) {
      setState((prev) => ({
        ...prev,
        isLoading: false,
        errorMessage: getAutoJournalPatternsErrorMessage(error),
      }));
    }
  }, []);

  useEffect(() => {
    void fetchPatterns();
  }, [fetchPatterns]);

  return { state, fetchPatterns };
};

interface AutoJournalPatternListContentProps {
  state: AutoJournalPatternListState;
  successMessage: string | null;
  isManager: boolean;
  onDismissSuccess: () => void;
  onRetry: () => Promise<void>;
  onCreate: () => void;
  onEdit: (pattern: AutoJournalPattern) => void;
}

const AutoJournalPatternListContent: React.FC<AutoJournalPatternListContentProps> = ({
  state,
  successMessage,
  isManager,
  onDismissSuccess,
  onRetry,
  onCreate,
  onEdit,
}) => {
  return (
    <div data-testid="auto-journal-pattern-list-page">
      <h1>自動仕訳パターン一覧</h1>
      {isManager && (
        <div style={{ marginBottom: '16px' }}>
          <Button onClick={onCreate}>新規登録</Button>
        </div>
      )}
      {successMessage && (
        <div style={{ marginBottom: '16px' }}>
          <SuccessNotification message={successMessage} onDismiss={onDismissSuccess} />
        </div>
      )}
      {state.isLoading && state.patterns.length === 0 && (
        <Loading message="自動仕訳パターンを読み込み中..." />
      )}
      {state.errorMessage ? (
        <ErrorMessage message={state.errorMessage} onRetry={onRetry} />
      ) : (
        <AutoJournalPatternList
          patterns={state.patterns}
          onEdit={onEdit}
          onDelete={() => void onRetry()}
        />
      )}
    </div>
  );
};

const AutoJournalPatternListPage: React.FC = () => {
  const { isAuthenticated, isLoading, hasRole } = useAuth();
  const location = useLocation();
  const navigate = useNavigate();
  const { state, fetchPatterns } = useAutoJournalPatternListFetch();
  const [successMessage, setSuccessMessage] = useState<string | null>(() => {
    const locationState = location.state as AutoJournalPatternListLocationState | null;
    return locationState?.successMessage ?? null;
  });

  const breadcrumbs = useMemo(
    () => [{ label: 'ホーム' }, { label: 'マスタ管理' }, { label: '自動仕訳パターン一覧' }],
    []
  );

  if (isLoading) {
    return <Loading message="認証情報を確認中..." fullScreen />;
  }

  if (!isAuthenticated) {
    return <Navigate to="/login" replace />;
  }

  if (!hasRole('ADMIN') && !hasRole('MANAGER')) {
    return <Navigate to="/" replace />;
  }

  return (
    <MainLayout breadcrumbs={breadcrumbs}>
      <AutoJournalPatternListContent
        state={state}
        successMessage={successMessage}
        isManager={hasRole('MANAGER')}
        onDismissSuccess={() => setSuccessMessage(null)}
        onRetry={fetchPatterns}
        onCreate={() => navigate('/master/auto-journal-patterns/new')}
        onEdit={(pattern) => navigate(`/master/auto-journal-patterns/${pattern.patternId}/edit`)}
      />
    </MainLayout>
  );
};

export default AutoJournalPatternListPage;
