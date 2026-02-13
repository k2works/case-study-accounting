/**
 * 日次残高照会 E2E テスト
 *
 * US-LDG-003: 日次残高照会
 *
 * 受入条件:
 * - 勘定科目を選択して日次残高を表示できる
 * - 日付、借方合計、貸方合計、残高が表示される
 * - 期間を指定して絞り込みできる
 * - 残高の推移をグラフで表示できる
 */

import {
  createVisitFunction,
  describeAccessControl,
  describeErrorHandling,
  itShouldShowDropdownOptions,
  itShouldShowSummaryLabels,
  itShouldShowTableHeaders,
} from '../../support/ledgerTestConfig';

// テスト共通設定
const TEST_CONFIG = {
  page: {
    path: '/ledger/daily-balance',
    testId: 'daily-balance-page',
  },
  selectors: {
    filter: '[data-testid="daily-balance-filter"]',
    accountSelect: 'daily-balance-filter-account',
    dateFrom: '#daily-balance-filter-date-from',
    dateTo: '#daily-balance-filter-date-to',
    summary: '[data-testid="daily-balance-summary"]',
    table: 'daily-balance-table',
    chart: '[data-testid="daily-balance-chart"]',
  },
} as const;

const visitDailyBalancePage = createVisitFunction(TEST_CONFIG);

const selectAccountAndVerifyTable = () => {
  cy.selectAccountAndWaitForTable(
    TEST_CONFIG.selectors.accountSelect,
    TEST_CONFIG.selectors.table
  );
};

describe('US-LDG-003: 日次残高照会', () => {
  beforeEach(() => {
    cy.clearAuth();
  });

  describe('画面表示', () => {
    beforeEach(() => {
      visitDailyBalancePage();
    });

    it('日次残高照会ページが表示される', () => {
      cy.contains('h1', '日次残高照会').should('be.visible');
      cy.get(TEST_CONFIG.selectors.filter).should('be.visible');
      cy.get(`#${TEST_CONFIG.selectors.accountSelect}`).should('be.visible');
      cy.get(TEST_CONFIG.selectors.dateFrom).should('be.visible');
      cy.get(TEST_CONFIG.selectors.dateTo).should('be.visible');
      cy.contains('button', '照会').should('be.visible');
    });

    itShouldShowDropdownOptions(TEST_CONFIG.selectors.accountSelect);
  });

  describe('勘定科目を選択して日次残高を表示できる', () => {
    beforeEach(() => {
      visitDailyBalancePage();
    });

    it('勘定科目を選択すると日次残高が表示される', () => {
      cy.selectAccountOption(TEST_CONFIG.selectors.accountSelect);
      cy.get(TEST_CONFIG.selectors.summary, { timeout: 15000 }).should('be.visible');
      cy.get(`[data-testid="${TEST_CONFIG.selectors.table}"]`, { timeout: 15000 }).should(
        'be.visible'
      );
    });

    itShouldShowTableHeaders(TEST_CONFIG, ['日付', '借方合計', '貸方合計', '残高']);

    itShouldShowSummaryLabels(TEST_CONFIG, ['期首残高', '借方合計', '貸方合計', '期末残高']);
  });

  describe('期間を指定して絞り込みできる', () => {
    beforeEach(() => {
      visitDailyBalancePage();
    });

    it('開始日と終了日を指定してフィルタリングできる', () => {
      selectAccountAndVerifyTable();
      cy.get(TEST_CONFIG.selectors.dateFrom).clear().type('2024-04-01');
      cy.get(TEST_CONFIG.selectors.dateTo).clear().type('2024-04-30');
      cy.contains('button', '照会').click();
      cy.wait(1000);
      cy.get(`[data-testid="${TEST_CONFIG.selectors.table}"]`, { timeout: 15000 }).should(
        'be.visible'
      );
    });

    it('開始日のみ指定してフィルタリングできる', () => {
      selectAccountAndVerifyTable();
      cy.get(TEST_CONFIG.selectors.dateFrom).clear().type('2024-01-01');
      cy.contains('button', '照会').click();
      cy.wait(1000);
      cy.get(`[data-testid="${TEST_CONFIG.selectors.table}"]`, { timeout: 15000 }).should(
        'be.visible'
      );
    });

    it('終了日のみ指定してフィルタリングできる', () => {
      selectAccountAndVerifyTable();
      cy.get(TEST_CONFIG.selectors.dateTo).clear().type('2024-12-31');
      cy.contains('button', '照会').click();
      cy.wait(1000);
      cy.get(`[data-testid="${TEST_CONFIG.selectors.table}"]`, { timeout: 15000 }).should(
        'be.visible'
      );
    });
  });

  describe('残高の推移をグラフで表示できる', () => {
    beforeEach(() => {
      visitDailyBalancePage();
    });

    it('勘定科目を選択すると残高推移グラフが表示される', () => {
      cy.selectAccountOption(TEST_CONFIG.selectors.accountSelect);
      cy.get(TEST_CONFIG.selectors.chart, { timeout: 15000 }).should('be.visible');
    });

    it('グラフにはRechartsのLineChartが含まれる', () => {
      cy.selectAccountOption(TEST_CONFIG.selectors.accountSelect);
      cy.get(TEST_CONFIG.selectors.chart, { timeout: 15000 }).should('be.visible');
      cy.get(`${TEST_CONFIG.selectors.chart} svg`).should('exist');
    });
  });

  describeAccessControl(TEST_CONFIG, visitDailyBalancePage, '日次残高ページ');

  describeErrorHandling(() => visitDailyBalancePage());
});
