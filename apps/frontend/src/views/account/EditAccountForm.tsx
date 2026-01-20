import React, { useState, ChangeEvent, FormEvent } from 'react';
import {
  updateAccount,
  getUpdateAccountErrorMessage,
  UpdateAccountRequest,
} from '../../api/updateAccount';
import type { Account } from '../../api/getAccounts';
import './EditAccountForm.css';

interface FormErrors {
  accountName?: string;
  accountType?: string;
}

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

const validateFormData = (formData: UpdateAccountRequest): FormErrors => {
  const errors: FormErrors = {};

  const accountNameError = validateAccountName(formData.accountName);
  if (accountNameError) errors.accountName = accountNameError;

  const accountTypeError = validateAccountType(formData.accountType);
  if (accountTypeError) errors.accountType = accountTypeError;

  return errors;
};

interface EditAccountFormProps {
  account: Account;
  onSuccess: (message: string) => void;
}

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
  <div className="edit-account-form__field">
    <label htmlFor="accountType" className="edit-account-form__label">
      勘定科目種別 <span className="edit-account-form__required">*</span>
    </label>
    <select
      id="accountType"
      name="accountType"
      className={`edit-account-form__input ${error ? 'is-error' : ''}`}
      value={value}
      onChange={onChange}
      disabled={disabled}
      data-testid="edit-account-type-select"
    >
      <option value="">選択してください</option>
      <option value="ASSET">ASSET</option>
      <option value="LIABILITY">LIABILITY</option>
      <option value="EQUITY">EQUITY</option>
      <option value="REVENUE">REVENUE</option>
      <option value="EXPENSE">EXPENSE</option>
    </select>
    {error && (
      <span className="edit-account-form__field-error" data-testid="edit-account-type-error">
        {error}
      </span>
    )}
  </div>
);

/**
 * 勘定科目編集フォームコンポーネント
 */
export const EditAccountForm: React.FC<EditAccountFormProps> = ({ account, onSuccess }) => {
  const [formData, setFormData] = useState<UpdateAccountRequest>({
    accountName: account.accountName,
    accountType: account.accountType,
  });
  const [errors, setErrors] = useState<FormErrors>({});
  const [isSubmitting, setIsSubmitting] = useState(false);
  const [submitError, setSubmitError] = useState<string | null>(null);

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

    const validationErrors = validateFormData(formData);
    setErrors(validationErrors);
    if (Object.keys(validationErrors).length > 0) {
      return;
    }

    setIsSubmitting(true);

    try {
      const response = await updateAccount(account.accountId, {
        accountName: formData.accountName.trim(),
        accountType: formData.accountType,
      });

      if (!response.success) {
        throw new Error(response.errorMessage || '勘定科目の更新に失敗しました');
      }

      const message = response.message || '勘定科目を更新しました';
      onSuccess(message);
    } catch (error) {
      setSubmitError(getUpdateAccountErrorMessage(error));
    } finally {
      setIsSubmitting(false);
    }
  };

  return (
    <form
      className="edit-account-form"
      onSubmit={handleSubmit}
      data-testid="edit-account-form"
      noValidate
    >
      {submitError && (
        <div className="edit-account-form__error" role="alert" data-testid="edit-account-error">
          <svg
            className="edit-account-form__error-icon"
            viewBox="0 0 24 24"
            fill="currentColor"
            aria-hidden="true"
          >
            <path d="M11 15h2v2h-2v-2zm0-8h2v6h-2V7zm.99-5C6.47 2 2 6.48 2 12s4.47 10 9.99 10C17.52 22 22 17.52 22 12S17.52 2 11.99 2zm0 18c-4.41 0-8-3.59-8-8s3.59-8 8-8 8 3.59 8 8-3.59 8-8 8z" />
          </svg>
          <span className="edit-account-form__error-text">{submitError}</span>
        </div>
      )}

      <div className="edit-account-form__field">
        <label htmlFor="accountCode" className="edit-account-form__label">
          勘定科目コード
        </label>
        <input
          id="accountCode"
          name="accountCode"
          type="text"
          className="edit-account-form__input"
          value={account.accountCode}
          disabled
          data-testid="edit-account-code-input"
        />
      </div>

      <div className="edit-account-form__field">
        <label htmlFor="accountName" className="edit-account-form__label">
          勘定科目名 <span className="edit-account-form__required">*</span>
        </label>
        <input
          id="accountName"
          name="accountName"
          type="text"
          className={`edit-account-form__input ${errors.accountName ? 'is-error' : ''}`}
          value={formData.accountName}
          onChange={handleChange}
          placeholder="例: 現金"
          disabled={isSubmitting}
          data-testid="edit-account-name-input"
        />
        {errors.accountName && (
          <span className="edit-account-form__field-error" data-testid="edit-account-name-error">
            {errors.accountName}
          </span>
        )}
      </div>

      <AccountTypeSelect
        value={formData.accountType}
        error={errors.accountType}
        disabled={isSubmitting}
        onChange={handleChange}
      />

      <button
        type="submit"
        className="edit-account-form__submit"
        disabled={isSubmitting}
        data-testid="edit-account-submit"
      >
        {isSubmitting ? '更新中...' : '更新'}
      </button>
    </form>
  );
};
