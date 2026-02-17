/**
 * 補助元帳照会 E2E テスト
 *
 * US-LDG-002: 補助元帳照会
 *
 * 受入条件:
 * - 勘定科目と補助科目を選択して元帳を表示できる
 * - 日付、仕訳番号、摘要、借方金額、貸方金額、残高が表示される
 * - 期間を指定して絞り込みできる
 */

import {
  createVisitFunction,
  describeAccessControl,
  describeErrorHandling,
  itShouldShowDropdownOptions,
  itShouldShowSummaryLabels,
  itShouldShowTableHeaders,
} from '../../support/ledgerTestConfig';

const TEST_CONFIG = {
  page: {
    path: '/subsidiary-ledger',
    testId: 'subsidiary-ledger-page',
  },
  selectors: {
    filter: '[data-testid="subsidiary-ledger-filter"]',
    accountSelect: 'subsidiary-ledger-filter-account',
    subAccount: '#subsidiary-ledger-filter-sub-account',
    dateFrom: '#subsidiary-ledger-filter-date-from',
    dateTo: '#subsidiary-ledger-filter-date-to',
    summary: '[data-testid="subsidiary-ledger-summary"]',
    table: 'subsidiary-ledger-table',
  },
} as const;

const visitSubsidiaryLedgerPage = createVisitFunction(TEST_CONFIG);

describe('US-LDG-002: 補助元帳照会', () => {
  beforeEach(() => {
    cy.clearAuth();
  });

  describe('画面表示', () => {
    beforeEach(() => {
      visitSubsidiaryLedgerPage();
    });

    it('補助元帳照会ページが表示される', () => {
      cy.contains('h1', '補助元帳照会').should('be.visible');
      cy.get(TEST_CONFIG.selectors.filter).should('be.visible');
      cy.get(`#${TEST_CONFIG.selectors.accountSelect}`).should('be.visible');
      cy.get(TEST_CONFIG.selectors.subAccount).should('be.visible');
      cy.get(TEST_CONFIG.selectors.dateFrom).should('be.visible');
      cy.get(TEST_CONFIG.selectors.dateTo).should('be.visible');
      cy.contains('button', '照会').should('be.visible');
    });

    itShouldShowDropdownOptions(TEST_CONFIG.selectors.accountSelect);
  });

  describe('勘定科目と補助科目を選択して元帳を表示できる', () => {
    beforeEach(() => {
      visitSubsidiaryLedgerPage();
    });

    it('勘定科目を選択すると補助元帳が表示される', () => {
      cy.selectAccountOption(TEST_CONFIG.selectors.accountSelect);
      cy.get(TEST_CONFIG.selectors.summary, { timeout: 15000 }).should('be.visible');
      cy.get(`[data-testid="${TEST_CONFIG.selectors.table}"]`, { timeout: 15000 }).should(
        'be.visible'
      );
    });

    it('補助科目を入力して照会できる', () => {
      cy.selectAccountOption(TEST_CONFIG.selectors.accountSelect);
      cy.get(TEST_CONFIG.selectors.subAccount).type('A001');
      cy.contains('button', '照会').click();
      cy.get(`[data-testid="${TEST_CONFIG.selectors.table}"]`, { timeout: 15000 }).should(
        'be.visible'
      );
    });
  });

  describe('日付、仕訳番号、摘要、借方金額、貸方金額、残高が表示される', () => {
    beforeEach(() => {
      visitSubsidiaryLedgerPage();
    });

    itShouldShowTableHeaders(TEST_CONFIG, ['日付', '仕訳番号', '摘要', '借方', '貸方', '残高']);

    itShouldShowSummaryLabels(TEST_CONFIG, ['前期繰越', '借方合計', '貸方合計', '期末残高']);
  });

  describe('期間を指定して絞り込みできる', () => {
    beforeEach(() => {
      visitSubsidiaryLedgerPage();
    });

    it('開始日と終了日を指定してフィルタリングできる', () => {
      cy.selectAccountAndWaitForTable(
        TEST_CONFIG.selectors.accountSelect,
        TEST_CONFIG.selectors.table
      );
      cy.get(TEST_CONFIG.selectors.dateFrom).clear().type('2024-04-01');
      cy.get(TEST_CONFIG.selectors.dateTo).clear().type('2024-04-30');
      cy.contains('button', '照会').click();
      cy.get(`[data-testid="${TEST_CONFIG.selectors.table}"]`, { timeout: 15000 }).should(
        'be.visible'
      );
    });
  });

  describeAccessControl(TEST_CONFIG, visitSubsidiaryLedgerPage, '補助元帳ページ');

  describeErrorHandling(() => visitSubsidiaryLedgerPage());
});
