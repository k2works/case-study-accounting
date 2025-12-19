# 第23章 単体テスト

本章では、フロントエンドの単体テスト実装について解説する。Vitest と Testing Library を使用したコンポーネントテスト、カスタムフックのテスト、MSW による API モックテストなど、品質を担保するためのテスト手法を構築していく。

## 23.1 テスト環境の構築

### Vitest の設定

```typescript
// vite.config.ts
import { defineConfig } from 'vite';
import react from '@vitejs/plugin-react';
import { resolve } from 'path';

export default defineConfig({
  plugins: [react()],
  resolve: {
    alias: {
      '@': resolve(__dirname, 'src'),
    },
  },
  test: {
    globals: true,
    environment: 'jsdom',
    setupFiles: ['./src/test/setup.ts'],
    include: ['src/**/*.{test,spec}.{js,ts,jsx,tsx}'],
    coverage: {
      provider: 'v8',
      reporter: ['text', 'html', 'lcov'],
      exclude: [
        'node_modules/',
        'src/test/',
        'src/api/generated/',
        'src/api/model/',
        '**/*.d.ts',
      ],
    },
  },
});
```

### テストセットアップファイル

```typescript
// src/test/setup.ts
import '@testing-library/jest-dom';
import { cleanup } from '@testing-library/react';
import { afterEach, beforeAll, afterAll, vi } from 'vitest';
import { server } from './mocks/server';

// React Testing Library のクリーンアップ
afterEach(() => {
  cleanup();
});

// MSW サーバーのセットアップ
beforeAll(() => {
  server.listen({ onUnhandledRequest: 'error' });
});

afterEach(() => {
  server.resetHandlers();
});

afterAll(() => {
  server.close();
});

// window.matchMedia のモック
Object.defineProperty(window, 'matchMedia', {
  writable: true,
  value: vi.fn().mockImplementation(query => ({
    matches: false,
    media: query,
    onchange: null,
    addListener: vi.fn(),
    removeListener: vi.fn(),
    addEventListener: vi.fn(),
    removeEventListener: vi.fn(),
    dispatchEvent: vi.fn(),
  })),
});

// ResizeObserver のモック
global.ResizeObserver = vi.fn().mockImplementation(() => ({
  observe: vi.fn(),
  unobserve: vi.fn(),
  disconnect: vi.fn(),
}));
```

### テストユーティリティ

```typescript
// src/test/utils.tsx
import { ReactElement } from 'react';
import { render, RenderOptions } from '@testing-library/react';
import { QueryClient, QueryClientProvider } from '@tanstack/react-query';
import { BrowserRouter } from 'react-router-dom';
import { AuthProvider } from '@/providers/AuthProvider';
import { MessageProvider } from '@/providers/MessageProvider';
import { AccountingPeriodProvider } from '@/providers/AccountingPeriodProvider';

// テスト用 QueryClient
const createTestQueryClient = () =>
  new QueryClient({
    defaultOptions: {
      queries: {
        retry: false,
        gcTime: 0,
      },
      mutations: {
        retry: false,
      },
    },
  });

// カスタムレンダラー
interface CustomRenderOptions extends Omit<RenderOptions, 'wrapper'> {
  initialRoute?: string;
  queryClient?: QueryClient;
}

export const renderWithProviders = (
  ui: ReactElement,
  options: CustomRenderOptions = {}
) => {
  const {
    initialRoute = '/',
    queryClient = createTestQueryClient(),
    ...renderOptions
  } = options;

  window.history.pushState({}, 'Test page', initialRoute);

  const Wrapper = ({ children }: { children: React.ReactNode }) => (
    <QueryClientProvider client={queryClient}>
      <BrowserRouter>
        <AuthProvider>
          <AccountingPeriodProvider>
            <MessageProvider>
              {children}
            </MessageProvider>
          </AccountingPeriodProvider>
        </AuthProvider>
      </BrowserRouter>
    </QueryClientProvider>
  );

  return {
    ...render(ui, { wrapper: Wrapper, ...renderOptions }),
    queryClient,
  };
};

// 再エクスポート
export * from '@testing-library/react';
export { renderWithProviders as render };
```

## 23.2 MSW による API モック

### MSW サーバー設定

```typescript
// src/test/mocks/server.ts
import { setupServer } from 'msw/node';
import { handlers } from './handlers';

export const server = setupServer(...handlers);
```

### ハンドラー定義

