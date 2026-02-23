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

/** アクション種別で検索してテーブルの表示を確認する */
const searchByAction = (action: string, expectedText: string) => {
  cy.get(TEST_CONFIG.selectors.actionTypeSelect).select(action);
  cy.contains('button', '検索').click();
  cy.get(TEST_CONFIG.selectors.table, { timeout: 15000 }).should('be.visible');
  cy.get(TEST_CONFIG.selectors.table).should('contain', expectedText);
};

describe('US-SYS-001: 監査ログ照会', () => {
  beforeEach(() => {
    cy.clearAuth();
  });

  // 管理者認証が必要な全テストを共通 beforeEach でまとめる
  describe('管理者機能', () => {
    beforeEach(() => {
      visitAuditLogPage('admin');
    });

    it('監査ログページとフィルターが表示される', () => {
      cy.contains('h1', '監査ログ').should('be.visible');
      cy.get(TEST_CONFIG.selectors.filter).should('be.visible');
      cy.get(TEST_CONFIG.selectors.userIdInput).should('be.visible');
      cy.get(TEST_CONFIG.selectors.actionTypeSelect).should('be.visible');
      cy.get(TEST_CONFIG.selectors.dateFromInput).should('be.visible');
      cy.get(TEST_CONFIG.selectors.dateToInput).should('be.visible');
      cy.contains('button', '検索').should('be.visible');
    });

    it('アクション種別のドロップダウンに選択肢がある', () => {
      const select = cy.get(TEST_CONFIG.selectors.actionTypeSelect);
      select.find('option').should('have.length.greaterThan', 1);
      ['すべて', 'ログイン', 'ログアウト', '作成', '承認'].forEach((label) => {
        select.contains('option', label).should('exist');
      });
    });

    it('検索すると監査ログテーブルとヘッダーが表示される', () => {
      cy.contains('button', '検索').click();
      cy.get(TEST_CONFIG.selectors.table, { timeout: 15000 }).should('be.visible');
      ['ID', 'ユーザーID', 'アクション', '対象種別', '対象ID', '説明', 'IPアドレス', '日時'].forEach(
        (header) => {
          cy.contains('th', header).should('be.visible');
        }
      );
      cy.contains('全').should('be.visible');
      cy.contains('件').should('be.visible');
      cy.contains('ページ').should('be.visible');
    });

    it('ユーザーID で検索できる', () => {
      cy.get(TEST_CONFIG.selectors.userIdInput).type('admin');
      cy.contains('button', '検索').click();
      cy.get(TEST_CONFIG.selectors.table, { timeout: 15000 }).should('be.visible');
      cy.get(TEST_CONFIG.selectors.table).should('contain', 'admin');
    });

    it('ログインでフィルタすると結果が絞り込まれる', () => {
      searchByAction('LOGIN', 'ログイン');
      cy.get(TEST_CONFIG.selectors.table).should('contain', 'ログイン成功');
    });

    it('承認でフィルタすると結果が絞り込まれる', () => {
      searchByAction('APPROVE', '承認');
    });

    it('作成の監査ログが表示される', () => {
      searchByAction('CREATE', '作成');
      cy.get(TEST_CONFIG.selectors.table).should('contain', '仕訳伝票');
    });

    it('日付範囲を指定して検索できる', () => {
      cy.get(TEST_CONFIG.selectors.dateFromInput).type('2026-01-01');
      cy.get(TEST_CONFIG.selectors.dateToInput).type('2026-12-31');
      cy.contains('button', '検索').click();
      cy.get(TEST_CONFIG.selectors.table, { timeout: 15000 }).should('be.visible');
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
