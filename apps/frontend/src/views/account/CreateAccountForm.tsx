import React, { useState, ChangeEvent, FormEvent } from 'react';
import { createAccount, getCreateAccountErrorMessage } from '../../api/createAccount';
import { SuccessNotification } from '../common/SuccessNotification';
import './CreateAccountForm.css';

interface FormData {
  accountCode: string;
  accountName: string;
  accountType: string;
}

interface FormErrors {
  accountCode?: string;
  accountName?: string;
  accountType?: string;
}

const initialFormData: FormData = {
  accountCode: '',
  accountName: '',
  accountType: '',
};

const accountCodePattern = /^\d{4}$/;

const validateAccountCode = (accountCode: string): string | undefined => {
  const trimmedAccountCode = accountCode.trim();
  if (!trimmedAccountCode) {
    return '勘定科目コードを入力してください';
  }
  if (!accountCodePattern.test(trimmedAccountCode)) {
    return '勘定科目コードは4桁の数字で入力してください';
  }
  return undefined;
};

const validateAccountName = (accountName: string): string | undefined => {
  if (!accountName.trim()) {
    return '勘定科目名を入力してください';
  }
  return undefined;
};

const validateAccountType = (accountType: string): string | undefined => {
  if (!accountType) {
    return '勘定科目種別を選択してください';
  }
  return undefined;
};

const validateFormData = (formData: FormData): FormErrors => {
  const errors: FormErrors = {};

  const accountCodeError = validateAccountCode(formData.accountCode);
  if (accountCodeError) errors.accountCode = accountCodeError;

  const accountNameError = validateAccountName(formData.accountName);
  if (accountNameError) errors.accountName = accountNameError;

  const accountTypeError = validateAccountType(formData.accountType);
  if (accountTypeError) errors.accountType = accountTypeError;

  return errors;
};

interface FormFieldProps {
  id: string;
  label: string;
  value: string;
  error?: string;
  placeholder: string;
  disabled: boolean;
  onChange: (e: ChangeEvent<HTMLInputElement>) => void;
  testId: string;
  errorTestId: string;
}

const FormField: React.FC<FormFieldProps> = ({
  id,
  label,
  value,
  error,
  placeholder,
  disabled,
  onChange,
  testId,
  errorTestId,
}) => (
  <div className="create-account-form__field">
    <label htmlFor={id} className="create-account-form__label">
      {label} <span className="create-account-form__required">*</span>
    </label>
    <input
      id={id}
      name={id}
      type="text"
      className={`create-account-form__input ${error ? 'is-error' : ''}`}
      value={value}
      onChange={onChange}
      placeholder={placeholder}
      disabled={disabled}
      data-testid={testId}
    />
    {error && (
      <span className="create-account-form__field-error" data-testid={errorTestId}>
        {error}
      </span>
    )}
  </div>
);

interface AccountTypeSelectProps {
  value: string;
  error?: string;
  disabled: boolean;
  onChange: (e: ChangeEvent<HTMLSelectElement>) => void;
}

const AccountTypeSelect: React.FC<AccountTypeSelectProps> = ({
  value,
  error,
  disabled,
  onChange,
}) => (
  <div className="create-account-form__field">
    <label htmlFor="accountType" className="create-account-form__label">
      勘定科目種別 <span className="create-account-form__required">*</span>
    </label>
    <select
      id="accountType"
      name="accountType"
      className={`create-account-form__input ${error ? 'is-error' : ''}`}
      value={value}
      onChange={onChange}
      disabled={disabled}
      data-testid="create-account-type-select"
    >
      <option value="">選択してください</option>
      <option value="ASSET">ASSET</option>
      <option value="LIABILITY">LIABILITY</option>
      <option value="EQUITY">EQUITY</option>
      <option value="REVENUE">REVENUE</option>
      <option value="EXPENSE">EXPENSE</option>
    </select>
    {error && (
      <span className="create-account-form__field-error" data-testid="create-account-type-error">
        {error}
      </span>
    )}
  </div>
);

/**
 * 勘定科目登録フォームコンポーネント
 */
export const CreateAccountForm: React.FC = () => {
  const [formData, setFormData] = useState<FormData>(initialFormData);
  const [errors, setErrors] = useState<FormErrors>({});
  const [isSubmitting, setIsSubmitting] = useState(false);
  const [submitError, setSubmitError] = useState<string | null>(null);
  const [successMessage, setSuccessMessage] = useState<string | null>(null);

  const handleChange = (e: ChangeEvent<HTMLInputElement | HTMLSelectElement>) => {
    const { name, value } = e.target;
    setFormData((prev) => ({ ...prev, [name]: value }));
    if (errors[name as keyof FormErrors]) {
      setErrors((prev) => ({ ...prev, [name]: undefined }));
    }
  };

  const handleSubmit = async (e: FormEvent) => {
    e.preventDefault();
    setSubmitError(null);
    setSuccessMessage(null);

    const validationErrors = validateFormData(formData);
    setErrors(validationErrors);
    if (Object.keys(validationErrors).length > 0) {
      return;
    }

    setIsSubmitting(true);

    try {
      const response = await createAccount({
        accountCode: formData.accountCode.trim(),
        accountName: formData.accountName.trim(),
        accountType: formData.accountType,
      });

      if (!response.success) {
        throw new Error(response.errorMessage || '勘定科目登録に失敗しました');
      }

      setSuccessMessage('勘定科目登録が完了しました');
      setFormData(initialFormData);
    } catch (error) {
      setSubmitError(getCreateAccountErrorMessage(error));
    } finally {
      setIsSubmitting(false);
    }
  };

  return (
    <form
      className="create-account-form"
      onSubmit={handleSubmit}
      data-testid="create-account-form"
      noValidate
    >
      {submitError && (
        <div className="create-account-form__error" role="alert" data-testid="create-account-error">
          <svg
            className="create-account-form__error-icon"
            viewBox="0 0 24 24"
            fill="currentColor"
            aria-hidden="true"
          >
            <path d="M11 15h2v2h-2v-2zm0-8h2v6h-2V7zm.99-5C6.47 2 2 6.48 2 12s4.47 10 9.99 10C17.52 22 22 17.52 22 12S17.52 2 11.99 2zm0 18c-4.41 0-8-3.59-8-8s3.59-8 8-8 8 3.59 8 8-3.59 8-8 8z" />
          </svg>
          <span className="create-account-form__error-text">{submitError}</span>
        </div>
      )}

      {successMessage && (
        <div className="create-account-form__success" data-testid="create-account-success">
          <SuccessNotification message={successMessage} onDismiss={() => setSuccessMessage(null)} />
        </div>
      )}

      <FormField
        id="accountCode"
        label="勘定科目コード"
        value={formData.accountCode}
        error={errors.accountCode}
        placeholder="例: 1000"
        disabled={isSubmitting}
        onChange={handleChange}
        testId="create-account-code-input"
        errorTestId="create-account-code-error"
      />

      <FormField
        id="accountName"
        label="勘定科目名"
        value={formData.accountName}
        error={errors.accountName}
        placeholder="例: 現金"
        disabled={isSubmitting}
        onChange={handleChange}
        testId="create-account-name-input"
        errorTestId="create-account-name-error"
      />

      <AccountTypeSelect
        value={formData.accountType}
        error={errors.accountType}
        disabled={isSubmitting}
        onChange={handleChange}
      />

      <button
        type="submit"
        className="create-account-form__submit"
        disabled={isSubmitting}
        data-testid="create-account-submit"
      >
        {isSubmitting ? '登録中...' : '登録'}
      </button>
    </form>
  );
};
