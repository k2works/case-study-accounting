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

import {
  createVisitFunction,
  describeAccessControl,
  describeErrorHandling,
  itShouldShowDropdownOptions,
  itShouldShowSummaryLabels,
  itShouldShowTableHeaders,
  describeChartDisplay,
} from '../../support/ledgerTestConfig';

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
} as const;

const visitMonthlyBalancePage = createVisitFunction(TEST_CONFIG);

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

    itShouldShowDropdownOptions(TEST_CONFIG.selectors.accountSelect);
  });

  describe('勘定科目を選択して月次残高を表示できる', () => {
    beforeEach(() => {
      visitMonthlyBalancePage();
    });

    it('勘定科目を選択すると月次残高が表示される', () => {
      cy.selectAccountOption(TEST_CONFIG.selectors.accountSelect);
      cy.get(TEST_CONFIG.selectors.summary, { timeout: 15000 }).should('be.visible');
    });

    itShouldShowTableHeaders(TEST_CONFIG, ['月', '借方合計', '貸方合計', '期末残高']);

    itShouldShowSummaryLabels(TEST_CONFIG, ['期首残高', '借方合計', '貸方合計', '期末残高']);
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

  describeAccessControl(TEST_CONFIG, visitMonthlyBalancePage, '月次残高ページ');

  describeErrorHandling(() => visitMonthlyBalancePage());
});
