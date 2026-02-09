import { http, HttpResponse } from 'msw';
import type { LoginRequest, LoginResponse } from '../api/model';

/**
 * 有効な JWT 形式のモックトークンを生成
 * AuthProvider の isTokenExpired で正しく解析できる形式
 */
const createMockJwt = (sub: string, expiresInSeconds = 3600): string => {
  const header = btoa(JSON.stringify({ alg: 'HS256', typ: 'JWT' }));
  const payload = btoa(
    JSON.stringify({
      sub,
      exp: Math.floor(Date.now() / 1000) + expiresInSeconds,
      iat: Math.floor(Date.now() / 1000),
    })
  );
  const signature = 'mock-signature';
  return `${header}.${payload}.${signature}`;
};

// Account type definition (will be replaced by generated types)
interface Account {
  accountId: number;
  accountCode: string;
  accountName: string;
  accountType: string;
}

// モック勘定科目データ（テスト用に複数種別を用意）
const mockAccounts: Account[] = [
  { accountId: 1, accountCode: '1000', accountName: '現金預金', accountType: 'ASSET' },
  { accountId: 2, accountCode: '1001', accountName: '現金', accountType: 'ASSET' },
  { accountId: 3, accountCode: '2000', accountName: '買掛金', accountType: 'LIABILITY' },
  { accountId: 4, accountCode: '2001', accountName: '未払金', accountType: 'LIABILITY' },
  { accountId: 5, accountCode: '3001', accountName: '資本金', accountType: 'EQUITY' },
  { accountId: 6, accountCode: '4001', accountName: '売上高', accountType: 'REVENUE' },
  { accountId: 7, accountCode: '5001', accountName: '仕入高', accountType: 'EXPENSE' },
  { accountId: 8, accountCode: '5002', accountName: '給与手当', accountType: 'EXPENSE' },
  { accountId: 9, accountCode: '9991', accountName: '削除テスト用科目', accountType: 'ASSET' },
  // E2E テスト用（使用中勘定科目の削除制限テスト）
  { accountId: 11, accountCode: '8999', accountName: '使用中削除テスト用', accountType: 'ASSET' },
];

// モックユーザー定義
const mockUsers: Record<string, { password: string; role: string }> = {
  admin: { password: 'Password123!', role: 'ADMIN' },
  user: { password: 'Password123!', role: 'USER' },
  manager: { password: 'Password123!', role: 'MANAGER' },
  viewer: { password: 'Password123!', role: 'VIEWER' },
};

interface UserRecord {
  id: string;
  username: string;
  email: string;
  displayName: string;
  role: string;
  lastLoginAt: string | null;
}

const mockUserRecords: UserRecord[] = [
  {
    id: '101',
    username: 'user_edit_nav',
    email: 'nav@example.com',
    displayName: 'ナビゲーション',
    role: 'USER',
    lastLoginAt: '2024-01-15T10:30:00',
  },
  {
    id: '102',
    username: 'user_edit_readonly',
    email: 'readonly@example.com',
    displayName: 'リードオンリー',
    role: 'MANAGER',
    lastLoginAt: '2024-02-10T14:00:00',
  },
  {
    id: '103',
    username: 'user_edit_display',
    email: 'display@example.com',
    displayName: '表示名テスト',
    role: 'USER',
    lastLoginAt: null,
  },
  {
    id: '104',
    username: 'user_edit_role',
    email: 'role@example.com',
    displayName: 'ロールテスト',
    role: 'VIEWER',
    lastLoginAt: '2024-03-05T09:15:00',
  },
  {
    id: '105',
    username: 'user_edit_password',
    email: 'password@example.com',
    displayName: 'パスワードテスト',
    role: 'ADMIN',
    lastLoginAt: '2024-03-20T16:45:00',
  },
  {
    id: '106',
    username: 'user_edit_validate',
    email: 'validate@example.com',
    displayName: 'バリデーション',
    role: 'USER',
    lastLoginAt: '2024-01-01T08:00:00',
  },
];

const findUserRecord = (id: string): UserRecord | undefined =>
  mockUserRecords.find((user) => user.id === id);

// エラーユーザー定義
const errorUsers: Record<string, string> = {
  locked:
    'アカウントがロックされています。ログイン試行が複数回失敗したため、セキュリティ保護のためロックされました。管理者にお問い合わせください。',
  inactive: 'アカウントが無効化されています。管理者にお問い合わせください。',
};

/**
 * ログインリクエストを処理
 */
