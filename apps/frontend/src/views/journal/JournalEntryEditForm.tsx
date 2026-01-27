import React, { ChangeEvent, FormEvent, useEffect, useMemo, useRef, useState } from 'react';
import type { UpdateJournalEntryRequest } from '../../api/updateJournalEntry';
import type { JournalEntry } from '../../api/getJournalEntry';
import type { Account } from '../../api/getAccounts';
import { Button, ErrorMessage, MoneyDisplay } from '../common';
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

interface JournalEntryLineState {
  id: number;
  accountId: string;
  debitAmount: string;
  creditAmount: string;
}

const createEmptyLine = (id: number): JournalEntryLineState => ({
  id,
  accountId: '',
  debitAmount: '',
  creditAmount: '',
});

const parseAmount = (value: string): number => {
  if (!value) return 0;
  const parsed = Number(value);
  return Number.isNaN(parsed) ? 0 : parsed;
};

const isValidAmountInput = (value: string): boolean => {
  if (value === '') return true;
  return /^\d+$/.test(value);
};

const isLineValid = (line: JournalEntryLineState): boolean => {
  const amountValue = parseAmount(line.debitAmount) || parseAmount(line.creditAmount);
  return line.accountId !== '' && amountValue > 0;
};

const validateRequiredFields = (journalDate: string, description: string): string | null => {
  if (!journalDate.trim()) return '仕訳日を入力してください';
  if (!description.trim()) return '摘要を入力してください';
  return null;
};

const validateLines = (lines: JournalEntryLineState[]): string | null => {
  if (lines.length === 0) return '明細行を追加してください';

  for (const line of lines) {
    const debit = parseAmount(line.debitAmount);
    const credit = parseAmount(line.creditAmount);
    if (line.accountId === '' || (debit <= 0 && credit <= 0)) {
      return '明細行の勘定科目と金額を入力してください';
    }
  }
  return null;
};

const updateLineAmount = (
  line: JournalEntryLineState,
  field: 'debitAmount' | 'creditAmount',
  value: string
): JournalEntryLineState => {
  const nextLine = { ...line, [field]: value };
  if (value !== '' && field === 'debitAmount') {
    nextLine.creditAmount = '';
  } else if (value !== '' && field === 'creditAmount') {
    nextLine.debitAmount = '';
  }
  return nextLine;
};

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
 * 仕訳編集フォーム状態管理フック
 */
