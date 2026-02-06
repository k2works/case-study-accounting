import React from 'react';
import { describe, it, expect, vi, beforeEach } from 'vitest';
import { render, screen, within } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { JournalEntryEditForm } from './JournalEntryEditForm';
import type { JournalEntry } from '../../api/getJournalEntry';
import type { Account } from '../../api/getAccounts';

vi.mock('../common', () => ({
  Button: ({
    children,
    onClick,
    disabled,
    type,
    ...rest
  }: {
    children: React.ReactNode;
    onClick?: () => void;
    disabled?: boolean;
    type?: string;
    variant?: string;
    size?: string;
    'data-testid'?: string;
  }) => (
    <button type={type as 'button' | 'submit'} onClick={onClick} disabled={disabled} {...rest}>
      {children}
    </button>
  ),
  ErrorMessage: ({ message }: { message: string }) => (
    <div data-testid="error-message">{message}</div>
  ),
  MoneyDisplay: ({
    amount,
    showSign,
  }: {
    amount: number;
    showSign?: boolean;
    colorize?: boolean;
  }) => (
    <span>
      {showSign && amount > 0 ? '+' : ''}
      {amount.toLocaleString()}
    </span>
  ),
}));

const mockAccounts: Account[] = [
  { accountId: 1, accountCode: '100', accountName: '現金', accountType: 'ASSET' },
  { accountId: 2, accountCode: '400', accountName: '売上', accountType: 'REVENUE' },
  { accountId: 3, accountCode: '500', accountName: '仕入', accountType: 'EXPENSE' },
];

const mockJournalEntry: JournalEntry = {
  journalEntryId: 1,
  journalDate: '2024-01-31',
  description: 'テスト仕訳',
  status: 'DRAFT',
  version: 1,
  lines: [
    {
      lineNumber: 1,
      accountId: 1,
      accountCode: '100',
      accountName: '現金',
      debitAmount: 1000,
      creditAmount: 0,
    },
    {
      lineNumber: 2,
      accountId: 2,
      accountCode: '400',
      accountName: '売上',
      debitAmount: 0,
      creditAmount: 1000,
    },
  ],
};