const handleLogin = (body: LoginRequest): LoginResponse => {
  // エラーユーザーチェック
  if (body.username in errorUsers) {
    return { success: false, errorMessage: errorUsers[body.username] };
  }

  // 正常ユーザーチェック
  const user = mockUsers[body.username];
  if (user && user.password === body.password) {
    return {
      success: true,
      accessToken: createMockJwt(body.username),
      refreshToken: createMockJwt(`${body.username}-refresh`, 86400),
      username: body.username,
      role: user.role,
    };
  }

  // 認証エラー
  return { success: false, errorMessage: 'ユーザーIDまたはパスワードが正しくありません' };
};

/**
 * 認証関連のハンドラー
 * Note: Orval 生成コードの URL が /api/auth/login で、
 * axios の baseURL が /api のため、実際のリクエスト先は /api/api/auth/login になる
 */
export const authHandlers = [
  // ログイン（Orval 生成コード対応: baseURL + /api/auth/login = /api/api/auth/login）
  http.post('*/auth/login', async ({ request }) => {
    const body = (await request.json()) as LoginRequest;
    return HttpResponse.json<LoginResponse>(handleLogin(body));
  }),

  // トークンリフレッシュ
  http.post('*/auth/refresh', async ({ request }) => {
    const body = (await request.json()) as { refreshToken: string };

    // JWT 形式のリフレッシュトークンをチェック（ペイロード部分を検証）
    if (body.refreshToken) {
      try {
        const payloadPart = body.refreshToken.split('.')[1];
        if (payloadPart) {
          const payload = JSON.parse(atob(payloadPart));
          if (payload.sub && payload.exp > Date.now() / 1000) {
            return HttpResponse.json({
              accessToken: createMockJwt(payload.sub.replace('-refresh', '')),
            });
          }
        }
      } catch {
        // パース失敗は無視
      }
    }

    return HttpResponse.json({ message: 'Invalid refresh token' }, { status: 401 });
  }),

  // ログアウト
  http.post('*/auth/logout', () => {
    return new HttpResponse(null, { status: 204 });
  }),

  // ユーザー登録
  http.post('*/auth/register', async ({ request }) => {
    const body = (await request.json()) as {
      username: string;
      email: string;
      password: string;
      displayName: string;
      role: string;
    };

    // 既存ユーザーとの重複チェック（admin, user, manager は既存ユーザー）
    const existingUsers = ['admin', 'user', 'manager', 'viewer'];
    if (existingUsers.includes(body.username)) {
      return HttpResponse.json({
        success: false,
        errorMessage: 'このユーザーIDは既に使用されています',
      });
    }

    // 成功ケース
    return HttpResponse.json({
      success: true,
      username: body.username,
      email: body.email,
      displayName: body.displayName,
      role: body.role,
    });
  }),
];

/**
 * ユーザー関連のハンドラー
 */
export const userHandlers = [
  // ユーザー一覧取得（フィルター/検索対応）
  http.get(/\/users\/?$/, ({ request }) => {
    const url = new URL(request.url);
    const role = url.searchParams.get('role');
    const keyword = url.searchParams.get('keyword');

    let filtered = [...mockUserRecords];

    // ロールでフィルタリング
    if (role) {
      filtered = filtered.filter((user) => user.role === role);
    }

    // キーワードで検索（ユーザーIDまたは表示名）
    if (keyword) {
      const lowerKeyword = keyword.toLowerCase();
      filtered = filtered.filter(
        (user) =>
          user.username.toLowerCase().includes(lowerKeyword) ||
          user.displayName.toLowerCase().includes(lowerKeyword)
      );
    }

    return HttpResponse.json<UserRecord[]>(filtered);
  }),

  // ユーザー詳細取得
  http.get(/\/users\/([^/]+)$/, ({ request }) => {
    const url = new URL(request.url);
    const match = url.pathname.match(/\/users\/([^/]+)$/);
    const id = match ? decodeURIComponent(match[1]) : '';
    const user = findUserRecord(id);

    if (!user) {
      return HttpResponse.json({ errorMessage: 'ユーザーが見つかりません' }, { status: 404 });
    }

    return HttpResponse.json<UserRecord>(user);
  }),

  // ユーザー更新
  http.put(/\/users\/([^/]+)$/, async ({ request }) => {
    const url = new URL(request.url);
    const match = url.pathname.match(/\/users\/([^/]+)$/);
    const id = match ? decodeURIComponent(match[1]) : '';
    const body = (await request.json()) as {
      displayName: string;
      role: string;
      password?: string;
    };

    const user = findUserRecord(id);
    if (!user) {
      return HttpResponse.json(
        { success: false, errorMessage: 'ユーザーが見つかりません' },
        { status: 404 }
      );
    }

    user.displayName = body.displayName;
    user.role = body.role;

    return HttpResponse.json({
      success: true,
      id: user.id,
      username: user.username,
      email: user.email,
      displayName: user.displayName,
      role: user.role,
    });
  }),

  // ユーザー削除（論理削除）
  http.delete(/\/users\/([^/]+)$/, ({ request }) => {
    const url = new URL(request.url);
    const match = url.pathname.match(/\/users\/([^/]+)$/);
    const id = match ? decodeURIComponent(match[1]) : '';

    const userIndex = mockUserRecords.findIndex((user) => user.id === id);
    if (userIndex === -1) {
      return HttpResponse.json(
        { success: false, errorMessage: 'ユーザーが見つかりません' },
        { status: 404 }
      );
    }

    // 論理削除: mockUserRecords から削除（フロントエンドでは一覧から消える）
    mockUserRecords.splice(userIndex, 1);

    return HttpResponse.json({
      success: true,
      errorMessage: null,
    });
  }),
];

