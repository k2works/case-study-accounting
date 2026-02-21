/**
 * 自動仕訳生成 E2E テスト
 *
 * US-JNL-006: 自動仕訳生成
 *
 * 受入条件:
 * - 自動仕訳設定を選択して仕訳を生成できる
 * - 生成された仕訳は編集可能
 * - 金額は手動で変更できる
 * - 生成成功時、確認メッセージが表示される
 */
describe('US-JNL-006: 自動仕訳生成', () => {
  before(() => {
    cy.clearAuth();
    cy.setupTestAccounts();
    cy.clearAuth();
  });

  beforeEach(() => {
    cy.clearAuth();
  });

  describe('アクセス制御', () => {
    it('一般ユーザーには自動仕訳ボタンが表示されない', () => {
      // Given: 一般ユーザーでログイン
      cy.login('user', 'Password123!');
      cy.get('[data-testid="dashboard"]').should('be.visible');

      // When: 仕訳入力ページにアクセス
      cy.visit('/journal/entries/new');
      cy.get('[data-testid="journal-entry-form"]').should('be.visible');

      // Then: 自動仕訳ボタンが表示されない
      cy.get('[data-testid="auto-journal-button"]').should('not.exist');
    });

    it('管理者には自動仕訳ボタンが表示される', () => {
      // Given: 管理者でログイン
      cy.login('admin', 'Password123!');
      cy.get('[data-testid="dashboard"]').should('be.visible');

      // When: 仕訳入力ページにアクセス
      cy.visit('/journal/entries/new');
      cy.get('[data-testid="journal-entry-form"]').should('be.visible');

      // Then: 自動仕訳ボタンが表示される
      cy.get('[data-testid="auto-journal-button"]').should('be.visible');
    });
  });

  describe('自動仕訳ダイアログ', () => {
    beforeEach(() => {
      // 管理者でログイン
      cy.login('admin', 'Password123!');
      cy.get('[data-testid="dashboard"]').should('be.visible');
      cy.visit('/journal/entries/new');
      cy.get('[data-testid="journal-entry-form"]').should('be.visible');
    });

    it('自動仕訳ボタンをクリックするとダイアログが開く', () => {
      // When: 自動仕訳ボタンをクリック
      cy.get('[data-testid="auto-journal-button"]').click();

      // Then: ダイアログが表示される
      cy.get('.auto-journal-dialog-overlay').should('be.visible');
      cy.contains('自動仕訳生成').should('be.visible');
      cy.get('#auto-journal-pattern').should('be.visible');
    });

    it('キャンセルボタンでダイアログを閉じることができる', () => {
      // Given: ダイアログを開く
      cy.get('[data-testid="auto-journal-button"]').click();
      cy.get('.auto-journal-dialog-overlay').should('be.visible');

      // When: キャンセルボタンをクリック
      cy.contains('button', 'キャンセル').click();

      // Then: ダイアログが閉じる
      cy.get('.auto-journal-dialog-overlay').should('not.exist');
    });

    it('パターンが未選択の場合は生成できない', () => {
      // Given: ダイアログを開く
      cy.get('[data-testid="auto-journal-button"]').click();
      cy.get('.auto-journal-dialog-overlay').should('be.visible');

      // When: パターンを選択せずに生成ボタンをクリック
      cy.contains('button', '生成').click();

      // Then: エラーメッセージが表示される
      cy.contains('自動仕訳パターンを選択してください').should('be.visible');
    });
  });
});
