/**
 * 自動仕訳パターン管理 E2E テスト
 *
 * US-MST-007: 自動仕訳パターン登録
 * US-MST-008: 自動仕訳パターン編集
 */
import {
  loginAndVisitMasterList,
  loginAndVisitPage,
  navigateToFirstRowEdit,
  describeMasterAccessControl,
  describeMasterDeleteTests,
} from '../../support/masterTestHelper';

const PAGE_CONFIG = {
  path: '/master/auto-journal-patterns',
  listTestId: 'auto-journal-pattern-list-page',
  displayName: '自動仕訳パターン',
};

describe('US-MST-007/008: 自動仕訳パターン管理', () => {
  beforeEach(() => {
    cy.clearAuth();
  });

  describe('一覧表示', () => {
    beforeEach(() => {
      loginAndVisitMasterList(PAGE_CONFIG);
    });

    it('自動仕訳パターン一覧が表示される', () => {
      cy.contains('h1', '自動仕訳パターン一覧').should('be.visible');
    });

    it('新規登録ボタンが表示される', () => {
      cy.contains('button', '新規登録').should('be.visible');
    });
  });

  describe('新規登録（US-MST-007）', () => {
    beforeEach(() => {
      loginAndVisitPage('/master/auto-journal-patterns/new', 'create-auto-journal-pattern-page');
    });

    it('新規登録フォームが表示される', () => {
      cy.get('[data-testid="pattern-code-input"]').should('be.visible');
      cy.get('[data-testid="pattern-name-input"]').should('be.visible');
      cy.get('[data-testid="source-table-input"]').should('be.visible');
      cy.get('[data-testid="description-input"]').should('be.visible');
      cy.get('[data-testid="add-item-button"]').should('be.visible');
      cy.get('[data-testid="create-pattern-submit"]').should('be.visible');
    });

    it('明細行が 1 行表示されている', () => {
      cy.get('[data-testid="item-row-0"]').should('be.visible');
    });

    it('管理者がパターンを登録できる（明細 2 行）', () => {
      cy.get('[data-testid="pattern-code-input"]').clear().type('TEST001');
      cy.get('[data-testid="pattern-name-input"]').clear().type('テスト売上パターン');
      cy.get('[data-testid="source-table-input"]').clear().type('sales');
      cy.get('[data-testid="description-input"]').clear().type('テスト用の自動仕訳パターン');

      cy.get('[data-testid="item-row-0"]').within(() => {
        cy.get('select').select('D');
        cy.get('input[placeholder*="勘定科目"]').clear().type('1100');
        cy.get('input[placeholder*="計算式"]').clear().type('amount');
        cy.get('input[placeholder*="摘要"]').clear().type('売上 {id}', { parseSpecialCharSequences: false });
      });

      cy.get('[data-testid="add-item-button"]').click();
      cy.get('[data-testid="item-row-1"]').within(() => {
        cy.get('select').select('C');
        cy.get('input[placeholder*="勘定科目"]').clear().type('4100');
        cy.get('input[placeholder*="計算式"]').clear().type('amount');
        cy.get('input[placeholder*="摘要"]').clear().type('売上 {id}', { parseSpecialCharSequences: false });
      });

      cy.get('[data-testid="create-pattern-submit"]').click();
      cy.contains('自動仕訳パターン登録が完了しました', { timeout: 10000 }).should('be.visible');
    });

    it('重複コードで登録するとエラーが表示される', () => {
      cy.get('[data-testid="pattern-code-input"]').clear().type('PAT001');
      cy.get('[data-testid="pattern-name-input"]').clear().type('重複テスト');
      cy.get('[data-testid="source-table-input"]').clear().type('sales');

      cy.get('[data-testid="item-row-0"]').within(() => {
        cy.get('select').select('D');
        cy.get('input[placeholder*="勘定科目"]').clear().type('1100');
        cy.get('input[placeholder*="計算式"]').clear().type('amount');
      });

      cy.get('[data-testid="create-pattern-submit"]').click();
      cy.contains('既に使用されています', { timeout: 10000 }).should('be.visible');
    });

    it('必須フィールドが空の場合バリデーションエラーが表示される', () => {
      cy.get('[data-testid="pattern-code-input"]').clear();
      cy.get('[data-testid="pattern-name-input"]').clear();
      cy.get('[data-testid="source-table-input"]').clear();
      cy.get('[data-testid="create-pattern-submit"]').click();
      cy.contains('パターンコードを入力してください').should('be.visible');
    });
  });

  describe('編集（US-MST-008）', () => {
    beforeEach(() => {
      loginAndVisitMasterList(PAGE_CONFIG);
      cy.get('table tbody tr').should('have.length.at.least', 1);
    });

    it('一覧の編集ボタンから編集ページに遷移できる', () => {
      navigateToFirstRowEdit('edit-auto-journal-pattern-page');
      cy.contains('h1', '自動仕訳パターン編集').should('be.visible');
    });

    it('パターンコードは変更できない', () => {
      navigateToFirstRowEdit('edit-auto-journal-pattern-page');
      cy.get('[data-testid="pattern-code-input"]').should('be.disabled');
      cy.get('[data-testid="pattern-code-input"]').invoke('val').should('not.be.empty');
    });

    it('既存パターンを編集できる', () => {
      navigateToFirstRowEdit('edit-auto-journal-pattern-page');
      cy.get('[data-testid="pattern-name-input"]').clear().type('更新済みパターン');
      cy.get('[data-testid="edit-pattern-submit"]').click();
      cy.url({ timeout: 10000 }).should('include', '/master/auto-journal-patterns');
      cy.url().should('not.include', '/edit');
      cy.contains('自動仕訳パターンを更新しました', { timeout: 10000 }).should('be.visible');
    });

    it('明細行を追加して更新できる', () => {
      navigateToFirstRowEdit('edit-auto-journal-pattern-page');
      cy.get('[data-testid="add-item-button"]').click();
      cy.get('[data-testid^="item-row-"]').last().within(() => {
        cy.get('select').select('C');
        cy.get('input[placeholder*="勘定科目"]').clear().type('5100');
        cy.get('input[placeholder*="計算式"]').clear().type('tax_amount');
      });
      cy.get('[data-testid="edit-pattern-submit"]').click();
      cy.url({ timeout: 10000 }).should('include', '/master/auto-journal-patterns');
      cy.url().should('not.include', '/edit');
    });
  });

  describe('削除', () => {
    beforeEach(() => {
      loginAndVisitMasterList(PAGE_CONFIG);
      cy.get('table tbody tr').should('have.length.at.least', 1);
    });

    describeMasterDeleteTests(PAGE_CONFIG, '削除しました');
  });

  describe('アクセス制御', () => {
    describeMasterAccessControl(PAGE_CONFIG);
  });
});
