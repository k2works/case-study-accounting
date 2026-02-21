import React, { useCallback, useEffect, useState } from 'react';
import { Navigate, useNavigate } from 'react-router-dom';
import { useAuth } from '../hooks/useAuth';
import { getAccounts, getAccountsErrorMessage } from '../api/getAccounts';
import type { Account } from '../api/getAccounts';
import type { Role } from '../types/auth';
import {
  createJournalEntry,
  createJournalEntryErrorMessage,
  type CreateJournalEntryRequest,
} from '../api/createJournalEntry';
import { MainLayout, Loading, SuccessNotification, ErrorMessage, Button } from '../views/common';
import { AutoJournalGenerateDialog } from '../views/journal/AutoJournalGenerateDialog';
import { JournalEntryForm } from '../views/journal/JournalEntryForm';

interface JournalEntryState {
  accounts: Account[];
  isLoading: boolean;
  errorMessage: string | null;
}

const initialState: JournalEntryState = {
  accounts: [],
  isLoading: false,
  errorMessage: null,
};

const BREADCRUMBS = [{ label: 'ホーム' }, { label: '仕訳' }, { label: '仕訳入力' }];

/**
 * 権限チェック
 */
const hasJournalEntryAccess = (hasRole: (role: Role) => boolean): boolean => {
  return hasRole('ADMIN') || hasRole('MANAGER') || hasRole('USER');
};

/**
 * 仕訳登録処理
 */
const submitJournalEntry = async (data: CreateJournalEntryRequest): Promise<void> => {
  const response = await createJournalEntry(data);
  if (!response.success) {
    throw new Error(response.errorMessage || '仕訳の登録に失敗しました');
  }
};

/**
 * 勘定科目取得成功時のステート
 */
const createSuccessState = (accounts: Account[]): JournalEntryState => ({
  accounts,
  isLoading: false,
  errorMessage: null,
});

/**
 * 勘定科目取得失敗時のステート
 */
const createErrorState = (error: unknown): JournalEntryState => ({
  accounts: [],
  isLoading: false,
  errorMessage: getAccountsErrorMessage(error),
});

/**
 * 勘定科目取得フック
 */
const useAccountsFetch = () => {
  const [state, setState] = useState<JournalEntryState>(initialState);

  const fetchAccounts = useCallback(async () => {
    setState((prev) => ({ ...prev, isLoading: true, errorMessage: null }));
    try {
      const data = await getAccounts();
      setState(createSuccessState(data));
    } catch (error) {
      setState(createErrorState(error));
    }
  }, []);

  useEffect(() => {
    void fetchAccounts();
  }, [fetchAccounts]);

  return { state, fetchAccounts };
};

/**
 * 仕訳登録実行
 */
const executeSubmit = async (
  data: CreateJournalEntryRequest,
  handlers: {
    onStart: () => void;
    onSuccess: () => void;
    onError: (error: unknown) => void;
    onComplete: () => void;
  }
): Promise<void> => {
  handlers.onStart();
  try {
    await submitJournalEntry(data);
    handlers.onSuccess();
  } catch (error) {
    handlers.onError(error);
  }
  handlers.onComplete();
};

/**
 * 仕訳登録フック
 */
const useJournalEntrySubmit = (onSuccess: () => void) => {
  const [isSubmitting, setIsSubmitting] = useState(false);
  const [submitError, setSubmitError] = useState<string | null>(null);
  const [successMessage, setSuccessMessage] = useState<string | null>(null);

  const handleSubmit = useCallback(
    async (data: CreateJournalEntryRequest) => {
      await executeSubmit(data, {
        onStart: () => {
          setSubmitError(null);
          setSuccessMessage(null);
          setIsSubmitting(true);
        },
        onSuccess: () => {
          setSuccessMessage('仕訳登録が完了しました');
          onSuccess();
        },
        onError: (error) => setSubmitError(createJournalEntryErrorMessage(error)),
        onComplete: () => setIsSubmitting(false),
      });
    },
    [onSuccess]
  );

  const dismissSuccess = useCallback(() => setSuccessMessage(null), []);

  return { isSubmitting, submitError, successMessage, handleSubmit, dismissSuccess };
};

