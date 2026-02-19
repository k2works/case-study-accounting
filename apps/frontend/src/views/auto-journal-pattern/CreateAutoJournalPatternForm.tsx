import React, { type ChangeEvent, type FormEvent, useState } from 'react';
import {
  createAutoJournalPattern,
  getCreateAutoJournalPatternErrorMessage,
  type CreateAutoJournalPatternRequest,
  type CreatePatternItemRequest,
} from '../../api/createAutoJournalPattern';
import { SuccessNotification } from '../common/SuccessNotification';
import {
  type FormErrors,
  createInitialErrors,
  inputClassName,
  safeText,
  hasFormErrors,
  validateItemErrors,
  PatternItemRow,
} from './PatternFormCommon';
import './CreateAutoJournalPatternForm.css';

const CSS_PREFIX = 'create-auto-journal-pattern-form';

const createEmptyItem = (lineNumber: number): CreatePatternItemRequest => ({
  lineNumber,
  debitCreditType: '',
  accountCode: '',
  amountFormula: '',
  descriptionTemplate: '',
});

const createInitialFormData = (): CreateAutoJournalPatternRequest => ({
  patternCode: '',
  patternName: '',
  sourceTableName: '',
  description: '',
  items: [createEmptyItem(1)],
});

const normalizeItems = (items: CreatePatternItemRequest[]): CreatePatternItemRequest[] => {
  return items.length > 0 ? items : [createEmptyItem(1)];
};

const validateFormData = (formData: CreateAutoJournalPatternRequest): FormErrors => {
  const errors: FormErrors = createInitialErrors(formData.items.length);

  if (!formData.patternCode.trim()) errors.patternCode = 'パターンコードを入力してください';
  if (!formData.patternName.trim()) errors.patternName = 'パターン名を入力してください';
  if (!formData.sourceTableName.trim())
    errors.sourceTableName = 'ソーステーブル名を入力してください';
  if (formData.items.length === 0) errors.items = '明細行を 1 件以上入力してください';

  validateItemErrors(formData.items, errors);
  return errors;
};

const getSanitizedPayload = (
  formData: CreateAutoJournalPatternRequest
): CreateAutoJournalPatternRequest => ({
  patternCode: formData.patternCode.trim(),
  patternName: formData.patternName.trim(),
  sourceTableName: formData.sourceTableName.trim(),
  description: formData.description?.trim() || undefined,
  items: formData.items.map((item, index) => ({
    lineNumber: index + 1,
    debitCreditType: item.debitCreditType,
    accountCode: item.accountCode.trim(),
    amountFormula: item.amountFormula.trim(),
    descriptionTemplate: item.descriptionTemplate?.trim() || undefined,
  })),
});

