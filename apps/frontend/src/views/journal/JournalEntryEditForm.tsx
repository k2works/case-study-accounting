import React, { FormEvent, useEffect, useState } from 'react';
import type { UpdateJournalEntryRequest } from '../../api/updateJournalEntry';
import type { JournalEntry } from '../../api/getJournalEntry';
import type { Account } from '../../api/getAccounts';
import { Button } from '../common';
import { JournalEntryFormBody } from './JournalEntryFormBody';
import {
  type JournalEntryLineState,
  createEmptyLine,
  buildLinePayload,
} from './journalEntryFormUtils';
import { useJournalEntryFormCore } from './useJournalEntryFormCore';
import './JournalEntryForm.css';

interface JournalEntryEditFormProps {
  accounts: Account[];
  journalEntry: JournalEntry;
  onSubmit: (data: UpdateJournalEntryRequest) => Promise<void>;
  onCancel: () => void;
  onDelete?: () => void;
  isSubmitting: boolean;
  isDeleting?: boolean;
  error?: string;
}

const sortLines = (lines: JournalEntry['lines']): JournalEntry['lines'] => {
  return [...lines].sort((a, b) => a.lineNumber - b.lineNumber);
};

const createInitialLines = (journalEntry: JournalEntry): JournalEntryLineState[] => {
  if (!journalEntry.lines || journalEntry.lines.length === 0) {
    return [createEmptyLine(0)];
  }
  return sortLines(journalEntry.lines).map((line, index) => ({
    id: line.lineNumber ?? index + 1,
    accountId: String(line.accountId),
    debitAmount: line.debitAmount !== undefined ? String(line.debitAmount) : '',
    creditAmount: line.creditAmount !== undefined ? String(line.creditAmount) : '',
  }));
};

const getMaxLineId = (lines: JournalEntryLineState[]): number => {
  if (lines.length === 0) return 0;
  return Math.max(...lines.map((line) => line.id));
};

/**
 * フォームフッターのアクションボタン
 */
interface FormFooterProps {
  isSubmitting: boolean;
  isDeleting: boolean;
  isSubmitDisabled: boolean;
  onCancel: () => void;
  onDelete?: () => void;
}

const FormFooter: React.FC<FormFooterProps> = ({
  isSubmitting,
  isDeleting,
  isSubmitDisabled,
  onCancel,
  onDelete,
}) => (
  <div className="journal-entry-form__footer">
    <Button
      type="submit"
      variant="primary"
      disabled={isSubmitDisabled}
      data-testid="journal-entry-submit"
    >
      {isSubmitting ? '保存中...' : '保存'}
    </Button>
    <Button
      type="button"
      variant="text"
      onClick={onCancel}
      disabled={isSubmitting || isDeleting}
      data-testid="journal-entry-cancel"
    >
      キャンセル
    </Button>
    {onDelete && (
      <Button
        type="button"
        variant="danger"
        onClick={onDelete}
        disabled={isSubmitting || isDeleting}
        data-testid="journal-entry-delete"
      >
        {isDeleting ? '削除中...' : '削除'}
      </Button>
    )}
  </div>
);

/**
 * 仕訳編集フォームコンポーネント
 */
export const JournalEntryEditForm: React.FC<JournalEntryEditFormProps> = ({
  accounts,
  journalEntry,
  onSubmit,
  onCancel,
  onDelete,
  isSubmitting,
  isDeleting = false,
  error,
}) => {
  const form = useJournalEntryFormCore({
    initialDate: journalEntry.journalDate,
    initialDescription: journalEntry.description,
    initialLines: createInitialLines(journalEntry),
  });
  const [version, setVersion] = useState(journalEntry.version);
  const isSubmitDisabled = !form.isSubmitReady || isSubmitting;

  useEffect(() => {
    form.setJournalDate(journalEntry.journalDate);
    form.setDescription(journalEntry.description);
    const newLines = createInitialLines(journalEntry);
    form.setLines(newLines);
    setVersion(journalEntry.version);
    form.lineIdRef.current = getMaxLineId(newLines);
    form.setFormError(null);
  }, [journalEntry]); // eslint-disable-line react-hooks/exhaustive-deps

  const handleSubmit = async (e: FormEvent) => {
    e.preventDefault();
    const validationError = form.validateForm();
    if (validationError) {
      form.setFormError(validationError);
      return;
    }
    form.setFormError(null);
    await onSubmit({
      journalDate: form.journalDate,
      description: form.description.trim(),
      version,
      lines: buildLinePayload(form.lines),
    });
  };

  return (
    <form
      className="journal-entry-form"
      onSubmit={handleSubmit}
      data-testid="journal-entry-edit-form"
    >
      <JournalEntryFormBody
        form={form}
        accounts={accounts}
        isSubmitting={isSubmitting}
        error={error}
      />
      <FormFooter
        isSubmitting={isSubmitting}
        isDeleting={isDeleting}
        isSubmitDisabled={isSubmitDisabled}
        onCancel={onCancel}
        onDelete={onDelete}
      />
    </form>
  );
};
