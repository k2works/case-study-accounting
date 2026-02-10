/**
 * 月次残高照会 E2E テスト
 *
 * US-LDG-004: 月次残高照会
 *
 * 受入条件:
 * - 勘定科目を選択して月次残高を表示できる
 * - 月、借方合計、貸方合計、残高が表示される
 * - 年度を指定して絞り込みできる
 * - 月次推移をグラフで表示できる
 */

// テスト共通設定
const TEST_CONFIG = {
  page: {
    path: '/ledger/monthly-balance',
    testId: 'monthly-balance-page',
  },
  selectors: {
    filter: '[data-testid="monthly-balance-filter"]',
    accountSelect: 'monthly-balance-filter-account',
    fiscalPeriod: '#monthly-balance-filter-fiscal-period',
    summary: '[data-testid="monthly-balance-summary"]',
    table: 'monthly-balance-table',
    chart: '[data-testid="monthly-balance-chart"]',
  },
  credentials: {
    admin: { username: 'admin', password: 'Password123!' },
    user: { username: 'user', password: 'Password123!' },
    manager: { username: 'manager', password: 'Password123!' },
  },
} as const;

const visitMonthlyBalancePage = (role: 'admin' | 'user' | 'manager' = 'admin') => {
  const { username, password } = TEST_CONFIG.credentials[role];
  cy.visitLedgerPage(username, password, TEST_CONFIG.page.path, TEST_CONFIG.page.testId);
};

describe('US-LDG-004: 月次残高照会', () => {
  beforeEach(() => {
    cy.clearAuth();
  });

  describe('画面表示', () => {
    beforeEach(() => {
      visitMonthlyBalancePage();
    });

    it('月次残高照会ページが表示される', () => {
      cy.contains('h1', '月次残高照会').should('be.visible');
      cy.get(TEST_CONFIG.selectors.filter).should('be.visible');
      cy.get(`#${TEST_CONFIG.selectors.accountSelect}`).should('be.visible');
      cy.get(TEST_CONFIG.selectors.fiscalPeriod).should('be.visible');
      cy.contains('button', '照会').should('be.visible');
    });

    it('勘定科目ドロップダウンに選択肢が表示される', () => {
      cy.get(`#${TEST_CONFIG.selectors.accountSelect} option`, { timeout: 15000 }).should(
        'have.length.greaterThan',
        1
      );
      cy.get(`#${TEST_CONFIG.selectors.accountSelect}`)
        .contains('option', '勘定科目を選択')
        .should('exist');
    });
  });

  describe('勘定科目を選択して月次残高を表示できる', () => {
    beforeEach(() => {
      visitMonthlyBalancePage();
    });

    it('勘定科目を選択すると月次残高が表示される', () => {
      cy.selectAccountOption(TEST_CONFIG.selectors.accountSelect);
      cy.get(TEST_CONFIG.selectors.summary, { timeout: 15000 }).should('be.visible');
    });

    it('月、借方合計、貸方合計、残高が表示される（テーブルヘッダー確認）', () => {
      cy.selectAccountOption(TEST_CONFIG.selectors.accountSelect);
      cy.get(`[data-testid="${TEST_CONFIG.selectors.table}"]`, { timeout: 15000 }).should(
        'be.visible'
      );
      ['月', '借方合計', '貸方合計', '期末残高'].forEach((header) => {
        cy.contains('th', header).should('be.visible');
      });
    });

    it('サマリ情報（期首残高、借方合計、貸方合計、期末残高）が表示される', () => {
      cy.selectAccountOption(TEST_CONFIG.selectors.accountSelect);
      cy.get(TEST_CONFIG.selectors.summary, { timeout: 15000 }).should('be.visible');
      ['期首残高', '借方合計', '貸方合計', '期末残高'].forEach((label) => {
        cy.get(TEST_CONFIG.selectors.summary).should('contain', label);
      });
    });
  });

  describe('年度を指定して絞り込みできる', () => {
    beforeEach(() => {
      visitMonthlyBalancePage();
    });

    it('年度を入力してフィルタリングできる', () => {
      cy.selectAccountOption(TEST_CONFIG.selectors.accountSelect);
      cy.get(TEST_CONFIG.selectors.summary, { timeout: 15000 }).should('be.visible');
      cy.get(TEST_CONFIG.selectors.fiscalPeriod).clear().type('2024');
      cy.contains('button', '照会').click();
      cy.wait(1000);
      cy.get(TEST_CONFIG.selectors.summary, { timeout: 15000 }).should('be.visible');
    });
  });

  describe('月次推移をグラフで表示できる', () => {
    beforeEach(() => {
      visitMonthlyBalancePage();
    });

    it('勘定科目を選択すると月次推移グラフが表示される', () => {
      cy.selectAccountOption(TEST_CONFIG.selectors.accountSelect);
      cy.get(TEST_CONFIG.selectors.chart, { timeout: 15000 }).should('be.visible');
    });

    it('グラフには Recharts の LineChart が含まれる', () => {
      cy.selectAccountOption(TEST_CONFIG.selectors.accountSelect);
      cy.get(TEST_CONFIG.selectors.chart, { timeout: 15000 }).should('be.visible');
      cy.get(`${TEST_CONFIG.selectors.chart} svg`).should('exist');
    });
  });

  describe('アクセス制御', () => {
    it('一般ユーザーも月次残高ページにアクセスできる', () => {
      visitMonthlyBalancePage('user');
      cy.get(TEST_CONFIG.selectors.filter).should('be.visible');
    });

    it('未認証ユーザーは月次残高ページにアクセスできない', () => {
      cy.visit(TEST_CONFIG.page.path);
      cy.url().should('include', '/login');
      cy.get('[data-testid="login-page"]', { timeout: 15000 }).should('be.visible');
    });

    it('経理責任者も月次残高ページにアクセスできる', () => {
      visitMonthlyBalancePage('manager');
      cy.get(`[data-testid="${TEST_CONFIG.page.testId}"]`).should('be.visible');
    });
  });

  describe('エラーハンドリング', () => {
    beforeEach(() => {
      visitMonthlyBalancePage();
    });

    it('勘定科目を選択せずに照会するとエラーメッセージが表示される', () => {
      cy.contains('button', '照会').click();
      cy.contains('勘定科目を選択してください').should('be.visible');
    });
  });
});
