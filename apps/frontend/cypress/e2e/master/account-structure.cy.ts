/**
 * 勘定科目構成管理 E2E テスト
 *
 * US-MST-005: 勘定科目構成登録
 * US-MST-006: 勘定科目構成編集
 */
import {
  loginAndVisitMasterList,
  navigateToFirstRowEdit,
  describeMasterAccessControl,
  describeMasterDeleteTests,
} from '../../support/masterTestHelper';

const PAGE_CONFIG = {
  path: '/master/account-structures',
  listTestId: 'account-structure-list-page',
  displayName: '勘定科目構成',
};

describe('US-MST-005: 勘定科目構成登録', () => {
  beforeEach(() => {
    cy.clearAuth();
  });

  describe('一覧表示', () => {
    beforeEach(() => {
      loginAndVisitMasterList(PAGE_CONFIG);
    });

    it('勘定科目構成一覧が表示される', () => {
      cy.contains('th', '勘定科目コード').should('be.visible');
      cy.contains('th', '勘定科目名').should('be.visible');
      cy.contains('th', 'パス').should('be.visible');
      cy.contains('th', '階層').should('be.visible');
      cy.contains('th', '親科目コード').should('be.visible');
      cy.contains('th', '表示順').should('be.visible');
      cy.get('table tbody tr').should('have.length.at.least', 1);
    });

    it('新規登録ボタンが表示される', () => {
      cy.contains('button', '新規登録').should('be.visible');
    });

    it('操作カラムに編集・削除ボタンが表示される', () => {
      cy.contains('th', '操作').should('be.visible');
      cy.get('table tbody tr').first().contains('button', '編集').should('be.visible');
      cy.get('table tbody tr').first().contains('button', '削除').should('be.visible');
    });
  });

  describe('新規登録', () => {
    beforeEach(() => {
      cy.login('admin', 'Password123!');
      cy.get('[data-testid="dashboard"]').should('be.visible');
      cy.visit('/master/account-structures/new');
    });

    it('新規登録フォームが表示される', () => {
      cy.get('#accountCode').should('be.visible');
      cy.get('#parentAccountCode').should('be.visible');
      cy.get('#displayOrder').should('be.visible');
      cy.contains('button', '登録').should('be.visible');
      cy.contains('button', '戻る').should('be.visible');
    });

    it('親科目と子科目を選択して登録できる', () => {
      cy.get('#accountCode').clear().type('2000');
      cy.get('#parentAccountCode').clear().type('1000');
      cy.get('#displayOrder').clear().type('10');
      cy.contains('button', '登録').click();
      cy.url({ timeout: 10000 }).should('include', '/master/account-structures');
      cy.contains('勘定科目構成を登録しました', { timeout: 10000 }).should('be.visible');
    });

    it('表示順を設定して登録できる', () => {
      cy.get('#accountCode').clear().type('3001');
      cy.get('#displayOrder').clear().type('5');
      cy.contains('button', '登録').click();
      cy.url({ timeout: 10000 }).should('include', '/master/account-structures');
    });

    it('勘定科目コードが空の場合バリデーションエラーが表示される', () => {
      cy.get('#accountCode').clear();
      cy.contains('button', '登録').click();
      cy.contains('勘定科目コードを入力してください').should('be.visible');
    });

    it('戻るボタンで一覧ページに戻れる', () => {
      cy.contains('button', '戻る').click();
      cy.url().should('include', '/master/account-structures');
      cy.url().should('not.include', '/new');
    });
  });

  describe('削除', () => {
    beforeEach(() => {
      loginAndVisitMasterList(PAGE_CONFIG);
      cy.get('table tbody tr').should('have.length.at.least', 1);
    });

    it('子構成がない場合、削除できる', () => {
      cy.on('window:confirm', () => true);
      cy.get('table tbody tr').last().contains('button', '削除').click();
      cy.contains('勘定科目構成を削除しました', { timeout: 10000 }).should('be.visible');
    });

    it('削除確認ダイアログでキャンセルした場合、削除されない', () => {
      cy.on('window:confirm', () => false);
      cy.get('table tbody tr').first().contains('button', '削除').click();
      cy.get('table tbody tr').should('have.length.at.least', 1);
    });
  });

  describe('編集（US-MST-006）', () => {
    beforeEach(() => {
      loginAndVisitMasterList(PAGE_CONFIG);
      cy.get('table tbody tr').should('have.length.at.least', 1);
    });

    it('一覧の編集ボタンから編集ページに遷移できる', () => {
      navigateToFirstRowEdit('edit-account-structure-page');
      cy.contains('h1', '勘定科目体系 編集').should('be.visible');
      cy.url().should('include', '/edit');
    });

    it('編集フォームが表示される', () => {
      navigateToFirstRowEdit('edit-account-structure-page');
      cy.get('#accountCode').should('be.visible');
      cy.get('#parentAccountCode').should('be.visible');
      cy.get('#displayOrder').should('be.visible');
      cy.contains('button', '更新').should('be.visible');
      cy.contains('button', '戻る').should('be.visible');
    });

    it('勘定科目コードは変更できない', () => {
      navigateToFirstRowEdit('edit-account-structure-page');
      cy.get('#accountCode').should('be.disabled');
      cy.get('#accountCode').invoke('val').should('not.be.empty');
    });

    it('親科目コードを編集して更新できる', () => {
      navigateToFirstRowEdit('edit-account-structure-page');
      cy.get('#parentAccountCode').clear();
      cy.contains('button', '更新').click();
      cy.url({ timeout: 10000 }).should('include', '/master/account-structures');
      cy.url().should('not.include', '/edit');
      cy.contains('勘定科目構成を更新しました', { timeout: 10000 }).should('be.visible');
    });

    it('表示順を編集して更新できる', () => {
      navigateToFirstRowEdit('edit-account-structure-page');
      cy.get('#displayOrder').clear().type('99');
      cy.contains('button', '更新').click();
      cy.url({ timeout: 10000 }).should('include', '/master/account-structures');
      cy.url().should('not.include', '/edit');
      cy.contains('勘定科目構成を更新しました', { timeout: 10000 }).should('be.visible');
    });

    it('戻るボタンで一覧ページに戻れる', () => {
      navigateToFirstRowEdit('edit-account-structure-page');
      cy.contains('button', '戻る').click();
      cy.url().should('include', '/master/account-structures');
      cy.url().should('not.include', '/edit');
      cy.get('[data-testid="account-structure-list-page"]').should('be.visible');
    });
  });

  describe('アクセス制御', () => {
    describeMasterAccessControl(PAGE_CONFIG);
  });
});
