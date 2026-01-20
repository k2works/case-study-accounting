import { describe, it, expect, vi, beforeEach } from 'vitest';
import { render, screen, waitFor } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { CreateAccountForm } from './CreateAccountForm';
import { createAccount } from '../../api/createAccount';

vi.mock('../../api/createAccount', () => ({
  createAccount: vi.fn(),
  getCreateAccountErrorMessage: (error: unknown) =>
    error instanceof Error ? error.message : '勘定科目登録に失敗しました',
}));

const mockCreateAccount = vi.mocked(createAccount);

const setupUser = () => userEvent.setup();
const accountCodeInput = () => screen.getByTestId('create-account-code-input');
const accountNameInput = () => screen.getByTestId('create-account-name-input');
const accountTypeSelect = () => screen.getByTestId('create-account-type-select');
const submitButton = () => screen.getByTestId('create-account-submit');
const delay = (ms: number) =>
  new Promise<{ success: boolean }>((resolve) => setTimeout(() => resolve({ success: true }), ms));

const fillRequiredFields = async (
  user: ReturnType<typeof userEvent.setup>,
  overrides?: Partial<{
    accountCode: string;
    accountName: string;
    accountType: string;
  }>
) => {
  const values = {
    accountCode: '1000',
    accountName: '現金',
    accountType: 'ASSET',
    ...overrides,
  };

  await user.clear(accountCodeInput());
  await user.type(accountCodeInput(), values.accountCode);
  await user.clear(accountNameInput());
  await user.type(accountNameInput(), values.accountName);
  await user.selectOptions(accountTypeSelect(), values.accountType);
};

describe('CreateAccountForm', () => {
  beforeEach(() => {
    vi.clearAllMocks();
  });

  it('renders form with all elements', () => {
    render(<CreateAccountForm />);
    expect(screen.getByTestId('create-account-form')).toBeInTheDocument();
    expect(accountCodeInput()).toBeInTheDocument();
    expect(accountNameInput()).toBeInTheDocument();
    expect(accountTypeSelect()).toBeInTheDocument();
    expect(submitButton()).toBeInTheDocument();
  });

  it('shows validation errors when required fields are empty', async () => {
    const user = setupUser();
    render(<CreateAccountForm />);

    await user.click(submitButton());

    expect(screen.getByTestId('create-account-code-error')).toBeInTheDocument();
    expect(screen.getByTestId('create-account-name-error')).toBeInTheDocument();
    expect(screen.getByTestId('create-account-type-error')).toBeInTheDocument();
  });

  it('shows error when account code is not four digits', async () => {
    const user = setupUser();
    render(<CreateAccountForm />);

    await fillRequiredFields(user, { accountCode: '12a' });
    await user.click(submitButton());

    expect(screen.getByTestId('create-account-code-error')).toBeInTheDocument();
    expect(screen.getByText('勘定科目コードは4桁の数字で入力してください')).toBeInTheDocument();
  });

  it('calls createAccount with trimmed values', async () => {
    const user = setupUser();
    mockCreateAccount.mockResolvedValue({ success: true });
    render(<CreateAccountForm />);

    await fillRequiredFields(user, {
      accountCode: '  1000  ',
      accountName: '  現金  ',
    });
    await user.click(submitButton());

    await waitFor(() =>
      expect(mockCreateAccount).toHaveBeenCalledWith({
        accountCode: '1000',
        accountName: '現金',
        accountType: 'ASSET',
      })
    );
  });

  it('disables form during submission', async () => {
    const user = setupUser();
    mockCreateAccount.mockImplementation(() => delay(100));
    render(<CreateAccountForm />);

    await fillRequiredFields(user);
    await user.click(submitButton());

    expect(accountCodeInput()).toBeDisabled();
    expect(accountNameInput()).toBeDisabled();
    expect(accountTypeSelect()).toBeDisabled();
    expect(submitButton()).toBeDisabled();
    expect(screen.getByText('登録中...')).toBeInTheDocument();
  });

  it('shows success message on successful registration', async () => {
    const user = setupUser();
    mockCreateAccount.mockResolvedValue({ success: true });
    render(<CreateAccountForm />);

    await fillRequiredFields(user);
    await user.click(submitButton());

    await waitFor(() => {
      expect(screen.getByTestId('create-account-success')).toBeInTheDocument();
      expect(screen.getByText('勘定科目登録が完了しました')).toBeInTheDocument();
    });
  });

  it('shows error message on registration failure response', async () => {
    const user = setupUser();
    mockCreateAccount.mockResolvedValue({
      success: false,
      errorMessage: '勘定科目コードは既に使用されています',
    });
    render(<CreateAccountForm />);

    await fillRequiredFields(user);
    await user.click(submitButton());

    await waitFor(() => {
      expect(screen.getByTestId('create-account-error')).toBeInTheDocument();
      expect(screen.getByText('勘定科目コードは既に使用されています')).toBeInTheDocument();
    });
  });
});
