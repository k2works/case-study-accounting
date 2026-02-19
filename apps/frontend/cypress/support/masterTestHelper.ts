/**
 * マスタ管理 E2E テスト共通ヘルパー
 *
 * 勘定科目構成・自動仕訳パターンなど、マスタ CRUD 画面の共通テストパターンを提供する。
 */

interface MasterPageConfig {
  /** ページ URL パス (例: '/master/auto-journal-patterns') */
  path: string;
  /** 一覧ページの data-testid (例: 'auto-journal-pattern-list-page') */
  listTestId: string;
  /** ページ表示名 (アクセス制御テストのコメント用) */
  displayName: string;
}

/**
 * 管理者としてログインし、指定のマスタ一覧ページに遷移する。
 */
export const loginAndVisitMasterList = (config: MasterPageConfig): void => {
  loginAsAdmin();
  cy.visit(config.path);
  cy.get(`[data-testid="${config.listTestId}"]`, { timeout: 15000 }).should('be.visible');
};

/**
 * 管理者としてログインし、ダッシュボード表示を確認する。
 */
export const loginAsAdmin = (): void => {
  cy.login('admin', 'Password123!');
  cy.get('[data-testid="dashboard"]').should('be.visible');
};

/**
 * 管理者としてログインし、指定パスに遷移する。
 */
export const loginAndVisitPage = (path: string, pageTestId: string): void => {
  loginAsAdmin();
  cy.visit(path);
  cy.get(`[data-testid="${pageTestId}"]`, { timeout: 15000 }).should('be.visible');
};

/**
 * 一覧の先頭行「編集」ボタンをクリックし、編集ページの表示を待つ。
 */
export const navigateToFirstRowEdit = (editPageTestId: string): void => {
  cy.get('table tbody tr').first().contains('button', '編集').click();
  cy.get(`[data-testid="${editPageTestId}"]`, { timeout: 10000 }).should('be.visible');
};

/**
 * マスタ管理画面のアクセス制御テストスイートを生成する。
 *
 * - 一般ユーザーはアクセスできない
 * - 未認証ユーザーはアクセスできない
 * - 経理責任者はアクセスできる
 */
export const describeMasterAccessControl = (config: MasterPageConfig): void => {
  it(`一般ユーザーは${config.displayName}ページにアクセスできない`, () => {
    cy.login('user', 'Password123!');
    cy.get('[data-testid="dashboard"]').should('be.visible');
    cy.visit(config.path);
    cy.url().should('not.include', config.path);
    cy.get('[data-testid="dashboard"]').should('be.visible');
  });

  it(`未認証ユーザーは${config.displayName}ページにアクセスできない`, () => {
    cy.visit(config.path);
    cy.url().should('include', '/login');
  });

  it(`経理責任者は${config.displayName}ページにアクセスできる`, () => {
    cy.login('manager', 'Password123!');
    cy.get('[data-testid="dashboard"]').should('be.visible');
    cy.visit(config.path);
    cy.get(`[data-testid="${config.listTestId}"]`, { timeout: 15000 }).should('be.visible');
  });
};

/**
 * マスタ管理画面の削除テスト共通パターンを生成する。
 *
 * - 確認ダイアログで OK → 削除成功メッセージ表示
 * - 確認ダイアログでキャンセル → 行数変化なし
 */
export const describeMasterDeleteTests = (
  config: MasterPageConfig,
  successMessage: string
): void => {
  it('削除確認ダイアログで OK した場合、削除される', () => {
    cy.on('window:confirm', () => true);
    cy.get('table tbody tr').first().contains('button', '削除').click();
    cy.contains(successMessage, { timeout: 10000 }).should('be.visible');
  });

  it('削除確認ダイアログでキャンセルした場合、削除されない', () => {
    cy.get('table tbody tr').then(($rows) => {
      const rowCount = $rows.length;
      cy.on('window:confirm', () => false);
      cy.get('table tbody tr').first().contains('button', '削除').click();
      cy.get('table tbody tr').should('have.length', rowCount);
    });
  });
};
