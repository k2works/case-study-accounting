import React, { type ChangeEvent, type FormEvent, useState } from 'react';
import type { AutoJournalPattern } from '../../api/getAutoJournalPatterns';
import {
  getUpdateAutoJournalPatternErrorMessage,
  updateAutoJournalPattern,
  type UpdateAutoJournalPatternRequest,
} from '../../api/updateAutoJournalPattern';
import {
  type FormErrors,
  createInitialErrors,
  hasFormErrors,
  validateCommonFields,
  createEmptyItem,
  normalizeItems,
  sanitizePatternItems,
  usePatternFormHandlers,
  PatternFormShell,
} from './PatternFormCommon';
import './EditAutoJournalPatternForm.css';

const CSS_PREFIX = 'edit-auto-journal-pattern-form';

const FORM_CONFIG = {
  formTestId: 'edit-auto-journal-pattern-form',
  errorTestId: 'edit-auto-journal-pattern-error',
  submitTestId: 'edit-pattern-submit',
  submitLabel: '更新',
  submittingLabel: '更新中...',
  keyPrefix: 'edit-pattern-item',
} as const;

interface EditAutoJournalPatternFormProps {
  pattern: AutoJournalPattern;
  onSuccess: (message: string) => void;
}

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
      : [createEmptyItem(1)],
});

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

  const { handleFieldChange, handleItemChange, handleAddItem, handleRemoveItem } =
    usePatternFormHandlers(
      {
        formData,
        setFormData,
        errors,
        setErrors,
        isSubmitting,
        setIsSubmitting,
        submitError,
        setSubmitError,
      },
      createEmptyItem,
      normalizeItems
    );

  const handleIsActiveChange = (event: ChangeEvent<HTMLInputElement>) => {
    setFormData((prev) => ({ ...prev, isActive: event.target.checked }));
  };

  const buildSubmitData = () => ({
    ...formData,
    patternName: formData.patternName.trim(),
    sourceTableName: formData.sourceTableName.trim(),
    description: formData.description?.trim() || undefined,
    items: sanitizePatternItems(formData.items),
  });

  const handleSubmit = async (event: FormEvent<HTMLFormElement>) => {
    event.preventDefault();
    setSubmitError(null);

    const validationErrors = validateCommonFields(formData);
    setErrors(validationErrors);
    if (hasFormErrors(validationErrors)) return;

    setIsSubmitting(true);
    try {
      const response = await updateAutoJournalPattern(pattern.patternId, buildSubmitData());
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
    <PatternFormShell
      cssPrefix={CSS_PREFIX}
      config={FORM_CONFIG}
      submitError={submitError}
      isSubmitting={isSubmitting}
      onSubmit={handleSubmit}
      formData={formData}
      errors={errors}
      items={formData.items}
      onFieldChange={handleFieldChange}
      onAddItem={handleAddItem}
      onItemChange={handleItemChange}
      onRemoveItem={handleRemoveItem}
      beforeFields={
        <div className={`${CSS_PREFIX}__field`}>
          <label htmlFor="patternCode" className={`${CSS_PREFIX}__label`}>
            パターンコード
          </label>
          <input
            id="patternCode"
            name="patternCode"
            type="text"
            className={`${CSS_PREFIX}__input`}
            value={pattern.patternCode}
            disabled
            data-testid="pattern-code-input"
          />
        </div>
      }
      afterFields={
        <div className={`${CSS_PREFIX}__checkbox-field`}>
          <label htmlFor="isActive" className={`${CSS_PREFIX}__checkbox-label`}>
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
      }
    />
  );
};
