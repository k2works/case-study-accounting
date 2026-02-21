import React, { useCallback, useEffect, useMemo, useState } from 'react';
import {
  generateAutoJournal,
  generateAutoJournalErrorMessage,
  type GenerateAutoJournalRequest,
  type GenerateAutoJournalResponse,
} from '../../api/generateAutoJournal';
import {
  getAutoJournalPatterns,
  getAutoJournalPatternsErrorMessage,
  type AutoJournalPattern,
} from '../../api/getAutoJournalPatterns';
import { Button, Loading } from '../common';

interface AutoJournalGenerateDialogProps {
  isOpen: boolean;
  onClose: () => void;
  onSuccess: (journalEntryId: number) => void;
}

interface AmountValidationResult {
  amounts: Record<string, number>;
  error: string | null;
}

interface GenerateRequestBuildResult {
  request: GenerateAutoJournalRequest | null;
  error: string | null;
}

interface AutoJournalDialogFormProps {
  activePatterns: AutoJournalPattern[];
  selectedPatternId: string;
  selectedPattern: AutoJournalPattern | null;
  amountVariables: string[];
  amountInputs: Record<string, string>;
  journalDate: string;
  description: string;
  submitError: string | null;
  patternError: string | null;
  isSubmitting: boolean;
  onSelectPattern: (patternId: string) => void;
  onAmountInputChange: (variable: string, value: string) => void;
  onJournalDateChange: (value: string) => void;
  onDescriptionChange: (value: string) => void;
  onGenerate: () => void;
  onClose: () => void;
}

const VARIABLE_PATTERN = /^([a-zA-Z_][a-zA-Z0-9_]*)/;

const createInitialJournalDate = (): string => new Date().toISOString().slice(0, 10);

const overlayStyle: React.CSSProperties = {
  position: 'fixed',
  inset: 0,
  background: 'rgba(0, 0, 0, 0.5)',
  display: 'flex',
  alignItems: 'center',
  justifyContent: 'center',
  zIndex: 1000,
};

const dialogStyle: React.CSSProperties = {
  background: 'white',
  borderRadius: '8px',
  padding: '24px',
  maxWidth: '700px',
  width: '90%',
  maxHeight: '80vh',
  overflowY: 'auto',
};

const closeButtonStyle: React.CSSProperties = {
  background: 'none',
  border: 'none',
  fontSize: '20px',
  cursor: 'pointer',
  padding: 0,
  lineHeight: 1,
};

const extractAmountVariables = (pattern: AutoJournalPattern | null): string[] => {
  if (!pattern) {
    return [];
  }

  const variableSet = new Set<string>();
  for (const item of pattern.items) {
    const match = item.amountFormula.match(VARIABLE_PATTERN);
    if (match?.[1]) {
      variableSet.add(match[1]);
    }
  }

  return Array.from(variableSet);
};

const validateAmounts = (
  variables: string[],
  inputs: Record<string, string>
): AmountValidationResult => {
  const amounts: Record<string, number> = {};

  for (const variable of variables) {
    const rawValue = inputs[variable];
    if (!rawValue || rawValue.trim() === '') {
      return { amounts: {}, error: `${variable} の金額を入力してください` };
    }

    const parsedValue = Number(rawValue);
    if (Number.isNaN(parsedValue)) {
      return { amounts: {}, error: `${variable} の金額は数値で入力してください` };
    }

    amounts[variable] = parsedValue;
  }

  return { amounts, error: null };
};

const buildGenerateRequest = (params: {
  selectedPattern: AutoJournalPattern | null;
  journalDate: string;
  description: string;
  amountVariables: string[];
  amountInputs: Record<string, string>;
}): GenerateRequestBuildResult => {
  if (!params.selectedPattern) {
    return { request: null, error: '自動仕訳パターンを選択してください' };
  }

  if (!params.journalDate) {
    return { request: null, error: '仕訳日を入力してください' };
  }

  const amountValidation = validateAmounts(params.amountVariables, params.amountInputs);
  if (amountValidation.error) {
    return { request: null, error: amountValidation.error };
  }

  return {
    request: {
      patternId: params.selectedPattern.patternId,
      amounts: amountValidation.amounts,
      journalDate: params.journalDate,
      description: params.description.trim() || undefined,
    },
    error: null,
  };
};

const resolveJournalEntryId = (response: GenerateAutoJournalResponse): number => {
  if (!response.success || !response.journalEntryId) {
    throw new Error(response.errorMessage || '自動仕訳の生成に失敗しました');
  }
  return response.journalEntryId;
};

