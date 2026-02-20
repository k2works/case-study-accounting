import React, { useCallback, useEffect, useState } from 'react';
import { useLocation, useNavigate } from 'react-router-dom';
import { useAuth } from '../hooks/useAuth';
import {
  getAutoJournalPatterns,
  getAutoJournalPatternsErrorMessage,
  type AutoJournalPattern,
} from '../api/getAutoJournalPatterns';
import { Loading, ErrorMessage, SuccessNotification, Button } from '../views/common';
import { AutoJournalPatternList } from '../views/auto-journal-pattern/AutoJournalPatternList';
import { ManagerPage } from './ManagerPage';

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

const breadcrumbs = [
  { label: 'ホーム' },
  { label: 'マスタ管理' },
  { label: '自動仕訳パターン一覧' },
];

const AutoJournalPatternListPage: React.FC = () => {
  const { hasRole } = useAuth();
  const location = useLocation();
  const navigate = useNavigate();
  const { state, fetchPatterns } = useAutoJournalPatternListFetch();
  const [successMessage, setSuccessMessage] = useState<string | null>(() => {
    const locationState = location.state as AutoJournalPatternListLocationState | null;
    return locationState?.successMessage ?? null;
  });

  return (
    <ManagerPage breadcrumbs={breadcrumbs}>
      <div data-testid="auto-journal-pattern-list-page">
        <h1>自動仕訳パターン一覧</h1>
        {hasRole('MANAGER') && (
          <div style={{ marginBottom: '16px' }}>
            <Button onClick={() => navigate('/master/auto-journal-patterns/new')}>新規登録</Button>
          </div>
        )}
        {successMessage && (
          <div style={{ marginBottom: '16px' }}>
            <SuccessNotification
              message={successMessage}
              onDismiss={() => setSuccessMessage(null)}
            />
          </div>
        )}
        {state.isLoading && state.patterns.length === 0 && (
          <Loading message="自動仕訳パターンを読み込み中..." />
        )}
        {state.errorMessage ? (
          <ErrorMessage message={state.errorMessage} onRetry={fetchPatterns} />
        ) : (
          <AutoJournalPatternList
            patterns={state.patterns}
            onEdit={(pattern) =>
              navigate(`/master/auto-journal-patterns/${pattern.patternId}/edit`)
            }
            onDelete={() => void fetchPatterns()}
          />
        )}
      </div>
    </ManagerPage>
  );
};

export default AutoJournalPatternListPage;