export const CreateAutoJournalPatternForm: React.FC = () => {
  const [formData, setFormData] = useState<CreateAutoJournalPatternRequest>(createInitialFormData);
  const [errors, setErrors] = useState<FormErrors>(createInitialErrors(1));
  const [isSubmitting, setIsSubmitting] = useState(false);
  const [submitError, setSubmitError] = useState<string | null>(null);
  const [successMessage, setSuccessMessage] = useState<string | null>(null);
  const submitLabel = isSubmitting ? '登録中...' : '登録';

  const handleFieldChange = (event: ChangeEvent<HTMLInputElement | HTMLTextAreaElement>) => {
    const { name, value } = event.target;
    setFormData((prev) => ({ ...prev, [name]: value }));
    setErrors((prev) => ({ ...prev, [name]: undefined }));
  };

  const handleItemChange = (index: number, field: string, value: string | number) => {
    setFormData((prev) => ({
      ...prev,
      items: prev.items.map((item, i) => (i === index ? { ...item, [field]: value } : item)),
    }));
    setErrors((prev) => ({
      ...prev,
      items: undefined,
      itemErrors: prev.itemErrors.map((e, i) => (i === index ? { ...e, [field]: undefined } : e)),
    }));
  };

  const handleAddItem = () => {
    setFormData((prev) => ({
      ...prev,
      items: [...prev.items, createEmptyItem(prev.items.length + 1)],
    }));
    setErrors((prev) => ({ ...prev, items: undefined, itemErrors: [...prev.itemErrors, {}] }));
  };

  const handleRemoveItem = (index: number) => {
    setFormData((prev) => {
      const nextItems = normalizeItems(
        prev.items.filter((_, i) => i !== index).map((item, i) => ({ ...item, lineNumber: i + 1 }))
      );
      return { ...prev, items: nextItems };
    });
    setErrors((prev) => {
      const nextErrors = prev.itemErrors.filter((_, i) => i !== index);
      return { ...prev, itemErrors: nextErrors.length > 0 ? nextErrors : [{}] };
    });
  };

  const handleSubmit = async (event: FormEvent<HTMLFormElement>) => {
    event.preventDefault();
    setSubmitError(null);
    setSuccessMessage(null);

    const validationErrors = validateFormData(formData);
    setErrors(validationErrors);
    if (hasFormErrors(validationErrors)) return;

    setIsSubmitting(true);
    try {
      const response = await createAutoJournalPattern(getSanitizedPayload(formData));
      if (!response.success) {
        throw new Error(response.errorMessage || '自動仕訳パターン登録に失敗しました');
      }
      setSuccessMessage('自動仕訳パターン登録が完了しました');
      setFormData(createInitialFormData());
      setErrors(createInitialErrors(1));
    } catch (error) {
      setSubmitError(getCreateAutoJournalPatternErrorMessage(error));
    } finally {
      setIsSubmitting(false);
    }
  };

  return (
    <form
      className={CSS_PREFIX}
      onSubmit={handleSubmit}
      data-testid="create-auto-journal-pattern-form"
      noValidate
    >
      {submitError && (
        <div
          className={`${CSS_PREFIX}__error`}
          role="alert"
          data-testid="create-auto-journal-pattern-error"
        >
          <span className={`${CSS_PREFIX}__error-text`}>{submitError}</span>
        </div>
      )}
      {successMessage && (
        <div className={`${CSS_PREFIX}__success`}>
          <SuccessNotification message={successMessage} onDismiss={() => setSuccessMessage(null)} />
        </div>
      )}

      <div className={`${CSS_PREFIX}__field`}>
        <label htmlFor="patternCode" className={`${CSS_PREFIX}__label`}>
          パターンコード <span className={`${CSS_PREFIX}__required`}>*</span>
        </label>
        <input
          id="patternCode"
          name="patternCode"
          type="text"
          className={inputClassName(`${CSS_PREFIX}__input`, Boolean(errors.patternCode))}
          value={formData.patternCode}
          onChange={handleFieldChange}
          disabled={isSubmitting}
          data-testid="pattern-code-input"
        />
        <span className={`${CSS_PREFIX}__field-error`}>{errors.patternCode || ''}</span>
      </div>

      <div className={`${CSS_PREFIX}__field`}>
        <label htmlFor="patternName" className={`${CSS_PREFIX}__label`}>
          パターン名 <span className={`${CSS_PREFIX}__required`}>*</span>
        </label>
        <input
          id="patternName"
          name="patternName"
          type="text"
          className={inputClassName(`${CSS_PREFIX}__input`, Boolean(errors.patternName))}
          value={formData.patternName}
          onChange={handleFieldChange}
          disabled={isSubmitting}
          data-testid="pattern-name-input"
        />
        <span className={`${CSS_PREFIX}__field-error`}>{errors.patternName || ''}</span>
      </div>

      <div className={`${CSS_PREFIX}__field`}>
        <label htmlFor="sourceTableName" className={`${CSS_PREFIX}__label`}>
          ソーステーブル名 <span className={`${CSS_PREFIX}__required`}>*</span>
        </label>
        <input
          id="sourceTableName"
          name="sourceTableName"
          type="text"
          className={inputClassName(`${CSS_PREFIX}__input`, Boolean(errors.sourceTableName))}
          value={formData.sourceTableName}
          onChange={handleFieldChange}
          disabled={isSubmitting}
          data-testid="source-table-input"
        />
        <span className={`${CSS_PREFIX}__field-error`}>{errors.sourceTableName || ''}</span>
      </div>

      <div className={`${CSS_PREFIX}__field`}>
        <label htmlFor="description" className={`${CSS_PREFIX}__label`}>
          説明
        </label>
        <textarea
          id="description"
          name="description"
          className={`${CSS_PREFIX}__textarea`}
          value={safeText(formData.description)}
          onChange={handleFieldChange}
          disabled={isSubmitting}
          rows={3}
          data-testid="description-input"
        />
      </div>

      <div className={`${CSS_PREFIX}__items-header`}>
        <h2 className={`${CSS_PREFIX}__items-title`}>仕訳明細</h2>
        <button
          type="button"
          className={`${CSS_PREFIX}__add-item`}
          onClick={handleAddItem}
          disabled={isSubmitting}
          data-testid="add-item-button"
        >
          行追加
        </button>
      </div>

      <span className={`${CSS_PREFIX}__field-error`}>{safeText(errors.items)}</span>

      <div className={`${CSS_PREFIX}__items`}>
        {formData.items.map((item, index) => (
          <PatternItemRow
            key={`create-pattern-item-${index}`}
            index={index}
            item={item}
            error={errors.itemErrors[index] ?? {}}
            isSubmitting={isSubmitting}
            removeDisabled={isSubmitting || formData.items.length <= 1}
            cssPrefix={CSS_PREFIX}
            onItemChange={handleItemChange}
            onRemove={handleRemoveItem}
          />
        ))}
      </div>

      <button
        type="submit"
        className={`${CSS_PREFIX}__submit`}
        disabled={isSubmitting}
        data-testid="create-pattern-submit"
      >
        {submitLabel}
      </button>
    </form>
  );
};
