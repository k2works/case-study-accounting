import { describe, it, expect, vi, beforeEach } from 'vitest';
import { render, screen, waitFor } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { EditAutoJournalPatternForm } from './EditAutoJournalPatternForm';
import { updateAutoJournalPattern } from '../../api/updateAutoJournalPattern';
import type { AutoJournalPattern } from '../../api/getAutoJournalPatterns';

vi.mock('../../api/updateAutoJournalPattern', () => ({
  updateAutoJournalPattern: vi.fn(),
  getUpdateAutoJournalPatternErrorMessage: (error: unknown) =>
    error instanceof Error ? error.message : '自動仕訳パターン更新に失敗しました',
}));

const mockUpdate = vi.mocked(updateAutoJournalPattern);

const mockPattern: AutoJournalPattern = {
  patternId: 1,
  patternCode: 'P001',
  patternName: '売上パターン',
  sourceTableName: 'sales',
  description: 'テスト説明',
  isActive: true,
  items: [
    {
      lineNumber: 1,
      debitCreditType: 'DEBIT',
      accountCode: '1100',
      amountFormula: 'amount',
      descriptionTemplate: 'desc',
    },
  ],
};

describe('EditAutoJournalPatternForm', () => {
  beforeEach(() => {
    vi.clearAllMocks();
  });

  it('既存データがフォームに表示される', () => {
    render(<EditAutoJournalPatternForm pattern={mockPattern} onSuccess={vi.fn()} />);

    expect(screen.getByTestId('pattern-code-input')).toHaveValue('P001');
    expect(screen.getByTestId('pattern-code-input')).toBeDisabled();
    expect(screen.getByTestId('pattern-name-input')).toHaveValue('売上パターン');
    expect(screen.getByTestId('source-table-input')).toHaveValue('sales');
    expect(screen.getByTestId('is-active-checkbox')).toBeChecked();
  });

  it('更新成功時に onSuccess コールバックが呼ばれる', async () => {
    const user = userEvent.setup();
    const onSuccess = vi.fn();
    mockUpdate.mockResolvedValue({ success: true, message: '更新しました' });
    render(<EditAutoJournalPatternForm pattern={mockPattern} onSuccess={onSuccess} />);

    await user.clear(screen.getByTestId('pattern-name-input'));
    await user.type(screen.getByTestId('pattern-name-input'), '更新済みパターン');
    await user.click(screen.getByTestId('edit-pattern-submit'));

    await waitFor(() => {
      expect(onSuccess).toHaveBeenCalledWith('更新しました');
    });
  });

  it('更新失敗時にエラーメッセージを表示する', async () => {
    const user = userEvent.setup();
    mockUpdate.mockRejectedValue(new Error('更新エラー'));
    render(<EditAutoJournalPatternForm pattern={mockPattern} onSuccess={vi.fn()} />);

    await user.click(screen.getByTestId('edit-pattern-submit'));

    await waitFor(() => {
      expect(screen.getByTestId('edit-auto-journal-pattern-error')).toBeInTheDocument();
    });
  });

  it('パターン名が空の場合バリデーションエラーを表示する', async () => {
    const user = userEvent.setup();
    render(<EditAutoJournalPatternForm pattern={mockPattern} onSuccess={vi.fn()} />);

    await user.clear(screen.getByTestId('pattern-name-input'));
    await user.click(screen.getByTestId('edit-pattern-submit'));

    expect(screen.getByText('パターン名を入力してください')).toBeInTheDocument();
    expect(mockUpdate).not.toHaveBeenCalled();
  });

  it('isActive チェックボックスを切り替えられる', async () => {
    const user = userEvent.setup();
    render(<EditAutoJournalPatternForm pattern={mockPattern} onSuccess={vi.fn()} />);

    const checkbox = screen.getByTestId('is-active-checkbox');
    expect(checkbox).toBeChecked();
    await user.click(checkbox);
    expect(checkbox).not.toBeChecked();
  });
});
