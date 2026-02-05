import type { ChangeEvent } from 'react';

// ─── Types ──────────────────────────────────────────

export interface JournalEntryLineState {
  id: number;
  accountId: string;
  debitAmount: string;
  creditAmount: string;
}

export interface FormAccount {
  accountId: number;
  accountCode: string;
  accountName: string;
}

export interface FormTotals {
  totalDebit: number;
  totalCredit: number;
  difference: number;
}

export interface JournalEntryFormState {
  journalDate: string;
  description: string;
  lines: JournalEntryLineState[];
  formError: string | null;
  setFormError: (error: string | null) => void;
  totals: FormTotals;
  isSubmitReady: boolean;
  handleDateChange: (e: ChangeEvent<HTMLInputElement>) => void;
  handleDescriptionChange: (e: ChangeEvent<HTMLInputElement>) => void;
  handleAccountChange: (id: number, value: string) => void;
  handleAmountChange: (id: number, field: 'debitAmount' | 'creditAmount', value: string) => void;
  handleAddLine: () => void;
  handleRemoveLine: (id: number) => void;
  validateForm: () => string | null;
}

// ─── Pure utility functions ─────────────────────────

export const createEmptyLine = (id: number): JournalEntryLineState => ({
  id,
  accountId: '',
  debitAmount: '',
  creditAmount: '',
});

export const parseAmount = (value: string): number => {
  if (!value) return 0;
  const parsed = Number(value);
  return Number.isNaN(parsed) ? 0 : parsed;
};

export const isValidAmountInput = (value: string): boolean => {
  if (value === '') return true;
  return /^\d+$/.test(value);
};

export const isLineValid = (line: JournalEntryLineState): boolean => {
  const amountValue = parseAmount(line.debitAmount) || parseAmount(line.creditAmount);
  return line.accountId !== '' && amountValue > 0;
};

export const validateRequiredFields = (journalDate: string, description: string): string | null => {
  if (!journalDate.trim()) return '仕訳日を入力してください';
  if (!description.trim()) return '摘要を入力してください';
  return null;
};

export const validateLines = (lines: JournalEntryLineState[]): string | null => {
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

export const updateLineAmount = (
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

export const computeTotals = (lines: JournalEntryLineState[]): FormTotals => {
  const totalDebit = lines.reduce((sum, line) => sum + parseAmount(line.debitAmount), 0);
  const totalCredit = lines.reduce((sum, line) => sum + parseAmount(line.creditAmount), 0);
  return { totalDebit, totalCredit, difference: totalDebit - totalCredit };
};

export const isBalancedTotals = (totals: FormTotals): boolean =>
  Math.abs(totals.difference) < 0.0001;

export const buildLinePayload = (lines: JournalEntryLineState[]) =>
  lines.map((line, index) => {
    const debit = parseAmount(line.debitAmount);
    const credit = parseAmount(line.creditAmount);
    return {
      lineNumber: index + 1,
      accountId: Number(line.accountId),
      debitAmount: debit > 0 ? debit : undefined,
      creditAmount: credit > 0 ? credit : undefined,
    };
  });
