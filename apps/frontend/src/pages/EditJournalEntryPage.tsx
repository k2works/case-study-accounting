import React, { useCallback, useEffect, useMemo, useState } from 'react';
import { Navigate, useNavigate, useParams } from 'react-router-dom';
import { useAuth } from '../hooks/useAuth';
import { getAccounts, getAccountsErrorMessage } from '../api/getAccounts';
import type { Account } from '../api/getAccounts';
import {
  getJournalEntry,
  getJournalEntryErrorMessage,
  type JournalEntry,
} from '../api/getJournalEntry';
import {
  updateJournalEntry,
  updateJournalEntryErrorMessage,
  type UpdateJournalEntryRequest,
} from '../api/updateJournalEntry';
import type { Role } from '../types/auth';
import { MainLayout, Loading, ErrorMessage } from '../views/common';
import { JournalEntryEditForm } from '../views/journal/JournalEntryEditForm';

interface JournalEntryState {
  journalEntry: JournalEntry | null;
  isLoading: boolean;
  errorMessage: string | null;
}

interface AccountsState {
  accounts: Account[];
  isLoading: boolean;
  errorMessage: string | null;
}

const initialJournalEntryState: JournalEntryState = {
  journalEntry: null,
  isLoading: false,
  errorMessage: null,
};

const initialAccountsState: AccountsState = {
  accounts: [],
  isLoading: false,
  errorMessage: null,
};

const BREADCRUMBS = [{ label: 'ホーム' }, { label: '仕訳' }, { label: '仕訳編集' }];

/**
 * 権限チェック
 */
const hasJournalEntryAccess = (hasRole: (role: Role) => boolean): boolean => {
  return hasRole('ADMIN') || hasRole('MANAGER') || hasRole('USER');
};

/**
 * 勘定科目取得フック
 */
const useAccountsFetch = () => {
  const [state, setState] = useState<AccountsState>(initialAccountsState);

  const fetchAccounts = useCallback(async () => {
    setState((prev) => ({ ...prev, isLoading: true, errorMessage: null }));
    try {
      const data = await getAccounts();
      setState({ accounts: data, isLoading: false, errorMessage: null });
    } catch (error) {
      setState({ accounts: [], isLoading: false, errorMessage: getAccountsErrorMessage(error) });
    }
  }, []);

  useEffect(() => {
    void fetchAccounts();
  }, [fetchAccounts]);

  return { state, fetchAccounts };
};

/**
 * 仕訳取得フック
 */
const useJournalEntryFetch = (journalEntryId: number, isInvalidId: boolean) => {
  const [state, setState] = useState<JournalEntryState>(initialJournalEntryState);

  const fetchJournalEntry = useCallback(async () => {
    if (isInvalidId) {
      setState({
        journalEntry: null,
        isLoading: false,
        errorMessage: '仕訳が見つかりません',
      });
      return;
    }

    setState((prev) => ({ ...prev, isLoading: true, errorMessage: null }));
    try {
      const data = await getJournalEntry(journalEntryId);
      setState({ journalEntry: data, isLoading: false, errorMessage: null });
    } catch (error) {
      setState({
        journalEntry: null,
        isLoading: false,
        errorMessage: getJournalEntryErrorMessage(error),
      });
    }
  }, [isInvalidId, journalEntryId]);

  useEffect(() => {
    void fetchJournalEntry();
  }, [fetchJournalEntry]);

  return { state, fetchJournalEntry };
};

/**
 * 仕訳更新処理
 */
const submitJournalEntryUpdate = async (
  journalEntryId: number,
  data: UpdateJournalEntryRequest
): Promise<void> => {
  const response = await updateJournalEntry(journalEntryId, data);
  if (!response.success) {
    throw new Error(response.errorMessage || '仕訳の更新に失敗しました');
  }
};

/**
 * ページ読み込み状態の判定
 */
const isDataLoading = (accountsState: AccountsState, journalState: JournalEntryState): boolean => {
  const accountsLoading = accountsState.isLoading && accountsState.accounts.length === 0;
  const journalLoading = journalState.isLoading && !journalState.journalEntry;
  return accountsLoading || journalLoading;
};

/**
 * 非下書き状態の判定
 */
const checkNonDraft = (journalEntry: JournalEntry | null): boolean => {
  return journalEntry ? journalEntry.status !== 'DRAFT' : false;
};

/**
 * エラーメッセージの取得
 */
const getPageError = (
  journalState: JournalEntryState,
  accountsState: AccountsState,
  isNonDraft: boolean
): string | null => {
  if (journalState.errorMessage) return journalState.errorMessage;
  if (!isNonDraft) return accountsState.errorMessage;
  return null;
};

