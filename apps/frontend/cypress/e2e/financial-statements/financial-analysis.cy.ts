/**
 * 財務分析 E2E テスト
 *
 * US-FS-003: 財務分析表示
 *
 * 受入条件:
 * - ROE、ROA、流動比率、自己資本比率が表示される
 * - 各指標の計算根拠（計算式）が表示される
 * - 業界平均との比較が表示される
 * - 前期比較ができる
 */

import { createVisitFunction } from '../../support/ledgerTestConfig';
import type { LedgerRole } from '../../support/ledgerTestConfig';

const TEST_CONFIG = {
  page: {
    path: '/financial-statements/analysis',
    testId: 'financial-analysis-page',
  },
  selectors: {
    filter: '[data-testid="financial-analysis-filter"]',
    dateFromInput: '#fa-filter-date-from',
    dateToInput: '#fa-filter-date-to',
    compFromInput: '#fa-filter-comp-from',
    compToInput: '#fa-filter-comp-to',
    indicators: '[data-testid="financial-analysis-indicators"]',
    trend: '[data-testid="financial-analysis-trend"]',
  },
} as const;

const visitFinancialAnalysisPage = createVisitFunction(TEST_CONFIG);

/** 表示ボタンをクリックして指標テーブルの表示を待つ */
const clickDisplayAndWaitIndicators = () => {
  cy.contains('button', '表示').click();
  cy.get(TEST_CONFIG.selectors.indicators, { timeout: 15000 }).should('be.visible');
};

/** 表示ボタンをクリックしてトレンドチャートの表示を待つ */
const clickDisplayAndWaitTrend = () => {
  cy.contains('button', '表示').click();
  cy.get(TEST_CONFIG.selectors.trend, { timeout: 15000 }).should('be.visible');
};

