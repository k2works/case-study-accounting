import React, { type ChangeEvent, type FormEvent } from 'react';

/* ---------- Types ---------- */

export interface ItemError {
  debitCreditType?: string;
  accountCode?: string;
  amountFormula?: string;
}

export interface FormErrors {
  patternCode?: string;
  patternName?: string;
  sourceTableName?: string;
  items?: string;
  itemErrors: ItemError[];
}

export interface PatternItemBase {
  lineNumber: number;
  debitCreditType: string;
  accountCode: string;
  amountFormula: string;
  descriptionTemplate?: string;
}

/* ---------- Utilities ---------- */

export const createInitialErrors = (count: number): FormErrors => ({
  itemErrors: Array.from({ length: count }, () => ({})),
});

export const inputClassName = (base: string, hasError: boolean): string =>
  hasError ? `${base} is-error` : base;

export const safeText = (value?: string): string => value ?? '';

export const hasFormErrors = (errors: FormErrors): boolean => {
  return (
    Boolean(errors.patternCode) ||
    Boolean(errors.patternName) ||
    Boolean(errors.sourceTableName) ||
    Boolean(errors.items) ||
    errors.itemErrors.some((itemError) => Object.values(itemError).some(Boolean))
  );
};

export const validateItemErrors = (
  items: { debitCreditType: string; accountCode: string; amountFormula: string }[],
  errors: FormErrors
): void => {
  items.forEach((item, index) => {
    if (!item.debitCreditType)
      errors.itemErrors[index].debitCreditType = '借方/貸方を選択してください';
    if (!item.accountCode.trim())
      errors.itemErrors[index].accountCode = '勘定科目コードを入力してください';
    if (!item.amountFormula.trim())
      errors.itemErrors[index].amountFormula = '金額計算式を入力してください';
  });
};

export const createEmptyItem = (lineNumber: number): PatternItemBase => ({
  lineNumber,
  debitCreditType: '',
  accountCode: '',
  amountFormula: '',
  descriptionTemplate: '',
});

export const normalizeItems = <T extends PatternItemBase>(items: T[]): T[] => {
  return items.length > 0 ? items : ([createEmptyItem(1)] as T[]);
};

export const sanitizePatternItems = (
  items: PatternItemBase[]
): {
  lineNumber: number;
  debitCreditType: string;
  accountCode: string;
  amountFormula: string;
  descriptionTemplate?: string;
}[] =>
  items.map((item, index) => ({
    lineNumber: index + 1,
    debitCreditType: item.debitCreditType,
    accountCode: item.accountCode.trim(),
    amountFormula: item.amountFormula.trim(),
    descriptionTemplate: item.descriptionTemplate?.trim() || undefined,
  }));

/* ---------- Shared Validation ---------- */

export const validateCommonFields = (formData: {
  patternName: string;
  sourceTableName: string;
  items: PatternItemBase[];
}): FormErrors => {
  const errors: FormErrors = createInitialErrors(formData.items.length);
  if (!formData.patternName.trim()) errors.patternName = 'パターン名を入力してください';
  if (!formData.sourceTableName.trim())
    errors.sourceTableName = 'ソーステーブル名を入力してください';
  if (formData.items.length === 0) errors.items = '明細行を 1 件以上入力してください';
  validateItemErrors(formData.items, errors);
  return errors;
};

/* ---------- Hook: usePatternFormHandlers ---------- */

interface FormState<T> {
  formData: T;
  setFormData: React.Dispatch<React.SetStateAction<T>>;
  errors: FormErrors;
  setErrors: React.Dispatch<React.SetStateAction<FormErrors>>;
  isSubmitting: boolean;
  setIsSubmitting: React.Dispatch<React.SetStateAction<boolean>>;
  submitError: string | null;
  setSubmitError: React.Dispatch<React.SetStateAction<string | null>>;
}

interface FormWithItems {
  items: PatternItemBase[];
}

export function usePatternFormHandlers<T extends FormWithItems>(
  state: FormState<T>,
  createEmpty: (lineNumber: number) => PatternItemBase,
  normalize: (items: PatternItemBase[]) => PatternItemBase[]
) {
  const { setFormData, setErrors } = state;

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
      items: [...prev.items, createEmpty(prev.items.length + 1)] as T['items'],
    }));
    setErrors((prev) => ({ ...prev, items: undefined, itemErrors: [...prev.itemErrors, {}] }));
  };

  const handleRemoveItem = (index: number) => {
    setFormData((prev) => {
      const nextItems = normalize(
        prev.items.filter((_, i) => i !== index).map((item, i) => ({ ...item, lineNumber: i + 1 }))
      );
      return { ...prev, items: nextItems as T['items'] };
    });
    setErrors((prev) => {
      const nextErrors = prev.itemErrors.filter((_, i) => i !== index);
      return { ...prev, itemErrors: nextErrors.length > 0 ? nextErrors : [{}] };
    });
  };

  return { handleFieldChange, handleItemChange, handleAddItem, handleRemoveItem };
}