interface ContentProps {
  accountsState: AccountsState;
  journalState: JournalEntryState;
  isSubmitting: boolean;
  submitError: string | null;
  onSubmit: (data: UpdateJournalEntryRequest) => Promise<void>;
  onCancel: () => void;
  onRetry: () => void;
}

/**
 * 編集フォーム表示コンポーネント
 */
const EditFormSection: React.FC<Omit<ContentProps, 'onRetry'>> = ({
  accountsState,
  journalState,
  isSubmitting,
  submitError,
  onSubmit,
  onCancel,
}) => {
  if (!journalState.journalEntry) return null;
  return (
    <JournalEntryEditForm
      accounts={accountsState.accounts}
      journalEntry={journalState.journalEntry}
      onSubmit={onSubmit}
      onCancel={onCancel}
      isSubmitting={isSubmitting}
      error={submitError || undefined}
    />
  );
};

type ContentState = 'loading' | 'error' | 'non-draft' | 'form' | 'empty';

const determineContentState = (
  accountsState: AccountsState,
  journalState: JournalEntryState
): { state: ContentState; pageError: string | null; isNonDraft: boolean } => {
  const isPageLoading = isDataLoading(accountsState, journalState);
  if (isPageLoading) return { state: 'loading', pageError: null, isNonDraft: false };

  const isNonDraft = checkNonDraft(journalState.journalEntry);
  const pageError = getPageError(journalState, accountsState, isNonDraft);

  if (pageError) return { state: 'error', pageError, isNonDraft };
  if (journalState.journalEntry && isNonDraft)
    return { state: 'non-draft', pageError: null, isNonDraft };
  if (journalState.journalEntry && !isNonDraft)
    return { state: 'form', pageError: null, isNonDraft };
  return { state: 'empty', pageError: null, isNonDraft: false };
};

/**
 * 仕訳編集ページコンテンツ
 */
const EditJournalEntryContent: React.FC<ContentProps> = (props) => {
  const { accountsState, journalState, onCancel, onRetry } = props;
  const { state, pageError } = determineContentState(accountsState, journalState);

  switch (state) {
    case 'loading':
      return <Loading message="仕訳情報を読み込み中..." />;
    case 'error':
      return <ErrorMessage message={pageError!} onRetry={onRetry} />;
    case 'non-draft':
      return <ErrorMessage message="下書き状態の仕訳のみ編集できます" onDismiss={onCancel} />;
    case 'form':
      return <EditFormSection {...props} />;
    default:
      return null;
  }
};

/**
 * 仕訳編集ページ
 */
const EditJournalEntryPage: React.FC = () => {
  const { isAuthenticated, isLoading, hasRole } = useAuth();
  const { id } = useParams();
  const navigate = useNavigate();

  const journalEntryId = Number(id);
  const isInvalidId = Number.isNaN(journalEntryId);

  const { state: accountsState, fetchAccounts } = useAccountsFetch();
  const { state: journalState, fetchJournalEntry } = useJournalEntryFetch(
    journalEntryId,
    isInvalidId
  );

  const [isSubmitting, setIsSubmitting] = useState(false);
  const [submitError, setSubmitError] = useState<string | null>(null);

  const fetchAll = useCallback(() => {
    void fetchAccounts();
    void fetchJournalEntry();
  }, [fetchAccounts, fetchJournalEntry]);

  const handleSubmit = useCallback(
    async (data: UpdateJournalEntryRequest) => {
      setIsSubmitting(true);
      setSubmitError(null);
      try {
        await submitJournalEntryUpdate(journalEntryId, data);
        navigate('/', { replace: true, state: { successMessage: '仕訳を更新しました' } });
      } catch (error) {
        setSubmitError(updateJournalEntryErrorMessage(error));
      } finally {
        setIsSubmitting(false);
      }
    },
    [journalEntryId, navigate]
  );

  const handleCancel = useCallback(() => navigate(-1), [navigate]);
  const breadcrumbs = useMemo(() => BREADCRUMBS, []);

  if (isLoading) {
    return <Loading message="認証情報を確認中..." fullScreen />;
  }

  if (!isAuthenticated) {
    return <Navigate to="/login" replace />;
  }

  if (!hasJournalEntryAccess(hasRole)) {
    return <Navigate to="/" replace />;
  }

  return (
    <MainLayout breadcrumbs={breadcrumbs}>
      <div data-testid="edit-journal-entry-page">
        <h1>仕訳編集</h1>
        <EditJournalEntryContent
          accountsState={accountsState}
          journalState={journalState}
          isSubmitting={isSubmitting}
          submitError={submitError}
          onSubmit={handleSubmit}
          onCancel={handleCancel}
          onRetry={fetchAll}
        />
      </div>
    </MainLayout>
  );
};

export default EditJournalEntryPage;