```typescript
// src/test/mocks/handlers.ts
import { http, HttpResponse } from 'msw';
import type { Account, JournalEntry, Balance } from '@/api/model';

// テストデータ
export const mockAccounts: Account[] = [
  {
    id: '1',
    accountCode: '111',
    accountName: '現金',
    bsPlType: 'B',
    debitCreditType: '借',
    elementType: '資産',
    displayOrder: 1,
    version: 1,
  },
  {
    id: '2',
    accountCode: '121',
    accountName: '普通預金',
    bsPlType: 'B',
    debitCreditType: '借',
    elementType: '資産',
    displayOrder: 2,
    version: 1,
  },
  {
    id: '3',
    accountCode: '211',
    accountName: '買掛金',
    bsPlType: 'B',
    debitCreditType: '貸',
    elementType: '負債',
    displayOrder: 10,
    version: 1,
  },
  {
    id: '4',
    accountCode: '411',
    accountName: '売上高',
    bsPlType: 'P',
    debitCreditType: '貸',
    elementType: '収益',
    displayOrder: 100,
    version: 1,
  },
];

export const mockJournalEntries: JournalEntry[] = [
  {
    id: '1',
    journalNumber: 1,
    journalDate: '2024-04-01',
    description: '売上計上',
    status: 'APPROVED',
    details: [
      {
        id: '1-1',
        accountCode: '111',
        accountName: '現金',
        debitAmount: 10000,
        creditAmount: 0,
      },
      {
        id: '1-2',
        accountCode: '411',
        accountName: '売上高',
        debitAmount: 0,
        creditAmount: 10000,
      },
    ],
    createdAt: '2024-04-01T10:00:00Z',
    createdBy: 'user1',
    version: 1,
  },
];

// ハンドラー
export const handlers = [
  // 勘定科目一覧
  http.get('/api/accounts', () => {
    return HttpResponse.json(mockAccounts);
  }),

  // 勘定科目詳細
  http.get('/api/accounts/:code', ({ params }) => {
    const account = mockAccounts.find(a => a.accountCode === params.code);
    if (!account) {
      return new HttpResponse(null, { status: 404 });
    }
    return HttpResponse.json(account);
  }),

  // 勘定科目登録
  http.post('/api/accounts', async ({ request }) => {
    const body = await request.json() as Partial<Account>;
    const newAccount: Account = {
      id: String(mockAccounts.length + 1),
      accountCode: body.accountCode!,
      accountName: body.accountName!,
      bsPlType: body.bsPlType!,
      debitCreditType: body.debitCreditType!,
      elementType: body.elementType!,
      displayOrder: mockAccounts.length + 1,
      version: 1,
    };
    return HttpResponse.json(newAccount, { status: 201 });
  }),

  // 仕訳一覧
  http.get('/api/journal-entries', ({ request }) => {
    const url = new URL(request.url);
    const page = parseInt(url.searchParams.get('page') || '0', 10);
    const size = parseInt(url.searchParams.get('size') || '20', 10);

    return HttpResponse.json({
      items: mockJournalEntries.slice(page * size, (page + 1) * size),
      totalCount: mockJournalEntries.length,
      page,
      size,
    });
  }),

  // 仕訳登録
  http.post('/api/journal-entries', async ({ request }) => {
    const body = await request.json() as Partial<JournalEntry>;
    const newEntry: JournalEntry = {
      id: String(mockJournalEntries.length + 1),
      journalNumber: mockJournalEntries.length + 1,
      journalDate: body.journalDate!,
      description: body.description!,
      status: 'DRAFT',
      details: body.details || [],
      createdAt: new Date().toISOString(),
      createdBy: 'test-user',
      version: 1,
    };
    return HttpResponse.json(newEntry, { status: 201 });
  }),

  // 認証
  http.post('/api/auth/login', async ({ request }) => {
    const body = await request.json() as { username: string; password: string };
    if (body.username === 'test' && body.password === 'password') {
      return HttpResponse.json({
        accessToken: 'mock-access-token',
        user: {
          id: '1',
          username: 'test',
          name: 'テストユーザー',
          roles: ['USER'],
        },
      });
    }
    return new HttpResponse(null, { status: 401 });
  }),
];
```

### ハンドラーのオーバーライド

```typescript
// src/test/mocks/handlers/override.ts
import { http, HttpResponse } from 'msw';
import { server } from '../server';

// エラーレスポンスをシミュレート
export const mockApiError = (
  method: 'get' | 'post' | 'put' | 'delete',
  path: string,
  status: number,
  message?: string
) => {
  const handler = http[method](path, () => {
    return new HttpResponse(
      JSON.stringify({ message: message || 'Error' }),
      { status }
    );
  });
  server.use(handler);
};

// 遅延レスポンスをシミュレート
export const mockApiDelay = (
  method: 'get' | 'post' | 'put' | 'delete',
  path: string,
  delayMs: number
) => {
  const handler = http[method](path, async () => {
    await new Promise(resolve => setTimeout(resolve, delayMs));
    return new HttpResponse(null, { status: 200 });
  });
  server.use(handler);
};
```

## 23.3 コンポーネントテスト

### 金額入力コンポーネントのテスト

