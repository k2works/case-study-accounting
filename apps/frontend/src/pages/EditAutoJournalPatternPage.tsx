import React, { useCallback, useEffect, useMemo, useState } from 'react';
import { Navigate, useNavigate, useParams } from 'react-router-dom';
import { useAuth } from '../hooks/useAuth';
import {
  getAutoJournalPattern,
  getAutoJournalPatternsErrorMessage,
  type AutoJournalPattern,
} from '../api/getAutoJournalPatterns';
import { MainLayout, Loading, ErrorMessage } from '../views/common';
import { EditAutoJournalPatternForm } from '../views/auto-journal-pattern/EditAutoJournalPatternForm';

interface AutoJournalPatternState {
  pattern: AutoJournalPattern | null;
  isLoading: boolean;
  errorMessage: string | null;
}

const initialState: AutoJournalPatternState = {
  pattern: null,
  isLoading: false,
  errorMessage: null,
};

const useAutoJournalPatternFetch = (patternId: number, isInvalidId: boolean) => {
  const [state, setState] = useState<AutoJournalPatternState>(initialState);

  const fetchPattern = useCallback(async () => {
    if (isInvalidId) {
      setState({
        pattern: null,
        isLoading: false,
        errorMessage: '自動仕訳パターンが見つかりませんでした',
      });
      return;
    }

    setState((prev) => ({ ...prev, isLoading: true, errorMessage: null }));
    try {
      const data = await getAutoJournalPattern(patternId);
      setState({ pattern: data, isLoading: false, errorMessage: null });
    } catch (error) {
      setState((prev) => ({
        ...prev,
        isLoading: false,
        errorMessage: getAutoJournalPatternsErrorMessage(error),
      }));
    }
  }, [isInvalidId, patternId]);

  useEffect(() => {
    void fetchPattern();
  }, [fetchPattern]);

  return { state, fetchPattern };
};

const EditAutoJournalPatternContent: React.FC<{
  state: AutoJournalPatternState;
  fetchPattern: () => Promise<void>;
  navigate: ReturnType<typeof useNavigate>;
}> = ({ state, fetchPattern, navigate }) => {
  const breadcrumbs = useMemo(
    () => [
      { label: 'ホーム' },
      { label: 'マスタ管理' },
      { label: '自動仕訳パターン一覧', path: '/master/auto-journal-patterns' },
      { label: '自動仕訳パターン編集' },
    ],
    []
  );

  return (
    <MainLayout breadcrumbs={breadcrumbs}>
      <div data-testid="edit-auto-journal-pattern-page">
        <h1>自動仕訳パターン編集</h1>
        {state.isLoading && <Loading message="自動仕訳パターンを読み込み中..." />}
        {state.errorMessage && <ErrorMessage message={state.errorMessage} onRetry={fetchPattern} />}
        {state.pattern && (
          <EditAutoJournalPatternForm
            pattern={state.pattern}
            onSuccess={(message) =>
              navigate('/master/auto-journal-patterns', {
                replace: true,
                state: { successMessage: message },
              })
            }
          />
        )}
      </div>
    </MainLayout>
  );
};

const EditAutoJournalPatternPage: React.FC = () => {
  const { isAuthenticated, isLoading, hasRole } = useAuth();
  const { id } = useParams();
  const navigate = useNavigate();

  const patternId = Number(id);
  const isInvalidId = Number.isNaN(patternId);
  const { state, fetchPattern } = useAutoJournalPatternFetch(patternId, isInvalidId);

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
    <EditAutoJournalPatternContent state={state} fetchPattern={fetchPattern} navigate={navigate} />
  );
};

export default EditAutoJournalPatternPage;