/**
 * 仕訳入力ページ - フォーム部分
 */
const JournalEntryFormSection: React.FC<{
  state: JournalEntryState;
  isSubmitting: boolean;
  submitError: string | null;
  successMessage: string | null;
  onSubmit: (data: CreateJournalEntryRequest) => Promise<void>;
  onCancel: () => void;
  onRetry: () => void;
  onDismissSuccess: () => void;
}> = ({
  state,
  isSubmitting,
  submitError,
  successMessage,
  onSubmit,
  onCancel,
  onRetry,
  onDismissSuccess,
}) => {
  if (state.errorMessage) {
    return <ErrorMessage message={state.errorMessage} onRetry={onRetry} />;
  }

  return (
    <>
      {successMessage && (
        <div style={{ marginBottom: '16px' }} data-testid="journal-entry-success">
          <SuccessNotification message={successMessage} onDismiss={onDismissSuccess} />
        </div>
      )}
      {state.isLoading && state.accounts.length === 0 && (
        <Loading message="勘定科目を読み込み中..." />
      )}
      <JournalEntryForm
        accounts={state.accounts}
        onSubmit={onSubmit}
        onCancel={onCancel}
        isSubmitting={isSubmitting}
        error={submitError || undefined}
      />
    </>
  );
};

/**
 * 仕訳入力ページ - メインコンテンツ
 */
const JournalEntryContent: React.FC = () => {
  const { user } = useAuth();
  const navigate = useNavigate();
  const { state, fetchAccounts } = useAccountsFetch();
  const [isAutoJournalDialogOpen, setIsAutoJournalDialogOpen] = useState(false);
  const canGenerateAutoJournal = user?.role === 'ADMIN' || user?.role === 'MANAGER';

  const handleSuccessRedirect = useCallback(() => {
    setTimeout(() => {
      navigate('/', { state: { successMessage: '仕訳登録が完了しました' } });
    }, 2000);
  }, [navigate]);

  const { isSubmitting, submitError, successMessage, handleSubmit, dismissSuccess } =
    useJournalEntrySubmit(handleSuccessRedirect);

  const handleAutoJournalSuccess = useCallback(
    (journalEntryId: number) => {
      navigate('/', {
        state: { successMessage: `自動仕訳を生成しました（仕訳 ID: ${journalEntryId}）` },
      });
    },
    [navigate]
  );

  return (
    <MainLayout breadcrumbs={BREADCRUMBS}>
      <div data-testid="create-journal-entry-page">
        <h1>仕訳入力</h1>
        {canGenerateAutoJournal && (
          <div style={{ marginBottom: '16px' }}>
            <Button type="button" onClick={() => setIsAutoJournalDialogOpen(true)}>
              自動仕訳
            </Button>
          </div>
        )}
        <JournalEntryFormSection
          state={state}
          isSubmitting={isSubmitting}
          submitError={submitError}
          successMessage={successMessage}
          onSubmit={handleSubmit}
          onCancel={() => navigate(-1)}
          onRetry={fetchAccounts}
          onDismissSuccess={dismissSuccess}
        />
        <AutoJournalGenerateDialog
          isOpen={isAutoJournalDialogOpen}
          onClose={() => setIsAutoJournalDialogOpen(false)}
          onSuccess={handleAutoJournalSuccess}
        />
      </div>
    </MainLayout>
  );
};

/**
 * 仕訳入力ページ
 */
const CreateJournalEntryPage: React.FC = () => {
  const { isAuthenticated, isLoading, hasRole } = useAuth();

  if (isLoading) {
    return <Loading message="認証情報を確認中..." fullScreen />;
  }

  if (!isAuthenticated) {
    return <Navigate to="/login" replace />;
  }

  if (!hasJournalEntryAccess(hasRole)) {
    return <Navigate to="/" replace />;
  }

  return <JournalEntryContent />;
};

export default CreateJournalEntryPage;
