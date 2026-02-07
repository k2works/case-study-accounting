import { describe, it, expect, vi, beforeEach } from 'vitest';
import { render, screen, fireEvent } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { LedgerFilterBase } from './LedgerFilterBase';
import { getAccounts } from '../../api/getAccounts';
import type { Account } from '../../api/getAccounts';

vi.mock('../../api/getAccounts', () => ({
  getAccounts: vi.fn(),
  getAccountsErrorMessage: (error: unknown) =>
    error instanceof Error ? error.message : '勘定科目一覧の取得に失敗しました',
}));

vi.mock('../common', () => ({
  Button: ({ children, onClick, ...rest }: { children: React.ReactNode; onClick?: () => void }) => (
    <button type="button" onClick={onClick} {...rest}>
      {children}
    </button>
  ),
  Loading: ({ message }: { message?: string }) => <div data-testid="loading">{message}</div>,
  ErrorMessage: ({ message, onRetry }: { message: string; onRetry?: () => void }) => (
    <div>
      <div data-testid="error-message">{message}</div>
      {onRetry && (
        <button type="button" onClick={onRetry}>
          再試行
        </button>
      )}
    </div>
  ),
}));

const mockGetAccounts = vi.mocked(getAccounts);

const defaultValues = {
  accountId: '',
  dateFrom: '',
  dateTo: '',
};

const mockAccounts: Account[] = [
  { accountId: 1, accountCode: '1000', accountName: '現金', accountType: 'ASSET' },
  { accountId: 2, accountCode: '2000', accountName: '売上', accountType: 'REVENUE' },
];

const createDeferred = <T,>() => {
  let resolve: (value: T) => void;
  let reject: (reason?: unknown) => void;
  const promise = new Promise<T>((res, rej) => {
    resolve = res;
    reject = rej;
  });
  return { promise, resolve: resolve!, reject: reject! };
};

describe('LedgerFilterBase', () => {
  beforeEach(() => {
    vi.clearAllMocks();
  });

  it('初期表示で勘定科目が読み込み中になる', async () => {
    const deferred = createDeferred<Account[]>();
    mockGetAccounts.mockReturnValueOnce(deferred.promise);

    render(
      <LedgerFilterBase
        testIdPrefix="ledger"
        values={defaultValues}
        onChange={vi.fn()}
        onSearch={vi.fn()}
      />
    );

    const loading = await screen.findByTestId('loading');
    expect(loading).toHaveTextContent('勘定科目を読み込み中...');

    deferred.resolve(mockAccounts);
    await screen.findByRole('option', { name: '1000 現金' });
  });

  it('勘定科目選択で onChange が呼ばれる', async () => {
    mockGetAccounts.mockResolvedValueOnce(mockAccounts);
    const onChange = vi.fn();
    const user = userEvent.setup();

    render(
      <LedgerFilterBase
        testIdPrefix="ledger"
        values={defaultValues}
        onChange={onChange}
        onSearch={vi.fn()}
      />
    );

    await screen.findByRole('option', { name: '1000 現金' });
    const accountSelect = screen.getByLabelText('勘定科目');
    await user.selectOptions(accountSelect, '1');

    expect(onChange).toHaveBeenCalledWith({ ...defaultValues, accountId: '1' });
  });

  it('日付入力で onChange が呼ばれる', async () => {
    mockGetAccounts.mockResolvedValueOnce(mockAccounts);
    const onChange = vi.fn();

    render(
      <LedgerFilterBase
        testIdPrefix="ledger"
        values={defaultValues}
        onChange={onChange}
        onSearch={vi.fn()}
      />
    );

    await screen.findByRole('option', { name: '1000 現金' });
    const dateFromInput = screen.getByLabelText('期間（開始）');
    const dateToInput = screen.getByLabelText('期間（終了）');

    fireEvent.change(dateFromInput, { target: { value: '2024-01-01' } });
    expect(onChange).toHaveBeenCalledWith({ ...defaultValues, dateFrom: '2024-01-01' });

    fireEvent.change(dateToInput, { target: { value: '2024-01-31' } });
    expect(onChange).toHaveBeenCalledWith({ ...defaultValues, dateTo: '2024-01-31' });
  });

  it('照会ボタンクリックで onSearch が呼ばれる', async () => {
    mockGetAccounts.mockResolvedValueOnce(mockAccounts);
    const onSearch = vi.fn();
    const user = userEvent.setup();

    render(
      <LedgerFilterBase
        testIdPrefix="ledger"
        values={defaultValues}
        onChange={vi.fn()}
        onSearch={onSearch}
      />
    );

    await screen.findByRole('option', { name: '1000 現金' });
    await user.click(screen.getByRole('button', { name: '照会' }));

    expect(onSearch).toHaveBeenCalled();
  });

  it('API エラー時にエラー表示とリトライができる', async () => {
    mockGetAccounts
      .mockRejectedValueOnce(new Error('取得エラー'))
      .mockResolvedValueOnce(mockAccounts);
    const user = userEvent.setup();

    render(
      <LedgerFilterBase
        testIdPrefix="ledger"
        values={defaultValues}
        onChange={vi.fn()}
        onSearch={vi.fn()}
      />
    );

    const errorMessage = await screen.findByTestId('error-message');
    expect(errorMessage).toHaveTextContent('取得エラー');

    await user.click(screen.getByRole('button', { name: '再試行' }));

    expect(mockGetAccounts).toHaveBeenCalledTimes(2);
    await screen.findByRole('option', { name: '1000 現金' });
  });

  it('testIdPrefix で data-testid が生成される', async () => {
    mockGetAccounts.mockResolvedValueOnce(mockAccounts);

    render(
      <LedgerFilterBase
        testIdPrefix="general-ledger"
        values={defaultValues}
        onChange={vi.fn()}
        onSearch={vi.fn()}
      />
    );

    await screen.findByRole('option', { name: '1000 現金' });
    expect(screen.getByTestId('general-ledger-filter')).toBeInTheDocument();
    expect(screen.getByLabelText('勘定科目')).toHaveAttribute(
      'id',
      'general-ledger-filter-account'
    );
    expect(screen.getByLabelText('期間（開始）')).toHaveAttribute(
      'id',
      'general-ledger-filter-date-from'
    );
    expect(screen.getByLabelText('期間（終了）')).toHaveAttribute(
      'id',
      'general-ledger-filter-date-to'
    );
  });
});
