import React from 'react';
import { describe, it, expect, vi, beforeEach } from 'vitest';
import { render, screen } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { JournalEntryFilter } from './JournalEntryFilter';
import type { JournalEntryFilterValues } from './JournalEntryFilter';

vi.mock('../common', () => ({
  Button: ({
    children,
    onClick,
    ...rest
  }: {
    children: React.ReactNode;
    onClick?: () => void;
    variant?: string;
  }) => (
    <button onClick={onClick} {...rest}>
      {children}
    </button>
  ),
}));

vi.mock('./JournalEntryFilter.css', () => ({}));

const defaultValues: JournalEntryFilterValues = {
  status: '',
  dateFrom: '',
  dateTo: '',
  accountId: '',
  amountFrom: '',
  amountTo: '',
  description: '',
};

describe('JournalEntryFilter', () => {
  const defaultProps = {
    values: defaultValues,
    onChange: vi.fn(),
    onSearch: vi.fn(),
    onReset: vi.fn(),
  };

  beforeEach(() => {
    vi.clearAllMocks();
  });

  it('フィルタコンポーネントが表示される', () => {
    render(<JournalEntryFilter {...defaultProps} />);

    expect(screen.getByTestId('journal-entry-filter')).toBeInTheDocument();
  });

  it('ステータスフィルタが表示される', () => {
    render(<JournalEntryFilter {...defaultProps} />);

    const statusSelect = screen.getByLabelText('ステータス');
    expect(statusSelect).toBeInTheDocument();
    expect(statusSelect).toHaveValue('');
  });

  it('日付フィルタが表示される', () => {
    render(<JournalEntryFilter {...defaultProps} />);

    expect(screen.getByLabelText('日付（開始）')).toBeInTheDocument();
    expect(screen.getByLabelText('日付（終了）')).toBeInTheDocument();
  });

  it('摘要フィルタが表示される', () => {
    render(<JournalEntryFilter {...defaultProps} />);

    expect(screen.getByLabelText('摘要')).toBeInTheDocument();
  });

  it('金額フィルタが表示される', () => {
    render(<JournalEntryFilter {...defaultProps} />);

    expect(screen.getByLabelText('金額（以上）')).toBeInTheDocument();
    expect(screen.getByLabelText('金額（以下）')).toBeInTheDocument();
  });

  it('勘定科目IDフィルタが表示される', () => {
    render(<JournalEntryFilter {...defaultProps} />);

    expect(screen.getByLabelText('勘定科目ID')).toBeInTheDocument();
  });

  it('ステータスを変更すると onChange が呼ばれる', async () => {
    render(<JournalEntryFilter {...defaultProps} />);

    const user = userEvent.setup();
    const statusSelect = screen.getByLabelText('ステータス');
    await user.selectOptions(statusSelect, 'DRAFT');

    expect(defaultProps.onChange).toHaveBeenCalledWith({
      ...defaultValues,
      status: 'DRAFT',
    });
  });

  it('摘要を変更すると onChange が呼ばれる', async () => {
    render(<JournalEntryFilter {...defaultProps} />);

    const user = userEvent.setup();
    const descInput = screen.getByLabelText('摘要');
    await user.type(descInput, '売上');

    expect(defaultProps.onChange).toHaveBeenCalled();
  });

  it('検索ボタンをクリックすると onSearch が呼ばれる', async () => {
    render(<JournalEntryFilter {...defaultProps} />);

    const user = userEvent.setup();
    await user.click(screen.getByText('検索'));

    expect(defaultProps.onSearch).toHaveBeenCalled();
  });

  it('リセットボタンをクリックすると onReset が呼ばれる', async () => {
    render(<JournalEntryFilter {...defaultProps} />);

    const user = userEvent.setup();
    await user.click(screen.getByText('リセット'));

    expect(defaultProps.onReset).toHaveBeenCalled();
  });

  it('設定された値が表示される', () => {
    const filledValues: JournalEntryFilterValues = {
      status: 'DRAFT',
      dateFrom: '2024-01-01',
      dateTo: '2024-01-31',
      accountId: '1',
      amountFrom: '100',
      amountTo: '5000',
      description: 'テスト',
    };

    render(<JournalEntryFilter {...defaultProps} values={filledValues} />);

    expect(screen.getByLabelText('ステータス')).toHaveValue('DRAFT');
    expect(screen.getByLabelText('日付（開始）')).toHaveValue('2024-01-01');
    expect(screen.getByLabelText('日付（終了）')).toHaveValue('2024-01-31');
    expect(screen.getByLabelText('勘定科目ID')).toHaveValue(1);
    expect(screen.getByLabelText('金額（以上）')).toHaveValue(100);
    expect(screen.getByLabelText('金額（以下）')).toHaveValue(5000);
    expect(screen.getByLabelText('摘要')).toHaveValue('テスト');
  });
});
