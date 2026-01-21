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

// モックユーザー定義
const mockUsers: Record<string, { password: string; role: string }> = {
  admin: { password: 'Password123!', role: 'ADMIN' },
  user: { password: 'Password123!', role: 'USER' },
  manager: { password: 'Password123!', role: 'MANAGER' },
  viewer: { password: 'Password123!', role: 'VIEWER' },
};

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

// 既存の勘定科目コードを追跡（重複チェック用）
const existingAccountCodes = new Set(['1000', '2000']);
let nextAccountId = 3;

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
    const id = match ? match[1] : '0';

    // 存在しない勘定科目のチェック（ID が 999 の場合はエラー）
    if (id === '999') {
      return HttpResponse.json(
        {
          success: false,
          errorMessage: '勘定科目が見つかりません',
        },
        { status: 404 }
      );
    }

    // 使用中勘定科目のチェック（ID が 888 の場合は使用中エラー）
    if (id === '888') {
      return HttpResponse.json(
        {
          success: false,
          errorMessage: 'この勘定科目は仕訳で使用されているため削除できません',
        },
        { status: 409 }
      );
    }

    // 成功ケース
    return HttpResponse.json({
      success: true,
      accountId: Number(id),
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

    // 成功ケース
    existingAccountCodes.add(body.accountCode);
    const accountId = nextAccountId++;
    return HttpResponse.json({
      success: true,
      accountId,
      accountCode: body.accountCode,
      accountName: body.accountName,
      accountType: body.accountType,
    });
  }),

  // 勘定科目一覧取得（:id なしの /accounts のみにマッチ）
  http.get(/\/accounts\/?$/, () => {
    return HttpResponse.json<Account[]>([
      {
        accountId: 1,
        accountCode: '1000',
        accountName: '現金預金',
        accountType: 'ASSET',
      },
      {
        accountId: 2,
        accountCode: '2000',
        accountName: '買掛金',
        accountType: 'LIABILITY',
      },
    ]);
  }),
];

/**
 * すべてのハンドラー
 */
export const handlers = [...authHandlers, ...accountHandlers];
