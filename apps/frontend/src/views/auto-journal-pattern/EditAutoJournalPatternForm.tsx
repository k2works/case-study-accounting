import React, { type ChangeEvent, type FormEvent, useState } from 'react';
import type { AutoJournalPattern } from '../../api/getAutoJournalPatterns';
import {
  getUpdateAutoJournalPatternErrorMessage,
  updateAutoJournalPattern,
  type UpdateAutoJournalPatternRequest,
  type UpdatePatternItemRequest,
} from '../../api/updateAutoJournalPattern';
import './EditAutoJournalPatternForm.css';

interface EditAutoJournalPatternFormProps {
  pattern: AutoJournalPattern;
  onSuccess: (message: string) => void;
}

interface ItemError {
  debitCreditType?: string;
  accountCode?: string;
  amountFormula?: string;
}

interface FormErrors {
  patternName?: string;
  sourceTableName?: string;
  items?: string;
  itemErrors: ItemError[];
}

const createDefaultItem = (lineNumber: number): UpdatePatternItemRequest => ({
  lineNumber,
  debitCreditType: '',
  accountCode: '',
  amountFormula: '',
  descriptionTemplate: '',
});

const createFormData = (pattern: AutoJournalPattern): UpdateAutoJournalPatternRequest => ({
  patternName: pattern.patternName,
  sourceTableName: pattern.sourceTableName,
  description: pattern.description ?? '',
  isActive: pattern.isActive,
  items:
    pattern.items.length > 0
      ? pattern.items.map((item, index) => ({
          lineNumber: index + 1,
          debitCreditType: item.debitCreditType,
          accountCode: item.accountCode,
          amountFormula: item.amountFormula,
          descriptionTemplate: item.descriptionTemplate ?? '',
        }))
      : [createDefaultItem(1)],
});

const createInitialErrors = (count: number): FormErrors => ({
  itemErrors: Array.from({ length: count }, () => ({})),
});

const normalizeItems = (items: UpdatePatternItemRequest[]): UpdatePatternItemRequest[] => {
  return items.length > 0 ? items : [createDefaultItem(1)];
};

const validateFormData = (formData: UpdateAutoJournalPatternRequest): FormErrors => {
  const errors: FormErrors = createInitialErrors(formData.items.length);

  if (!formData.patternName.trim()) errors.patternName = 'パターン名を入力してください';
  if (!formData.sourceTableName.trim())
    errors.sourceTableName = 'ソーステーブル名を入力してください';
  if (formData.items.length === 0) errors.items = '明細行を 1 件以上入力してください';

  formData.items.forEach((item, index) => {
    if (!item.debitCreditType)
      errors.itemErrors[index].debitCreditType = '借方/貸方を選択してください';
    if (!item.accountCode.trim())
      errors.itemErrors[index].accountCode = '勘定科目コードを入力してください';
    if (!item.amountFormula.trim())
      errors.itemErrors[index].amountFormula = '金額計算式を入力してください';
  });

  return errors;
};

const hasErrors = (errors: FormErrors): boolean => {
  return (
    Boolean(errors.patternName) ||
    Boolean(errors.sourceTableName) ||
    Boolean(errors.items) ||
    errors.itemErrors.some((itemError) => Object.values(itemError).some(Boolean))
  );
};

const getSanitizedPayload = (
  formData: UpdateAutoJournalPatternRequest
): UpdateAutoJournalPatternRequest => ({
  patternName: formData.patternName.trim(),
  sourceTableName: formData.sourceTableName.trim(),
  description: formData.description?.trim() || undefined,
  isActive: formData.isActive,
  items: formData.items.map((item, index) => ({
    lineNumber: index + 1,
    debitCreditType: item.debitCreditType,
    accountCode: item.accountCode.trim(),
    amountFormula: item.amountFormula.trim(),
    descriptionTemplate: item.descriptionTemplate?.trim() || undefined,
  })),
});

const inputClassName = (base: string, hasError: boolean): string =>
  hasError ? `${base} is-error` : base;
const safeText = (value?: string): string => value ?? '';

interface ItemRowProps {
  index: number;
  item: UpdatePatternItemRequest;
  error: ItemError;
  isSubmitting: boolean;
  removeDisabled: boolean;
  onItemChange: (
    index: number,
    field: keyof UpdatePatternItemRequest,
    value: string | number
  ) => void;
  onRemove: (index: number) => void;
}