// 既存の勘定科目コードを追跡（重複チェック用）
const existingAccountCodes = new Set(mockAccounts.map((a) => a.accountCode));
let nextAccountId = mockAccounts.length + 1;

/**
 * 勘定科目関連のハンドラー
 * Note: Orval 生成コードの URL と axios の baseURL の組み合わせで
 * 実際のリクエスト先は /api/api/accounts になる
 */
export const accountHandlers = [
  // 勘定科目詳細取得（正規表現で明確にマッチ）
  http.get(/\/accounts\/(\d+)$/, ({ request }) => {
    const url = new URL(request.url);
    const match = url.pathname.match(/\/accounts\/(\d+)$/);
    const id = match ? match[1] : '0';
    return HttpResponse.json<Account>({
      accountId: Number(id),
      accountCode: '1000',
      accountName: '現金預金',
      accountType: 'ASSET',
    });
  }),

  // 勘定科目更新（正規表現で明確にマッチ）
  http.put(/\/accounts\/(\d+)$/, async ({ request }) => {
    const url = new URL(request.url);
    const match = url.pathname.match(/\/accounts\/(\d+)$/);
    const id = match ? match[1] : '0';
    const body = (await request.json()) as { accountName: string; accountType: string };
    return HttpResponse.json({
      success: true,
      accountId: Number(id),
      accountCode: '1000',
      accountName: body.accountName,
      accountType: body.accountType,
      message: '勘定科目を更新しました',
    });
  }),

  // 勘定科目削除（正規表現で明確にマッチ）
  http.delete(/\/accounts\/(\d+)$/, ({ request }) => {
    const url = new URL(request.url);
    const match = url.pathname.match(/\/accounts\/(\d+)$/);
    const id = match ? parseInt(match[1], 10) : 0;

    // 存在しない勘定科目のチェック（ID が 999 の場合はエラー）
    if (id === 999) {
      return HttpResponse.json(
        {
          success: false,
          errorMessage: '勘定科目が見つかりません',
        },
        { status: 404 }
      );
    }

    // 使用中勘定科目のチェック（ID が 10 は使用中削除テスト用）
    if (id === 10) {
      return HttpResponse.json(
        {
          success: false,
          errorMessage: 'この勘定科目は仕訳で使用されているため削除できません',
        },
        { status: 409 }
      );
    }

    // 成功ケース - mockAccounts から削除
    const index = mockAccounts.findIndex((a) => a.accountId === id);
    if (index !== -1) {
      mockAccounts.splice(index, 1);
    }

    return HttpResponse.json({
      success: true,
      accountId: id,
      message: '勘定科目を削除しました',
    });
  }),

  // 勘定科目登録
  http.post('*/accounts', async ({ request }) => {
    const body = (await request.json()) as {
      accountCode: string;
      accountName: string;
      accountType: string;
    };

    // 重複チェック
    if (existingAccountCodes.has(body.accountCode)) {
      return HttpResponse.json({
        success: false,
        errorMessage: '勘定科目コードは既に使用されています',
      });
    }

    // 成功ケース - mockAccounts にも追加
    existingAccountCodes.add(body.accountCode);
    const accountId = nextAccountId++;
    const newAccount: Account = {
      accountId,
      accountCode: body.accountCode,
      accountName: body.accountName,
      accountType: body.accountType,
    };
    mockAccounts.push(newAccount);

    return HttpResponse.json({
      success: true,
      accountId,
      accountCode: body.accountCode,
      accountName: body.accountName,
      accountType: body.accountType,
    });
  }),

  // 勘定科目一覧取得（:id なしの /accounts のみにマッチ、フィルタリング対応）
  http.get(/\/accounts\/?$/, ({ request }) => {
    const url = new URL(request.url);
    // フロントエンドは 'type' パラメータを使用
    const accountType = url.searchParams.get('type');
    const keyword = url.searchParams.get('keyword');

    let filtered = [...mockAccounts];

    // 勘定科目種別でフィルタリング
    if (accountType) {
      filtered = filtered.filter((a) => a.accountType === accountType);
    }

    // キーワードで検索（科目コードまたは科目名）
    if (keyword) {
      const lowerKeyword = keyword.toLowerCase();
      filtered = filtered.filter(
        (a) =>
          a.accountCode.toLowerCase().includes(lowerKeyword) ||
          a.accountName.toLowerCase().includes(lowerKeyword)
      );
    }

    return HttpResponse.json<Account[]>(filtered);
  }),
];

