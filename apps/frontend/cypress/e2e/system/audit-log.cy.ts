/**
 * 監査ログ照会 E2E テスト
 *
 * US-SYS-001: 監査ログ照会
 *
 * 受入条件:
 * - 監査ログの一覧が表示される
 * - ユーザーID、アクション種別、日付範囲で検索できる
 * - ログイン履歴が確認できる
 * - データ変更履歴が確認できる
 * - ADMIN のみアクセスできる
 */

import { createVisitFunction } from '../../support/ledgerTestConfig';

const TEST_CONFIG = {
  page: {
    path: '/system/audit',
    testId: 'audit-log-page',
  },
  selectors: {
    filter: '[data-testid="audit-log-filter"]',
    table: '[data-testid="audit-log-table"]',
    empty: '[data-testid="audit-log-empty"]',
    userIdInput: '#audit-filter-user-id',
    actionTypeSelect: '#audit-filter-action-type',
    dateFromInput: '#audit-filter-date-from',
    dateToInput: '#audit-filter-date-to',
  },
} as const;

const visitAuditLogPage = createVisitFunction(TEST_CONFIG);

/** 検索ボタンをクリックしてテーブルの表示を待つ */
const clickSearchAndWaitTable = () => {
  cy.contains('button', '検索').click();
  cy.get(TEST_CONFIG.selectors.table, { timeout: 15000 }).should('be.visible');
};

describe('US-SYS-001: 監査ログ照会', () => {
  beforeEach(() => {
    cy.clearAuth();
  });

  describe('画面表示', () => {
    beforeEach(() => {
      visitAuditLogPage('admin');
    });

    it('監査ログページが表示される', () => {
      cy.contains('h1', '監査ログ').should('be.visible');
      cy.get(TEST_CONFIG.selectors.filter).should('be.visible');
    });

    it('検索フィルターが表示される', () => {
      cy.get(TEST_CONFIG.selectors.userIdInput).should('be.visible');
      cy.get(TEST_CONFIG.selectors.actionTypeSelect).should('be.visible');
      cy.get(TEST_CONFIG.selectors.dateFromInput).should('be.visible');
      cy.get(TEST_CONFIG.selectors.dateToInput).should('be.visible');
      cy.contains('button', '検索').should('be.visible');
    });

    it('アクション種別のドロップダウンに選択肢がある', () => {
      cy.get(TEST_CONFIG.selectors.actionTypeSelect).find('option').should('have.length.greaterThan', 1);
      cy.get(TEST_CONFIG.selectors.actionTypeSelect).contains('option', 'すべて').should('exist');
      cy.get(TEST_CONFIG.selectors.actionTypeSelect).contains('option', 'ログイン').should('exist');
      cy.get(TEST_CONFIG.selectors.actionTypeSelect).contains('option', 'ログアウト').should('exist');
      cy.get(TEST_CONFIG.selectors.actionTypeSelect).contains('option', '作成').should('exist');
      cy.get(TEST_CONFIG.selectors.actionTypeSelect).contains('option', '承認').should('exist');
    });
  });

  describe('監査ログの一覧表示', () => {
    beforeEach(() => {
      visitAuditLogPage('admin');
    });

    it('検索ボタンをクリックすると監査ログテーブルが表示される', () => {
      clickSearchAndWaitTable();
    });

    it('テーブルに必要なヘッダーが表示される', () => {
      clickSearchAndWaitTable();
      ['ID', 'ユーザーID', 'アクション', '対象種別', '対象ID', '説明', 'IPアドレス', '日時'].forEach(
        (header) => {
          cy.contains('th', header).should('be.visible');
        }
      );
    });

    it('件数とページ情報が表示される', () => {
      clickSearchAndWaitTable();
      cy.contains('全').should('be.visible');
      cy.contains('件').should('be.visible');
      cy.contains('ページ').should('be.visible');
    });
  });

  describe('ユーザーID で検索できる', () => {
    beforeEach(() => {
      visitAuditLogPage('admin');
    });

    it('ユーザーID を入力して検索すると結果が絞り込まれる', () => {
      cy.get(TEST_CONFIG.selectors.userIdInput).type('admin');
      clickSearchAndWaitTable();
      cy.get(TEST_CONFIG.selectors.table).should('contain', 'admin');
    });
  });

  describe('アクション種別で検索できる', () => {
    beforeEach(() => {
      visitAuditLogPage('admin');
    });

    it('ログインでフィルタすると結果が絞り込まれる', () => {
      cy.get(TEST_CONFIG.selectors.actionTypeSelect).select('LOGIN');
      clickSearchAndWaitTable();
      cy.get(TEST_CONFIG.selectors.table).should('contain', 'ログイン');
    });

    it('承認でフィルタすると結果が絞り込まれる', () => {
      cy.get(TEST_CONFIG.selectors.actionTypeSelect).select('APPROVE');
      clickSearchAndWaitTable();
      cy.get(TEST_CONFIG.selectors.table).should('contain', '承認');
    });
  });

  describe('ログイン履歴が確認できる', () => {
    beforeEach(() => {
      visitAuditLogPage('admin');
    });

    it('ログインの監査ログが表示される', () => {
      cy.get(TEST_CONFIG.selectors.actionTypeSelect).select('LOGIN');
      clickSearchAndWaitTable();
      cy.get(TEST_CONFIG.selectors.table).should('contain', 'ログイン');
      cy.get(TEST_CONFIG.selectors.table).should('contain', 'ログイン成功');
    });
  });

  describe('データ変更履歴が確認できる', () => {
    beforeEach(() => {
      visitAuditLogPage('admin');
    });

    it('作成の監査ログが表示される', () => {
      cy.get(TEST_CONFIG.selectors.actionTypeSelect).select('CREATE');
      clickSearchAndWaitTable();
      cy.get(TEST_CONFIG.selectors.table).should('contain', '作成');
      cy.get(TEST_CONFIG.selectors.table).should('contain', '仕訳伝票');
    });
  });

  describe('日付範囲で検索できる', () => {
    beforeEach(() => {
      visitAuditLogPage('admin');
    });

    it('日付範囲を指定して検索できる', () => {
      cy.get(TEST_CONFIG.selectors.dateFromInput).type('2026-01-01');
      cy.get(TEST_CONFIG.selectors.dateToInput).type('2026-12-31');
      clickSearchAndWaitTable();
    });
  });

  describe('アクセス制御', () => {
    it('管理者は監査ログページにアクセスできる', () => {
      visitAuditLogPage('admin');
      cy.get(TEST_CONFIG.selectors.filter).should('be.visible');
    });

    it('経理責任者は監査ログページにアクセスできない', () => {
      cy.visitLedgerPage('manager', 'Password123!', '/', 'dashboard');
      cy.visit(TEST_CONFIG.page.path);
      cy.url().should('not.include', '/system/audit');
    });

    it('一般ユーザーは監査ログページにアクセスできない', () => {
      cy.visitLedgerPage('user', 'Password123!', '/', 'dashboard');
      cy.visit(TEST_CONFIG.page.path);
      cy.url().should('not.include', '/system/audit');
    });

    it('未認証ユーザーは監査ログページにアクセスできない', () => {
      cy.visit(TEST_CONFIG.page.path);
      cy.url().should('include', '/login');
      cy.get('[data-testid="login-page"]', { timeout: 15000 }).should('be.visible');
    });
  });
});
