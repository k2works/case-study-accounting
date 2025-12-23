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
  accountCode: string;
  accountName: string;
  bsplType: string;
  debitCreditType: string;
  elementType: string;
  displayOrder: number;
  version: number;
}

/**
 * 認証関連のハンドラー
 * Note: Orval 生成コードの URL が /api/auth/login で、
 * axios の baseURL が /api のため、実際のリクエスト先は /api/api/auth/login になる
 */
export const authHandlers = [
  // ログイン（Orval 生成コード対応: baseURL + /api/auth/login = /api/api/auth/login）
  http.post('*/auth/login', async ({ request }) => {
    const body = (await request.json()) as LoginRequest;

    // 成功ケース: admin/Password123!
    if (body.username === 'admin' && body.password === 'Password123!') {
      return HttpResponse.json<LoginResponse>({
        success: true,
        accessToken: createMockJwt('admin'),
        refreshToken: createMockJwt('admin-refresh', 86400),
        username: 'admin',
        role: 'ADMIN',
      });
    }

    // 成功ケース: user/Password123!
    if (body.username === 'user' && body.password === 'Password123!') {
      return HttpResponse.json<LoginResponse>({
        success: true,
        accessToken: createMockJwt('user'),
        refreshToken: createMockJwt('user-refresh', 86400),
        username: 'user',
        role: 'USER',
      });
    }

    // 失敗ケース: ロックされたアカウント
    if (body.username === 'locked') {
      return HttpResponse.json<LoginResponse>({
        success: false,
        errorMessage:
          'アカウントがロックされています。ログイン試行が複数回失敗したため、セキュリティ保護のためロックされました。管理者にお問い合わせください。',
      });
    }

    // 失敗ケース: 無効化されたアカウント
    if (body.username === 'inactive') {
      return HttpResponse.json<LoginResponse>({
        success: false,
        errorMessage: 'アカウントが無効化されています。管理者にお問い合わせください。',
      });
    }

    // 失敗ケース: 認証エラー
    return HttpResponse.json<LoginResponse>({
      success: false,
      errorMessage: 'ユーザーIDまたはパスワードが正しくありません',
    });
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
];

/**
 * 勘定科目関連のハンドラー
 */
export const accountHandlers = [
  // 勘定科目一覧取得
  http.get('*/accounts', () => {
    return HttpResponse.json<Account[]>([
      {
        accountCode: '111',
        accountName: '現金預金',
        bsplType: 'B',
        debitCreditType: '借',
        elementType: '資産',
        displayOrder: 1,
        version: 1,
      },
      {
        accountCode: '211',
        accountName: '買掛金',
        bsplType: 'B',
        debitCreditType: '貸',
        elementType: '負債',
        displayOrder: 10,
        version: 1,
      },
    ]);
  }),

  // 勘定科目詳細取得
  http.get('*/accounts/:code', ({ params }) => {
    const { code } = params;
    return HttpResponse.json<Account>({
      accountCode: code as string,
      accountName: '現金預金',
      bsplType: 'B',
      debitCreditType: '借',
      elementType: '資産',
      displayOrder: 1,
      version: 1,
    });
  }),
];

/**
 * すべてのハンドラー
 */
export const handlers = [...authHandlers, ...accountHandlers];
