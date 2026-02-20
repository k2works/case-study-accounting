import React, { type FormEvent, useState } from 'react';
import {
  createAutoJournalPattern,
  getCreateAutoJournalPatternErrorMessage,
  type CreateAutoJournalPatternRequest,
} from '../../api/createAutoJournalPattern';
import { SuccessNotification } from '../common/SuccessNotification';
import {
  type FormErrors,
  createInitialErrors,
  inputClassName,
  hasFormErrors,
  validateCommonFields,
  createEmptyItem,
  normalizeItems,
  sanitizePatternItems,
  usePatternFormHandlers,
  PatternFormShell,
} from './PatternFormCommon';
import './CreateAutoJournalPatternForm.css';

const CSS_PREFIX = 'create-auto-journal-pattern-form';

const FORM_CONFIG = {
  formTestId: 'create-auto-journal-pattern-form',
  errorTestId: 'create-auto-journal-pattern-error',
  submitTestId: 'create-pattern-submit',
  submitLabel: '登録',
  submittingLabel: '登録中...',
  keyPrefix: 'create-pattern-item',
} as const;

const createInitialFormData = (): CreateAutoJournalPatternRequest => ({
  patternCode: '',
  patternName: '',
  sourceTableName: '',
  description: '',
  items: [createEmptyItem(1)],
});

const validateFormData = (formData: CreateAutoJournalPatternRequest): FormErrors => {
  const errors = validateCommonFields(formData);
  if (!formData.patternCode.trim()) errors.patternCode = 'パターンコードを入力してください';
  return errors;
};

export const CreateAutoJournalPatternForm: React.FC = () => {
  const [formData, setFormData] = useState<CreateAutoJournalPatternRequest>(createInitialFormData);
  const [errors, setErrors] = useState<FormErrors>(createInitialErrors(1));
  const [isSubmitting, setIsSubmitting] = useState(false);
  const [submitError, setSubmitError] = useState<string | null>(null);
  const [successMessage, setSuccessMessage] = useState<string | null>(null);

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

  const handleSubmit = async (event: FormEvent<HTMLFormElement>) => {
    event.preventDefault();
    setSubmitError(null);
    setSuccessMessage(null);

    const validationErrors = validateFormData(formData);
    setErrors(validationErrors);
    if (hasFormErrors(validationErrors)) return;

    setIsSubmitting(true);
    try {
      const response = await createAutoJournalPattern({
        ...formData,
        patternCode: formData.patternCode.trim(),
        patternName: formData.patternName.trim(),
        sourceTableName: formData.sourceTableName.trim(),
        description: formData.description?.trim() || undefined,
        items: sanitizePatternItems(formData.items),
      });
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
        <>
          {successMessage && (
            <div className={`${CSS_PREFIX}__success`}>
              <SuccessNotification
                message={successMessage}
                onDismiss={() => setSuccessMessage(null)}
              />
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
        </>
      }
    />
  );
};
