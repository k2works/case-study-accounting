import React, { FormEvent } from 'react';
import type { CreateJournalEntryRequest } from '../../api/createJournalEntry';
import type { FormAccount } from './journalEntryFormUtils';
import { buildLinePayload } from './journalEntryFormUtils';
import { useJournalEntryFormCore } from './useJournalEntryFormCore';
import { JournalEntryFormBody } from './JournalEntryFormBody';
import { Button } from '../common';
import './JournalEntryForm.css';

interface JournalEntryFormProps {
  accounts: FormAccount[];
  onSubmit: (data: CreateJournalEntryRequest) => Promise<void>;
  onCancel: () => void;
  isSubmitting: boolean;
  error?: string;
}

/**
 * 仕訳入力フォームコンポーネント
 */
export const JournalEntryForm: React.FC<JournalEntryFormProps> = ({
  accounts,
  onSubmit,
  onCancel,
  isSubmitting,
  error,
}) => {
  const form = useJournalEntryFormCore();
  const isSubmitDisabled = !form.isSubmitReady || isSubmitting;

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
      lines: buildLinePayload(form.lines),
    });
  };

  return (
    <form className="journal-entry-form" onSubmit={handleSubmit} data-testid="journal-entry-form">
      <JournalEntryFormBody
        form={form}
        accounts={accounts}
        isSubmitting={isSubmitting}
        error={error}
      />
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
          disabled={isSubmitting}
          data-testid="journal-entry-cancel"
        >
          キャンセル
        </Button>
      </div>
    </form>
  );
};
