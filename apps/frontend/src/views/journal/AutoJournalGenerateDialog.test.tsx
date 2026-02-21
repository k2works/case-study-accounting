import React from 'react';
import { act, fireEvent, render, screen, waitFor } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { beforeEach, describe, expect, it, vi } from 'vitest';
import { AutoJournalGenerateDialog } from './AutoJournalGenerateDialog';
import {
  getAutoJournalPatterns,
  getAutoJournalPatternsErrorMessage,
  type AutoJournalPattern,
} from '../../api/getAutoJournalPatterns';
import {
  generateAutoJournal,
  generateAutoJournalErrorMessage,
  type GenerateAutoJournalResponse,
} from '../../api/generateAutoJournal';

vi.mock('../../api/getAutoJournalPatterns', () => ({
  getAutoJournalPatterns: vi.fn(),
  getAutoJournalPatternsErrorMessage: vi.fn((error: unknown) =>
    error instanceof Error ? error.message : '自動仕訳パターン一覧の取得に失敗しました'
  ),
}));

vi.mock('../../api/generateAutoJournal', () => ({
  generateAutoJournal: vi.fn(),
  generateAutoJournalErrorMessage: vi.fn((error: unknown) =>
    error instanceof Error ? error.message : '自動仕訳の生成に失敗しました'
  ),
}));

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
    type?: 'button' | 'submit' | 'reset';
    variant?: string;
  }) => (
    <button type={type ?? 'button'} onClick={onClick} disabled={disabled} {...rest}>
      {children}
    </button>
  ),
  Loading: ({ message }: { message?: string }) => <div>{message ?? '読み込み中...'}</div>,
}));

const mockGetAutoJournalPatterns = vi.mocked(getAutoJournalPatterns);
const mockGetAutoJournalPatternsErrorMessage = vi.mocked(getAutoJournalPatternsErrorMessage);
const mockGenerateAutoJournal = vi.mocked(generateAutoJournal);
const mockGenerateAutoJournalErrorMessage = vi.mocked(generateAutoJournalErrorMessage);

const activePattern: AutoJournalPattern = {
  patternId: 1,
  patternCode: 'P001',
  patternName: '売上計上',
  sourceTableName: 'sales',
  description: '売上から仕訳を生成',
  isActive: true,
  items: [
    {
      lineNumber: 1,
      debitCreditType: 'D',
      accountCode: '1110',
      amountFormula: 'amount',
    },
    {
      lineNumber: 2,
      debitCreditType: 'C',
      accountCode: '4110',
      amountFormula: 'amount',
    },
  ],
};

const inactivePattern: AutoJournalPattern = {
  patternId: 2,
  patternCode: 'P002',
  patternName: '無効パターン',
  sourceTableName: 'sales',
  isActive: false,
  items: [
    {
      lineNumber: 1,
      debitCreditType: 'D',
      accountCode: '9999',
      amountFormula: 'disabledAmount',
    },
  ],
};

const createDeferred = <T,>() => {
  let resolve!: (value: T) => void;
  let reject!: (reason?: unknown) => void;
  const promise = new Promise<T>((res, rej) => {
    resolve = res;
    reject = rej;
  });
  return { promise, resolve, reject };
};

const setup = (overrides?: Partial<React.ComponentProps<typeof AutoJournalGenerateDialog>>) => {
  const onClose = vi.fn();
  const onSuccess = vi.fn();

  const result = render(
    <AutoJournalGenerateDialog
      isOpen={true}
      onClose={onClose}
      onSuccess={onSuccess}
      {...overrides}
    />
  );

  return {
    ...result,
    onClose,
    onSuccess,
  };
};