```typescript
// src/components/common/MoneyInput.test.tsx
import { describe, it, expect, vi } from 'vitest';
import { render, screen, fireEvent } from '@/test/utils';
import userEvent from '@testing-library/user-event';
import { MoneyInput } from './MoneyInput';

describe('MoneyInput', () => {
  it('初期値が正しく表示される', () => {
    render(<MoneyInput value={1000} onChange={() => {}} />);

    const input = screen.getByRole('textbox');
    expect(input).toHaveValue('1,000');
  });

  it('0の場合は空文字が表示される', () => {
    render(<MoneyInput value={0} onChange={() => {}} />);

    const input = screen.getByRole('textbox');
    expect(input).toHaveValue('');
  });

  it('数値入力時にonChangeが呼ばれる', async () => {
    const user = userEvent.setup();
    const handleChange = vi.fn();
    render(<MoneyInput value={0} onChange={handleChange} />);

    const input = screen.getByRole('textbox');
    await user.type(input, '1234');

    expect(handleChange).toHaveBeenCalledWith(1234);
  });

  it('3桁区切りで表示される', async () => {
    const user = userEvent.setup();
    const handleChange = vi.fn();
    render(<MoneyInput value={0} onChange={handleChange} />);

    const input = screen.getByRole('textbox');
    await user.type(input, '1234567');

    // フォーカスを外すとフォーマットされる
    await user.tab();

    expect(handleChange).toHaveBeenCalledWith(1234567);
  });

  it('負数が入力できる', async () => {
    const user = userEvent.setup();
    const handleChange = vi.fn();
    render(<MoneyInput value={0} onChange={handleChange} allowNegative />);

    const input = screen.getByRole('textbox');
    await user.type(input, '-1000');

    expect(handleChange).toHaveBeenCalledWith(-1000);
  });

  it('負数が許可されていない場合、マイナスは無視される', async () => {
    const user = userEvent.setup();
    const handleChange = vi.fn();
    render(<MoneyInput value={0} onChange={handleChange} />);

    const input = screen.getByRole('textbox');
    await user.type(input, '-1000');

    // マイナス記号は無視される
    expect(handleChange).toHaveBeenCalledWith(1000);
  });

  it('文字列は入力できない', async () => {
    const user = userEvent.setup();
    const handleChange = vi.fn();
    render(<MoneyInput value={0} onChange={handleChange} />);

    const input = screen.getByRole('textbox');
    await user.type(input, 'abc');

    // onChangeは呼ばれない
    expect(handleChange).not.toHaveBeenCalled();
  });

  it('disabledの場合は入力できない', () => {
    render(<MoneyInput value={1000} onChange={() => {}} disabled />);

    const input = screen.getByRole('textbox');
    expect(input).toBeDisabled();
  });

  it('最大値を超える値は制限される', async () => {
    const user = userEvent.setup();
    const handleChange = vi.fn();
    render(
      <MoneyInput value={0} onChange={handleChange} max={100000} />
    );

    const input = screen.getByRole('textbox');
    await user.type(input, '999999');

    // 最大値に制限される
    expect(handleChange).toHaveBeenLastCalledWith(100000);
  });
});
```

### 勘定科目選択コンポーネントのテスト

```typescript
// src/components/common/AccountSelector.test.tsx
import { describe, it, expect, vi, beforeEach } from 'vitest';
import { render, screen, waitFor } from '@/test/utils';
import userEvent from '@testing-library/user-event';
import { AccountSelector } from './AccountSelector';
import { mockAccounts } from '@/test/mocks/handlers';

describe('AccountSelector', () => {
  const defaultProps = {
    value: '',
    onChange: vi.fn(),
  };

  beforeEach(() => {
    vi.clearAllMocks();
  });

  it('勘定科目一覧が読み込まれる', async () => {
    render(<AccountSelector {...defaultProps} />);

    // ローディング状態
    expect(screen.getByText('読み込み中...')).toBeInTheDocument();

    // データ読み込み完了
    await waitFor(() => {
      expect(screen.queryByText('読み込み中...')).not.toBeInTheDocument();
    });

    // セレクトボックスに選択肢が表示される
    const select = screen.getByRole('combobox');
    expect(select).toBeInTheDocument();
  });

  it('勘定科目を選択するとonChangeが呼ばれる', async () => {
    const user = userEvent.setup();
    const handleChange = vi.fn();

    render(<AccountSelector {...defaultProps} onChange={handleChange} />);

    await waitFor(() => {
      expect(screen.queryByText('読み込み中...')).not.toBeInTheDocument();
    });

    const select = screen.getByRole('combobox');
    await user.selectOptions(select, '111');

    expect(handleChange).toHaveBeenCalledWith('111');
  });

  it('初期値が正しく選択される', async () => {
    render(<AccountSelector {...defaultProps} value="111" />);

    await waitFor(() => {
      const select = screen.getByRole('combobox');
      expect(select).toHaveValue('111');
    });
  });

  it('B/Sでフィルタできる', async () => {
    render(
      <AccountSelector {...defaultProps} filter={{ bsPlType: 'B' }} />
    );

    await waitFor(() => {
      expect(screen.queryByText('読み込み中...')).not.toBeInTheDocument();
    });

    // P/L科目（売上高）は表示されない
    const options = screen.getAllByRole('option');
    const optionTexts = options.map(o => o.textContent);

    expect(optionTexts.some(t => t?.includes('現金'))).toBe(true);
    expect(optionTexts.some(t => t?.includes('売上高'))).toBe(false);
  });

  it('要素区分でフィルタできる', async () => {
    render(
      <AccountSelector {...defaultProps} filter={{ elementType: '資産' }} />
    );

    await waitFor(() => {
      expect(screen.queryByText('読み込み中...')).not.toBeInTheDocument();
    });

    const options = screen.getAllByRole('option');
    const optionTexts = options.map(o => o.textContent);

    expect(optionTexts.some(t => t?.includes('現金'))).toBe(true);
    expect(optionTexts.some(t => t?.includes('買掛金'))).toBe(false); // 負債
  });

  it('requiredの場合、未選択でバリデーションエラー', async () => {
    render(<AccountSelector {...defaultProps} required />);

    await waitFor(() => {
      expect(screen.queryByText('読み込み中...')).not.toBeInTheDocument();
    });

    const select = screen.getByRole('combobox');
    expect(select).toBeRequired();
  });
});
```

