import { describe, it, expect, vi, beforeEach } from 'vitest';
import { render, screen, waitFor } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { CreateAutoJournalPatternForm } from './CreateAutoJournalPatternForm';
import { createAutoJournalPattern } from '../../api/createAutoJournalPattern';

vi.mock('../../api/createAutoJournalPattern', () => ({
  createAutoJournalPattern: vi.fn(),
  getCreateAutoJournalPatternErrorMessage: (error: unknown) =>
    error instanceof Error ? error.message : '自動仕訳パターン登録に失敗しました',
}));

const mockCreate = vi.mocked(createAutoJournalPattern);

const setupUser = () => userEvent.setup();
const patternCodeInput = () => screen.getByTestId('pattern-code-input');
const patternNameInput = () => screen.getByTestId('pattern-name-input');
const sourceTableInput = () => screen.getByTestId('source-table-input');
const submitButton = () => screen.getByTestId('create-pattern-submit');

const fillRequiredFields = async (user: ReturnType<typeof userEvent.setup>) => {
  await user.type(patternCodeInput(), 'P001');
  await user.type(patternNameInput(), '売上パターン');
  await user.type(sourceTableInput(), 'sales');
};

describe('CreateAutoJournalPatternForm', () => {
  beforeEach(() => {
    vi.clearAllMocks();
  });

  it('フォームフィールドが表示される', () => {
    render(<CreateAutoJournalPatternForm />);

    expect(patternCodeInput()).toBeInTheDocument();
    expect(patternNameInput()).toBeInTheDocument();
    expect(sourceTableInput()).toBeInTheDocument();
    expect(screen.getByTestId('description-input')).toBeInTheDocument();
    expect(screen.getByTestId('add-item-button')).toBeInTheDocument();
    expect(submitButton()).toBeInTheDocument();
  });

  it('必須フィールドが空の場合バリデーションエラーを表示する', async () => {
    const user = setupUser();
    render(<CreateAutoJournalPatternForm />);

    await user.click(submitButton());

    expect(screen.getByText('パターンコードを入力してください')).toBeInTheDocument();
  });

  it('登録成功時に成功メッセージを表示する', async () => {
    const user = setupUser();
    mockCreate.mockResolvedValue({ success: true, patternId: 1 });
    render(<CreateAutoJournalPatternForm />);

    await fillRequiredFields(user);
    // 明細行のフィールドを埋める
    const selects = screen.getAllByRole('combobox');
    await user.selectOptions(selects[0], 'DEBIT');
    const itemInputs = screen.getByTestId('item-row-0').querySelectorAll('input[type="text"]');
    await user.type(itemInputs[0], '1100');
    await user.type(itemInputs[1], 'amount');

    await user.click(submitButton());

    await waitFor(() => {
      expect(screen.getByText('自動仕訳パターン登録が完了しました')).toBeInTheDocument();
    });
  });

  it('登録失敗時にエラーメッセージを表示する', async () => {
    const user = setupUser();
    mockCreate.mockRejectedValue(new Error('コードが重複しています'));
    render(<CreateAutoJournalPatternForm />);

    await fillRequiredFields(user);
    const selects = screen.getAllByRole('combobox');
    await user.selectOptions(selects[0], 'DEBIT');
    const itemInputs = screen.getByTestId('item-row-0').querySelectorAll('input[type="text"]');
    await user.type(itemInputs[0], '1100');
    await user.type(itemInputs[1], 'amount');

    await user.click(submitButton());

    await waitFor(() => {
      expect(screen.getByTestId('create-auto-journal-pattern-error')).toBeInTheDocument();
    });
  });

  it('送信中はボタンが無効になる', async () => {
    const user = setupUser();
    mockCreate.mockImplementation(() => new Promise(() => {})); // never resolves
    render(<CreateAutoJournalPatternForm />);

    await fillRequiredFields(user);
    const selects = screen.getAllByRole('combobox');
    await user.selectOptions(selects[0], 'DEBIT');
    const itemInputs = screen.getByTestId('item-row-0').querySelectorAll('input[type="text"]');
    await user.type(itemInputs[0], '1100');
    await user.type(itemInputs[1], 'amount');

    await user.click(submitButton());

    await waitFor(() => {
      expect(submitButton()).toBeDisabled();
    });
  });
});