const PatternItemsTable: React.FC<{ selectedPattern: AutoJournalPattern | null }> = ({
  selectedPattern,
}) => {
  if (!selectedPattern) {
    return null;
  }

  return (
    <div className="journal-entry-form__field">
      <label className="journal-entry-form__label">明細行</label>
      <div className="journal-entry-form__table-wrapper">
        <table className="journal-entry-form__table">
          <thead>
            <tr>
              <th className="journal-entry-form__th journal-entry-form__th--number">行</th>
              <th className="journal-entry-form__th">勘定科目</th>
              <th className="journal-entry-form__th">貸借</th>
              <th className="journal-entry-form__th">数式</th>
            </tr>
          </thead>
          <tbody>
            {selectedPattern.items.map((item) => (
              <tr key={`${item.lineNumber}-${item.accountCode}`}>
                <td className="journal-entry-form__td journal-entry-form__td--number">
                  {item.lineNumber}
                </td>
                <td className="journal-entry-form__td">{item.accountCode}</td>
                <td className="journal-entry-form__td">
                  {item.debitCreditType === 'D' ? '借方' : '貸方'}
                </td>
                <td className="journal-entry-form__td">{item.amountFormula}</td>
              </tr>
            ))}
          </tbody>
        </table>
      </div>
    </div>
  );
};

const AmountInputFields: React.FC<{
  amountVariables: string[];
  amountInputs: Record<string, string>;
  isSubmitting: boolean;
  onAmountInputChange: (variable: string, value: string) => void;
}> = ({ amountVariables, amountInputs, isSubmitting, onAmountInputChange }) => {
  if (amountVariables.length === 0) {
    return null;
  }

  return (
    <>
      {amountVariables.map((variable) => (
        <div className="journal-entry-form__field" key={variable}>
          <label className="journal-entry-form__label" htmlFor={`amount-${variable}`}>
            {variable}
            <span className="journal-entry-form__required"> *</span>
          </label>
          <input
            id={`amount-${variable}`}
            type="number"
            className="journal-entry-form__input"
            value={amountInputs[variable] ?? ''}
            onChange={(event) => onAmountInputChange(variable, event.target.value)}
            disabled={isSubmitting}
          />
        </div>
      ))}
    </>
  );
};

const AutoJournalDialogForm: React.FC<AutoJournalDialogFormProps> = ({
  activePatterns,
  selectedPatternId,
  selectedPattern,
  amountVariables,
  amountInputs,
  journalDate,
  description,
  submitError,
  patternError,
  isSubmitting,
  onSelectPattern,
  onAmountInputChange,
  onJournalDateChange,
  onDescriptionChange,
  onGenerate,
  onClose,
}) => {
  return (
    <>
      {patternError && <div className="journal-entry-form__error">{patternError}</div>}

      <div className="journal-entry-form__field">
        <label className="journal-entry-form__label" htmlFor="auto-journal-pattern">
          自動仕訳パターン
          <span className="journal-entry-form__required"> *</span>
        </label>
        <select
          id="auto-journal-pattern"
          className="journal-entry-form__select"
          value={selectedPatternId}
          onChange={(event) => onSelectPattern(event.target.value)}
          disabled={isSubmitting}
        >
          <option value="">選択してください</option>
          {activePatterns.map((pattern) => (
            <option key={pattern.patternId} value={pattern.patternId}>
              {pattern.patternCode} - {pattern.patternName}
            </option>
          ))}
        </select>
      </div>

      <PatternItemsTable selectedPattern={selectedPattern} />

      <AmountInputFields
        amountVariables={amountVariables}
        amountInputs={amountInputs}
        isSubmitting={isSubmitting}
        onAmountInputChange={onAmountInputChange}
      />

      <div className="journal-entry-form__field">
        <label className="journal-entry-form__label" htmlFor="auto-journal-date">
          仕訳日
          <span className="journal-entry-form__required"> *</span>
        </label>
        <input
          id="auto-journal-date"
          type="date"
          className="journal-entry-form__input"
          value={journalDate}
          onChange={(event) => onJournalDateChange(event.target.value)}
          disabled={isSubmitting}
        />
      </div>

      <div className="journal-entry-form__field">
        <label className="journal-entry-form__label" htmlFor="auto-journal-description">
          摘要
        </label>
        <input
          id="auto-journal-description"
          type="text"
          className="journal-entry-form__input"
          value={description}
          onChange={(event) => onDescriptionChange(event.target.value)}
          disabled={isSubmitting}
        />
      </div>

      {submitError && <div className="journal-entry-form__error">{submitError}</div>}

      <div className="journal-entry-form__footer" style={{ marginTop: '24px' }}>
        <Button type="button" variant="primary" onClick={onGenerate} disabled={isSubmitting}>
          {isSubmitting ? '生成中...' : '生成'}
        </Button>
        <Button type="button" variant="text" onClick={onClose} disabled={isSubmitting}>
          キャンセル
        </Button>
      </div>
    </>
  );
};