### 仕訳入力フォームのテスト

```typescript
// src/components/journal/JournalEntryForm.test.tsx
import { describe, it, expect, vi, beforeEach } from 'vitest';
import { render, screen, waitFor, within } from '@/test/utils';
import userEvent from '@testing-library/user-event';
import { JournalEntryForm } from './JournalEntryForm';

describe('JournalEntryForm', () => {
  const defaultProps = {
    onSubmit: vi.fn(),
    onCancel: vi.fn(),
  };

  beforeEach(() => {
    vi.clearAllMocks();
  });

  it('フォームが正しくレンダリングされる', async () => {
    render(<JournalEntryForm {...defaultProps} />);

    expect(screen.getByLabelText('仕訳日付')).toBeInTheDocument();
    expect(screen.getByLabelText('摘要')).toBeInTheDocument();
    expect(screen.getByRole('button', { name: '明細追加' })).toBeInTheDocument();
    expect(screen.getByRole('button', { name: '保存' })).toBeInTheDocument();
    expect(screen.getByRole('button', { name: 'キャンセル' })).toBeInTheDocument();
  });

  it('明細行を追加できる', async () => {
    const user = userEvent.setup();
    render(<JournalEntryForm {...defaultProps} />);

    const addButton = screen.getByRole('button', { name: '明細追加' });
    await user.click(addButton);

    // 明細行が追加される
    const detailRows = screen.getAllByTestId('journal-detail-row');
    expect(detailRows).toHaveLength(1);
  });

  it('貸借バランスが計算される', async () => {
    const user = userEvent.setup();
    render(<JournalEntryForm {...defaultProps} />);

    // 明細行を2つ追加
    const addButton = screen.getByRole('button', { name: '明細追加' });
    await user.click(addButton);
    await user.click(addButton);

    // 借方に10000円を入力
    const debitInputs = screen.getAllByTestId('debit-amount');
    await user.type(debitInputs[0], '10000');

    // 貸方に10000円を入力
    const creditInputs = screen.getAllByTestId('credit-amount');
    await user.type(creditInputs[1], '10000');

    // バランスが取れている
    await waitFor(() => {
      expect(screen.getByTestId('balance-status')).toHaveTextContent('バランス');
    });
  });

  it('貸借不一致の場合はエラー表示', async () => {
    const user = userEvent.setup();
    render(<JournalEntryForm {...defaultProps} />);

    const addButton = screen.getByRole('button', { name: '明細追加' });
    await user.click(addButton);
    await user.click(addButton);

    const debitInputs = screen.getAllByTestId('debit-amount');
    await user.type(debitInputs[0], '10000');

    const creditInputs = screen.getAllByTestId('credit-amount');
    await user.type(creditInputs[1], '5000');

    await waitFor(() => {
      expect(screen.getByTestId('balance-status')).toHaveTextContent('差額: 5,000');
    });
  });

  it('貸借不一致の場合は保存ボタンが無効', async () => {
    const user = userEvent.setup();
    render(<JournalEntryForm {...defaultProps} />);

    const addButton = screen.getByRole('button', { name: '明細追加' });
    await user.click(addButton);
    await user.click(addButton);

    const debitInputs = screen.getAllByTestId('debit-amount');
    await user.type(debitInputs[0], '10000');

    const saveButton = screen.getByRole('button', { name: '保存' });
    expect(saveButton).toBeDisabled();
  });

  it('正常な入力でonSubmitが呼ばれる', async () => {
    const user = userEvent.setup();
    const handleSubmit = vi.fn();
    render(<JournalEntryForm {...defaultProps} onSubmit={handleSubmit} />);

    // 日付入力
    const dateInput = screen.getByLabelText('仕訳日付');
    await user.clear(dateInput);
    await user.type(dateInput, '2024-04-01');

    // 摘要入力
    const descInput = screen.getByLabelText('摘要');
    await user.type(descInput, 'テスト仕訳');

    // 明細追加
    const addButton = screen.getByRole('button', { name: '明細追加' });
    await user.click(addButton);
    await user.click(addButton);

    // 勘定科目と金額を入力
    await waitFor(() => {
      expect(screen.queryByText('読み込み中...')).not.toBeInTheDocument();
    });

    const accountSelects = screen.getAllByTestId('account-select');
    await user.selectOptions(accountSelects[0], '111');
    await user.selectOptions(accountSelects[1], '411');

    const debitInputs = screen.getAllByTestId('debit-amount');
    await user.type(debitInputs[0], '10000');

    const creditInputs = screen.getAllByTestId('credit-amount');
    await user.type(creditInputs[1], '10000');

    // 保存
    const saveButton = screen.getByRole('button', { name: '保存' });
    await user.click(saveButton);

    expect(handleSubmit).toHaveBeenCalledWith(
      expect.objectContaining({
        journalDate: '2024-04-01',
        description: 'テスト仕訳',
        details: expect.arrayContaining([
          expect.objectContaining({
            accountCode: '111',
            debitAmount: 10000,
            creditAmount: 0,
          }),
          expect.objectContaining({
            accountCode: '411',
            debitAmount: 0,
            creditAmount: 10000,
          }),
        ]),
      })
    );
  });

  it('キャンセルボタンでonCancelが呼ばれる', async () => {
    const user = userEvent.setup();
    const handleCancel = vi.fn();
    render(<JournalEntryForm {...defaultProps} onCancel={handleCancel} />);

    const cancelButton = screen.getByRole('button', { name: 'キャンセル' });
    await user.click(cancelButton);

    expect(handleCancel).toHaveBeenCalled();
  });
});
```

