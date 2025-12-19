import { http, HttpResponse } from 'msw';

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

export const handlers = [
  // 勘定科目一覧取得
  http.get('/api/accounts', () => {
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
  http.get('/api/accounts/:code', ({ params }) => {
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

  // 認証
  http.post('/api/auth/login', async ({ request }) => {
    const body = (await request.json()) as { username: string; password: string };
    if (body.username === 'admin' && body.password === 'password') {
      return HttpResponse.json({
        accessToken: 'mock-access-token',
        refreshToken: 'mock-refresh-token',
        user: {
          id: 1,
          username: 'admin',
          role: 'ADMIN',
        },
      });
    }
    return new HttpResponse(null, { status: 401 });
  }),
];