/* ---------- Shared Form Fields ---------- */

interface PatternFieldsProps {
  cssPrefix: string;
  formData: { patternName: string; sourceTableName: string; description?: string };
  errors: FormErrors;
  isSubmitting: boolean;
  onChange: (event: ChangeEvent<HTMLInputElement | HTMLTextAreaElement>) => void;
}

export const PatternFormFields: React.FC<PatternFieldsProps> = ({
  cssPrefix,
  formData,
  errors,
  isSubmitting,
  onChange,
}) => (
  <>
    <div className={`${cssPrefix}__field`}>
      <label htmlFor="patternName" className={`${cssPrefix}__label`}>
        パターン名 <span className={`${cssPrefix}__required`}>*</span>
      </label>
      <input
        id="patternName"
        name="patternName"
        type="text"
        className={inputClassName(`${cssPrefix}__input`, Boolean(errors.patternName))}
        value={formData.patternName}
        onChange={onChange}
        disabled={isSubmitting}
        data-testid="pattern-name-input"
      />
      <span className={`${cssPrefix}__field-error`}>{errors.patternName || ''}</span>
    </div>

    <div className={`${cssPrefix}__field`}>
      <label htmlFor="sourceTableName" className={`${cssPrefix}__label`}>
        ソーステーブル名 <span className={`${cssPrefix}__required`}>*</span>
      </label>
      <input
        id="sourceTableName"
        name="sourceTableName"
        type="text"
        className={inputClassName(`${cssPrefix}__input`, Boolean(errors.sourceTableName))}
        value={formData.sourceTableName}
        onChange={onChange}
        disabled={isSubmitting}
        data-testid="source-table-input"
      />
      <span className={`${cssPrefix}__field-error`}>{errors.sourceTableName || ''}</span>
    </div>

    <div className={`${cssPrefix}__field`}>
      <label htmlFor="description" className={`${cssPrefix}__label`}>
        説明
      </label>
      <textarea
        id="description"
        name="description"
        className={`${cssPrefix}__textarea`}
        value={safeText(formData.description)}
        onChange={onChange}
        disabled={isSubmitting}
        rows={3}
        data-testid="description-input"
      />
    </div>
  </>
);

/* ---------- Items Section ---------- */

interface ItemsSectionProps {
  cssPrefix: string;
  items: PatternItemBase[];
  errors: FormErrors;
  isSubmitting: boolean;
  keyPrefix: string;
  onAddItem: () => void;
  onItemChange: (index: number, field: string, value: string | number) => void;
  onRemoveItem: (index: number) => void;
}

export const PatternItemsSection: React.FC<ItemsSectionProps> = ({
  cssPrefix,
  items,
  errors,
  isSubmitting,
  keyPrefix,
  onAddItem,
  onItemChange,
  onRemoveItem,
}) => (
  <>
    <div className={`${cssPrefix}__items-header`}>
      <h2 className={`${cssPrefix}__items-title`}>仕訳明細</h2>
      <button
        type="button"
        className={`${cssPrefix}__add-item`}
        onClick={onAddItem}
        disabled={isSubmitting}
        data-testid="add-item-button"
      >
        行追加
      </button>
    </div>

    <span className={`${cssPrefix}__field-error`}>{safeText(errors.items)}</span>

    <div className={`${cssPrefix}__items`}>
      {items.map((item, index) => (
        <PatternItemRow
          key={`${keyPrefix}-${index}`}
          index={index}
          item={item}
          error={errors.itemErrors[index] ?? {}}
          isSubmitting={isSubmitting}
          removeDisabled={isSubmitting || items.length <= 1}
          cssPrefix={cssPrefix}
          onItemChange={onItemChange}
          onRemove={onRemoveItem}
        />
      ))}
    </div>
  </>
);

/* ---------- Form Shell ---------- */

interface FormShellConfig {
  formTestId: string;
  errorTestId: string;
  submitTestId: string;
  submitLabel: string;
  submittingLabel: string;
  keyPrefix: string;
}

interface PatternFormShellProps {
  cssPrefix: string;
  config: FormShellConfig;
  submitError: string | null;
  isSubmitting: boolean;
  onSubmit: (event: FormEvent<HTMLFormElement>) => void;
  formData: { patternName: string; sourceTableName: string; description?: string };
  errors: FormErrors;
  items: PatternItemBase[];
  onFieldChange: (event: ChangeEvent<HTMLInputElement | HTMLTextAreaElement>) => void;
  onAddItem: () => void;
  onItemChange: (index: number, field: string, value: string | number) => void;
  onRemoveItem: (index: number) => void;
  beforeFields?: React.ReactNode;
  afterFields?: React.ReactNode;
}

