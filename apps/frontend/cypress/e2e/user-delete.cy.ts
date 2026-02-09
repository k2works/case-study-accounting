/**
 * ユーザー削除 E2E テスト
 *
 * US-AUTH-005: ユーザー削除
 *
 * 受入条件:
 * - 削除確認ダイアログが表示される
 * - 削除後、ユーザー一覧から消える
 * - 削除されたユーザーはログインできない
 * - 監査ログは保持される（バックエンド論理削除で保証）
 */
describe('US-AUTH-005: ユーザー削除', () => {
  // テスト用ユーザーID（handlers.ts の mockUserRecords に対応）
  const testUsers = {
    deleteTarget: '101', // user_edit_nav - 削除テスト対象
    deleteTarget2: '102', // user_edit_readonly - 2番目の削除対象
  };

  const loginAsAdminAndVisitUserList = () => {
    cy.login('admin', 'Password123!');
    cy.get('[data-testid="dashboard"]').should('be.visible');
    cy.visit('/users');
    cy.get('[data-testid="user-list-page"]').should('be.visible');
    cy.get('[data-testid="user-list"]').should('be.visible');
  };

  beforeEach(() => {
    // 各テスト前に認証情報をクリア
    cy.clearAuth();
  });

  describe('削除確認ダイアログ', () => {
    it('削除ボタンをクリックすると確認ダイアログが表示される', () => {
      // Given: ユーザー一覧ページが表示されている
      loginAsAdminAndVisitUserList();

      // When: 削除ボタンをクリック
      cy.get(`[data-testid="user-delete-${testUsers.deleteTarget}"]`).should('be.visible').click();

      // Then: 確認ダイアログが表示される
      cy.get('[data-testid="confirm-modal-confirm"]').should('be.visible');
      cy.get('[data-testid="confirm-modal-cancel"]').should('be.visible');
    });

    it('確認ダイアログでキャンセルをクリックするとダイアログが閉じる', () => {
      // Given: 削除確認ダイアログが表示されている
      loginAsAdminAndVisitUserList();
      cy.get(`[data-testid="user-delete-${testUsers.deleteTarget}"]`).click();
      cy.get('[data-testid="confirm-modal-confirm"]').should('be.visible');

      // When: キャンセルボタンをクリック
      cy.get('[data-testid="confirm-modal-cancel"]').click();

      // Then: ダイアログが閉じる（確認ボタンが見えなくなる）
      cy.get('[data-testid="confirm-modal-confirm"]').should('not.exist');
      
      // ユーザーはまだ一覧に表示されている
      cy.get(`[data-testid="user-delete-${testUsers.deleteTarget}"]`).should('be.visible');
    });
  });

  describe('削除実行', () => {
    it('削除を確認するとユーザーが一覧から消える', () => {
      // Given: ユーザー一覧ページが表示されている
      loginAsAdminAndVisitUserList();

      // 削除対象のユーザーが存在することを確認
      cy.get(`[data-testid="user-delete-${testUsers.deleteTarget2}"]`).should('be.visible');

      // When: 削除ボタンをクリックして確認
      cy.get(`[data-testid="user-delete-${testUsers.deleteTarget2}"]`).click();
      cy.get('[data-testid="confirm-modal-confirm"]').should('be.visible').click();

      // Then: 成功メッセージが表示される
      cy.get('[data-testid="success-notification"]').should('be.visible');
      cy.get('[data-testid="success-notification"]').should('contain', 'ユーザーを削除しました');

      // ユーザーが一覧から消える
      cy.get(`[data-testid="user-delete-${testUsers.deleteTarget2}"]`).should('not.exist');
    });

    it('削除成功後、成功メッセージを閉じることができる', () => {
      // Given: ユーザー削除後、成功メッセージが表示されている
      loginAsAdminAndVisitUserList();
      cy.get(`[data-testid="user-delete-${testUsers.deleteTarget}"]`).click();
      cy.get('[data-testid="confirm-modal-confirm"]').click();
      cy.get('[data-testid="success-notification"]').should('be.visible');

      // When: 成功メッセージを閉じる
      cy.get('[data-testid="success-notification"]').find('button').click();

      // Then: 成功メッセージが消える
      cy.get('[data-testid="success-notification"]').should('not.exist');
    });
  });

  describe('権限チェック', () => {
    it('管理者以外はユーザー一覧ページにアクセスできない', () => {
      // Given: 一般ユーザーでログイン
      cy.login('user', 'Password123!');
      cy.get('[data-testid="dashboard"]').should('be.visible');

      // When: ユーザー一覧ページに直接アクセス
      cy.visit('/users');

      // Then: ホームにリダイレクトされる（または権限エラー）
      cy.url().should('not.include', '/users');
    });
  });
});