## 23.4 カスタムフックのテスト

### useBalanceValidation のテスト

```typescript
// src/hooks/useBalanceValidation.test.ts
import { describe, it, expect } from 'vitest';
import { renderHook, act } from '@testing-library/react';
import { useBalanceValidation } from './useBalanceValidation';

describe('useBalanceValidation', () => {
  it('空の明細では isValid が false', () => {
    const { result } = renderHook(() => useBalanceValidation([]));

    expect(result.current.isValid).toBe(false);
    expect(result.current.totalDebit).toBe(0);
    expect(result.current.totalCredit).toBe(0);
    expect(result.current.difference).toBe(0);
  });

  it('貸借一致の場合は isValid が true', () => {
    const details = [
      { debitAmount: 10000, creditAmount: 0 },
      { debitAmount: 0, creditAmount: 10000 },
    ];

    const { result } = renderHook(() => useBalanceValidation(details));

    expect(result.current.isValid).toBe(true);
    expect(result.current.totalDebit).toBe(10000);
    expect(result.current.totalCredit).toBe(10000);
    expect(result.current.difference).toBe(0);
  });

  it('貸借不一致の場合は isValid が false', () => {
    const details = [
      { debitAmount: 10000, creditAmount: 0 },
      { debitAmount: 0, creditAmount: 5000 },
    ];

    const { result } = renderHook(() => useBalanceValidation(details));

    expect(result.current.isValid).toBe(false);
    expect(result.current.totalDebit).toBe(10000);
    expect(result.current.totalCredit).toBe(5000);
    expect(result.current.difference).toBe(5000);
  });

  it('複数行の合計が計算される', () => {
    const details = [
      { debitAmount: 5000, creditAmount: 0 },
      { debitAmount: 3000, creditAmount: 0 },
      { debitAmount: 2000, creditAmount: 0 },
      { debitAmount: 0, creditAmount: 10000 },
    ];

    const { result } = renderHook(() => useBalanceValidation(details));

    expect(result.current.isValid).toBe(true);
    expect(result.current.totalDebit).toBe(10000);
    expect(result.current.totalCredit).toBe(10000);
  });

  it('小数点以下の金額も正確に計算される', () => {
    const details = [
      { debitAmount: 100.01, creditAmount: 0 },
      { debitAmount: 200.02, creditAmount: 0 },
      { debitAmount: 0, creditAmount: 300.03 },
    ];

    const { result } = renderHook(() => useBalanceValidation(details));

    expect(result.current.isValid).toBe(true);
    expect(result.current.totalDebit).toBeCloseTo(300.03);
    expect(result.current.totalCredit).toBeCloseTo(300.03);
  });

  it('明細が更新されると再計算される', () => {
    const initialDetails = [
      { debitAmount: 10000, creditAmount: 0 },
      { debitAmount: 0, creditAmount: 5000 },
    ];

    const { result, rerender } = renderHook(
      ({ details }) => useBalanceValidation(details),
      { initialProps: { details: initialDetails } }
    );

    expect(result.current.isValid).toBe(false);

    const updatedDetails = [
      { debitAmount: 10000, creditAmount: 0 },
      { debitAmount: 0, creditAmount: 10000 },
    ];

    rerender({ details: updatedDetails });

    expect(result.current.isValid).toBe(true);
  });
});
```