describe('US-FS-003: 財務分析表示', () => {
  beforeEach(() => {
    cy.clearAuth();
  });

  describe('画面表示', () => {
    beforeEach(() => {
      visitFinancialAnalysisPage('admin');
    });

    it('財務分析ページが表示される', () => {
      cy.contains('h1', '財務分析').should('be.visible');
      cy.get(TEST_CONFIG.selectors.filter).should('be.visible');
      cy.get(TEST_CONFIG.selectors.dateFromInput).should('be.visible');
      cy.get(TEST_CONFIG.selectors.dateToInput).should('be.visible');
      cy.contains('button', '表示').should('be.visible');
    });

    it('前期比較の日付入力フィールドが表示される', () => {
      cy.get(TEST_CONFIG.selectors.compFromInput).should('be.visible');
      cy.get(TEST_CONFIG.selectors.compToInput).should('be.visible');
    });
  });

  describe('ROE、ROA、流動比率、自己資本比率が表示される', () => {
    beforeEach(() => {
      visitFinancialAnalysisPage('admin');
    });

    it('表示ボタンをクリックすると財務指標テーブルが表示される', () => {
      clickDisplayAndWaitIndicators();
    });

    it('収益性指標（ROE、ROA、売上高利益率）が表示される', () => {
      clickDisplayAndWaitIndicators();
      cy.get(TEST_CONFIG.selectors.indicators).should('contain', '収益性');
      cy.get(TEST_CONFIG.selectors.indicators).should('contain', 'ROE');
      cy.get(TEST_CONFIG.selectors.indicators).should('contain', 'ROA');
      cy.get(TEST_CONFIG.selectors.indicators).should('contain', '売上高利益率');
    });

    it('安全性指標（流動比率、自己資本比率、負債比率）が表示される', () => {
      clickDisplayAndWaitIndicators();
      cy.get(TEST_CONFIG.selectors.indicators).should('contain', '安全性');
      cy.get(TEST_CONFIG.selectors.indicators).should('contain', '流動比率');
      cy.get(TEST_CONFIG.selectors.indicators).should('contain', '自己資本比率');
      cy.get(TEST_CONFIG.selectors.indicators).should('contain', '負債比率');
    });

    it('効率性指標（総資産回転率）が表示される', () => {
      clickDisplayAndWaitIndicators();
      cy.get(TEST_CONFIG.selectors.indicators).should('contain', '効率性');
      cy.get(TEST_CONFIG.selectors.indicators).should('contain', '総資産回転率');
    });

    it('テーブルに指標名、当期、計算式、業界平均のヘッダーが表示される', () => {
      clickDisplayAndWaitIndicators();
      ['指標名', '当期', '計算式', '業界平均'].forEach((header) => {
        cy.contains('th', header).should('be.visible');
      });
    });
  });

  describe('各指標の計算根拠が表示される', () => {
    beforeEach(() => {
      visitFinancialAnalysisPage('admin');
    });

    it('各指標に計算式が表示される', () => {
      clickDisplayAndWaitIndicators();
      cy.get(TEST_CONFIG.selectors.indicators).should('contain', '当期純利益 ÷ 自己資本 × 100');
      cy.get(TEST_CONFIG.selectors.indicators).should('contain', '当期純利益 ÷ 総資産 × 100');
      cy.get(TEST_CONFIG.selectors.indicators).should('contain', '当期純利益 ÷ 売上高 × 100');
      cy.get(TEST_CONFIG.selectors.indicators).should('contain', '流動資産 ÷ 流動負債 × 100');
      cy.get(TEST_CONFIG.selectors.indicators).should('contain', '自己資本 ÷ 総資産 × 100');
      cy.get(TEST_CONFIG.selectors.indicators).should('contain', '売上高 ÷ 総資産');
    });
  });

  describe('業界平均との比較が表示される', () => {
    beforeEach(() => {
      visitFinancialAnalysisPage('admin');
    });

    it('指標比較チャートが表示される', () => {
      clickDisplayAndWaitTrend();
      cy.get(TEST_CONFIG.selectors.trend).should('contain', '指標比較チャート');
      cy.get(TEST_CONFIG.selectors.trend).should('contain', '業界平均');
    });

    it('各カテゴリのチャートが表示される', () => {
      clickDisplayAndWaitTrend();
      ['収益性', '安全性', '効率性'].forEach((category) => {
        cy.get(TEST_CONFIG.selectors.trend).should('contain', category);
      });
    });
  });

  describe('前期比較ができる', () => {
    beforeEach(() => {
      visitFinancialAnalysisPage('admin');
    });

    it('前期比較の日付を入力して表示すると比較データが表示される', () => {
      cy.get(TEST_CONFIG.selectors.dateFromInput).type('2026-01-01');
      cy.get(TEST_CONFIG.selectors.dateToInput).type('2026-01-31');
      cy.get(TEST_CONFIG.selectors.compFromInput).type('2025-01-01');
      cy.get(TEST_CONFIG.selectors.compToInput).type('2025-01-31');
      clickDisplayAndWaitIndicators();
      ['前期', '増減', '変化率'].forEach((header) => {
        cy.contains('th', header).should('be.visible');
      });
    });
  });

  describe('期間指定', () => {
    beforeEach(() => {
      visitFinancialAnalysisPage('admin');
    });

    it('期間を指定して表示できる', () => {
      cy.get(TEST_CONFIG.selectors.dateFromInput).type('2026-01-01');
      cy.get(TEST_CONFIG.selectors.dateToInput).type('2026-01-31');
      clickDisplayAndWaitIndicators();
    });

    it('期間なしで全期間の財務分析を表示できる', () => {
      clickDisplayAndWaitIndicators();
    });
  });

  describe('アクセス制御', () => {
    it('管理者は財務分析ページにアクセスできる', () => {
      visitFinancialAnalysisPage('admin');
      cy.get(TEST_CONFIG.selectors.filter).should('be.visible');
    });

    it('経理責任者は財務分析ページにアクセスできる', () => {
      visitFinancialAnalysisPage('manager');
      cy.get(`[data-testid="${TEST_CONFIG.page.testId}"]`).should('be.visible');
    });

    it('一般ユーザーは財務分析ページにアクセスできない', () => {
      // USER ロールでログインしてからアクセスを試みる
      cy.visitLedgerPage('user', 'Password123!', '/', 'dashboard');
      cy.visit(TEST_CONFIG.page.path);
      // ManagerRoute により、USER ロールではダッシュボードにリダイレクトされる
      cy.url().should('not.include', '/financial-statements/analysis');
    });

    it('未認証ユーザーは財務分析ページにアクセスできない', () => {
      cy.visit(TEST_CONFIG.page.path);
      cy.url().should('include', '/login');
      cy.get('[data-testid="login-page"]', { timeout: 15000 }).should('be.visible');
    });
  });
});