export const AutoJournalGenerateDialog: React.FC<AutoJournalGenerateDialogProps> = ({
  isOpen,
  onClose,
  onSuccess,
}) => {
  const [patterns, setPatterns] = useState<AutoJournalPattern[]>([]);
  const [isLoading, setIsLoading] = useState(false);
  const [isSubmitting, setIsSubmitting] = useState(false);
  const [patternError, setPatternError] = useState<string | null>(null);
  const [submitError, setSubmitError] = useState<string | null>(null);
  const [selectedPatternId, setSelectedPatternId] = useState<string>('');
  const [amountInputs, setAmountInputs] = useState<Record<string, string>>({});
  const [journalDate, setJournalDate] = useState<string>(createInitialJournalDate);
  const [description, setDescription] = useState<string>('');

  const activePatterns = useMemo(() => patterns.filter((pattern) => pattern.isActive), [patterns]);

  const selectedPattern = useMemo(
    () => activePatterns.find((pattern) => pattern.patternId === Number(selectedPatternId)) ?? null,
    [activePatterns, selectedPatternId]
  );

  const amountVariables = useMemo(() => extractAmountVariables(selectedPattern), [selectedPattern]);

  useEffect(() => {
    if (!isOpen) {
      return;
    }

    setSelectedPatternId('');
    setAmountInputs({});
    setJournalDate(createInitialJournalDate());
    setDescription('');
    setPatternError(null);
    setSubmitError(null);
    setIsLoading(true);

    const fetchPatterns = async () => {
      try {
        const data = await getAutoJournalPatterns();
        setPatterns(data);
      } catch (error) {
        setPatternError(getAutoJournalPatternsErrorMessage(error));
      } finally {
        setIsLoading(false);
      }
    };

    void fetchPatterns();
  }, [isOpen]);

  useEffect(() => {
    setAmountInputs((prev) =>
      amountVariables.reduce<Record<string, string>>((next, key) => {
        next[key] = prev[key] ?? '';
        return next;
      }, {})
    );
  }, [amountVariables]);

  const handleOverlayClick = useCallback(
    (event: React.MouseEvent<HTMLDivElement>) => {
      if (event.currentTarget === event.target) {
        onClose();
      }
    },
    [onClose]
  );

  const handleAmountInputChange = useCallback((variable: string, value: string) => {
    setAmountInputs((prev) => ({ ...prev, [variable]: value }));
  }, []);

  const handleGenerate = useCallback(async () => {
    const generateRequest = buildGenerateRequest({
      selectedPattern,
      journalDate,
      description,
      amountVariables,
      amountInputs,
    });

    if (!generateRequest.request || generateRequest.error) {
      setSubmitError(generateRequest.error);
      return;
    }

    setSubmitError(null);
    setIsSubmitting(true);

    try {
      const response = await generateAutoJournal(generateRequest.request);
      const journalEntryId = resolveJournalEntryId(response);
      onClose();
      onSuccess(journalEntryId);
    } catch (error) {
      setSubmitError(generateAutoJournalErrorMessage(error));
    } finally {
      setIsSubmitting(false);
    }
  }, [
    amountInputs,
    amountVariables,
    description,
    journalDate,
    onClose,
    onSuccess,
    selectedPattern,
  ]);

  if (!isOpen) {
    return null;
  }

  return (
    <div
      className="auto-journal-dialog-overlay"
      style={overlayStyle}
      onClick={handleOverlayClick}
      role="presentation"
    >
      <div className="auto-journal-dialog" style={dialogStyle}>
        <div
          style={{
            display: 'flex',
            justifyContent: 'space-between',
            alignItems: 'center',
            marginBottom: '16px',
          }}
        >
          <h2 style={{ margin: 0 }}>自動仕訳生成</h2>
          <button
            type="button"
            style={closeButtonStyle}
            onClick={onClose}
            aria-label="閉じる"
            disabled={isSubmitting}
          >
            ×
          </button>
        </div>

        {isLoading ? (
          <Loading message="自動仕訳パターンを読み込み中..." />
        ) : (
          <AutoJournalDialogForm
            activePatterns={activePatterns}
            selectedPatternId={selectedPatternId}
            selectedPattern={selectedPattern}
            amountVariables={amountVariables}
            amountInputs={amountInputs}
            journalDate={journalDate}
            description={description}
            submitError={submitError}
            patternError={patternError}
            isSubmitting={isSubmitting}
            onSelectPattern={setSelectedPatternId}
            onAmountInputChange={handleAmountInputChange}
            onJournalDateChange={setJournalDate}
            onDescriptionChange={setDescription}
            onGenerate={() => void handleGenerate()}
            onClose={onClose}
          />
        )}
      </div>
    </div>
  );
};