### useTaxCalculation のテスト

```typescript
// src/hooks/useTaxCalculation.test.ts
import { describe, it, expect } from 'vitest';
import { renderHook, act } from '@testing-library/react';
import { useTaxCalculation } from './useTaxCalculation';

describe('useTaxCalculation', () => {
  it('税込金額から税抜金額と消費税額を計算する', () => {
    const { result } = renderHook(() => useTaxCalculation());

    act(() => {
      result.current.calculateFromTaxIncluded(11000, 0.1);
    });

    expect(result.current.taxExcluded).toBe(10000);
    expect(result.current.taxAmount).toBe(1000);
    expect(result.current.taxIncluded).toBe(11000);
  });

  it('税抜金額から税込金額と消費税額を計算する', () => {
    const { result } = renderHook(() => useTaxCalculation());

    act(() => {
      result.current.calculateFromTaxExcluded(10000, 0.1);
    });

    expect(result.current.taxExcluded).toBe(10000);
    expect(result.current.taxAmount).toBe(1000);
    expect(result.current.taxIncluded).toBe(11000);
  });

  it('8%の軽減税率で計算できる', () => {
    const { result } = renderHook(() => useTaxCalculation());

    act(() => {
      result.current.calculateFromTaxExcluded(10000, 0.08);
    });

    expect(result.current.taxAmount).toBe(800);
    expect(result.current.taxIncluded).toBe(10800);
  });

  it('端数は切り捨て', () => {
    const { result } = renderHook(() => useTaxCalculation());

    act(() => {
      result.current.calculateFromTaxExcluded(999, 0.1);
    });

    // 999 * 0.1 = 99.9 → 切り捨てで 99
    expect(result.current.taxAmount).toBe(99);
    expect(result.current.taxIncluded).toBe(1098);
  });

  it('税込からの逆算でも端数処理が正しい', () => {
    const { result } = renderHook(() => useTaxCalculation());

    act(() => {
      result.current.calculateFromTaxIncluded(1098, 0.1);
    });

    // 1098 / 1.1 = 998.18... → 切り捨てで 998
    // 税額 = 1098 - 998 = 100
    expect(result.current.taxExcluded).toBe(998);
    expect(result.current.taxAmount).toBe(100);
  });
});
```

### useJournalSearch のテスト

```typescript
// src/hooks/useJournalSearch.test.tsx
import { describe, it, expect, vi, beforeEach } from 'vitest';
import { renderHook, waitFor } from '@testing-library/react';
import { QueryClient, QueryClientProvider } from '@tanstack/react-query';
import { BrowserRouter } from 'react-router-dom';
import { useJournalSearch } from './useJournalSearch';

const createWrapper = () => {
  const queryClient = new QueryClient({
    defaultOptions: {
      queries: { retry: false },
    },
  });

  return ({ children }: { children: React.ReactNode }) => (
    <QueryClientProvider client={queryClient}>
      <BrowserRouter>{children}</BrowserRouter>
    </QueryClientProvider>
  );
};

describe('useJournalSearch', () => {
  beforeEach(() => {
    // URLをリセット
    window.history.pushState({}, '', '/journal');
  });

  it('初期状態で仕訳一覧を取得する', async () => {
    const { result } = renderHook(() => useJournalSearch(), {
      wrapper: createWrapper(),
    });

    expect(result.current.isLoading).toBe(true);

    await waitFor(() => {
      expect(result.current.isLoading).toBe(false);
    });

    expect(result.current.journals).toBeDefined();
    expect(result.current.journals.length).toBeGreaterThan(0);
  });

  it('検索条件を更新するとURLが変更される', async () => {
    const { result } = renderHook(() => useJournalSearch(), {
      wrapper: createWrapper(),
    });

    await waitFor(() => {
      expect(result.current.isLoading).toBe(false);
    });

    result.current.updateSearchParams({
      startDate: '2024-04-01',
      endDate: '2024-04-30',
    });

    expect(window.location.search).toContain('startDate=2024-04-01');
    expect(window.location.search).toContain('endDate=2024-04-30');
  });

  it('URLパラメータから検索条件を復元する', async () => {
    window.history.pushState({}, '', '/journal?startDate=2024-04-01&keyword=test');

    const { result } = renderHook(() => useJournalSearch(), {
      wrapper: createWrapper(),
    });

    expect(result.current.searchParams.startDate).toBe('2024-04-01');
    expect(result.current.searchParams.keyword).toBe('test');
  });

  it('ページを変更するとデータが再取得される', async () => {
    const { result } = renderHook(() => useJournalSearch(), {
      wrapper: createWrapper(),
    });

    await waitFor(() => {
      expect(result.current.isLoading).toBe(false);
    });

    result.current.goToPage(1);

    expect(result.current.currentPage).toBe(1);
  });

  it('検索をクリアすると初期状態に戻る', async () => {
    window.history.pushState({}, '', '/journal?startDate=2024-04-01');

    const { result } = renderHook(() => useJournalSearch(), {
      wrapper: createWrapper(),
    });

    result.current.clearSearch();

    expect(result.current.searchParams.startDate).toBeUndefined();
    expect(window.location.search).toBe('');
  });
});
```