// モック仕訳データ
interface JournalEntrySummary {
  journalEntryId: number;
  journalDate: string;
  description: string;
  totalDebitAmount: number;
  totalCreditAmount: number;
  status: string;
  version: number;
}

const mockJournalEntries: JournalEntrySummary[] = [
  {
    journalEntryId: 1,
    journalDate: '2024-04-01',
    description: '現金売上',
    totalDebitAmount: 10000,
    totalCreditAmount: 10000,
    status: 'DRAFT',
    version: 1,
  },
  {
    journalEntryId: 2,
    journalDate: '2024-04-05',
    description: '仕入支払',
    totalDebitAmount: 5000,
    totalCreditAmount: 5000,
    status: 'DRAFT',
    version: 1,
  },
  {
    journalEntryId: 3,
    journalDate: '2024-04-10',
    description: '経費精算',
    totalDebitAmount: 3000,
    totalCreditAmount: 3000,
    status: 'APPROVED',
    version: 1,
  },
  {
    journalEntryId: 4,
    journalDate: '2024-04-15',
    description: '給与支払',
    totalDebitAmount: 200000,
    totalCreditAmount: 200000,
    status: 'CONFIRMED',
    version: 1,
  },
  {
    journalEntryId: 5,
    journalDate: '2024-04-20',
    description: '備品購入',
    totalDebitAmount: 50000,
    totalCreditAmount: 50000,
    status: 'PENDING',
    version: 1,
  },
  // テスト用仕訳エントリ（E2E テストで使用）
  {
    journalEntryId: 100,
    journalDate: '2024-01-15',
    description: 'ユーザーテスト仕訳',
    totalDebitAmount: 5000,
    totalCreditAmount: 5000,
    status: 'DRAFT',
    version: 1,
  },
  {
    journalEntryId: 101,
    journalDate: '2024-02-01',
    description: '編集テスト用仕訳',
    totalDebitAmount: 10000,
    totalCreditAmount: 10000,
    status: 'DRAFT',
    version: 1,
  },
  {
    journalEntryId: 102,
    journalDate: '2024-03-01',
    description: '削除テスト用仕訳',
    totalDebitAmount: 8000,
    totalCreditAmount: 8000,
    status: 'DRAFT',
    version: 1,
  },
  {
    journalEntryId: 103,
    journalDate: '2024-03-01',
    description: 'バランステスト仕訳',
    totalDebitAmount: 5000,
    totalCreditAmount: 5000,
    status: 'DRAFT',
    version: 1,
  },
  {
    journalEntryId: 104,
    journalDate: '2024-04-01',
    description: '保存テスト仕訳',
    totalDebitAmount: 8000,
    totalCreditAmount: 8000,
    status: 'DRAFT',
    version: 1,
  },
  // 削除テスト用エントリ
  {
    journalEntryId: 105,
    journalDate: '2024-05-02',
    description: 'ダイアログテスト仕訳',
    totalDebitAmount: 4000,
    totalCreditAmount: 4000,
    status: 'DRAFT',
    version: 1,
  },
  {
    journalEntryId: 106,
    journalDate: '2024-05-03',
    description: '削除実行テスト仕訳',
    totalDebitAmount: 5000,
    totalCreditAmount: 5000,
    status: 'DRAFT',
    version: 1,
  },
  {
    journalEntryId: 107,
    journalDate: '2024-05-04',
    description: 'ユーザー削除テスト',
    totalDebitAmount: 2000,
    totalCreditAmount: 2000,
    status: 'DRAFT',
    version: 1,
  },
];

