import React, { type ChangeEvent, type MutableRefObject, useMemo, useRef, useState } from 'react';
import {
  type JournalEntryLineState,
  type JournalEntryFormState,
  type FormTotals,
  createEmptyLine,
  isValidAmountInput,
  isLineValid,
  validateRequiredFields,
  validateLines,
  updateLineAmount,
  computeTotals,
  isBalancedTotals,
} from './journalEntryFormUtils';

interface CoreFormOptions {
  initialDate?: string;
  initialDescription?: string;
  initialLines?: JournalEntryLineState[];
}

export interface CoreFormReturn extends JournalEntryFormState {
  setJournalDate: (date: string) => void;
  setDescription: (desc: string) => void;
  setLines: (lines: JournalEntryLineState[]) => void;
  lineIdRef: MutableRefObject<number>;
}

const buildLineHandlers = (
  setLines: React.Dispatch<React.SetStateAction<JournalEntryLineState[]>>,
  clearError: () => void,
  lineIdRef: MutableRefObject<number>
) => {
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

  return { handleAccountChange, handleAmountChange, handleAddLine, handleRemoveLine };
};

const buildValidateForm = (
  journalDate: string,
  description: string,
  lines: JournalEntryLineState[],
  balanced: boolean
) => {
  return (): string | null => {
    const fieldError = validateRequiredFields(journalDate, description);
    if (fieldError) return fieldError;
    const lineError = validateLines(lines);
    if (lineError) return lineError;
    if (!balanced) return '貸借が一致していません';
    return null;
  };
};

const resolveOptions = (options: CoreFormOptions) => ({
  initialDate: options.initialDate ?? '',
  initialDescription: options.initialDescription ?? '',
  initialLines: options.initialLines ?? [createEmptyLine(0)],
});

const computeSubmitReady = (
  journalDate: string,
  description: string,
  lines: JournalEntryLineState[],
  balanced: boolean
): boolean => {
  const hasRequiredFields = journalDate.trim() !== '' && description.trim() !== '';
  const areLinesValid = lines.length > 0 && lines.every(isLineValid);
  return hasRequiredFields && areLinesValid && balanced;
};

export const useJournalEntryFormCore = (options: CoreFormOptions = {}): CoreFormReturn => {
  const { initialDate, initialDescription, initialLines } = resolveOptions(options);

  const lineIdRef = useRef(0);
  const [journalDate, setJournalDate] = useState(initialDate);
  const [description, setDescription] = useState(initialDescription);
  const [lines, setLines] = useState<JournalEntryLineState[]>(initialLines);
  const [formError, setFormError] = useState<string | null>(null);

  const totals: FormTotals = useMemo(() => computeTotals(lines), [lines]);
  const balanced = isBalancedTotals(totals);

  const clearError = () => setFormError(null);

  const handleDateChange = (e: ChangeEvent<HTMLInputElement>) => {
    setJournalDate(e.target.value);
    clearError();
  };

  const handleDescriptionChange = (e: ChangeEvent<HTMLInputElement>) => {
    setDescription(e.target.value);
    clearError();
  };

  const { handleAccountChange, handleAmountChange, handleAddLine, handleRemoveLine } =
    buildLineHandlers(setLines, clearError, lineIdRef);

  const validateForm = buildValidateForm(journalDate, description, lines, balanced);

  return {
    journalDate,
    setJournalDate,
    description,
    setDescription,
    lines,
    setLines,
    lineIdRef,
    formError,
    setFormError,
    totals,
    isSubmitReady: computeSubmitReady(journalDate, description, lines, balanced),
    handleDateChange,
    handleDescriptionChange,
    handleAccountChange,
    handleAmountChange,
    handleAddLine,
    handleRemoveLine,
    validateForm,
  };
};
