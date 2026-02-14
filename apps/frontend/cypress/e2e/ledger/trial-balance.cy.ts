/**
 * 残高試算表 E2E テスト
 *
 * US-LDG-005: 残高試算表表示
 *
 * 受入条件:
 * - 全勘定科目の借方残高、貸方残高が一覧表示される
 * - 借方合計と貸方合計が一致していることを確認できる
 * - 基準日を指定して残高を計算できる
 * - 勘定科目種別ごとの小計が表示される
 */

import {
  createVisitFunction,
  describeAccessControl,
} from '../../support/ledgerTestConfig';

// テスト共通設定
const TEST_CONFIG = {
  page: {
    path: '/ledger/trial-balance',
    testId: 'trial-balance-page',
  },
  selectors: {
    filter: '[data-testid="trial-balance-filter"]',
    dateInput: '#trial-balance-filter-date',
    summary: '[data-testid="trial-balance-summary"]',
    table: 'trial-balance-table',
  },
} as const;

const visitTrialBalancePage = createVisitFunction(TEST_CONFIG);

describe('US-LDG-005: 残高試算表表示', () => {
  beforeEach(() => {
    cy.clearAuth();
  });

  describe('画面表示', () => {
    beforeEach(() => {
      visitTrialBalancePage();
    });

    it('残高試算表ページが表示される', () => {
      cy.contains('h1', '残高試算表').should('be.visible');
      cy.get(TEST_CONFIG.selectors.filter).should('be.visible');
      cy.get(TEST_CONFIG.selectors.dateInput).should('be.visible');
      cy.contains('button', '表示').should('be.visible');
    });
  });

  describe('全勘定科目の借方残高、貸方残高が一覧表示される', () => {
    beforeEach(() => {
      visitTrialBalancePage();
    });

    it('表示ボタンをクリックすると試算表が表示される', () => {
      cy.contains('button', '表示').click();
      cy.get(`[data-testid="${TEST_CONFIG.selectors.table}"]`, { timeout: 15000 }).should(
        'be.visible'
      );
    });

    it('勘定科目コード、勘定科目名、借方残高、貸方残高のヘッダーが表示される', () => {
      cy.contains('button', '表示').click();
      cy.get(`[data-testid="${TEST_CONFIG.selectors.table}"]`, { timeout: 15000 }).should(
        'be.visible'
      );
      ['勘定科目コード', '勘定科目名', '借方残高', '貸方残高'].forEach((header) => {
        cy.contains('th', header).should('be.visible');
      });
    });
  });

  describe('借方合計と貸方合計が一致していることを確認できる', () => {
    beforeEach(() => {
      visitTrialBalancePage();
    });

    it('サマリに借方合計、貸方合計、貸借一致情報が表示される', () => {
      cy.contains('button', '表示').click();
      cy.get(TEST_CONFIG.selectors.summary, { timeout: 15000 }).should('be.visible');
      ['借方合計', '貸方合計', '貸借一致'].forEach((label) => {
        cy.get(TEST_CONFIG.selectors.summary).should('contain', label);
      });
    });

    it('貸借一致状態が表示される', () => {
      cy.contains('button', '表示').click();
      cy.get(TEST_CONFIG.selectors.summary, { timeout: 15000 }).should('be.visible');
      // 「一致」または「不一致」のいずれかが表示される
      cy.get(TEST_CONFIG.selectors.summary).then(($summary) => {
        const text = $summary.text();
        expect(text.includes('一致') || text.includes('不一致')).to.be.true;
      });
    });
  });

  describe('基準日を指定して残高を計算できる', () => {
    beforeEach(() => {
      visitTrialBalancePage();
    });

    it('基準日を入力して表示できる', () => {
      cy.get(TEST_CONFIG.selectors.dateInput).type('2026-01-31');
      cy.contains('button', '表示').click();
      cy.get(TEST_CONFIG.selectors.summary, { timeout: 15000 }).should('be.visible');
      cy.get(TEST_CONFIG.selectors.summary).should('contain', '2026-01-31');
    });

    it('基準日なしで全期間の試算表を表示できる', () => {
      cy.contains('button', '表示').click();
      cy.get(TEST_CONFIG.selectors.summary, { timeout: 15000 }).should('be.visible');
      cy.get(TEST_CONFIG.selectors.summary).should('contain', '全期間');
    });
  });

  describe('勘定科目種別ごとの小計が表示される', () => {
    beforeEach(() => {
      visitTrialBalancePage();
    });

    it('種別小計テーブルに資産、負債、純資産、収益、費用の小計が表示される', () => {
      cy.contains('button', '表示').click();
      cy.get(TEST_CONFIG.selectors.summary, { timeout: 15000 }).should('be.visible');
      ['資産', '負債', '純資産', '収益', '費用'].forEach((category) => {
        cy.get(TEST_CONFIG.selectors.summary).should('contain', category);
      });
    });

    it('種別小計テーブルに借方小計と貸方小計のヘッダーが表示される', () => {
      cy.contains('button', '表示').click();
      cy.get(TEST_CONFIG.selectors.summary, { timeout: 15000 }).should('be.visible');
      ['種別', '借方小計', '貸方小計'].forEach((header) => {
        cy.get(TEST_CONFIG.selectors.summary).should('contain', header);
      });
    });
  });

  describeAccessControl(TEST_CONFIG, visitTrialBalancePage, '残高試算表ページ');
});
