import React, { type ChangeEvent, type FormEvent, useState } from 'react';
import {
  createAutoJournalPattern,
  getCreateAutoJournalPatternErrorMessage,
  type CreateAutoJournalPatternRequest,
  type CreatePatternItemRequest,
} from '../../api/createAutoJournalPattern';
import { SuccessNotification } from '../common/SuccessNotification';
import './CreateAutoJournalPatternForm.css';

interface ItemError {
  debitCreditType?: string;
  accountCode?: string;
  amountFormula?: string;
}

interface FormErrors {
  patternCode?: string;
  patternName?: string;
  sourceTableName?: string;
  items?: string;
  itemErrors: ItemError[];
}

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

const createInitialErrors = (count: number): FormErrors => ({
  itemErrors: Array.from({ length: count }, () => ({})),
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
    Boolean(errors.patternCode) ||
    Boolean(errors.patternName) ||
    Boolean(errors.sourceTableName) ||
    Boolean(errors.items) ||
    errors.itemErrors.some((itemError) => Object.values(itemError).some(Boolean))
  );
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

const inputClassName = (base: string, hasError: boolean): string =>
  hasError ? `${base} is-error` : base;
const safeText = (value?: string): string => value ?? '';

interface ItemRowProps {
  index: number;
  item: CreatePatternItemRequest;
  error: ItemError;
  isSubmitting: boolean;
  removeDisabled: boolean;
  onItemChange: (
    index: number,
    field: keyof CreatePatternItemRequest,
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
    <div className="create-auto-journal-pattern-form__item-row" data-testid={`item-row-${index}`}>
      <div className="create-auto-journal-pattern-form__item-grid">
        <div className="create-auto-journal-pattern-form__field">
          <label className="create-auto-journal-pattern-form__label">行番号</label>
          <input
            type="number"
            className="create-auto-journal-pattern-form__input"
            value={item.lineNumber}
            disabled
          />
        </div>

        <div className="create-auto-journal-pattern-form__field">
          <label className="create-auto-journal-pattern-form__label">
            借方/貸方 <span className="create-auto-journal-pattern-form__required">*</span>
          </label>
          <select
            className={inputClassName(
              'create-auto-journal-pattern-form__input',
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
          <span className="create-auto-journal-pattern-form__field-error">
            {error.debitCreditType || ''}
          </span>
        </div>

        <div className="create-auto-journal-pattern-form__field">
          <label className="create-auto-journal-pattern-form__label">
            勘定科目コード <span className="create-auto-journal-pattern-form__required">*</span>
          </label>
          <input
            type="text"
            className={inputClassName(
              'create-auto-journal-pattern-form__input',
              Boolean(error.accountCode)
            )}
            value={item.accountCode}
            onChange={(event) => onItemChange(index, 'accountCode', event.target.value)}
            disabled={isSubmitting}
          />
          <span className="create-auto-journal-pattern-form__field-error">
            {error.accountCode || ''}
          </span>
        </div>

        <div className="create-auto-journal-pattern-form__field">
          <label className="create-auto-journal-pattern-form__label">
            金額計算式 <span className="create-auto-journal-pattern-form__required">*</span>
          </label>
          <input
            type="text"
            className={inputClassName(
              'create-auto-journal-pattern-form__input',
              Boolean(error.amountFormula)
            )}
            value={item.amountFormula}
            onChange={(event) => onItemChange(index, 'amountFormula', event.target.value)}
            disabled={isSubmitting}
          />
          <span className="create-auto-journal-pattern-form__field-error">
            {error.amountFormula || ''}
          </span>
        </div>

        <div className="create-auto-journal-pattern-form__field">
          <label className="create-auto-journal-pattern-form__label">説明テンプレート</label>
          <input
            type="text"
            className="create-auto-journal-pattern-form__input"
            value={safeText(item.descriptionTemplate)}
            onChange={(event) => onItemChange(index, 'descriptionTemplate', event.target.value)}
            disabled={isSubmitting}
          />
        </div>
      </div>
      <button
        type="button"
        className="create-auto-journal-pattern-form__remove-item"
        onClick={() => onRemove(index)}
        disabled={removeDisabled}
      >
        削除
      </button>
    </div>
  );
};

interface NotificationsProps {
  submitError: string | null;
  successMessage: string | null;
  onDismissSuccess: () => void;
}

const Notifications: React.FC<NotificationsProps> = ({
  submitError,
  successMessage,
  onDismissSuccess,
}) => {
  return (
    <>
      {submitError ? (
        <div
          className="create-auto-journal-pattern-form__error"
          role="alert"
          data-testid="create-auto-journal-pattern-error"
        >
          <span className="create-auto-journal-pattern-form__error-text">{submitError}</span>
        </div>
      ) : null}

      {successMessage ? (
        <div className="create-auto-journal-pattern-form__success">
          <SuccessNotification message={successMessage} onDismiss={onDismissSuccess} />
        </div>
      ) : null}
    </>
  );
};

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

  const handleItemChange = (
    index: number,
    field: keyof CreatePatternItemRequest,
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
      items: [...prev.items, createEmptyItem(prev.items.length + 1)],
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
    setSuccessMessage(null);

    const validationErrors = validateFormData(formData);
    setErrors(validationErrors);
    if (hasErrors(validationErrors)) return;

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
      className="create-auto-journal-pattern-form"
      onSubmit={handleSubmit}
      data-testid="create-auto-journal-pattern-form"
      noValidate
    >
      <Notifications
        submitError={submitError}
        successMessage={successMessage}
        onDismissSuccess={() => setSuccessMessage(null)}
      />

      <div className="create-auto-journal-pattern-form__field">
        <label htmlFor="patternCode" className="create-auto-journal-pattern-form__label">
          パターンコード <span className="create-auto-journal-pattern-form__required">*</span>
        </label>
        <input
          id="patternCode"
          name="patternCode"
          type="text"
          className={inputClassName(
            'create-auto-journal-pattern-form__input',
            Boolean(errors.patternCode)
          )}
          value={formData.patternCode}
          onChange={handleFieldChange}
          disabled={isSubmitting}
          data-testid="pattern-code-input"
        />
        <span className="create-auto-journal-pattern-form__field-error">
          {errors.patternCode || ''}
        </span>
      </div>

      <div className="create-auto-journal-pattern-form__field">
        <label htmlFor="patternName" className="create-auto-journal-pattern-form__label">
          パターン名 <span className="create-auto-journal-pattern-form__required">*</span>
        </label>
        <input
          id="patternName"
          name="patternName"
          type="text"
          className={inputClassName(
            'create-auto-journal-pattern-form__input',
            Boolean(errors.patternName)
          )}
          value={formData.patternName}
          onChange={handleFieldChange}
          disabled={isSubmitting}
          data-testid="pattern-name-input"
        />
        <span className="create-auto-journal-pattern-form__field-error">
          {errors.patternName || ''}
        </span>
      </div>

      <div className="create-auto-journal-pattern-form__field">
        <label htmlFor="sourceTableName" className="create-auto-journal-pattern-form__label">
          ソーステーブル名 <span className="create-auto-journal-pattern-form__required">*</span>
        </label>
        <input
          id="sourceTableName"
          name="sourceTableName"
          type="text"
          className={inputClassName(
            'create-auto-journal-pattern-form__input',
            Boolean(errors.sourceTableName)
          )}
          value={formData.sourceTableName}
          onChange={handleFieldChange}
          disabled={isSubmitting}
          data-testid="source-table-input"
        />
        <span className="create-auto-journal-pattern-form__field-error">
          {errors.sourceTableName || ''}
        </span>
      </div>

      <div className="create-auto-journal-pattern-form__field">
        <label htmlFor="description" className="create-auto-journal-pattern-form__label">
          説明
        </label>
        <textarea
          id="description"
          name="description"
          className="create-auto-journal-pattern-form__textarea"
          value={safeText(formData.description)}
          onChange={handleFieldChange}
          disabled={isSubmitting}
          rows={3}
          data-testid="description-input"
        />
      </div>

      <div className="create-auto-journal-pattern-form__items-header">
        <h2 className="create-auto-journal-pattern-form__items-title">仕訳明細</h2>
        <button
          type="button"
          className="create-auto-journal-pattern-form__add-item"
          onClick={handleAddItem}
          disabled={isSubmitting}
          data-testid="add-item-button"
        >
          行追加
        </button>
      </div>

      <span className="create-auto-journal-pattern-form__field-error">
        {safeText(errors.items)}
      </span>

      <div className="create-auto-journal-pattern-form__items">
        {formData.items.map((item, index) => (
          <ItemRow
            key={`create-pattern-item-${index}`}
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
        className="create-auto-journal-pattern-form__submit"
        disabled={isSubmitting}
        data-testid="create-pattern-submit"
      >
        {submitLabel}
      </button>
    </form>
  );
};