let nextJournalEntryId = 200;

type MockEntry = (typeof mockJournalEntries)[number];

const applySearchFilters = (entries: MockEntry[], params: URLSearchParams): MockEntry[] => {
  let filtered = [...entries];
  const statusParams = params.getAll('status');
  if (statusParams.length > 0) {
    filtered = filtered.filter((e) => statusParams.includes(e.status));
  }
  const dateFrom = params.get('dateFrom');
  if (dateFrom) filtered = filtered.filter((e) => e.journalDate >= dateFrom);
  const dateTo = params.get('dateTo');
  if (dateTo) filtered = filtered.filter((e) => e.journalDate <= dateTo);
  return filtered;
};

const applyAmountAndDescriptionFilters = (
  entries: MockEntry[],
  params: URLSearchParams
): MockEntry[] => {
  let filtered = [...entries];
  const amountFrom = params.get('amountFrom');
  if (amountFrom) filtered = filtered.filter((e) => e.totalDebitAmount >= parseFloat(amountFrom));
  const amountTo = params.get('amountTo');
  if (amountTo) filtered = filtered.filter((e) => e.totalDebitAmount <= parseFloat(amountTo));
  const description = params.get('description');
  if (description) {
    const desc = description.toLowerCase();
    filtered = filtered.filter((e) => e.description.toLowerCase().includes(desc));
  }
  return filtered;
};

/**
 * 仕訳関連のハンドラー
 */