const useJournalEntryEditFormState = (journalEntry: JournalEntry) => {
  const lineIdRef = useRef(0);
  const [journalDate, setJournalDate] = useState(journalEntry.journalDate);
  const [description, setDescription] = useState(journalEntry.description);
  const [lines, setLines] = useState<JournalEntryLineState[]>(() =>
    createInitialLines(journalEntry)
  );
  const [version, setVersion] = useState(journalEntry.version);
  const [formError, setFormError] = useState<string | null>(null);

  useEffect(() => {
    setJournalDate(journalEntry.journalDate);
    setDescription(journalEntry.description);
    const initialLines = createInitialLines(journalEntry);
    setLines(initialLines);
    setVersion(journalEntry.version);
    lineIdRef.current = getMaxLineId(initialLines);
    setFormError(null);
  }, [journalEntry]);

  const totals = useMemo(() => {
    const totalDebit = lines.reduce((sum, line) => sum + parseAmount(line.debitAmount), 0);
    const totalCredit = lines.reduce((sum, line) => sum + parseAmount(line.creditAmount), 0);
    return { totalDebit, totalCredit, difference: totalDebit - totalCredit };
  }, [lines]);

  const isBalanced = Math.abs(totals.difference) < 0.0001;
  const hasRequiredFields = journalDate.trim() !== '' && description.trim() !== '';
  const areLinesValid = lines.length > 0 && lines.every(isLineValid);

  const clearError = () => formError && setFormError(null);

  const handleDateChange = (e: ChangeEvent<HTMLInputElement>) => {
    setJournalDate(e.target.value);
    clearError();
  };

  const handleDescriptionChange = (e: ChangeEvent<HTMLInputElement>) => {
    setDescription(e.target.value);
    clearError();
  };

  const handleAccountChange = (id: number, value: string) => {
    setLines((prev) => prev.map((line) => (line.id === id ? { ...line, accountId: value } : line)));
    clearError();
  };

  const handleAmountChange = (id: number, field: 'debitAmount' | 'creditAmount', value: string) => {
    if (!isValidAmountInput(value)) return;
    setLines((prev) =>
      prev.map((line) => (line.id === id ? updateLineAmount(line, field, value) : line))
    );
    clearError();
  };

  const handleAddLine = () => {
    const nextId = lineIdRef.current + 1;
    lineIdRef.current = nextId;
    setLines((prev) => [...prev, createEmptyLine(nextId)]);
  };

  const handleRemoveLine = (id: number) => {
    setLines((prev) =>
      prev.length <= 1 ? [createEmptyLine(prev[0].id)] : prev.filter((l) => l.id !== id)
    );
    clearError();
  };

  const validateForm = (): string | null => {
    const fieldError = validateRequiredFields(journalDate, description);
    if (fieldError) return fieldError;
    const lineError = validateLines(lines);
    if (lineError) return lineError;
    if (!isBalanced) return '貸借が一致していません';
    return null;
  };

  const buildRequestPayload = (): UpdateJournalEntryRequest => ({
    journalDate,
    description: description.trim(),
    version,
    lines: lines.map((line, index) => {
      const debit = parseAmount(line.debitAmount);
      const credit = parseAmount(line.creditAmount);
      return {
        lineNumber: index + 1,
        accountId: Number(line.accountId),
        debitAmount: debit > 0 ? debit : undefined,
        creditAmount: credit > 0 ? credit : undefined,
      };
    }),
  });

  return {
    journalDate,
    description,
    lines,
    formError,
    setFormError,
    totals,
    isSubmitReady: hasRequiredFields && areLinesValid && isBalanced,
    handleDateChange,
    handleDescriptionChange,
    handleAccountChange,
    handleAmountChange,
    handleAddLine,
    handleRemoveLine,
    validateForm,
    buildRequestPayload,
  };
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
  const form = useJournalEntryEditFormState(journalEntry);
  const isSubmitDisabled = !form.isSubmitReady || isSubmitting;

  const handleSubmit = async (e: FormEvent) => {
    e.preventDefault();
    const validationError = form.validateForm();
    if (validationError) {
      form.setFormError(validationError);
      return;
    }
    form.setFormError(null);
    await onSubmit(form.buildRequestPayload());
  };

  return (
    <form
      className="journal-entry-form"
      onSubmit={handleSubmit}
      data-testid="journal-entry-edit-form"
    >
      <div className="journal-entry-form__field">
        <label htmlFor="journalDate" className="journal-entry-form__label">
          仕訳日 <span className="journal-entry-form__required">*</span>
        </label>
        <input
          id="journalDate"
          type="date"
          className="journal-entry-form__input"
          value={form.journalDate}
          onChange={form.handleDateChange}
          disabled={isSubmitting}
          data-testid="journal-entry-date-input"
        />
      </div>
      <div className="journal-entry-form__field">
        <label htmlFor="description" className="journal-entry-form__label">
          摘要 <span className="journal-entry-form__required">*</span>
        </label>
        <input
          id="description"
          type="text"
          className="journal-entry-form__input"
          value={form.description}
          onChange={form.handleDescriptionChange}
          disabled={isSubmitting}
          placeholder="摘要を入力"
          data-testid="journal-entry-description-input"
        />
      </div>

      <div className="journal-entry-form__table-wrapper">
        <table className="journal-entry-form__table">
          <thead>
            <tr>
              <th className="journal-entry-form__th journal-entry-form__th--number">行番号</th>
              <th className="journal-entry-form__th">勘定科目</th>
              <th className="journal-entry-form__th journal-entry-form__th--amount">借方金額</th>
              <th className="journal-entry-form__th journal-entry-form__th--amount">貸方金額</th>
              <th className="journal-entry-form__th journal-entry-form__th--action">操作</th>
            </tr>
          </thead>
          <tbody>
            {form.lines.map((line, index) => (
              <tr key={line.id}>
                <td className="journal-entry-form__td journal-entry-form__td--number">
                  {index + 1}
                </td>
                <td className="journal-entry-form__td">
                  <select
                    className="journal-entry-form__select"
                    value={line.accountId}
                    onChange={(e) => form.handleAccountChange(line.id, e.target.value)}
                    disabled={isSubmitting}
                    data-testid={`journal-entry-account-${line.id}`}
                  >
                    <option value="">選択してください</option>
                    {accounts.map((account) => (
                      <option key={account.accountId} value={account.accountId}>
                        {account.accountCode} {account.accountName}
                      </option>
                    ))}
                  </select>
                </td>
                <td className="journal-entry-form__td journal-entry-form__td--amount">
                  <input
                    type="number"
                    inputMode="numeric"
                    min="0"
                    step="1"
                    className="journal-entry-form__input journal-entry-form__input--amount"
                    value={line.debitAmount}
                    onChange={(e) =>
                      form.handleAmountChange(line.id, 'debitAmount', e.target.value)
                    }
                    disabled={isSubmitting}
                    data-testid={`journal-entry-debit-${line.id}`}
                  />
                </td>
                <td className="journal-entry-form__td journal-entry-form__td--amount">
                  <input
                    type="number"
                    inputMode="numeric"
                    min="0"
                    step="1"
                    className="journal-entry-form__input journal-entry-form__input--amount"
                    value={line.creditAmount}
                    onChange={(e) =>
                      form.handleAmountChange(line.id, 'creditAmount', e.target.value)
                    }
                    disabled={isSubmitting}
                    data-testid={`journal-entry-credit-${line.id}`}
                  />
                </td>
                <td className="journal-entry-form__td journal-entry-form__td--action">
                  <Button
                    type="button"
                    variant="danger"
                    size="small"
                    onClick={() => form.handleRemoveLine(line.id)}
                    disabled={isSubmitting}
                    data-testid={`journal-entry-remove-${line.id}`}
                  >
                    削除
                  </Button>
                </td>
              </tr>
            ))}
          </tbody>
          <tfoot>
            <tr className="journal-entry-form__totals">
              <td className="journal-entry-form__td" colSpan={2}>
                合計
              </td>
              <td className="journal-entry-form__td journal-entry-form__td--amount">
                <MoneyDisplay amount={form.totals.totalDebit} />
              </td>
              <td className="journal-entry-form__td journal-entry-form__td--amount">
                <MoneyDisplay amount={form.totals.totalCredit} />
              </td>
              <td className="journal-entry-form__td journal-entry-form__td--action">
                <span className="journal-entry-form__difference" data-testid="journal-entry-diff">
                  差額: <MoneyDisplay amount={form.totals.difference} showSign colorize />
                </span>
              </td>
            </tr>
          </tfoot>
        </table>
      </div>

      <div className="journal-entry-form__actions">
        <Button
          type="button"
          variant="secondary"
          onClick={form.handleAddLine}
          disabled={isSubmitting}
          data-testid="journal-entry-add-line"
        >
          行追加
        </Button>
      </div>

      {(form.formError || error) && (
        <div className="journal-entry-form__error" data-testid="journal-entry-error">
          <ErrorMessage message={form.formError || error || ''} />
        </div>
      )}

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
