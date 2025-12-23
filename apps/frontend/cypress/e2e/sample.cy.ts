/**
 * アプリケーション基本 E2E テスト
 */
describe('アプリケーション', () => {
  beforeEach(() => {
    cy.clearAuth();
  });

  it('ログインページが表示される', () => {
    cy.visit('/login');
    cy.contains('財務会計システム').should('be.visible');
    cy.get('[data-testid="login-form"]').should('be.visible');
  });
});