const ItemRow: React.FC<ItemRowProps> = ({
  index,
  item,
  error,
  isSubmitting,
  removeDisabled,
  onItemChange,
  onRemove,
}) => {
  return (
    <div className="edit-auto-journal-pattern-form__item-row" data-testid={`item-row-${index}`}>
      <div className="edit-auto-journal-pattern-form__item-grid">
        <div className="edit-auto-journal-pattern-form__field">
          <label className="edit-auto-journal-pattern-form__label">行番号</label>
          <input
            type="number"
            className="edit-auto-journal-pattern-form__input"
            value={item.lineNumber}
            disabled
          />
        </div>

        <div className="edit-auto-journal-pattern-form__field">
          <label className="edit-auto-journal-pattern-form__label">
            借方/貸方 <span className="edit-auto-journal-pattern-form__required">*</span>
          </label>
          <select
            className={inputClassName(
              'edit-auto-journal-pattern-form__input',
              Boolean(error.debitCreditType)
            )}
            value={item.debitCreditType}
            onChange={(event) => onItemChange(index, 'debitCreditType', event.target.value)}
            disabled={isSubmitting}
          >
            <option value="">選択してください</option>
            <option value="DEBIT">借方 (D)</option>
            <option value="CREDIT">貸方 (C)</option>
          </select>
          <span className="edit-auto-journal-pattern-form__field-error">
            {error.debitCreditType || ''}
          </span>
        </div>

        <div className="edit-auto-journal-pattern-form__field">
          <label className="edit-auto-journal-pattern-form__label">
            勘定科目コード <span className="edit-auto-journal-pattern-form__required">*</span>
          </label>
          <input
            type="text"
            className={inputClassName(
              'edit-auto-journal-pattern-form__input',
              Boolean(error.accountCode)
            )}
            value={item.accountCode}
            onChange={(event) => onItemChange(index, 'accountCode', event.target.value)}
            disabled={isSubmitting}
          />
          <span className="edit-auto-journal-pattern-form__field-error">
            {error.accountCode || ''}
          </span>
        </div>

        <div className="edit-auto-journal-pattern-form__field">
          <label className="edit-auto-journal-pattern-form__label">
            金額計算式 <span className="edit-auto-journal-pattern-form__required">*</span>
          </label>
          <input
            type="text"
            className={inputClassName(
              'edit-auto-journal-pattern-form__input',
              Boolean(error.amountFormula)
            )}
            value={item.amountFormula}
            onChange={(event) => onItemChange(index, 'amountFormula', event.target.value)}
            disabled={isSubmitting}
          />
          <span className="edit-auto-journal-pattern-form__field-error">
            {error.amountFormula || ''}
          </span>
        </div>

        <div className="edit-auto-journal-pattern-form__field">
          <label className="edit-auto-journal-pattern-form__label">説明テンプレート</label>
          <input
            type="text"
            className="edit-auto-journal-pattern-form__input"
            value={safeText(item.descriptionTemplate)}
            onChange={(event) => onItemChange(index, 'descriptionTemplate', event.target.value)}
            disabled={isSubmitting}
          />
        </div>
      </div>
      <button
        type="button"
        className="edit-auto-journal-pattern-form__remove-item"
        onClick={() => onRemove(index)}
        disabled={removeDisabled}
      >
        削除
      </button>
    </div>
  );
};

const ErrorNotification: React.FC<{ message: string | null }> = ({ message }) => {
  return message ? (
    <div
      className="edit-auto-journal-pattern-form__error"
      role="alert"
      data-testid="edit-auto-journal-pattern-error"
    >
      <span className="edit-auto-journal-pattern-form__error-text">{message}</span>
    </div>
  ) : null;
};

