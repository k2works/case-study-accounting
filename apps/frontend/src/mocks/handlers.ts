import { http, HttpResponse } from 'msw';
import type { LoginRequest, LoginResponse } from '../api/model';

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
        accessToken: 'mock-access-token-admin',
        refreshToken: 'mock-refresh-token-admin',
        username: 'admin',
        role: 'ADMIN',
      });
    }

    // 成功ケース: user/Password123!
    if (body.username === 'user' && body.password === 'Password123!') {
      return HttpResponse.json<LoginResponse>({
        success: true,
        accessToken: 'mock-access-token-user',
        refreshToken: 'mock-refresh-token-user',
        username: 'user',
        role: 'USER',
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

    if (body.refreshToken?.startsWith('mock-refresh-token')) {
      return HttpResponse.json({
        accessToken: 'mock-new-access-token',
      });
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