export const PatternFormShell: React.FC<PatternFormShellProps> = ({
  cssPrefix,
  config,
  submitError,
  isSubmitting,
  onSubmit,
  formData,
  errors,
  items,
  onFieldChange,
  onAddItem,
  onItemChange,
  onRemoveItem,
  beforeFields,
  afterFields,
}) => (
  <form className={cssPrefix} onSubmit={onSubmit} data-testid={config.formTestId} noValidate>
    {submitError && (
      <div className={`${cssPrefix}__error`} role="alert" data-testid={config.errorTestId}>
        <span className={`${cssPrefix}__error-text`}>{submitError}</span>
      </div>
    )}
    {beforeFields}
    <PatternFormFields
      cssPrefix={cssPrefix}
      formData={formData}
      errors={errors}
      isSubmitting={isSubmitting}
      onChange={onFieldChange}
    />
    {afterFields}
    <PatternItemsSection
      cssPrefix={cssPrefix}
      items={items}
      errors={errors}
      isSubmitting={isSubmitting}
      keyPrefix={config.keyPrefix}
      onAddItem={onAddItem}
      onItemChange={onItemChange}
      onRemoveItem={onRemoveItem}
    />
    <button
      type="submit"
      className={`${cssPrefix}__submit`}
      disabled={isSubmitting}
      data-testid={config.submitTestId}
    >
      {isSubmitting ? config.submittingLabel : config.submitLabel}
    </button>
  </form>
);

/* ---------- Item Row ---------- */

interface ItemRowProps {
  index: number;
  item: PatternItemBase;
  error: ItemError;
  isSubmitting: boolean;
  removeDisabled: boolean;
  cssPrefix: string;
  onItemChange: (index: number, field: string, value: string | number) => void;
  onRemove: (index: number) => void;
}

export const PatternItemRow: React.FC<ItemRowProps> = ({
  index,
  item,
  error,
  isSubmitting,
  removeDisabled,
  cssPrefix,
  onItemChange,
  onRemove,
}) => {
  return (
    <div className={`${cssPrefix}__item-row`} data-testid={`item-row-${index}`}>
      <div className={`${cssPrefix}__item-grid`}>
        <div className={`${cssPrefix}__field`}>
          <label className={`${cssPrefix}__label`}>行番号</label>
          <input type="number" className={`${cssPrefix}__input`} value={item.lineNumber} disabled />
        </div>

        <div className={`${cssPrefix}__field`}>
          <label className={`${cssPrefix}__label`}>
            借方/貸方 <span className={`${cssPrefix}__required`}>*</span>
          </label>
          <select
            className={inputClassName(`${cssPrefix}__input`, Boolean(error.debitCreditType))}
            value={item.debitCreditType}
            onChange={(event) => onItemChange(index, 'debitCreditType', event.target.value)}
            disabled={isSubmitting}
          >
            <option value="">選択してください</option>
            <option value="D">借方 (D)</option>
            <option value="C">貸方 (C)</option>
          </select>
          <span className={`${cssPrefix}__field-error`}>{error.debitCreditType || ''}</span>
        </div>

        <div className={`${cssPrefix}__field`}>
          <label className={`${cssPrefix}__label`}>
            勘定科目コード <span className={`${cssPrefix}__required`}>*</span>
          </label>
          <input
            type="text"
            className={inputClassName(`${cssPrefix}__input`, Boolean(error.accountCode))}
            value={item.accountCode}
            onChange={(event) => onItemChange(index, 'accountCode', event.target.value)}
            disabled={isSubmitting}
            placeholder="勘定科目コード"
          />
          <span className={`${cssPrefix}__field-error`}>{error.accountCode || ''}</span>
        </div>

        <div className={`${cssPrefix}__field`}>
          <label className={`${cssPrefix}__label`}>
            金額計算式 <span className={`${cssPrefix}__required`}>*</span>
          </label>
          <input
            type="text"
            className={inputClassName(`${cssPrefix}__input`, Boolean(error.amountFormula))}
            value={item.amountFormula}
            onChange={(event) => onItemChange(index, 'amountFormula', event.target.value)}
            disabled={isSubmitting}
            placeholder="計算式"
          />
          <span className={`${cssPrefix}__field-error`}>{error.amountFormula || ''}</span>
        </div>

        <div className={`${cssPrefix}__field`}>
          <label className={`${cssPrefix}__label`}>説明テンプレート</label>
          <input
            type="text"
            className={`${cssPrefix}__input`}
            value={safeText(item.descriptionTemplate)}
            onChange={(event) => onItemChange(index, 'descriptionTemplate', event.target.value)}
            disabled={isSubmitting}
            placeholder="摘要テンプレート"
          />
        </div>
      </div>
      <button
        type="button"
        className={`${cssPrefix}__remove-item`}
        onClick={() => onRemove(index)}
        disabled={removeDisabled}
      >
        削除
      </button>
    </div>
  );
};
