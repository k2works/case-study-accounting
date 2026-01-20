import React, { useState, ChangeEvent, FormEvent } from 'react';
import { registerUser, getRegisterErrorMessage } from '../../api/registerUser';
import { SuccessNotification } from '../common/SuccessNotification';
import './RegisterUserForm.css';

interface FormData {
  username: string;
  email: string;
  password: string;
  confirmPassword: string;
  displayName: string;
  role: string;
}

interface FormErrors {
  username?: string;
  email?: string;
  password?: string;
  confirmPassword?: string;
  displayName?: string;
  role?: string;
}

const initialFormData: FormData = {
  username: '',
  email: '',
  password: '',
  confirmPassword: '',
  displayName: '',
  role: '',
};

const emailPattern = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;

const validateUsername = (username: string): string | undefined => {
  if (!username.trim()) {
    return 'ユーザー名を入力してください';
  }
  return undefined;
};

const validateEmail = (email: string): string | undefined => {
  const trimmedEmail = email.trim();
  if (!trimmedEmail) {
    return 'メールアドレスを入力してください';
  }
  if (!emailPattern.test(trimmedEmail)) {
    return 'メールアドレスの形式が不正です';
  }
  return undefined;
};

const validatePassword = (password: string): string | undefined => {
  const trimmedPassword = password.trim();
  if (!trimmedPassword) {
    return 'パスワードを入力してください';
  }
  if (trimmedPassword.length < 8) {
    return 'パスワードは8文字以上です';
  }
  return undefined;
};

const validateConfirmPassword = (password: string, confirmPassword: string): string | undefined => {
  const trimmedConfirmPassword = confirmPassword.trim();
  if (!trimmedConfirmPassword) {
    return '確認用パスワードを入力してください';
  }
  if (password.trim() !== trimmedConfirmPassword) {
    return 'パスワードが一致しません';
  }
  return undefined;
};

const validateDisplayName = (displayName: string): string | undefined => {
  if (!displayName.trim()) {
    return '表示名を入力してください';
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

  const usernameError = validateUsername(formData.username);
  if (usernameError) errors.username = usernameError;

  const emailError = validateEmail(formData.email);
  if (emailError) errors.email = emailError;

  const passwordError = validatePassword(formData.password);
  if (passwordError) errors.password = passwordError;

  const confirmPasswordError = validateConfirmPassword(formData.password, formData.confirmPassword);
  if (confirmPasswordError) errors.confirmPassword = confirmPasswordError;

  const displayNameError = validateDisplayName(formData.displayName);
  if (displayNameError) errors.displayName = displayNameError;

  const roleError = validateRole(formData.role);
  if (roleError) errors.role = roleError;

  return errors;
};

interface FormFieldProps {
  id: string;
  label: string;
  type: string;
  value: string;
  error?: string;
  placeholder: string;
  autoComplete: string;
  disabled: boolean;
  onChange: (e: ChangeEvent<HTMLInputElement>) => void;
  testId: string;
  errorTestId: string;
}

const FormField: React.FC<FormFieldProps> = ({
  id,
  label,
  type,
  value,
  error,
  placeholder,
  autoComplete,
  disabled,
  onChange,
  testId,
  errorTestId,
}) => (
  <div className="register-form__field">
    <label htmlFor={id} className="register-form__label">
      {label} <span className="register-form__required">*</span>
    </label>
    <input
      id={id}
      name={id}
      type={type}
      className={`register-form__input ${error ? 'is-error' : ''}`}
      value={value}
      onChange={onChange}
      placeholder={placeholder}
      autoComplete={autoComplete}
      disabled={disabled}
      data-testid={testId}
    />
    {error && (
      <span className="register-form__field-error" data-testid={errorTestId}>
        {error}
      </span>
    )}
  </div>
);

interface RoleSelectProps {
  value: string;
  error?: string;
  disabled: boolean;
  onChange: (e: ChangeEvent<HTMLSelectElement>) => void;
}

const RoleSelect: React.FC<RoleSelectProps> = ({ value, error, disabled, onChange }) => (
  <div className="register-form__field">
    <label htmlFor="role" className="register-form__label">
      ロール <span className="register-form__required">*</span>
    </label>
    <select
      id="role"
      name="role"
      className={`register-form__input ${error ? 'is-error' : ''}`}
      value={value}
      onChange={onChange}
      disabled={disabled}
      data-testid="register-role-select"
    >
      <option value="">選択してください</option>
      <option value="ADMIN">ADMIN</option>
      <option value="MANAGER">MANAGER</option>
      <option value="USER">USER</option>
      <option value="VIEWER">VIEWER</option>
    </select>
    {error && (
      <span className="register-form__field-error" data-testid="role-error">
        {error}
      </span>
    )}
  </div>
);

/**
 * ユーザー登録フォームコンポーネント
 */
export const RegisterUserForm: React.FC = () => {
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
      const response = await registerUser({
        username: formData.username.trim(),
        email: formData.email.trim(),
        password: formData.password.trim(),
        displayName: formData.displayName.trim(),
        role: formData.role,
      });

      if (!response.success) {
        throw new Error(response.errorMessage || 'ユーザー登録に失敗しました');
      }

      setSuccessMessage('ユーザー登録が完了しました');
      setFormData(initialFormData);
    } catch (error) {
      setSubmitError(getRegisterErrorMessage(error));
    } finally {
      setIsSubmitting(false);
    }
  };

  return (
    <form className="register-form" onSubmit={handleSubmit} data-testid="register-form" noValidate>
      {submitError && (
        <div className="register-form__error" role="alert" data-testid="register-error">
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
        <div className="register-form__success" data-testid="register-success">
          <SuccessNotification message={successMessage} onDismiss={() => setSuccessMessage(null)} />
        </div>
      )}

      <FormField
        id="username"
        label="ユーザー名"
        type="text"
        value={formData.username}
        error={errors.username}
        placeholder="ユーザー名を入力"
        autoComplete="username"
        disabled={isSubmitting}
        onChange={handleChange}
        testId="register-username-input"
        errorTestId="username-error"
      />

      <FormField
        id="email"
        label="メールアドレス"
        type="email"
        value={formData.email}
        error={errors.email}
        placeholder="mail@example.com"
        autoComplete="email"
        disabled={isSubmitting}
        onChange={handleChange}
        testId="register-email-input"
        errorTestId="email-error"
      />

      <FormField
        id="password"
        label="パスワード"
        type="password"
        value={formData.password}
        error={errors.password}
        placeholder="8 文字以上で入力"
        autoComplete="new-password"
        disabled={isSubmitting}
        onChange={handleChange}
        testId="register-password-input"
        errorTestId="password-error"
      />

      <FormField
        id="confirmPassword"
        label="確認用パスワード"
        type="password"
        value={formData.confirmPassword}
        error={errors.confirmPassword}
        placeholder="パスワードを再入力"
        autoComplete="new-password"
        disabled={isSubmitting}
        onChange={handleChange}
        testId="register-confirm-password-input"
        errorTestId="confirm-password-error"
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
        testId="register-display-name-input"
        errorTestId="display-name-error"
      />

      <RoleSelect
        value={formData.role}
        error={errors.role}
        disabled={isSubmitting}
        onChange={handleChange}
      />

      <button
        type="submit"
        className="register-form__submit"
        disabled={isSubmitting}
        data-testid="register-submit"
      >
        {isSubmitting ? '登録中...' : 'ユーザー登録'}
      </button>
    </form>
  );
};