describe('AutoJournalGenerateDialog', () => {
  beforeEach(() => {
    vi.clearAllMocks();
    mockGetAutoJournalPatterns.mockResolvedValue([activePattern, inactivePattern]);
    mockGenerateAutoJournal.mockResolvedValue({
      success: true,
      journalEntryId: 100,
    } satisfies GenerateAutoJournalResponse);
  });

  it('isOpen=false の場合は何も表示しない', () => {
    setup({ isOpen: false });

    expect(screen.queryByText('自動仕訳生成')).not.toBeInTheDocument();
    expect(mockGetAutoJournalPatterns).not.toHaveBeenCalled();
  });

  it('isOpen=true の場合はタイトルを表示する', async () => {
    setup({ isOpen: true });

    expect(screen.getByText('自動仕訳生成')).toBeInTheDocument();
    await waitFor(() => {
      expect(mockGetAutoJournalPatterns).toHaveBeenCalled();
    });
  });

  it('loading 中は読み込みメッセージを表示する', async () => {
    const deferred = createDeferred<AutoJournalPattern[]>();
    mockGetAutoJournalPatterns.mockReturnValueOnce(deferred.promise);

    setup({ isOpen: true });

    expect(screen.getByText('自動仕訳パターンを読み込み中...')).toBeInTheDocument();

    await act(async () => {
      deferred.resolve([activePattern]);
      await deferred.promise;
    });
  });

  it('パターン取得エラー時にエラーメッセージを表示する', async () => {
    mockGetAutoJournalPatterns.mockRejectedValueOnce(new Error('取得失敗'));
    mockGetAutoJournalPatternsErrorMessage.mockReturnValueOnce('パターン取得エラー');

    setup({ isOpen: true });

    await waitFor(() => {
      expect(screen.getByText('パターン取得エラー')).toBeInTheDocument();
    });
  });

  it('パターン選択肢には有効パターンのみ表示する', async () => {
    setup({ isOpen: true });

    await waitFor(() => {
      expect(screen.getByLabelText('自動仕訳パターン *')).toBeInTheDocument();
    });

    expect(screen.getByRole('option', { name: 'P001 - 売上計上' })).toBeInTheDocument();
    expect(screen.queryByRole('option', { name: 'P002 - 無効パターン' })).not.toBeInTheDocument();
  });

  it('パターンを選択すると PatternItemsTable を表示する', async () => {
    setup({ isOpen: true });
    const user = userEvent.setup();

    const select = await screen.findByLabelText('自動仕訳パターン *');
    await user.selectOptions(select, '1');

    expect(screen.getByText('明細行')).toBeInTheDocument();
    expect(screen.getByText('1110')).toBeInTheDocument();
    expect(screen.getAllByText('amount').length).toBeGreaterThan(0);
  });

  it('extractAmountVariables 相当の挙動で数式先頭の変数を抽出する', async () => {
    const formulaPattern: AutoJournalPattern = {
      ...activePattern,
      patternId: 3,
      patternCode: 'P003',
      patternName: '計算パターン',
      items: [
        { lineNumber: 1, debitCreditType: 'D', accountCode: '1110', amountFormula: 'amount * 0.1' },
        { lineNumber: 2, debitCreditType: 'C', accountCode: '4110', amountFormula: 'amount + 100' },
      ],
    };
    mockGetAutoJournalPatterns.mockResolvedValueOnce([formulaPattern]);

    setup({ isOpen: true });
    const user = userEvent.setup();

    const select = await screen.findByLabelText('自動仕訳パターン *');
    await user.selectOptions(select, '3');

    const amountInput = screen.getByLabelText('amount *');
    expect(amountInput).toBeInTheDocument();
    expect(screen.queryByLabelText('tax *')).not.toBeInTheDocument();
  });

  it('AmountInputFields は抽出された各変数の入力欄を表示する', async () => {
    const multiVariablePattern: AutoJournalPattern = {
      ...activePattern,
      patternId: 4,
      patternCode: 'P004',
      patternName: '複数変数パターン',
      items: [
        { lineNumber: 1, debitCreditType: 'D', accountCode: '1110', amountFormula: 'base * 0.8' },
        { lineNumber: 2, debitCreditType: 'C', accountCode: '4110', amountFormula: 'tax + 0' },
      ],
    };
    mockGetAutoJournalPatterns.mockResolvedValueOnce([multiVariablePattern]);

    setup({ isOpen: true });
    const user = userEvent.setup();

    const select = await screen.findByLabelText('自動仕訳パターン *');
    await user.selectOptions(select, '4');

    expect(screen.getByLabelText('base *')).toBeInTheDocument();
    expect(screen.getByLabelText('tax *')).toBeInTheDocument();
  });

  it('金額検証: 空入力の場合はエラーを表示する', async () => {
    setup({ isOpen: true });
    const user = userEvent.setup();

    const select = await screen.findByLabelText('自動仕訳パターン *');
    await user.selectOptions(select, '1');
    await user.click(screen.getByRole('button', { name: '生成' }));

    expect(screen.getByText('amount の金額を入力してください')).toBeInTheDocument();
  });

  it('金額検証: 非数値の場合はエラーを表示する', async () => {
    setup({ isOpen: true });
    const user = userEvent.setup();

    const select = await screen.findByLabelText('自動仕訳パターン *');
    await user.selectOptions(select, '1');

    const amountInput = screen.getByLabelText('amount *');
    const originalValueDescriptor = Object.getOwnPropertyDescriptor(amountInput, 'value');
    Object.defineProperty(amountInput, 'value', {
      configurable: true,
      get: () => 'abc',
      set: () => undefined,
    });
    fireEvent.change(amountInput);
    await user.click(screen.getByRole('button', { name: '生成' }));
    if (originalValueDescriptor) {
      Object.defineProperty(amountInput, 'value', originalValueDescriptor);
    }

    expect(screen.getByText('amount の金額は数値で入力してください')).toBeInTheDocument();
  });

  it('パターン未選択で生成すると submit エラーを表示する', async () => {
    setup({ isOpen: true });
    const user = userEvent.setup();

    await screen.findByLabelText('自動仕訳パターン *');
    await user.click(screen.getByRole('button', { name: '生成' }));

    expect(screen.getByText('自動仕訳パターンを選択してください')).toBeInTheDocument();
  });

  it('仕訳日未入力で生成すると submit エラーを表示する', async () => {
    setup({ isOpen: true });
    const user = userEvent.setup();

    const select = await screen.findByLabelText('自動仕訳パターン *');
    await user.selectOptions(select, '1');
    await user.type(screen.getByLabelText('amount *'), '1000');

    const dateInput = screen.getByLabelText('仕訳日 *');
    await user.clear(dateInput);
    await user.click(screen.getByRole('button', { name: '生成' }));

    expect(screen.getByText('仕訳日を入力してください')).toBeInTheDocument();
  });

  it('生成成功時に onSuccess と onClose を呼ぶ', async () => {
    const { onClose, onSuccess } = setup({ isOpen: true });
    const user = userEvent.setup();

    const select = await screen.findByLabelText('自動仕訳パターン *');
    await user.selectOptions(select, '1');
    await user.type(screen.getByLabelText('amount *'), '1200');
    await user.click(screen.getByRole('button', { name: '生成' }));

    await waitFor(() => {
      expect(mockGenerateAutoJournal).toHaveBeenCalledWith(
        expect.objectContaining({
          patternId: 1,
          amounts: { amount: 1200 },
        })
      );
      expect(onClose).toHaveBeenCalledTimes(1);
      expect(onSuccess).toHaveBeenCalledWith(100);
    });
  });

  it('生成失敗時にエラーメッセージを表示する', async () => {
    mockGenerateAutoJournal.mockResolvedValueOnce({
      success: false,
      errorMessage: '生成に失敗しました',
    });

    setup({ isOpen: true });
    const user = userEvent.setup();

    const select = await screen.findByLabelText('自動仕訳パターン *');
    await user.selectOptions(select, '1');
    await user.type(screen.getByLabelText('amount *'), '1200');
    await user.click(screen.getByRole('button', { name: '生成' }));

    await waitFor(() => {
      expect(mockGenerateAutoJournalErrorMessage).toHaveBeenCalled();
      expect(screen.getByText('生成に失敗しました')).toBeInTheDocument();
    });
  });

  it('オーバーレイクリックで onClose を呼ぶ', async () => {
    const { onClose } = setup({ isOpen: true });
    await screen.findByText('自動仕訳生成');

    await userEvent.click(screen.getByRole('presentation'));

    expect(onClose).toHaveBeenCalledTimes(1);
  });

  it('閉じるボタン (×) クリックで onClose を呼ぶ', async () => {
    const { onClose } = setup({ isOpen: true });
    const user = userEvent.setup();

    await user.click(await screen.findByRole('button', { name: '閉じる' }));

    expect(onClose).toHaveBeenCalledTimes(1);
  });

  it('キャンセルボタン クリックで onClose を呼ぶ', async () => {
    const { onClose } = setup({ isOpen: true });
    const user = userEvent.setup();

    await user.click(await screen.findByRole('button', { name: 'キャンセル' }));

    expect(onClose).toHaveBeenCalledTimes(1);
  });

  it('ダイアログ再オープン時にフォームをリセットする', async () => {
    const onClose = vi.fn();
    const onSuccess = vi.fn();
    const user = userEvent.setup();

    const { rerender } = render(
      <AutoJournalGenerateDialog isOpen={true} onClose={onClose} onSuccess={onSuccess} />
    );

    const select = await screen.findByLabelText('自動仕訳パターン *');
    await user.selectOptions(select, '1');
    await user.type(screen.getByLabelText('amount *'), '500');
    await user.type(screen.getByLabelText('摘要'), '初回入力');

    expect(screen.getByLabelText('摘要')).toHaveValue('初回入力');

    rerender(<AutoJournalGenerateDialog isOpen={false} onClose={onClose} onSuccess={onSuccess} />);
    expect(screen.queryByText('自動仕訳生成')).not.toBeInTheDocument();

    rerender(<AutoJournalGenerateDialog isOpen={true} onClose={onClose} onSuccess={onSuccess} />);

    await waitFor(() => {
      expect(mockGetAutoJournalPatterns).toHaveBeenCalledTimes(2);
    });

    expect(screen.getByLabelText('自動仕訳パターン *')).toHaveValue('');
    expect(screen.getByLabelText('摘要')).toHaveValue('');
    expect(screen.queryByLabelText('amount *')).not.toBeInTheDocument();
  });

  it('isSubmitting 中は入力とボタンを無効化し、生成中... を表示する', async () => {
    const deferred = createDeferred<GenerateAutoJournalResponse>();
    mockGenerateAutoJournal.mockReturnValueOnce(deferred.promise);

    setup({ isOpen: true });
    const user = userEvent.setup();

    const select = await screen.findByLabelText('自動仕訳パターン *');
    await user.selectOptions(select, '1');
    await user.type(screen.getByLabelText('amount *'), '1000');
    await user.click(screen.getByRole('button', { name: '生成' }));

    expect(screen.getByRole('button', { name: '生成中...' })).toBeDisabled();
    expect(screen.getByLabelText('自動仕訳パターン *')).toBeDisabled();
    expect(screen.getByLabelText('amount *')).toBeDisabled();
    expect(screen.getByLabelText('仕訳日 *')).toBeDisabled();
    expect(screen.getByLabelText('摘要')).toBeDisabled();
    expect(screen.getByRole('button', { name: 'キャンセル' })).toBeDisabled();
    expect(screen.getByRole('button', { name: '閉じる' })).toBeDisabled();

    await act(async () => {
      deferred.resolve({ success: true, journalEntryId: 101 });
      await deferred.promise;
    });
  });

  it('PatternItemsTable で貸借区分を正しく表示する (D→借方, C→貸方)', async () => {
    setup({ isOpen: true });
    const user = userEvent.setup();

    const select = await screen.findByLabelText('自動仕訳パターン *');
    await user.selectOptions(select, '1');

    expect(screen.getByText('借方')).toBeInTheDocument();
    expect(screen.getByText('貸方')).toBeInTheDocument();
  });
});
