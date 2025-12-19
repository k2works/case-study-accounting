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

// 開発環境用のデフォルト認証情報
const DEV_CREDENTIALS = {
  username: 'admin',
  password: 'Password123!',
};

const getInitialFormData = (): FormData => ({
  username: import.meta.env.DEV ? DEV_CREDENTIALS.username : '',
  password: import.meta.env.DEV ? DEV_CREDENTIALS.password : '',
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
      await login(formData.username, formData.password);
    } catch (error) {
      setLoginError(getErrorMessage(error));
    } finally {
      setIsSubmitting(false);
    }
  };

  return (
    <form className="login-form" onSubmit={handleSubmit}>
      {loginError && (
        <div className="login-form__error">
          <span>{loginError}</span>
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
        />
        {errors.username && <span className="login-form__field-error">{errors.username}</span>}
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
        />
        {errors.password && <span className="login-form__field-error">{errors.password}</span>}
      </div>

      <button type="submit" className="login-form__submit" disabled={isSubmitting}>
        {isSubmitting ? 'ログイン中...' : 'ログイン'}
      </button>
    </form>
  );
};
