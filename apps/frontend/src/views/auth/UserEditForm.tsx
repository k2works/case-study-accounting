import React, { useEffect, useState, ChangeEvent, FormEvent } from 'react';
import type { GetUserResponse } from '../../api/getUser';
import { updateUser, getUpdateErrorMessage } from '../../api/updateUser';
import { SuccessNotification } from '../common/SuccessNotification';
import { FormField, RoleSelect } from './RegisterUserForm';
import './RegisterUserForm.css';

interface UserEditFormProps {
  user: GetUserResponse;
  onSuccess: (message: string) => void;
}

interface FormData {
  displayName: string;
  password: string;
  role: string;
}

interface FormErrors {
  displayName?: string;
  password?: string;
  role?: string;
}

const validateDisplayName = (displayName: string): string | undefined => {
  if (!displayName.trim()) {
    return '表示名を入力してください';
  }
  return undefined;
};

const validatePassword = (password: string): string | undefined => {
  const trimmedPassword = password.trim();
  if (!trimmedPassword) {
    return undefined;
  }
  if (trimmedPassword.length < 8) {
    return 'パスワードは8文字以上です';
  }
  return undefined;
};

const validateRole = (role: string): string | undefined => {
  if (!role) {
    return 'ロールを選択してください';
  }
  return undefined;
};

const validateFormData = (formData: FormData): FormErrors => {
  const errors: FormErrors = {};

  const displayNameError = validateDisplayName(formData.displayName);
  if (displayNameError) errors.displayName = displayNameError;

  const passwordError = validatePassword(formData.password);
  if (passwordError) errors.password = passwordError;

  const roleError = validateRole(formData.role);
  if (roleError) errors.role = roleError;

  return errors;
};

const buildInitialFormData = (user: GetUserResponse): FormData => ({
  displayName: user.displayName,
  password: '',
  role: user.role,
});

/**
 * ユーザー編集フォームコンポーネント
 */
export const UserEditForm: React.FC<UserEditFormProps> = ({ user, onSuccess }) => {
  const [formData, setFormData] = useState<FormData>(() => buildInitialFormData(user));
  const [errors, setErrors] = useState<FormErrors>({});
  const [isSubmitting, setIsSubmitting] = useState(false);
  const [submitError, setSubmitError] = useState<string | null>(null);
  const [successMessage, setSuccessMessage] = useState<string | null>(null);

  useEffect(() => {
    setFormData(buildInitialFormData(user));
    setErrors({});
    setSubmitError(null);
    setSuccessMessage(null);
  }, [user]);

  const handleChange = (e: ChangeEvent<HTMLInputElement | HTMLSelectElement>) => {
    const { name, value } = e.target;
    setFormData((prev) => ({ ...prev, [name]: value }));
    if (errors[name as keyof FormErrors]) {
      setErrors((prev) => ({ ...prev, [name]: undefined }));
    }
  };
  const handleReadonlyChange = (event: ChangeEvent<HTMLInputElement>) => {
    event.preventDefault();
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
      const trimmedPassword = formData.password.trim();
      const payload = {
        displayName: formData.displayName.trim(),
        role: formData.role,
        ...(trimmedPassword ? { password: trimmedPassword } : {}),
      };
      const response = await updateUser(user.id, payload);

      if (!response.success) {
        throw new Error(response.errorMessage || 'ユーザー更新に失敗しました');
      }

      const message = 'ユーザー更新が完了しました';
      setSuccessMessage(message);
      setFormData((prev) => ({ ...prev, password: '' }));
      onSuccess(message);
    } catch (error) {
      setSubmitError(getUpdateErrorMessage(error));
    } finally {
      setIsSubmitting(false);
    }
  };

  return (
    <form className="register-form" onSubmit={handleSubmit} data-testid="user-edit-form" noValidate>
      {submitError && (
        <div className="register-form__error" role="alert" data-testid="user-edit-error">
          <svg
            className="register-form__error-icon"
            viewBox="0 0 24 24"
            fill="currentColor"
            aria-hidden="true"
          >
            <path d="M12 2C6.48 2 2 6.48 2 12s4.48 10 10 10 10-4.48 10-10S17.52 2 12 2zm1 15h-2v-2h2v2zm0-4h-2V7h2v6z" />
          </svg>
          <span className="register-form__error-text">{submitError}</span>
        </div>
      )}

      {successMessage && (
        <div className="register-form__success" data-testid="user-edit-success">
          <SuccessNotification message={successMessage} onDismiss={() => setSuccessMessage(null)} />
        </div>
      )}

      <FormField
        id="userId"
        label="ユーザー ID"
        type="text"
        value={user.id}
        placeholder=""
        autoComplete="off"
        disabled
        onChange={handleReadonlyChange}
        testId="user-edit-id-input"
        errorTestId="user-edit-id-error"
        required={false}
      />

      <FormField
        id="username"
        label="ユーザー名"
        type="text"
        value={user.username}
        placeholder=""
        autoComplete="username"
        disabled
        onChange={handleReadonlyChange}
        testId="user-edit-username-input"
        errorTestId="user-edit-username-error"
        required={false}
      />

      <FormField
        id="email"
        label="メールアドレス"
        type="email"
        value={user.email}
        placeholder=""
        autoComplete="email"
        disabled
        onChange={handleReadonlyChange}
        testId="user-edit-email-input"
        errorTestId="user-edit-email-error"
        required={false}
      />

      <FormField
        id="displayName"
        label="表示名"
        type="text"
        value={formData.displayName}
        error={errors.displayName}
        placeholder="表示名を入力"
        autoComplete="name"
        disabled={isSubmitting}
        onChange={handleChange}
        testId="user-edit-display-name-input"
        errorTestId="user-edit-display-name-error"
      />

      <FormField
        id="password"
        label="パスワード（任意）"
        type="password"
        value={formData.password}
        error={errors.password}
        placeholder="8 文字以上で入力"
        autoComplete="new-password"
        disabled={isSubmitting}
        onChange={handleChange}
        testId="user-edit-password-input"
        errorTestId="user-edit-password-error"
        required={false}
      />

      <RoleSelect
        value={formData.role}
        error={errors.role}
        disabled={isSubmitting}
        onChange={handleChange}
        testId="user-edit-role-select"
        errorTestId="user-edit-role-error"
      />

      <button
        type="submit"
        className="register-form__submit"
        disabled={isSubmitting}
        data-testid="user-edit-submit"
      >
        {isSubmitting ? '更新中...' : 'ユーザー更新'}
      </button>
    </form>
  );
};