export const EditAutoJournalPatternForm: React.FC<EditAutoJournalPatternFormProps> = ({
  pattern,
  onSuccess,
}) => {
  const [formData, setFormData] = useState<UpdateAutoJournalPatternRequest>(
    createFormData(pattern)
  );
  const [errors, setErrors] = useState<FormErrors>(createInitialErrors(pattern.items.length || 1));
  const [isSubmitting, setIsSubmitting] = useState(false);
  const [submitError, setSubmitError] = useState<string | null>(null);
  const submitLabel = isSubmitting ? '更新中...' : '更新';

  const handleFieldChange = (event: ChangeEvent<HTMLInputElement | HTMLTextAreaElement>) => {
    const { name, value } = event.target;
    setFormData((prev) => ({ ...prev, [name]: value }));
    setErrors((prev) => ({ ...prev, [name]: undefined }));
  };

  const handleIsActiveChange = (event: ChangeEvent<HTMLInputElement>) => {
    setFormData((prev) => ({ ...prev, isActive: event.target.checked }));
  };

  const handleItemChange = (
    index: number,
    field: keyof UpdatePatternItemRequest,
    value: string | number
  ) => {
    setFormData((prev) => ({
      ...prev,
      items: prev.items.map((item, itemIndex) =>
        itemIndex === index ? { ...item, [field]: value } : item
      ),
    }));

    setErrors((prev) => ({
      ...prev,
      items: undefined,
      itemErrors: prev.itemErrors.map((itemError, itemIndex) =>
        itemIndex === index ? { ...itemError, [field]: undefined } : itemError
      ),
    }));
  };

  const handleAddItem = () => {
    setFormData((prev) => ({
      ...prev,
      items: [...prev.items, createDefaultItem(prev.items.length + 1)],
    }));
    setErrors((prev) => ({ ...prev, items: undefined, itemErrors: [...prev.itemErrors, {}] }));
  };

  const handleRemoveItem = (index: number) => {
    setFormData((prev) => {
      const nextItems = normalizeItems(
        prev.items
          .filter((_, itemIndex) => itemIndex !== index)
          .map((item, itemIndex) => ({ ...item, lineNumber: itemIndex + 1 }))
      );
      return { ...prev, items: nextItems };
    });

    setErrors((prev) => {
      const nextErrors = prev.itemErrors.filter((_, itemIndex) => itemIndex !== index);
      return { ...prev, itemErrors: nextErrors.length > 0 ? nextErrors : [{}] };
    });
  };

  const handleSubmit = async (event: FormEvent<HTMLFormElement>) => {
    event.preventDefault();
    setSubmitError(null);

    const validationErrors = validateFormData(formData);
    setErrors(validationErrors);
    if (hasErrors(validationErrors)) return;

    setIsSubmitting(true);

    try {
      const response = await updateAutoJournalPattern(
        pattern.patternId,
        getSanitizedPayload(formData)
      );
      if (!response.success) {
        throw new Error(response.errorMessage || '自動仕訳パターン更新に失敗しました');
      }
      onSuccess(response.message || '自動仕訳パターンを更新しました');
    } catch (error) {
      setSubmitError(getUpdateAutoJournalPatternErrorMessage(error));
    } finally {
      setIsSubmitting(false);
    }
  };

  return (
    <form
      className="edit-auto-journal-pattern-form"
      onSubmit={handleSubmit}
      data-testid="edit-auto-journal-pattern-form"
      noValidate
    >
      <ErrorNotification message={submitError} />

      <div className="edit-auto-journal-pattern-form__field">
        <label htmlFor="patternCode" className="edit-auto-journal-pattern-form__label">
          パターンコード
        </label>
        <input
          id="patternCode"
          name="patternCode"
          type="text"
          className="edit-auto-journal-pattern-form__input"
          value={pattern.patternCode}
          disabled
        />
      </div>

      <div className="edit-auto-journal-pattern-form__field">
        <label htmlFor="patternName" className="edit-auto-journal-pattern-form__label">
          パターン名 <span className="edit-auto-journal-pattern-form__required">*</span>
        </label>
        <input
          id="patternName"
          name="patternName"
          type="text"
          className={inputClassName(
            'edit-auto-journal-pattern-form__input',
            Boolean(errors.patternName)
          )}
          value={formData.patternName}
          onChange={handleFieldChange}
          disabled={isSubmitting}
        />
        <span className="edit-auto-journal-pattern-form__field-error">
          {errors.patternName || ''}
        </span>
      </div>

      <div className="edit-auto-journal-pattern-form__field">
        <label htmlFor="sourceTableName" className="edit-auto-journal-pattern-form__label">
          ソーステーブル名 <span className="edit-auto-journal-pattern-form__required">*</span>
        </label>
        <input
          id="sourceTableName"
          name="sourceTableName"
          type="text"
          className={inputClassName(
            'edit-auto-journal-pattern-form__input',
            Boolean(errors.sourceTableName)
          )}
          value={formData.sourceTableName}
          onChange={handleFieldChange}
          disabled={isSubmitting}
        />
        <span className="edit-auto-journal-pattern-form__field-error">
          {errors.sourceTableName || ''}
        </span>
      </div>

      <div className="edit-auto-journal-pattern-form__field">
        <label htmlFor="description" className="edit-auto-journal-pattern-form__label">
          説明
        </label>
        <textarea
          id="description"
          name="description"
          className="edit-auto-journal-pattern-form__textarea"
          value={safeText(formData.description)}
          onChange={handleFieldChange}
          disabled={isSubmitting}
          rows={3}
        />
      </div>

      <div className="edit-auto-journal-pattern-form__checkbox-field">
        <label htmlFor="isActive" className="edit-auto-journal-pattern-form__checkbox-label">
          <input
            id="isActive"
            type="checkbox"
            checked={formData.isActive}
            onChange={handleIsActiveChange}
            disabled={isSubmitting}
            data-testid="is-active-checkbox"
          />
          有効
        </label>
      </div>

      <div className="edit-auto-journal-pattern-form__items-header">
        <h2 className="edit-auto-journal-pattern-form__items-title">仕訳明細</h2>
        <button
          type="button"
          className="edit-auto-journal-pattern-form__add-item"
          onClick={handleAddItem}
          disabled={isSubmitting}
          data-testid="add-item-button"
        >
          行追加
        </button>
      </div>

      <span className="edit-auto-journal-pattern-form__field-error">{safeText(errors.items)}</span>

      <div className="edit-auto-journal-pattern-form__items">
        {formData.items.map((item, index) => (
          <ItemRow
            key={`edit-pattern-item-${index}`}
            index={index}
            item={item}
            error={errors.itemErrors[index] ?? {}}
            isSubmitting={isSubmitting}
            removeDisabled={isSubmitting || formData.items.length <= 1}
            onItemChange={handleItemChange}
            onRemove={handleRemoveItem}
          />
        ))}
      </div>

      <button
        type="submit"
        className="edit-auto-journal-pattern-form__submit"
        disabled={isSubmitting}
        data-testid="edit-pattern-submit"
      >
        {submitLabel}
      </button>
    </form>
  );
};
