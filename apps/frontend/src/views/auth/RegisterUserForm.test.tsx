import { describe, it, expect, vi, beforeEach } from 'vitest';
import { render, screen, waitFor } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { RegisterUserForm } from './RegisterUserForm';
import { registerUser } from '../../api/registerUser';

vi.mock('../../api/registerUser', () => ({
  registerUser: vi.fn(),
  getRegisterErrorMessage: (error: unknown) =>
    error instanceof Error ? error.message : 'ユーザー登録に失敗しました',
}));

const mockRegisterUser = vi.mocked(registerUser);

const setupUser = () => userEvent.setup();
const usernameInput = () => screen.getByTestId('register-username-input');
const emailInput = () => screen.getByTestId('register-email-input');
const passwordInput = () => screen.getByTestId('register-password-input');
const confirmPasswordInput = () => screen.getByTestId('register-confirm-password-input');
const displayNameInput = () => screen.getByTestId('register-display-name-input');
const roleSelect = () => screen.getByTestId('register-role-select');
const submitButton = () => screen.getByTestId('register-submit');
const delay = (ms: number) =>
  new Promise<{ success: boolean }>((resolve) => setTimeout(() => resolve({ success: true }), ms));

const fillRequiredFields = async (
  user: ReturnType<typeof userEvent.setup>,
  overrides?: Partial<{
    username: string;
    email: string;
    password: string;
    confirmPassword: string;
    displayName: string;
    role: string;
  }>
) => {
  const values = {
    username: 'newuser',
    email: 'newuser@example.com',
    password: 'password123',
    confirmPassword: 'password123',
    displayName: '新規ユーザー',
    role: 'USER',
    ...overrides,
  };

  await user.clear(usernameInput());
  await user.type(usernameInput(), values.username);
  await user.clear(emailInput());
  await user.type(emailInput(), values.email);
  await user.clear(passwordInput());
  await user.type(passwordInput(), values.password);
  await user.clear(confirmPasswordInput());
  await user.type(confirmPasswordInput(), values.confirmPassword);
  await user.clear(displayNameInput());
  await user.type(displayNameInput(), values.displayName);
  await user.selectOptions(roleSelect(), values.role);
};