describe('JournalEntryEditForm', () => {
  const defaultProps = {
    accounts: mockAccounts,
    journalEntry: mockJournalEntry,
    onSubmit: vi.fn(),
    onCancel: vi.fn(),
    onDelete: vi.fn(),
    isSubmitting: false,
    isDeleting: false,
  };

  beforeEach(() => {
    vi.clearAllMocks();
  });

  it('フォームが正しく表示される', () => {
    render(<JournalEntryEditForm {...defaultProps} />);

    expect(screen.getByTestId('journal-entry-edit-form')).toBeInTheDocument();
    expect(screen.getByTestId('journal-entry-date-input')).toHaveValue('2024-01-31');
    expect(screen.getByTestId('journal-entry-description-input')).toHaveValue('テスト仕訳');
  });

  it('明細行が表示される', () => {
    render(<JournalEntryEditForm {...defaultProps} />);

    expect(screen.getByTestId('journal-entry-account-1')).toHaveValue('1');
    expect(screen.getByTestId('journal-entry-debit-1')).toHaveValue(1000);
    expect(screen.getByTestId('journal-entry-account-2')).toHaveValue('2');
    expect(screen.getByTestId('journal-entry-credit-2')).toHaveValue(1000);
  });

  it('日付を変更できる', async () => {
    render(<JournalEntryEditForm {...defaultProps} />);

    const user = userEvent.setup();
    const dateInput = screen.getByTestId('journal-entry-date-input');
    await user.clear(dateInput);
    await user.type(dateInput, '2024-02-15');

    expect(dateInput).toHaveValue('2024-02-15');
  });

  it('摘要を変更できる', async () => {
    render(<JournalEntryEditForm {...defaultProps} />);

    const user = userEvent.setup();
    const descInput = screen.getByTestId('journal-entry-description-input');
    await user.clear(descInput);
    await user.type(descInput, '更新後の摘要');

    expect(descInput).toHaveValue('更新後の摘要');
  });

  it('行を追加できる', async () => {
    render(<JournalEntryEditForm {...defaultProps} />);

    const user = userEvent.setup();
    await user.click(screen.getByTestId('journal-entry-add-line'));

    const rows = screen.getByTestId('journal-entry-edit-form').querySelectorAll('tbody tr');
    expect(rows.length).toBe(3);
  });

  it('行を削除できる', async () => {
    render(<JournalEntryEditForm {...defaultProps} />);

    const user = userEvent.setup();
    await user.click(screen.getByTestId('journal-entry-remove-1'));

    const rows = screen.getByTestId('journal-entry-edit-form').querySelectorAll('tbody tr');
    expect(rows.length).toBe(1);
  });

  it('保存ボタンとキャンセルボタンが表示される', () => {
    render(<JournalEntryEditForm {...defaultProps} />);

    expect(screen.getByTestId('journal-entry-submit')).toBeInTheDocument();
    expect(screen.getByTestId('journal-entry-cancel')).toBeInTheDocument();
    expect(screen.getByTestId('journal-entry-delete')).toBeInTheDocument();
  });

  it('キャンセルボタンをクリックすると onCancel が呼ばれる', async () => {
    render(<JournalEntryEditForm {...defaultProps} />);

    const user = userEvent.setup();
    await user.click(screen.getByTestId('journal-entry-cancel'));

    expect(defaultProps.onCancel).toHaveBeenCalled();
  });

  it('削除ボタンをクリックすると onDelete が呼ばれる', async () => {
    render(<JournalEntryEditForm {...defaultProps} />);

    const user = userEvent.setup();
    await user.click(screen.getByTestId('journal-entry-delete'));

    expect(defaultProps.onDelete).toHaveBeenCalled();
  });

  it('送信中は保存ボタンテキストが変わる', () => {
    render(<JournalEntryEditForm {...defaultProps} isSubmitting={true} />);

    expect(screen.getByTestId('journal-entry-submit')).toHaveTextContent('保存中...');
  });

  it('削除中は削除ボタンテキストが変わる', () => {
    render(<JournalEntryEditForm {...defaultProps} isDeleting={true} />);

    expect(screen.getByTestId('journal-entry-delete')).toHaveTextContent('削除中...');
  });

  it('外部エラーを表示する', () => {
    render(<JournalEntryEditForm {...defaultProps} error="更新に失敗しました" />);

    expect(screen.getByTestId('journal-entry-error')).toBeInTheDocument();
    expect(screen.getByText('更新に失敗しました')).toBeInTheDocument();
  });

  it('onDelete が未指定の場合、削除ボタンは表示されない', () => {
    const propsWithoutDelete = { ...defaultProps, onDelete: undefined };
    render(<JournalEntryEditForm {...propsWithoutDelete} />);

    expect(screen.queryByTestId('journal-entry-delete')).not.toBeInTheDocument();
  });

  it('貸借一致時に差額 0 が表示される', () => {
    render(<JournalEntryEditForm {...defaultProps} />);

    const diff = screen.getByTestId('journal-entry-diff');
    expect(diff).toHaveTextContent('0');
  });

  it('勘定科目を変更できる', async () => {
    render(<JournalEntryEditForm {...defaultProps} />);

    const user = userEvent.setup();
    const accountSelect = screen.getByTestId('journal-entry-account-1');
    await user.selectOptions(accountSelect, '3');

    expect(accountSelect).toHaveValue('3');
  });

  it('フォームを送信できる', async () => {
    const onSubmit = vi.fn().mockResolvedValue(undefined);
    render(<JournalEntryEditForm {...defaultProps} onSubmit={onSubmit} />);

    const user = userEvent.setup();
    const form = screen.getByTestId('journal-entry-edit-form');
    const submitBtn = within(form).getByTestId('journal-entry-submit');
    await user.click(submitBtn);

    expect(onSubmit).toHaveBeenCalled();
  });
});
