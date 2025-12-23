// カスタムコマンドの型定義
declare global {
  namespace Cypress {
    interface Chainable {
      /**
       * ログインコマンド
       * @param username - ユーザー名
       * @param password - パスワード
       */
      login(username: string, password: string): Chainable<void>;

      /**
       * ログアウトコマンド
       */
      logout(): Chainable<void>;

      /**
       * ローカルストレージをクリアしてログアウト状態にする
       */
      clearAuth(): Chainable<void>;
    }
  }
}

/**
 * ログインコマンド
 * ログインフォームを使用して認証を行う
 */
Cypress.Commands.add('login', (username: string, password: string) => {
  cy.visit('/login');

  // フォームが表示されるまで待機
  cy.get('[data-testid="login-form"]').should('be.visible');

  // 入力フィールドをクリアしてから入力
  cy.get('[data-testid="username-input"]').clear().type(username);
  cy.get('[data-testid="password-input"]').clear().type(password);

  // ログインボタンをクリック
  cy.get('[data-testid="login-submit"]').click();
});

/**
 * ログアウトコマンド
 * ヘッダーのログアウトボタンをクリックしてログアウトする
 */
Cypress.Commands.add('logout', () => {
  cy.get('[data-testid="logout-button"]').click();
});

/**
 * 認証情報クリアコマンド
 * ローカルストレージから認証情報を削除する
 */
Cypress.Commands.add('clearAuth', () => {
  cy.clearLocalStorage('accessToken');
  cy.clearLocalStorage('refreshToken');
  cy.clearLocalStorage('user');
});

export {};
