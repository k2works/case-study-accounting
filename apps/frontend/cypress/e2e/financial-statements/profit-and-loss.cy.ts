/**
 * 損益計算書 E2E テスト
 *
 * US-FS-002: 損益計算書表示
 *
 * 受入条件:
 * - 収益、費用の各科目と金額が表示される
 * - 当期純利益が計算・表示される
 * - 期間を指定して表示できる
 * - 前期比較ができる
 * - PDF/Excel でダウンロードできる
 */

import {
  createVisitFunction,
  describeAccessControl,
} from '../../support/ledgerTestConfig';

const TEST_CONFIG = {
  page: {
    path: '/financial-statements/income-statement',
    testId: 'profit-and-loss-page',
  },
  selectors: {
    filter: '[data-testid="profit-and-loss-filter"]',
    dateFromInput: '#pl-filter-date-from',
    dateToInput: '#pl-filter-date-to',
    comparativeFromInput: '#pl-filter-comparative-from',
    comparativeToInput: '#pl-filter-comparative-to',
    summary: '[data-testid="profit-and-loss-summary"]',
    table: 'profit-and-loss-table',
  },
} as const;

const visitProfitAndLossPage = createVisitFunction(TEST_CONFIG);

describe('US-FS-002: 損益計算書表示', () => {
  beforeEach(() => {
    cy.clearAuth();
  });

  describe('画面表示', () => {
    beforeEach(() => {
      visitProfitAndLossPage();
    });

    it('損益計算書ページが表示される', () => {
      cy.contains('h1', '損益計算書').should('be.visible');
      cy.get(TEST_CONFIG.selectors.filter).should('be.visible');
      cy.get(TEST_CONFIG.selectors.dateFromInput).should('be.visible');
      cy.get(TEST_CONFIG.selectors.dateToInput).should('be.visible');
      cy.get(TEST_CONFIG.selectors.comparativeFromInput).should('be.visible');
      cy.get(TEST_CONFIG.selectors.comparativeToInput).should('be.visible');
      cy.contains('button', '表示').should('be.visible');
    });
  });

  describe('収益、費用の各科目と金額が表示される', () => {
    beforeEach(() => {
      visitProfitAndLossPage();
    });

    it('表示ボタンをクリックすると損益計算書が表示される', () => {
      cy.contains('button', '表示').click();
      cy.get(`[data-testid="${TEST_CONFIG.selectors.table}"]`, { timeout: 15000 }).should(
        'be.visible'
      );
    });

    it('収益の部と費用の部が表示される', () => {
      cy.contains('button', '表示').click();
      cy.get(`[data-testid="${TEST_CONFIG.selectors.table}"]`, { timeout: 15000 }).should(
        'be.visible'
      );
      cy.get(`[data-testid="${TEST_CONFIG.selectors.table}"]`).should('contain', '収益の部');
      cy.get(`[data-testid="${TEST_CONFIG.selectors.table}"]`).should('contain', '費用の部');
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

  describe('当期純利益が計算・表示される', () => {
    beforeEach(() => {
      visitProfitAndLossPage();
    });

    it('サマリに収益合計、費用合計、当期純利益が表示される', () => {
      cy.contains('button', '表示').click();
      cy.get(TEST_CONFIG.selectors.summary, { timeout: 15000 }).should('be.visible');
      ['収益合計', '費用合計', '当期純利益'].forEach((label) => {
        cy.get(TEST_CONFIG.selectors.summary).should('contain', label);
      });
    });

    it('テーブルに当期純利益行が表示される', () => {
      cy.contains('button', '表示').click();
      cy.get(`[data-testid="${TEST_CONFIG.selectors.table}"]`, { timeout: 15000 }).should(
        'be.visible'
      );
      cy.get(`[data-testid="${TEST_CONFIG.selectors.table}"]`).should('contain', '当期純利益');
    });
  });

  describe('期間を指定して表示できる', () => {
    beforeEach(() => {
      visitProfitAndLossPage();
    });

    it('期間を入力して表示できる', () => {
      cy.get(TEST_CONFIG.selectors.dateFromInput).type('2026-01-01');
      cy.get(TEST_CONFIG.selectors.dateToInput).type('2026-01-31');
      cy.contains('button', '表示').click();
      cy.get(TEST_CONFIG.selectors.summary, { timeout: 15000 }).should('be.visible');
      cy.get(TEST_CONFIG.selectors.summary).should('contain', '2026-01-01');
      cy.get(TEST_CONFIG.selectors.summary).should('contain', '2026-01-31');
    });

    it('期間なしで全期間の損益計算書を表示できる', () => {
      cy.contains('button', '表示').click();
      cy.get(TEST_CONFIG.selectors.summary, { timeout: 15000 }).should('be.visible');
      cy.get(TEST_CONFIG.selectors.summary).should('contain', '全期間');
    });
  });

  describe('前期比較ができる', () => {
    beforeEach(() => {
      visitProfitAndLossPage();
    });

    it('前期比較期間を入力して表示できる', () => {
      cy.get(TEST_CONFIG.selectors.dateFromInput).type('2026-01-01');
      cy.get(TEST_CONFIG.selectors.dateToInput).type('2026-03-31');
      cy.get(TEST_CONFIG.selectors.comparativeFromInput).type('2025-01-01');
      cy.get(TEST_CONFIG.selectors.comparativeToInput).type('2025-03-31');
      cy.contains('button', '表示').click();
      cy.get(TEST_CONFIG.selectors.summary, { timeout: 15000 }).should('be.visible');
      cy.get(TEST_CONFIG.selectors.summary).should('contain', '前期比較');
    });

    it('前期比較時にテーブルに前期と増減のヘッダーが表示される', () => {
      cy.get(TEST_CONFIG.selectors.dateFromInput).type('2026-01-01');
      cy.get(TEST_CONFIG.selectors.dateToInput).type('2026-03-31');
      cy.get(TEST_CONFIG.selectors.comparativeFromInput).type('2025-01-01');
      cy.get(TEST_CONFIG.selectors.comparativeToInput).type('2025-03-31');
      cy.contains('button', '表示').click();
      cy.get(`[data-testid="${TEST_CONFIG.selectors.table}"]`, { timeout: 15000 }).should(
        'be.visible'
      );
      ['前期', '増減'].forEach((header) => {
        cy.contains('th', header).should('be.visible');
      });
    });
  });

  describe('CSV/Excel/PDF でダウンロードできる', () => {
    beforeEach(() => {
      visitProfitAndLossPage();
    });

    it('表示後にエクスポートボタンが表示される', () => {
      cy.contains('button', '表示').click();
      cy.get(TEST_CONFIG.selectors.summary, { timeout: 15000 }).should('be.visible');
      cy.contains('button', 'CSV').should('be.visible');
      cy.contains('button', 'Excel').should('be.visible');
      cy.contains('button', 'PDF').should('be.visible');
    });
  });

  describeAccessControl(TEST_CONFIG, visitProfitAndLossPage, '損益計算書ページ');
});