describe('RegisterUserForm', () => {
  beforeEach(() => {
    vi.clearAllMocks();
  });

  it('renders form with all elements', () => {
    render(<RegisterUserForm />);
    expect(screen.getByTestId('register-form')).toBeInTheDocument();
    expect(usernameInput()).toBeInTheDocument();
    expect(emailInput()).toBeInTheDocument();
    expect(passwordInput()).toBeInTheDocument();
    expect(confirmPasswordInput()).toBeInTheDocument();
    expect(displayNameInput()).toBeInTheDocument();
    expect(roleSelect()).toBeInTheDocument();
    expect(submitButton()).toBeInTheDocument();
  });

  it('shows validation errors when required fields are empty', async () => {
    const user = setupUser();
    render(<RegisterUserForm />);

    await user.click(submitButton());

    expect(screen.getByTestId('username-error')).toBeInTheDocument();
    expect(screen.getByTestId('email-error')).toBeInTheDocument();
    expect(screen.getByTestId('password-error')).toBeInTheDocument();
    expect(screen.getByTestId('confirm-password-error')).toBeInTheDocument();
    expect(screen.getByTestId('display-name-error')).toBeInTheDocument();
    expect(screen.getByTestId('role-error')).toBeInTheDocument();
  });

  it('shows error when email format is invalid', async () => {
    const user = setupUser();
    render(<RegisterUserForm />);

    // Fill all required fields with valid values, except email
    await user.type(usernameInput(), 'newuser');
    await user.type(emailInput(), 'notavalidemail'); // Invalid email format (no @)
    await user.type(passwordInput(), 'password123');
    await user.type(confirmPasswordInput(), 'password123');
    await user.type(displayNameInput(), '新規ユーザー');
    // Select role using fireEvent for more reliable selection
    await user.selectOptions(roleSelect(), ['USER']);

    // Submit the form
    await user.click(submitButton());

    // Check for email format error
    await waitFor(() => {
      const emailError = screen.queryByTestId('email-error');
      expect(emailError).toBeInTheDocument();
    });
    expect(screen.getByText('メールアドレスの形式が不正です')).toBeInTheDocument();
  });

  it('shows error when password is too short', async () => {
    const user = setupUser();
    render(<RegisterUserForm />);

    await fillRequiredFields(user, { password: 'short', confirmPassword: 'short' });
    await user.click(submitButton());

    expect(screen.getByTestId('password-error')).toBeInTheDocument();
    expect(screen.getByText('パスワードは8文字以上です')).toBeInTheDocument();
  });

  it('shows error when confirm password does not match', async () => {
    const user = setupUser();
    render(<RegisterUserForm />);

    await fillRequiredFields(user, { confirmPassword: 'password456' });
    await user.click(submitButton());

    expect(screen.getByTestId('confirm-password-error')).toBeInTheDocument();
    expect(screen.getByText('パスワードが一致しません')).toBeInTheDocument();
  });

  it('calls registerUser with trimmed values', async () => {
    const user = setupUser();
    mockRegisterUser.mockResolvedValue({ success: true });
    render(<RegisterUserForm />);

    await fillRequiredFields(user, {
      username: '  newuser  ',
      email: '  newuser@example.com  ',
      displayName: '  新規ユーザー  ',
    });
    await user.click(submitButton());

    await waitFor(() =>
      expect(mockRegisterUser).toHaveBeenCalledWith({
        username: 'newuser',
        email: 'newuser@example.com',
        password: 'password123',
        displayName: '新規ユーザー',
        role: 'USER',
      })
    );
  });

  it('disables form during submission', async () => {
    const user = setupUser();
    mockRegisterUser.mockImplementation(() => delay(100));
    render(<RegisterUserForm />);

    await fillRequiredFields(user);
    await user.click(submitButton());

    expect(usernameInput()).toBeDisabled();
    expect(emailInput()).toBeDisabled();
    expect(passwordInput()).toBeDisabled();
    expect(confirmPasswordInput()).toBeDisabled();
    expect(displayNameInput()).toBeDisabled();
    expect(roleSelect()).toBeDisabled();
    expect(submitButton()).toBeDisabled();
    expect(screen.getByText('登録中...')).toBeInTheDocument();
  });

  it('shows success message on successful registration', async () => {
    const user = setupUser();
    mockRegisterUser.mockResolvedValue({ success: true });
    render(<RegisterUserForm />);

    await fillRequiredFields(user);
    await user.click(submitButton());

    await waitFor(() => {
      expect(screen.getByTestId('register-success')).toBeInTheDocument();
      expect(screen.getByText('ユーザー登録が完了しました')).toBeInTheDocument();
    });
  });

  it('shows error message on registration failure response', async () => {
    const user = setupUser();
    mockRegisterUser.mockResolvedValue({
      success: false,
      errorMessage: 'ユーザー名は既に使用されています',
    });
    render(<RegisterUserForm />);

    await fillRequiredFields(user);
    await user.click(submitButton());

    await waitFor(() => {
      expect(screen.getByTestId('register-error')).toBeInTheDocument();
      expect(screen.getByText('ユーザー名は既に使用されています')).toBeInTheDocument();
    });
  });

  it('shows error message on thrown error', async () => {
    const user = setupUser();
    mockRegisterUser.mockRejectedValue(new Error('登録に失敗しました'));
    render(<RegisterUserForm />);

    await fillRequiredFields(user);
    await user.click(submitButton());

    await waitFor(() => {
      expect(screen.getByTestId('register-error')).toBeInTheDocument();
      expect(screen.getByText('登録に失敗しました')).toBeInTheDocument();
    });
  });
});