export const journalEntryHandlers = [
  // 仕訳検索 (US-JNL-005) - must be before the list handler
  http.get(/\/journal-entries\/search/, ({ request }) => {
    const url = new URL(request.url);
    const page = parseInt(url.searchParams.get('page') || '0', 10);
    const size = parseInt(url.searchParams.get('size') || '20', 10);

    let filtered = applySearchFilters(mockJournalEntries, url.searchParams);
    filtered = applyAmountAndDescriptionFilters(filtered, url.searchParams);

    const totalElements = filtered.length;
    const totalPages = Math.ceil(totalElements / size);
    const start = page * size;
    const content = filtered.slice(start, start + size);

    return HttpResponse.json({
      content,
      page,
      size,
      totalElements,
      totalPages,
    });
  }),
  // 仕訳一覧取得（ページネーション対応）
  http.get(/\/journal-entries\/?$/, ({ request }) => {
    const url = new URL(request.url);
    const page = parseInt(url.searchParams.get('page') || '0', 10);
    const size = parseInt(url.searchParams.get('size') || '20', 10);
    const statusFilter = url.searchParams.getAll('status');
    const dateFrom = url.searchParams.get('dateFrom');
    const dateTo = url.searchParams.get('dateTo');

    // フィルタリング
    let filtered = [...mockJournalEntries];
    if (statusFilter.length > 0) {
      filtered = filtered.filter((entry) => statusFilter.includes(entry.status));
    }
    if (dateFrom) {
      filtered = filtered.filter((entry) => entry.journalDate >= dateFrom);
    }
    if (dateTo) {
      filtered = filtered.filter((entry) => entry.journalDate <= dateTo);
    }

    // ページネーション
    const totalElements = filtered.length;
    const totalPages = Math.ceil(totalElements / size);
    const start = page * size;
    const content = filtered.slice(start, start + size);

    return HttpResponse.json({
      content,
      page,
      size,
      totalElements,
      totalPages,
    });
  }),

  // 仕訳詳細取得
  http.get(/\/journal-entries\/(\d+)$/, ({ request }) => {
    const url = new URL(request.url);
    const match = url.pathname.match(/\/journal-entries\/(\d+)$/);
    const id = match ? parseInt(match[1], 10) : 0;

    const entry = mockJournalEntries.find((e) => e.journalEntryId === id);
    if (!entry) {
      return HttpResponse.json({ message: '仕訳が見つかりません' }, { status: 404 });
    }

    return HttpResponse.json({
      journalEntryId: entry.journalEntryId,
      journalDate: entry.journalDate,
      description: entry.description,
      status: entry.status,
      version: entry.version,
      lines: [
        {
          lineNumber: 1,
          accountId: 1,
          accountCode: '1000',
          accountName: '現金預金',
          debitAmount: entry.totalDebitAmount,
          creditAmount: 0,
        },
        {
          lineNumber: 2,
          accountId: 2,
          accountCode: '4000',
          accountName: '売上高',
          debitAmount: 0,
          creditAmount: entry.totalCreditAmount,
        },
      ],
    });
  }),

  // 仕訳登録（mockJournalEntries に永続化して一覧・詳細取得で参照可能にする）
  http.post('*/journal-entries', async ({ request }) => {
    const body = (await request.json()) as {
      journalDate: string;
      description: string;
      lines?: Array<{
        lineNumber: number;
        accountId: number;
        debitAmount?: number;
        creditAmount?: number;
      }>;
    };
    const journalEntryId = nextJournalEntryId++;

    const totalDebit = body.lines?.reduce((sum, l) => sum + (l.debitAmount || 0), 0) || 0;
    const totalCredit = body.lines?.reduce((sum, l) => sum + (l.creditAmount || 0), 0) || 0;

    const newEntry: JournalEntrySummary = {
      journalEntryId,
      journalDate: body.journalDate,
      description: body.description,
      totalDebitAmount: totalDebit,
      totalCreditAmount: totalCredit,
      status: 'DRAFT',
      version: 1,
    };
    mockJournalEntries.unshift(newEntry);

    return HttpResponse.json({
      success: true,
      journalEntryId,
      journalDate: body.journalDate,
      description: body.description,
      status: 'DRAFT',
    });
  }),

  // 仕訳更新
  http.put(/\/journal-entries\/(\d+)$/, async ({ request }) => {
    const url = new URL(request.url);
    const match = url.pathname.match(/\/journal-entries\/(\d+)$/);
    const id = match ? parseInt(match[1], 10) : 0;
    const body = (await request.json()) as {
      journalDate: string;
      description: string;
      version: number;
    };

    const entry = mockJournalEntries.find((e) => e.journalEntryId === id);
    if (!entry) {
      return HttpResponse.json(
        { success: false, errorMessage: '仕訳が見つかりません' },
        { status: 404 }
      );
    }

    return HttpResponse.json({
      success: true,
      journalEntryId: id,
      journalDate: body.journalDate,
      description: body.description,
      status: entry.status,
      version: entry.version + 1,
      message: '仕訳を更新しました',
    });
  }),

  // 仕訳削除
  http.delete(/\/journal-entries\/(\d+)$/, ({ request }) => {
    const url = new URL(request.url);
    const match = url.pathname.match(/\/journal-entries\/(\d+)$/);
    const id = match ? parseInt(match[1], 10) : 0;

    const entry = mockJournalEntries.find((e) => e.journalEntryId === id);
    if (!entry) {
      return HttpResponse.json(
        { success: false, errorMessage: '仕訳が見つかりません' },
        { status: 404 }
      );
    }

    if (entry.status !== 'DRAFT') {
      return HttpResponse.json(
        { success: false, errorMessage: '下書きステータスの仕訳のみ削除できます' },
        { status: 400 }
      );
    }

    return HttpResponse.json({
      success: true,
      message: '仕訳を削除しました',
    });
  }),

  // 仕訳承認申請 (US-JNL-007)
  http.post(/\/journal-entries\/(\d+)\/submit$/, ({ request }) => {
    const url = new URL(request.url);
    const match = url.pathname.match(/\/journal-entries\/(\d+)\/submit$/);
    const id = match ? parseInt(match[1], 10) : 0;

    const entry = mockJournalEntries.find((e) => e.journalEntryId === id);
    if (!entry) {
      return HttpResponse.json(
        { success: false, errorMessage: '仕訳が見つかりません' },
        { status: 404 }
      );
    }

    if (entry.status !== 'DRAFT') {
      return HttpResponse.json(
        { success: false, errorMessage: '下書き状態の仕訳のみ承認申請可能です' },
        { status: 400 }
      );
    }

    // ステータスを更新
    entry.status = 'PENDING';

    return HttpResponse.json({
      success: true,
      journalEntryId: id,
      status: 'PENDING',
      message: '仕訳を承認申請しました',
    });
  }),
  // 仕訳承認 (US-JNL-008)
  http.post(/\/journal-entries\/(\d+)\/approve$/, ({ request }) => {
    const url = new URL(request.url);
    const match = url.pathname.match(/\/journal-entries\/(\d+)\/approve$/);
    const id = match ? parseInt(match[1], 10) : 0;

    const entry = mockJournalEntries.find((e) => e.journalEntryId === id);
    if (!entry) {
      return HttpResponse.json(
        { success: false, errorMessage: '仕訳が見つかりません' },
        { status: 404 }
      );
    }

    if (entry.status !== 'PENDING') {
      return HttpResponse.json(
        { success: false, errorMessage: '承認待ち状態の仕訳のみ承認可能です' },
        { status: 400 }
      );
    }

    // ステータスを更新
    entry.status = 'APPROVED';

    return HttpResponse.json({
      success: true,
      journalEntryId: id,
      status: 'APPROVED',
      approvedBy: 'manager',
      approvedAt: new Date().toISOString(),
      message: '仕訳を承認しました',
    });
  }),
];

