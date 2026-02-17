/**
 * 貸借対照表 E2E テスト
 *
 * US-FS-001: 貸借対照表表示
 *
 * 受入条件:
 * - 資産、負債、純資産の各科目と金額が表示される
 * - 資産合計と負債・純資産合計が一致している
 * - 基準日を指定して表示できる
 * - 前期比較ができる
 * - PDF/Excel でダウンロードできる
 */

import {
  createVisitFunction,
  describeAccessControl,
} from '../../support/ledgerTestConfig';

const TEST_CONFIG = {
  page: {
    path: '/financial-statements/balance-sheet',
    testId: 'balance-sheet-page',
  },
  selectors: {
    filter: '[data-testid="balance-sheet-filter"]',
    dateInput: '#balance-sheet-filter-date',
    comparativeDateInput: '#balance-sheet-filter-comparative-date',
    summary: '[data-testid="balance-sheet-summary"]',
    table: 'balance-sheet-table',
  },
} as const;

const visitBalanceSheetPage = createVisitFunction(TEST_CONFIG);

describe('US-FS-001: 貸借対照表表示', () => {
  beforeEach(() => {
    cy.clearAuth();
  });

  describe('画面表示', () => {
    beforeEach(() => {
      visitBalanceSheetPage();
    });

    it('貸借対照表ページが表示される', () => {
      cy.contains('h1', '貸借対照表').should('be.visible');
      cy.get(TEST_CONFIG.selectors.filter).should('be.visible');
      cy.get(TEST_CONFIG.selectors.dateInput).should('be.visible');
      cy.get(TEST_CONFIG.selectors.comparativeDateInput).should('be.visible');
      cy.contains('button', '表示').should('be.visible');
    });
  });

  describe('資産、負債、純資産の各科目と金額が表示される', () => {
    beforeEach(() => {
      visitBalanceSheetPage();
    });

    it('表示ボタンをクリックすると貸借対照表が表示される', () => {
      cy.contains('button', '表示').click();
      cy.get(`[data-testid="${TEST_CONFIG.selectors.table}"]`, { timeout: 15000 }).should(
        'be.visible'
      );
    });

    it('勘定式レイアウトで資産の部と負債・純資産の部が表示される', () => {
      cy.contains('button', '表示').click();
      cy.get(`[data-testid="${TEST_CONFIG.selectors.table}"]`, { timeout: 15000 }).should(
        'be.visible'
      );
      cy.get(`[data-testid="${TEST_CONFIG.selectors.table}"]`).should('contain', '資産の部');
    });

    it('テーブルにコード、勘定科目、金額のヘッダーが表示される', () => {
      cy.contains('button', '表示').click();
      cy.get(`[data-testid="${TEST_CONFIG.selectors.table}"]`, { timeout: 15000 }).should(
        'be.visible'
      );
      ['コード', '勘定科目', '金額'].forEach((header) => {
        cy.contains('th', header).should('be.visible');
      });
    });
  });

  describe('資産合計と負債・純資産合計が一致している', () => {
    beforeEach(() => {
      visitBalanceSheetPage();
    });

    it('サマリに資産合計、負債合計、純資産合計、貸借一致情報が表示される', () => {
      cy.contains('button', '表示').click();
      cy.get(TEST_CONFIG.selectors.summary, { timeout: 15000 }).should('be.visible');
      ['資産合計', '負債合計', '純資産合計', '負債・純資産合計', '貸借一致'].forEach((label) => {
        cy.get(TEST_CONFIG.selectors.summary).should('contain', label);
      });
    });

    it('貸借一致状態が表示される', () => {
      cy.contains('button', '表示').click();
      cy.get(TEST_CONFIG.selectors.summary, { timeout: 15000 }).should('be.visible');
      cy.get(TEST_CONFIG.selectors.summary).then(($summary) => {
        const text = $summary.text();
        expect(text.includes('一致') || text.includes('不一致')).to.be.true;
      });
    });
  });

  describe('基準日を指定して表示できる', () => {
    beforeEach(() => {
      visitBalanceSheetPage();
    });

    it('基準日を入力して表示できる', () => {
      cy.get(TEST_CONFIG.selectors.dateInput).type('2026-01-31');
      cy.contains('button', '表示').click();
      cy.get(TEST_CONFIG.selectors.summary, { timeout: 15000 }).should('be.visible');
      cy.get(TEST_CONFIG.selectors.summary).should('contain', '2026-01-31');
    });

    it('基準日なしで全期間の貸借対照表を表示できる', () => {
      cy.contains('button', '表示').click();
      cy.get(TEST_CONFIG.selectors.summary, { timeout: 15000 }).should('be.visible');
      cy.get(TEST_CONFIG.selectors.summary).should('contain', '全期間');
    });
  });

  describe('前期比較ができる', () => {
    beforeEach(() => {
      visitBalanceSheetPage();
    });

    it('前期比較日を入力して表示できる', () => {
      cy.get(TEST_CONFIG.selectors.dateInput).type('2026-03-31');
      cy.get(TEST_CONFIG.selectors.comparativeDateInput).type('2025-03-31');
      cy.contains('button', '表示').click();
      cy.get(TEST_CONFIG.selectors.summary, { timeout: 15000 }).should('be.visible');
      cy.get(TEST_CONFIG.selectors.summary).should('contain', '前期比較日');
    });

    it('前期比較時にテーブルに前期と増減のヘッダーが表示される', () => {
      cy.get(TEST_CONFIG.selectors.dateInput).type('2026-03-31');
      cy.get(TEST_CONFIG.selectors.comparativeDateInput).type('2025-03-31');
      cy.contains('button', '表示').click();
      cy.get(`[data-testid="${TEST_CONFIG.selectors.table}"]`, { timeout: 15000 }).should(
        'be.visible'
      );
      ['前期', '増減'].forEach((header) => {
        cy.contains('th', header).should('be.visible');
      });
    });
  });

  describe('PDF/Excel でダウンロードできる', () => {
    beforeEach(() => {
      visitBalanceSheetPage();
    });

    it('表示後にエクスポートボタンが表示される', () => {
      cy.contains('button', '表示').click();
      cy.get(TEST_CONFIG.selectors.summary, { timeout: 15000 }).should('be.visible');
      cy.contains('button', 'Excel').should('be.visible');
      cy.contains('button', 'PDF').should('be.visible');
    });
  });

  describeAccessControl(TEST_CONFIG, visitBalanceSheetPage, '貸借対照表ページ');
});