## 23.5 金額計算のテスト

### decimal.js を使用した精度テスト

```typescript
// src/utils/money.test.ts
import { describe, it, expect } from 'vitest';
import {
  add,
  subtract,
  multiply,
  divide,
  formatMoney,
  parseMoney,
  roundMoney,
} from './money';

describe('Money Utils', () => {
  describe('add', () => {
    it('整数の加算', () => {
      expect(add(100, 200)).toBe(300);
    });

    it('小数の加算（浮動小数点誤差なし）', () => {
      // JavaScript: 0.1 + 0.2 = 0.30000000000000004
      expect(add(0.1, 0.2)).toBe(0.3);
    });

    it('大きな金額の加算', () => {
      expect(add(99999999, 1)).toBe(100000000);
    });
  });

  describe('subtract', () => {
    it('整数の減算', () => {
      expect(subtract(300, 100)).toBe(200);
    });

    it('小数の減算（浮動小数点誤差なし）', () => {
      expect(subtract(0.3, 0.1)).toBe(0.2);
    });

    it('結果がマイナスになる減算', () => {
      expect(subtract(100, 200)).toBe(-100);
    });
  });

  describe('multiply', () => {
    it('整数の乗算', () => {
      expect(multiply(100, 3)).toBe(300);
    });

    it('消費税計算（10%）', () => {
      expect(multiply(10000, 0.1)).toBe(1000);
    });

    it('消費税計算（8%）', () => {
      expect(multiply(10000, 0.08)).toBe(800);
    });

    it('端数が出る乗算', () => {
      // 999 * 0.1 = 99.9
      expect(multiply(999, 0.1)).toBe(99.9);
    });
  });

  describe('divide', () => {
    it('整数の除算', () => {
      expect(divide(300, 3)).toBe(100);
    });

    it('税込から税抜への計算', () => {
      // 11000 / 1.1 = 10000
      expect(divide(11000, 1.1)).toBe(10000);
    });

    it('割り切れない除算', () => {
      // 10000 / 3 = 3333.333...
      expect(divide(10000, 3)).toBeCloseTo(3333.33, 2);
    });

    it('ゼロ除算でエラー', () => {
      expect(() => divide(100, 0)).toThrow();
    });
  });

  describe('roundMoney', () => {
    it('切り捨て', () => {
      expect(roundMoney(99.9, 'floor')).toBe(99);
    });

    it('切り上げ', () => {
      expect(roundMoney(99.1, 'ceil')).toBe(100);
    });

    it('四捨五入', () => {
      expect(roundMoney(99.4, 'round')).toBe(99);
      expect(roundMoney(99.5, 'round')).toBe(100);
    });
  });

  describe('formatMoney', () => {
    it('3桁区切り', () => {
      expect(formatMoney(1000000)).toBe('1,000,000');
    });

    it('負数', () => {
      expect(formatMoney(-1000)).toBe('-1,000');
    });

    it('ゼロ', () => {
      expect(formatMoney(0)).toBe('0');
    });

    it('小数', () => {
      expect(formatMoney(1000.5)).toBe('1,000.5');
    });

    it('単位付き', () => {
      expect(formatMoney(1000, { suffix: '円' })).toBe('1,000円');
    });
  });

  describe('parseMoney', () => {
    it('カンマ区切りを解析', () => {
      expect(parseMoney('1,000,000')).toBe(1000000);
    });

    it('円マーク付きを解析', () => {
      expect(parseMoney('¥1,000')).toBe(1000);
    });

    it('負数を解析', () => {
      expect(parseMoney('-1,000')).toBe(-1000);
    });

    it('空文字は0', () => {
      expect(parseMoney('')).toBe(0);
    });

    it('無効な文字列はNaN', () => {
      expect(parseMoney('abc')).toBeNaN();
    });
  });
});
```

## 23.6 API 連携テスト

### API エラーハンドリングのテスト

