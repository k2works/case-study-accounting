import React from 'react';

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

export interface PatternItemBase {
  lineNumber: number;
  debitCreditType: string;
  accountCode: string;
  amountFormula: string;
  descriptionTemplate?: string;
}

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
            <option value="DEBIT">借方 (D)</option>
            <option value="CREDIT">貸方 (C)</option>
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