// 総勘定元帳モックデータ
interface GeneralLedgerEntry {
  journalEntryId: number;
  journalDate: string;
  description: string;
  debitAmount: number;
  creditAmount: number;
  runningBalance: number;
}

interface GeneralLedgerResult {
  content: GeneralLedgerEntry[];
  accountId: number;
  accountCode: string;
  accountName: string;
  openingBalance: number;
  debitTotal: number;
  creditTotal: number;
  closingBalance: number;
  page: number;
  size: number;
  totalElements: number;
  totalPages: number;
}

const mockGeneralLedgerEntries: GeneralLedgerEntry[] = [
  {
    journalEntryId: 1,
    journalDate: '2024-04-01',
    description: '現金売上',
    debitAmount: 10000,
    creditAmount: 0,
    runningBalance: 10000,
  },
  {
    journalEntryId: 2,
    journalDate: '2024-04-05',
    description: '仕入支払',
    debitAmount: 0,
    creditAmount: 5000,
    runningBalance: 5000,
  },
  {
    journalEntryId: 3,
    journalDate: '2024-04-10',
    description: '売上入金',
    debitAmount: 30000,
    creditAmount: 0,
    runningBalance: 35000,
  },
  {
    journalEntryId: 4,
    journalDate: '2024-06-01',
    description: '元帳テスト仕訳',
    debitAmount: 25000,
    creditAmount: 0,
    runningBalance: 60000,
  },
  {
    journalEntryId: 5,
    journalDate: '2024-06-15',
    description: '詳細遷移テスト',
    debitAmount: 30000,
    creditAmount: 0,
    runningBalance: 90000,
  },
];

// 日次残高モックデータ
interface DailyBalanceEntry {
  date: string;
  debitTotal: number;
  creditTotal: number;
  balance: number;
  transactionCount: number;
}

interface DailyBalanceResult {
  accountId: number;
  accountCode: string;
  accountName: string;
  openingBalance: number;
  debitTotal: number;
  creditTotal: number;
  closingBalance: number;
  entries: DailyBalanceEntry[];
}

const mockDailyBalanceEntries: DailyBalanceEntry[] = [
  {
    date: '2024-04-01',
    debitTotal: 10000,
    creditTotal: 0,
    balance: 10000,
    transactionCount: 2,
  },
  {
    date: '2024-04-02',
    debitTotal: 5000,
    creditTotal: 2000,
    balance: 13000,
    transactionCount: 3,
  },
  {
    date: '2024-04-03',
    debitTotal: 0,
    creditTotal: 4000,
    balance: 9000,
    transactionCount: 1,
  },
  {
    date: '2024-04-04',
    debitTotal: 15000,
    creditTotal: 0,
    balance: 24000,
    transactionCount: 4,
  },
  {
    date: '2024-04-05',
    debitTotal: 2000,
    creditTotal: 7000,
    balance: 19000,
    transactionCount: 2,
  },
];

// 総勘定元帳ヘルパー関数
const filterEntriesByDateRange = (
  entries: GeneralLedgerEntry[],
  dateFrom: string | null,
  dateTo: string | null
): GeneralLedgerEntry[] => {
  let filtered = [...entries];
  if (dateFrom) {
    filtered = filtered.filter((entry) => entry.journalDate >= dateFrom);
  }
  if (dateTo) {
    filtered = filtered.filter((entry) => entry.journalDate <= dateTo);
  }
  return filtered;
};

const recalculateRunningBalances = (entries: GeneralLedgerEntry[]): GeneralLedgerEntry[] => {
  let runningBalance = 0;
  return entries.map((entry) => {
    runningBalance = runningBalance + entry.debitAmount - entry.creditAmount;
    return { ...entry, runningBalance };
  });
};

