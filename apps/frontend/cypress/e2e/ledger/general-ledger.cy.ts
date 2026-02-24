/**
 * 総勘定元帳照会 E2E テスト
 *
 * US-LDG-001: 総勘定元帳照会
 *
 * 受入条件:
 * - 勘定科目を選択して元帳を表示できる
 * - 日付、仕訳番号、摘要、借方金額、貸方金額、残高が表示される
 * - 期間を指定して絞り込みできる
 * - 仕訳の詳細画面に遷移できる
 */

import {
  createVisitFunction,
  describeAccessControl,
  describeErrorHandling,
} from '../../support/ledgerTestConfig';

const TEST_CONFIG = {
  page: {
    path: '/general-ledger',
    testId: 'general-ledger-page',
  },
  selectors: {
    filter: '[data-testid="general-ledger-filter"]',
    accountSelect: 'general-ledger-filter-account',
    summary: '[data-testid="general-ledger-summary"]',
    table: 'general-ledger-table',
  },
} as const;

const visitGeneralLedgerPage = createVisitFunction(TEST_CONFIG);

/** 勘定科目を選択してデータが表示されるのを待つ */
const selectAccountAndWait = () => {
  cy.get('#general-ledger-filter-account option', { timeout: 15000 }).should(
    'have.length.greaterThan',
    1
  );
  cy.get('#general-ledger-filter-account').select(1);
  cy.get('[data-testid="general-ledger-summary"]', { timeout: 15000 }).should('be.visible');
  cy.get('[data-testid="general-ledger-table"]', { timeout: 15000 }).should('be.visible');
};

describe('US-LDG-001: 総勘定元帳照会', () => {
  // MSW モードでは勘定科目はモックデータで提供されるため、setupTestAccounts は不要
  beforeEach(() => {
    cy.clearAuth();
  });

  describe('画面表示', () => {
    beforeEach(() => {
      visitGeneralLedgerPage();
    });

    it('総勘定元帳照会ページが表示される', () => {
      cy.contains('h1', '総勘定元帳照会').should('be.visible');
      cy.get(TEST_CONFIG.selectors.filter).should('be.visible');
      cy.get('#general-ledger-filter-account').should('be.visible');
      cy.get('#general-ledger-filter-date-from').should('be.visible');
      cy.get('#general-ledger-filter-date-to').should('be.visible');
      cy.contains('button', '照会').should('be.visible');
    });

    it('勘定科目ドロップダウンに選択肢が表示される', () => {
      cy.get('#general-ledger-filter-account option', { timeout: 15000 }).should(
        'have.length.greaterThan',
        1
      );
      cy.get('#general-ledger-filter-account').contains('option', '勘定科目を選択').should('exist');
    });
  });

  describe('勘定科目を選択して元帳を表示できる', () => {
    beforeEach(() => {
      visitGeneralLedgerPage();
    });

    it('勘定科目を選択すると元帳が表示される', () => {
      selectAccountAndWait();
    });

    it('日付、仕訳番号、摘要、借方金額、貸方金額、残高が表示される', () => {
      selectAccountAndWait();
      ['日付', '仕訳番号', '摘要', '借方', '貸方', '残高'].forEach((header) => {
        cy.contains('th', header).should('be.visible');
      });
    });

    it('サマリ情報（前期繰越、借方合計、貸方合計、期末残高）が表示される', () => {
      selectAccountAndWait();
      ['前期繰越', '借方合計', '貸方合計', '期末残高'].forEach((label) => {
        cy.get('[data-testid="general-ledger-summary"]').should('contain', label);
      });
    });
  });

  describe('期間を指定して絞り込みできる', () => {
    beforeEach(() => {
      visitGeneralLedgerPage();
    });

    it('開始日と終了日を指定してフィルタリングできる', () => {
      selectAccountAndWait();
      cy.get('#general-ledger-filter-date-from').clear().type('2024-04-01');
      cy.get('#general-ledger-filter-date-to').clear().type('2024-04-30');
      cy.contains('button', '照会').click();
      cy.wait(1000);
      cy.get('[data-testid="general-ledger-table"]', { timeout: 15000 }).should('be.visible');
    });

    it('開始日のみ指定してフィルタリングできる', () => {
      selectAccountAndWait();
      cy.get('#general-ledger-filter-date-from').clear().type('2024-01-01');
      cy.contains('button', '照会').click();
      cy.wait(1000);
      cy.get('[data-testid="general-ledger-table"]', { timeout: 15000 }).should('be.visible');
    });

    it('終了日のみ指定してフィルタリングできる', () => {
      selectAccountAndWait();
      cy.get('#general-ledger-filter-date-to').clear().type('2024-12-31');
      cy.contains('button', '照会').click();
      cy.wait(1000);
      cy.get('[data-testid="general-ledger-table"]', { timeout: 15000 }).should('be.visible');
    });
  });

  describe('仕訳の詳細画面に遷移できる', () => {
    beforeEach(() => {
      visitGeneralLedgerPage();
    });

    it('仕訳番号をクリックすると仕訳詳細画面に遷移する', () => {
      selectAccountAndWait();
      cy.get('[data-testid="general-ledger-table"] table tbody tr', { timeout: 15000 }).should(
        'have.length.at.least',
        1
      );
      cy.get('[data-testid="general-ledger-table"] table tbody tr').first().find('a').first().click();
      cy.url().should('include', '/journal/entries/');
      cy.url().should('include', '/edit');
      cy.get('[data-testid="journal-entry-edit-form"]', { timeout: 15000 }).should('be.visible');
    });
  });

  describe('ページネーション', () => {
    beforeEach(() => {
      visitGeneralLedgerPage();
    });

    it('勘定科目を選択するとページネーションUIが表示される', () => {
      selectAccountAndWait();
      cy.get('.pagination', { timeout: 15000 }).should('exist');
    });

    it('表示件数を変更できる', () => {
      selectAccountAndWait();
      cy.get('.pagination', { timeout: 15000 }).should('be.visible');
      // select 要素が安定するのを待ってから操作する（React 再レンダリング対策）
      cy.get('.pagination__select', { timeout: 10000 }).should('be.visible');
      cy.get('.pagination__select').select('10');
      cy.get('.pagination__select', { timeout: 10000 }).should('have.value', '10');
    });
  });

  describe('データダウンロード', () => {
    beforeEach(() => {
      visitGeneralLedgerPage();
    });

    it('CSV/Excel でダウンロードできる', () => {
      selectAccountAndWait();
      cy.contains('button', 'CSV').should('be.visible');
      cy.contains('button', 'Excel').should('be.visible');
    });
  });

  describeAccessControl(TEST_CONFIG, visitGeneralLedgerPage, '総勘定元帳ページ');
  describeErrorHandling(() => visitGeneralLedgerPage());
});
