import React, { useState, FormEvent, ChangeEvent } from 'react';
import { useAuth } from '../../hooks/useAuth';
import './LoginForm.css';

interface FormData {
  username: string;
  password: string;
}

interface FormErrors {
  username?: string;
  password?: string;
}

// デフォルト認証情報（開発/デモ環境用）
const DEFAULT_CREDENTIALS = {
  username: 'admin',
  password: 'Password123!',
};

// 開発環境またはデモモードの場合はデフォルト値を設定
const shouldUseDefaultCredentials = (): boolean => {
  return import.meta.env.DEV || import.meta.env.VITE_DEMO_MODE === 'true';
};

const getInitialFormData = (): FormData => ({
  username: shouldUseDefaultCredentials() ? DEFAULT_CREDENTIALS.username : '',
  password: shouldUseDefaultCredentials() ? DEFAULT_CREDENTIALS.password : '',
});

const validateFormData = (formData: FormData): FormErrors => {
  const newErrors: FormErrors = {};

  if (!formData.username) {
    newErrors.username = 'ユーザー名を入力してください';
  }

  if (!formData.password) {
    newErrors.password = 'パスワードを入力してください';
  } else if (formData.password.length < 8) {
    newErrors.password = 'パスワードは8文字以上です';
  }

  return newErrors;
};

const getErrorMessage = (error: unknown): string => {
  if (error instanceof Error) {
    return error.message;
  }
  return 'ログインに失敗しました';
};

/**
 * ログインフォームコンポーネント
 */
export const LoginForm: React.FC = () => {
  const { login } = useAuth();

  const [formData, setFormData] = useState<FormData>(getInitialFormData);
  const [errors, setErrors] = useState<FormErrors>({});
  const [isSubmitting, setIsSubmitting] = useState(false);
  const [loginError, setLoginError] = useState<string | null>(null);

  const handleChange = (e: ChangeEvent<HTMLInputElement>) => {
    const { name, value } = e.target;
    setFormData((prev) => ({ ...prev, [name]: value }));
    if (errors[name as keyof FormErrors]) {
      setErrors((prev) => ({ ...prev, [name]: undefined }));
    }
  };

  const handleSubmit = async (e: FormEvent) => {
    e.preventDefault();
    setLoginError(null);

    const validationErrors = validateFormData(formData);
    setErrors(validationErrors);
    if (Object.keys(validationErrors).length > 0) {
      return;
    }

    setIsSubmitting(true);

    try {
      await login(formData.username.trim(), formData.password.trim());
    } catch (error) {
      setLoginError(getErrorMessage(error));
    } finally {
      setIsSubmitting(false);
    }
  };

  return (
    <form className="login-form" onSubmit={handleSubmit} data-testid="login-form">
      {loginError && (
        <div className="login-form__error" role="alert" data-testid="login-error">
          <svg
            className="login-form__error-icon"
            viewBox="0 0 24 24"
            fill="currentColor"
            aria-hidden="true"
          >
            <path d="M12 2C6.48 2 2 6.48 2 12s4.48 10 10 10 10-4.48 10-10S17.52 2 12 2zm1 15h-2v-2h2v2zm0-4h-2V7h2v6z" />
          </svg>
          <span className="login-form__error-text">{loginError}</span>
        </div>
      )}

      <div className="login-form__field">
        <label htmlFor="username" className="login-form__label">
          ユーザー名 <span className="login-form__required">*</span>
        </label>
        <input
          id="username"
          name="username"
          type="text"
          className={`login-form__input ${errors.username ? 'is-error' : ''}`}
          value={formData.username}
          onChange={handleChange}
          placeholder="ユーザー名を入力"
          autoComplete="username"
          disabled={isSubmitting}
          data-testid="username-input"
        />
        {errors.username && (
          <span className="login-form__field-error" data-testid="username-error">
            {errors.username}
          </span>
        )}
      </div>

      <div className="login-form__field">
        <label htmlFor="password" className="login-form__label">
          パスワード <span className="login-form__required">*</span>
        </label>
        <input
          id="password"
          name="password"
          type="password"
          className={`login-form__input ${errors.password ? 'is-error' : ''}`}
          value={formData.password}
          onChange={handleChange}
          placeholder="パスワードを入力"
          autoComplete="current-password"
          disabled={isSubmitting}
          data-testid="password-input"
        />
        {errors.password && (
          <span className="login-form__field-error" data-testid="password-error">
            {errors.password}
          </span>
        )}
      </div>

      <button
        type="submit"
        className="login-form__submit"
        disabled={isSubmitting}
        data-testid="login-submit"
      >
        {isSubmitting ? 'ログイン中...' : 'ログイン'}
      </button>
    </form>
  );
};
