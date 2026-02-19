import React, { useCallback, useEffect, useState } from 'react';
import { useNavigate, useParams } from 'react-router-dom';
import {
  getAutoJournalPattern,
  getAutoJournalPatternsErrorMessage,
  type AutoJournalPattern,
} from '../api/getAutoJournalPatterns';
import { Loading, ErrorMessage } from '../views/common';
import { EditAutoJournalPatternForm } from '../views/auto-journal-pattern/EditAutoJournalPatternForm';
import { ManagerPage } from './ManagerPage';

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

const breadcrumbs = [
  { label: 'ホーム' },
  { label: 'マスタ管理' },
  { label: '自動仕訳パターン一覧', path: '/master/auto-journal-patterns' },
  { label: '自動仕訳パターン編集' },
];

const EditAutoJournalPatternPage: React.FC = () => {
  const { id } = useParams();
  const navigate = useNavigate();

  const patternId = Number(id);
  const isInvalidId = Number.isNaN(patternId);
  const { state, fetchPattern } = useAutoJournalPatternFetch(patternId, isInvalidId);

  return (
    <ManagerPage breadcrumbs={breadcrumbs}>
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
    </ManagerPage>
  );
};

export default EditAutoJournalPatternPage;
