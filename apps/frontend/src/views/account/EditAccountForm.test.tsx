import { describe, it, expect, vi, beforeEach } from 'vitest';
import { render, screen, waitFor } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { EditAccountForm } from './EditAccountForm';
import { updateAccount } from '../../api/updateAccount';
import type { Account } from '../../api/getAccounts';

vi.mock('../../api/updateAccount', () => ({
  updateAccount: vi.fn(),
  getUpdateAccountErrorMessage: (error: unknown) =>
    error instanceof Error ? error.message : '勘定科目の更新に失敗しました',
}));

const mockUpdateAccount = vi.mocked(updateAccount);

const setupUser = () => userEvent.setup();
const accountCodeInput = () => screen.getByTestId('edit-account-code-input');
const accountNameInput = () => screen.getByTestId('edit-account-name-input');
const accountTypeSelect = () => screen.getByTestId('edit-account-type-select');
const submitButton = () => screen.getByTestId('edit-account-submit');
const delay = (ms: number) =>
  new Promise<{ success: boolean }>((resolve) => {
    setTimeout(() => resolve({ success: true }), ms);
  });

const baseAccount: Account = {
  accountId: 1,
  accountCode: '1000',
  accountName: '現金',
  accountType: 'ASSET',
};

describe('EditAccountForm', () => {
  beforeEach(() => {
    vi.clearAllMocks();
  });

  it('フォームが正しく表示される', () => {
    render(<EditAccountForm account={baseAccount} onSuccess={vi.fn()} />);

    expect(screen.getByTestId('edit-account-form')).toBeInTheDocument();
    expect(accountCodeInput()).toBeInTheDocument();
    expect(accountNameInput()).toBeInTheDocument();
    expect(accountTypeSelect()).toBeInTheDocument();
    expect(submitButton()).toBeInTheDocument();
  });

  it('初期値が正しくセットされる', () => {
    render(<EditAccountForm account={baseAccount} onSuccess={vi.fn()} />);

    expect(accountCodeInput()).toHaveValue('1000');
    expect(accountCodeInput()).toBeDisabled();
    expect(accountNameInput()).toHaveValue('現金');
    expect(accountTypeSelect()).toHaveValue('ASSET');
  });

  it('バリデーションエラーの表示（空の勘定科目名、未選択の勘定科目種別）', async () => {
    const user = setupUser();
    render(<EditAccountForm account={baseAccount} onSuccess={vi.fn()} />);

    await user.clear(accountNameInput());
    await user.selectOptions(accountTypeSelect(), ['']);
    await user.click(submitButton());

    expect(screen.getByTestId('edit-account-name-error')).toBeInTheDocument();
    expect(screen.getByText('勘定科目名を入力してください')).toBeInTheDocument();
    expect(screen.getByTestId('edit-account-type-error')).toBeInTheDocument();
    expect(screen.getByText('勘定科目種別を選択してください')).toBeInTheDocument();
  });

  it('更新成功時に onSuccess が呼ばれる', async () => {
    const user = setupUser();
    const onSuccess = vi.fn();
    mockUpdateAccount.mockResolvedValue({
      success: true,
      message: '勘定科目を更新しました',
    });
    render(<EditAccountForm account={baseAccount} onSuccess={onSuccess} />);

    await user.clear(accountNameInput());
    await user.type(accountNameInput(), '  普通預金  ');
    await user.selectOptions(accountTypeSelect(), ['ASSET']);
    await user.click(submitButton());

    await waitFor(() => {
      expect(mockUpdateAccount).toHaveBeenCalledWith(1, {
        accountName: '普通預金',
        accountType: 'ASSET',
      });
      expect(onSuccess).toHaveBeenCalledWith('勘定科目を更新しました');
    });
  });

  it('更新失敗時のエラーメッセージ表示', async () => {
    const user = setupUser();
    mockUpdateAccount.mockResolvedValue({
      success: false,
      errorMessage: '更新に失敗しました',
    });
    render(<EditAccountForm account={baseAccount} onSuccess={vi.fn()} />);

    await user.click(submitButton());

    await waitFor(() => {
      expect(screen.getByTestId('edit-account-error')).toBeInTheDocument();
      expect(screen.getByText('更新に失敗しました')).toBeInTheDocument();
    });
  });

  it('送信中のボタン無効化', async () => {
    const user = setupUser();
    mockUpdateAccount.mockImplementation(() => delay(100));
    render(<EditAccountForm account={baseAccount} onSuccess={vi.fn()} />);

    await user.click(submitButton());

    expect(accountNameInput()).toBeDisabled();
    expect(accountTypeSelect()).toBeDisabled();
    expect(submitButton()).toBeDisabled();
    expect(screen.getByText('更新中...')).toBeInTheDocument();
  });
});