const filterDailyEntriesByDateRange = (
  entries: DailyBalanceEntry[],
  dateFrom: string | null,
  dateTo: string | null
): DailyBalanceEntry[] => {
  let filtered = [...entries];
  if (dateFrom) {
    filtered = filtered.filter((entry) => entry.date >= dateFrom);
  }
  if (dateTo) {
    filtered = filtered.filter((entry) => entry.date <= dateTo);
  }
  return filtered;
};

const recalculateDailyBalances = (entries: DailyBalanceEntry[]): DailyBalanceEntry[] => {
  let balance = 0;
  return entries.map((entry) => {
    balance = balance + entry.debitTotal - entry.creditTotal;
    return { ...entry, balance };
  });
};

const buildGeneralLedgerResult = (
  content: GeneralLedgerEntry[],
  accountId: number,
  page: number,
  size: number,
  totalElements: number
): GeneralLedgerResult => {
  const debitTotal = content.reduce((sum, e) => sum + e.debitAmount, 0);
  const creditTotal = content.reduce((sum, e) => sum + e.creditAmount, 0);
  const closingBalance = content.length > 0 ? content[content.length - 1].runningBalance : 0;

  return {
    content,
    accountId,
    accountCode: accountId === 1 ? '1000' : '2000',
    accountName: accountId === 1 ? '現金預金' : '買掛金',
    openingBalance: 0,
    debitTotal,
    creditTotal,
    closingBalance,
    page,
    size,
    totalElements,
    totalPages: Math.ceil(totalElements / size),
  };
};

const buildDailyBalanceResult = (
  entries: DailyBalanceEntry[],
  accountId: number
): DailyBalanceResult => {
  const debitTotal = entries.reduce((sum, e) => sum + e.debitTotal, 0);
  const creditTotal = entries.reduce((sum, e) => sum + e.creditTotal, 0);
  const closingBalance = entries.length > 0 ? entries[entries.length - 1].balance : 0;

  return {
    accountId,
    accountCode: accountId === 1 ? '1000' : '2000',
    accountName: accountId === 1 ? '現金預金' : '買掛金',
    openingBalance: 0,
    debitTotal,
    creditTotal,
    closingBalance,
    entries,
  };
};

/**
 * 総勘定元帳関連のハンドラー
 */
export const generalLedgerHandlers = [
  http.get('*/general-ledger', ({ request }) => {
    const url = new URL(request.url);
    const accountId = parseInt(url.searchParams.get('accountId') || '0', 10);
    const page = parseInt(url.searchParams.get('page') || '0', 10);
    const size = parseInt(url.searchParams.get('size') || '20', 10);
    const dateFrom = url.searchParams.get('dateFrom');
    const dateTo = url.searchParams.get('dateTo');

    if (!accountId) {
      return HttpResponse.json({ errorMessage: '勘定科目を選択してください' }, { status: 400 });
    }

    const filtered = filterEntriesByDateRange(mockGeneralLedgerEntries, dateFrom, dateTo);
    const withBalances = recalculateRunningBalances(filtered);
    const start = page * size;
    const content = withBalances.slice(start, start + size);
    const result = buildGeneralLedgerResult(content, accountId, page, size, withBalances.length);

    return HttpResponse.json(result);
  }),
];

/**
 * 日次残高関連のハンドラー
 */
export const dailyBalanceHandlers = [
  http.get('*/daily-balance', ({ request }) => {
    const url = new URL(request.url);
    const accountId = parseInt(url.searchParams.get('accountId') || '0', 10);
    const dateFrom = url.searchParams.get('dateFrom');
    const dateTo = url.searchParams.get('dateTo');

    if (!accountId) {
      return HttpResponse.json({ errorMessage: '勘定科目を選択してください' }, { status: 400 });
    }

    const filtered = filterDailyEntriesByDateRange(mockDailyBalanceEntries, dateFrom, dateTo);
    const withBalances = recalculateDailyBalances(filtered);
    const result = buildDailyBalanceResult(withBalances, accountId);

    return HttpResponse.json(result);
  }),
];

/**
 * すべてのハンドラー
 */
export const handlers = [
  ...authHandlers,
  ...userHandlers,
  ...accountHandlers,
  ...journalEntryHandlers,
  ...generalLedgerHandlers,
  ...dailyBalanceHandlers,
];
