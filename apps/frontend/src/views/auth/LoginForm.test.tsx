import { describe, it, expect, vi, beforeEach } from 'vitest';
import { render, screen, waitFor } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { LoginForm } from './LoginForm';

const mockLogin = vi.fn();

vi.mock('../../hooks/useAuth', () => ({
  useAuth: () => ({ login: mockLogin }),
}));

// ヘルパー関数
const setupUser = () => userEvent.setup();
const usernameInput = () => screen.getByTestId('username-input');
const passwordInput = () => screen.getByTestId('password-input');
const submitButton = () => screen.getByTestId('login-submit');
const delay = (ms: number) => new Promise((resolve) => setTimeout(resolve, ms));

const fillForm = async (
  user: ReturnType<typeof userEvent.setup>,
  username: string,
  password: string
) => {
  await user.clear(usernameInput());
  await user.clear(passwordInput());
  if (username) await user.type(usernameInput(), username);
  if (password) await user.type(passwordInput(), password);
};

const fillAndSubmit = async (
  user: ReturnType<typeof userEvent.setup>,
  username: string,
  password: string
) => {
  await fillForm(user, username, password);
  await user.click(submitButton());
};

describe('LoginForm', () => {
  beforeEach(() => {
    vi.clearAllMocks();
  });

  describe('rendering', () => {
    it('renders form with all elements', () => {
      render(<LoginForm />);
      expect(screen.getByTestId('login-form')).toBeInTheDocument();
      expect(usernameInput()).toBeInTheDocument();
      expect(passwordInput()).toBeInTheDocument();
      expect(submitButton()).toBeInTheDocument();
      expect(screen.getByLabelText(/ユーザー名/)).toBeInTheDocument();
      expect(screen.getByLabelText(/パスワード/)).toBeInTheDocument();
    });
  });

  describe('form validation', () => {
    it('shows error when username is empty', async () => {
      const user = setupUser();
      render(<LoginForm />);

      await fillAndSubmit(user, '', 'password123');

      expect(screen.getByTestId('username-error')).toBeInTheDocument();
      expect(screen.getByText('ユーザー名を入力してください')).toBeInTheDocument();
    });

    it('shows error when password is empty', async () => {
      const user = setupUser();
      render(<LoginForm />);

      await fillAndSubmit(user, 'testuser', '');

      expect(screen.getByTestId('password-error')).toBeInTheDocument();
      expect(screen.getByText('パスワードを入力してください')).toBeInTheDocument();
    });

    it('shows error when password is too short', async () => {
      const user = setupUser();
      render(<LoginForm />);

      await fillAndSubmit(user, 'testuser', 'short');

      expect(screen.getByTestId('password-error')).toBeInTheDocument();
      expect(screen.getByText('パスワードは8文字以上です')).toBeInTheDocument();
    });

    it('clears error when input is corrected', async () => {
      const user = setupUser();
      render(<LoginForm />);

      await fillAndSubmit(user, '', '');
      expect(screen.getByTestId('username-error')).toBeInTheDocument();

      await user.type(usernameInput(), 'testuser');
      expect(screen.queryByTestId('username-error')).not.toBeInTheDocument();
    });
  });

  describe('form submission', () => {
    it('calls login with trimmed credentials', async () => {
      const user = setupUser();
      mockLogin.mockResolvedValue(undefined);
      render(<LoginForm />);

      await fillAndSubmit(user, '  testuser  ', '  password123  ');

      await waitFor(() => expect(mockLogin).toHaveBeenCalledWith('testuser', 'password123'));
    });

    it('disables form during submission', async () => {
      const user = setupUser();
      mockLogin.mockImplementation(() => delay(100));
      render(<LoginForm />);

      await fillAndSubmit(user, 'testuser', 'password123');

      expect(usernameInput()).toBeDisabled();
      expect(passwordInput()).toBeDisabled();
      expect(submitButton()).toBeDisabled();
      expect(screen.getByText('ログイン中...')).toBeInTheDocument();
    });

    it('shows error message on login failure', async () => {
      const user = setupUser();
      mockLogin.mockRejectedValue(new Error('ユーザー名またはパスワードが正しくありません'));
      render(<LoginForm />);

      await fillAndSubmit(user, 'testuser', 'wrongpassword');

      await waitFor(() => {
        expect(screen.getByTestId('login-error')).toBeInTheDocument();
        expect(
          screen.getByText('ユーザー名またはパスワードが正しくありません')
        ).toBeInTheDocument();
      });
    });

    it('shows generic error message on unknown error', async () => {
      const user = setupUser();
      mockLogin.mockRejectedValue('unknown error');
      render(<LoginForm />);

      await fillAndSubmit(user, 'testuser', 'password123');

      await waitFor(() => {
        expect(screen.getByTestId('login-error')).toBeInTheDocument();
        expect(screen.getByText('ログインに失敗しました')).toBeInTheDocument();
      });
    });

    it('clears login error on new submission', async () => {
      const user = setupUser();
      mockLogin.mockRejectedValueOnce(new Error('Error')).mockResolvedValueOnce(undefined);
      render(<LoginForm />);

      await fillAndSubmit(user, 'testuser', 'password123');
      await waitFor(() => expect(screen.getByTestId('login-error')).toBeInTheDocument());

      await user.click(submitButton());
      await waitFor(() => expect(screen.queryByTestId('login-error')).not.toBeInTheDocument());
    });

    it('re-enables form after submission completes', async () => {
      const user = setupUser();
      mockLogin.mockRejectedValue(new Error('Error'));
      render(<LoginForm />);

      await fillAndSubmit(user, 'testuser', 'password123');

      await waitFor(() => {
        expect(usernameInput()).not.toBeDisabled();
        expect(passwordInput()).not.toBeDisabled();
        expect(submitButton()).not.toBeDisabled();
      });
    });
  });

  describe('input handling', () => {
    it('updates input values on typing', async () => {
      const user = setupUser();
      render(<LoginForm />);

      await user.clear(usernameInput());
      await user.type(usernameInput(), 'newuser');
      expect(usernameInput()).toHaveValue('newuser');

      await user.clear(passwordInput());
      await user.type(passwordInput(), 'newpassword');
      expect(passwordInput()).toHaveValue('newpassword');
    });
  });

  describe('accessibility', () => {
    it('error message has alert role', async () => {
      const user = setupUser();
      mockLogin.mockRejectedValue(new Error('Error'));
      render(<LoginForm />);

      await fillAndSubmit(user, 'testuser', 'password123');

      await waitFor(() => expect(screen.getByRole('alert')).toBeInTheDocument());
    });
  });
});
