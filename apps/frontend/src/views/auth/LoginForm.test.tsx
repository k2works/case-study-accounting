import { describe, it, expect, vi, beforeEach } from 'vitest';
import { render, screen, waitFor } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { LoginForm } from './LoginForm';

const mockLogin = vi.fn();

vi.mock('../../hooks/useAuth', () => ({
  useAuth: () => ({
    login: mockLogin,
  }),
}));

describe('LoginForm', () => {
  beforeEach(() => {
    vi.clearAllMocks();
  });

  it('renders login form', () => {
    render(<LoginForm />);
    expect(screen.getByTestId('login-form')).toBeInTheDocument();
  });

  it('renders username input', () => {
    render(<LoginForm />);
    expect(screen.getByTestId('username-input')).toBeInTheDocument();
    expect(screen.getByLabelText(/ユーザー名/)).toBeInTheDocument();
  });

  it('renders password input', () => {
    render(<LoginForm />);
    expect(screen.getByTestId('password-input')).toBeInTheDocument();
    expect(screen.getByLabelText(/パスワード/)).toBeInTheDocument();
  });

  it('renders submit button', () => {
    render(<LoginForm />);
    expect(screen.getByTestId('login-submit')).toBeInTheDocument();
    expect(screen.getByRole('button', { name: 'ログイン' })).toBeInTheDocument();
  });

  describe('form validation', () => {
    it('shows error when username is empty', async () => {
      const user = userEvent.setup();
      render(<LoginForm />);

      await user.clear(screen.getByTestId('username-input'));
      await user.clear(screen.getByTestId('password-input'));
      await user.type(screen.getByTestId('password-input'), 'password123');
      await user.click(screen.getByTestId('login-submit'));

      expect(screen.getByTestId('username-error')).toBeInTheDocument();
      expect(screen.getByText('ユーザー名を入力してください')).toBeInTheDocument();
    });

    it('shows error when password is empty', async () => {
      const user = userEvent.setup();
      render(<LoginForm />);

      await user.clear(screen.getByTestId('username-input'));
      await user.clear(screen.getByTestId('password-input'));
      await user.type(screen.getByTestId('username-input'), 'testuser');
      await user.click(screen.getByTestId('login-submit'));

      expect(screen.getByTestId('password-error')).toBeInTheDocument();
      expect(screen.getByText('パスワードを入力してください')).toBeInTheDocument();
    });

    it('shows error when password is too short', async () => {
      const user = userEvent.setup();
      render(<LoginForm />);

      await user.clear(screen.getByTestId('username-input'));
      await user.clear(screen.getByTestId('password-input'));
      await user.type(screen.getByTestId('username-input'), 'testuser');
      await user.type(screen.getByTestId('password-input'), 'short');
      await user.click(screen.getByTestId('login-submit'));

      expect(screen.getByTestId('password-error')).toBeInTheDocument();
      expect(screen.getByText('パスワードは8文字以上です')).toBeInTheDocument();
    });

    it('clears error when input is corrected', async () => {
      const user = userEvent.setup();
      render(<LoginForm />);

      await user.clear(screen.getByTestId('username-input'));
      await user.clear(screen.getByTestId('password-input'));
      await user.click(screen.getByTestId('login-submit'));

      expect(screen.getByTestId('username-error')).toBeInTheDocument();

      await user.type(screen.getByTestId('username-input'), 'testuser');
      expect(screen.queryByTestId('username-error')).not.toBeInTheDocument();
    });
  });

  describe('form submission', () => {
    it('calls login with trimmed credentials', async () => {
      const user = userEvent.setup();
      mockLogin.mockResolvedValue(undefined);
      render(<LoginForm />);

      await user.clear(screen.getByTestId('username-input'));
      await user.clear(screen.getByTestId('password-input'));
      await user.type(screen.getByTestId('username-input'), '  testuser  ');
      await user.type(screen.getByTestId('password-input'), '  password123  ');
      await user.click(screen.getByTestId('login-submit'));

      await waitFor(() => {
        expect(mockLogin).toHaveBeenCalledWith('testuser', 'password123');
      });
    });

    it('disables form during submission', async () => {
      const user = userEvent.setup();
      mockLogin.mockImplementation(() => new Promise((resolve) => setTimeout(resolve, 100)));
      render(<LoginForm />);

      await user.clear(screen.getByTestId('username-input'));
      await user.clear(screen.getByTestId('password-input'));
      await user.type(screen.getByTestId('username-input'), 'testuser');
      await user.type(screen.getByTestId('password-input'), 'password123');
      await user.click(screen.getByTestId('login-submit'));

      expect(screen.getByTestId('username-input')).toBeDisabled();
      expect(screen.getByTestId('password-input')).toBeDisabled();
      expect(screen.getByTestId('login-submit')).toBeDisabled();
      expect(screen.getByText('ログイン中...')).toBeInTheDocument();
    });

    it('shows error message on login failure', async () => {
      const user = userEvent.setup();
      mockLogin.mockRejectedValue(new Error('ユーザー名またはパスワードが正しくありません'));
      render(<LoginForm />);

      await user.clear(screen.getByTestId('username-input'));
      await user.clear(screen.getByTestId('password-input'));
      await user.type(screen.getByTestId('username-input'), 'testuser');
      await user.type(screen.getByTestId('password-input'), 'wrongpassword');
      await user.click(screen.getByTestId('login-submit'));

      await waitFor(() => {
        expect(screen.getByTestId('login-error')).toBeInTheDocument();
        expect(
          screen.getByText('ユーザー名またはパスワードが正しくありません')
        ).toBeInTheDocument();
      });
    });

    it('shows generic error message on unknown error', async () => {
      const user = userEvent.setup();
      mockLogin.mockRejectedValue('unknown error');
      render(<LoginForm />);

      await user.clear(screen.getByTestId('username-input'));
      await user.clear(screen.getByTestId('password-input'));
      await user.type(screen.getByTestId('username-input'), 'testuser');
      await user.type(screen.getByTestId('password-input'), 'password123');
      await user.click(screen.getByTestId('login-submit'));

      await waitFor(() => {
        expect(screen.getByTestId('login-error')).toBeInTheDocument();
        expect(screen.getByText('ログインに失敗しました')).toBeInTheDocument();
      });
    });

    it('clears login error on new submission', async () => {
      const user = userEvent.setup();
      mockLogin.mockRejectedValueOnce(new Error('Error')).mockResolvedValueOnce(undefined);
      render(<LoginForm />);

      await user.clear(screen.getByTestId('username-input'));
      await user.clear(screen.getByTestId('password-input'));
      await user.type(screen.getByTestId('username-input'), 'testuser');
      await user.type(screen.getByTestId('password-input'), 'password123');
      await user.click(screen.getByTestId('login-submit'));

      await waitFor(() => {
        expect(screen.getByTestId('login-error')).toBeInTheDocument();
      });

      await user.click(screen.getByTestId('login-submit'));

      await waitFor(() => {
        expect(screen.queryByTestId('login-error')).not.toBeInTheDocument();
      });
    });

    it('re-enables form after submission completes', async () => {
      const user = userEvent.setup();
      mockLogin.mockRejectedValue(new Error('Error'));
      render(<LoginForm />);

      await user.clear(screen.getByTestId('username-input'));
      await user.clear(screen.getByTestId('password-input'));
      await user.type(screen.getByTestId('username-input'), 'testuser');
      await user.type(screen.getByTestId('password-input'), 'password123');
      await user.click(screen.getByTestId('login-submit'));

      await waitFor(() => {
        expect(screen.getByTestId('username-input')).not.toBeDisabled();
        expect(screen.getByTestId('password-input')).not.toBeDisabled();
        expect(screen.getByTestId('login-submit')).not.toBeDisabled();
      });
    });
  });

  describe('input handling', () => {
    it('updates username on input', async () => {
      const user = userEvent.setup();
      render(<LoginForm />);

      await user.clear(screen.getByTestId('username-input'));
      await user.type(screen.getByTestId('username-input'), 'newuser');

      expect(screen.getByTestId('username-input')).toHaveValue('newuser');
    });

    it('updates password on input', async () => {
      const user = userEvent.setup();
      render(<LoginForm />);

      await user.clear(screen.getByTestId('password-input'));
      await user.type(screen.getByTestId('password-input'), 'newpassword');

      expect(screen.getByTestId('password-input')).toHaveValue('newpassword');
    });
  });

  describe('accessibility', () => {
    it('has proper labels for inputs', () => {
      render(<LoginForm />);
      expect(screen.getByLabelText(/ユーザー名/)).toBeInTheDocument();
      expect(screen.getByLabelText(/パスワード/)).toBeInTheDocument();
    });

    it('error message has alert role', async () => {
      const user = userEvent.setup();
      mockLogin.mockRejectedValue(new Error('Error'));
      render(<LoginForm />);

      await user.clear(screen.getByTestId('username-input'));
      await user.clear(screen.getByTestId('password-input'));
      await user.type(screen.getByTestId('username-input'), 'testuser');
      await user.type(screen.getByTestId('password-input'), 'password123');
      await user.click(screen.getByTestId('login-submit'));

      await waitFor(() => {
        expect(screen.getByRole('alert')).toBeInTheDocument();
      });
    });
  });
});