```typescript
// src/containers/AccountContainer.test.tsx
import { describe, it, expect, vi, beforeEach } from 'vitest';
import { render, screen, waitFor } from '@/test/utils';
import userEvent from '@testing-library/user-event';
import { AccountContainer } from './AccountContainer';
import { mockApiError } from '@/test/mocks/handlers/override';

describe('AccountContainer', () => {
  beforeEach(() => {
    vi.clearAllMocks();
  });

  it('勘定科目一覧を表示する', async () => {
    render(<AccountContainer />);

    await waitFor(() => {
      expect(screen.getByText('現金')).toBeInTheDocument();
      expect(screen.getByText('普通預金')).toBeInTheDocument();
    });
  });

  it('API エラー時にエラーメッセージを表示する', async () => {
    mockApiError('get', '/api/accounts', 500, 'Internal Server Error');

    render(<AccountContainer />);

    await waitFor(() => {
      expect(screen.getByText(/エラーが発生しました/)).toBeInTheDocument();
    });
  });

  it('認証エラー時にログイン画面にリダイレクトされる', async () => {
    mockApiError('get', '/api/accounts', 401, 'Unauthorized');

    render(<AccountContainer />);

    await waitFor(() => {
      // ログイン画面へのリダイレクトをテスト
      expect(window.location.pathname).toBe('/login');
    });
  });

  it('新規作成が成功するとメッセージが表示される', async () => {
    const user = userEvent.setup();
    render(<AccountContainer />);

    await waitFor(() => {
      expect(screen.getByText('現金')).toBeInTheDocument();
    });

    // 新規作成ボタンをクリック
    const createButton = screen.getByRole('button', { name: '新規作成' });
    await user.click(createButton);

    // フォームに入力
    await user.type(screen.getByLabelText('科目コード'), '999');
    await user.type(screen.getByLabelText('科目名'), 'テスト科目');

    // 保存
    const saveButton = screen.getByRole('button', { name: '保存' });
    await user.click(saveButton);

    await waitFor(() => {
      expect(screen.getByText(/登録しました/)).toBeInTheDocument();
    });
  });
});
```

## 23.7 スナップショットテスト

```typescript
// src/components/statement/BalanceSheetView.test.tsx
import { describe, it, expect } from 'vitest';
import { render } from '@/test/utils';
import { BalanceSheetView } from './BalanceSheetView';

describe('BalanceSheetView', () => {
  const mockData = {
    periodEnd: '2024-03-31',
    assets: [
      {
        id: '1',
        code: '1',
        name: '資産の部',
        amount: 1000000,
        children: [
          { id: '11', code: '11', name: '流動資産', amount: 600000, children: [] },
          { id: '12', code: '12', name: '固定資産', amount: 400000, children: [] },
        ],
      },
    ],
    liabilities: [
      {
        id: '2',
        code: '2',
        name: '負債の部',
        amount: 300000,
        children: [],
      },
    ],
    equity: [
      {
        id: '3',
        code: '3',
        name: '純資産の部',
        amount: 700000,
        children: [],
      },
    ],
    totalAssets: 1000000,
    totalLiabilities: 300000,
    totalEquity: 700000,
  };

  it('勘定式でスナップショットと一致する', () => {
    const { container } = render(
      <BalanceSheetView
        data={mockData}
        layout="account"
        isLoading={false}
      />
    );

    expect(container).toMatchSnapshot();
  });

  it('報告式でスナップショットと一致する', () => {
    const { container } = render(
      <BalanceSheetView
        data={mockData}
        layout="report"
        isLoading={false}
      />
    );

    expect(container).toMatchSnapshot();
  });
});
```

## 23.8 テスト実行設定

### package.json スクリプト

```json
{
  "scripts": {
    "test": "vitest",
    "test:ui": "vitest --ui",
    "test:run": "vitest run",
    "test:coverage": "vitest run --coverage",
    "test:watch": "vitest watch"
  }
}
```

### カバレッジ設定

```typescript
// vite.config.ts
export default defineConfig({
  test: {
    coverage: {
      provider: 'v8',
      reporter: ['text', 'html', 'lcov'],
      exclude: [
        'node_modules/',
        'src/test/',
        'src/api/generated/',
        'src/api/model/',
        '**/*.d.ts',
        '**/*.test.{ts,tsx}',
      ],
      thresholds: {
        lines: 80,
        functions: 80,
        branches: 70,
        statements: 80,
      },
    },
  },
});
```

## 23.9 まとめ

本章では、フロントエンドの単体テスト実装について解説した。主なポイントは以下の通りである：

1. **Vitest と Testing Library**: モダンで高速なテスト環境
2. **MSW によるモック**: 実際の API を使わずに型安全なテスト
3. **コンポーネントテスト**: ユーザー操作をシミュレート
4. **カスタムフックテスト**: renderHook による状態管理テスト
5. **金額計算テスト**: decimal.js による精度保証の検証
6. **スナップショットテスト**: UI の予期しない変更を検出

これらのテストにより、リファクタリングや機能追加時の回帰を防ぎ、コードの品質を維持できる。次章では、E2E テストについて解説する。
