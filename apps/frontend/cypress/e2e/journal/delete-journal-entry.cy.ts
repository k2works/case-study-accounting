/**
 * 仕訳削除 E2E テスト
 *
 * US-JNL-003: 仕訳削除
 *
 * 受入条件:
 * - 「下書き」ステータスの仕訳のみ削除できる
 * - 削除前に確認ダイアログが表示される
 * - 削除成功時、確認メッセージが表示される
 * - 削除後、一覧画面に戻る
 */

/**
 * 仕訳を登録するヘルパー関数
 * 勘定科目リストから動的に選択する
 */
const createTestJournalEntry = (date: string, description: string, amount: string) => {
  cy.visit('/journal/entries/new');
  cy.get('[data-testid="journal-entry-form"]').should('be.visible');

  // 勘定科目が読み込まれるのを待つ（MSW 環境でも安定動作）
  cy.get('[data-testid="journal-entry-account-0"] option', { timeout: 15000 }).should(
    'have.length.greaterThan',
    1
  );

  cy.get('[data-testid="journal-entry-date-input"]').type(date);
  cy.get('[data-testid="journal-entry-description-input"]').type(description);

  // 最初の有効な勘定科目を選択（index 1 = 最初のoption以外）
  cy.get('[data-testid="journal-entry-account-0"]').select(1);
  cy.get('[data-testid="journal-entry-debit-0"]').type(amount);

  // 行を追加して2番目の勘定科目を選択
  cy.get('[data-testid="journal-entry-add-line"]').click();
  cy.get('[data-testid="journal-entry-account-1"]').select(2);
  cy.get('[data-testid="journal-entry-credit-1"]').type(amount);

  cy.get('[data-testid="journal-entry-submit"]').click();
};

/**
 * 仕訳一覧ページから仕訳の編集画面に遷移するヘルパー
 * MSW 環境でも cy.intercept を使わずに動作する
 * @param filterDescription 摘要でフィルタして特定の仕訳を見つける（省略時は先頭行）
 */
const navigateToEditViaList = (filterDescription?: string) => {
  cy.visit('/journal/entries');
  cy.get('[data-testid="journal-entry-list-page"]', { timeout: 15000 }).should('be.visible');
  cy.get('table tbody tr', { timeout: 15000 }).should('have.length.at.least', 1);

  if (filterDescription) {
    cy.get('#journal-entry-filter-description').type(filterDescription);
    cy.contains('button', '検索').click();
    cy.get('table tbody tr', { timeout: 10000 }).should('have.length.at.least', 1);
  }

  cy.get('table tbody tr').first().contains('button', '編集').click();
  cy.get('[data-testid="journal-entry-edit-form"]', { timeout: 15000 }).should('be.visible');
};

describe('US-JNL-003: 仕訳削除', () => {
  // テストスイート開始前に勘定科目をセットアップ
  before(() => {
    cy.clearAuth();
    cy.setupTestAccounts();
    cy.clearAuth();
  });

  beforeEach(() => {
    // 各テスト前に認証情報をクリア
    cy.clearAuth();
  });

  describe('削除ボタン表示', () => {
    beforeEach(() => {
      // 管理者でログインして仕訳を登録
      cy.login('admin', 'Password123!');
      cy.get('[data-testid="dashboard"]').should('be.visible');

      createTestJournalEntry('2024-05-01', '削除テスト用仕訳', '3000');
      cy.get('[data-testid="journal-entry-success"]').should('be.visible');

      // 仕訳一覧から摘要で検索して編集画面に遷移
      navigateToEditViaList('削除テスト用仕訳');
    });

    it('仕訳編集画面に削除ボタンが表示される', () => {
      // Then: 編集ページに削除ボタンが表示される
      cy.get('[data-testid="edit-journal-entry-page"]').should('be.visible');
      cy.get('[data-testid="journal-entry-delete"]').should('be.visible');
    });
  });

  describe('削除確認ダイアログ', () => {
    beforeEach(() => {
      // 管理者でログインして仕訳を登録
      cy.login('admin', 'Password123!');
      cy.get('[data-testid="dashboard"]').should('be.visible');

      createTestJournalEntry('2024-05-02', 'ダイアログテスト仕訳', '4000');
      cy.get('[data-testid="journal-entry-success"]').should('be.visible');

      // 仕訳一覧から摘要で検索して編集画面に遷移
      navigateToEditViaList('ダイアログテスト仕訳');
    });

    it('削除ボタンをクリックすると確認ダイアログが表示される', () => {
      // When: 削除ボタンをクリック
      cy.get('[data-testid="journal-entry-delete"]').click();

      // Then: 確認ダイアログが表示される
      cy.get('.modal').should('be.visible');
      cy.contains('仕訳の削除').should('be.visible');
      cy.contains('この仕訳を削除してもよろしいですか').should('be.visible');
    });

    it('確認ダイアログでキャンセルすると削除されない', () => {
      // When: 削除ボタンをクリックして確認ダイアログを表示
      cy.get('[data-testid="journal-entry-delete"]').click();
      cy.get('.modal').should('be.visible');

      // キャンセルボタンをクリック
      cy.get('[data-testid="confirm-modal-cancel"]').click();

      // Then: ダイアログが閉じ、編集ページに留まる
      cy.get('.modal').should('not.exist');
      cy.get('[data-testid="journal-entry-edit-form"]').should('be.visible');
    });
  });

  describe('仕訳削除実行', () => {
    beforeEach(() => {
      // 管理者でログインして仕訳を登録
      cy.login('admin', 'Password123!');
      cy.get('[data-testid="dashboard"]').should('be.visible');

      createTestJournalEntry('2024-05-03', '削除実行テスト仕訳', '5000');
      cy.get('[data-testid="journal-entry-success"]').should('be.visible');

      // 仕訳一覧から摘要で検索して編集画面に遷移
      navigateToEditViaList('削除実行テスト仕訳');
    });

    it('確認ダイアログで削除を確定すると仕訳が削除される', () => {
      // When: 削除ボタンをクリックして確認ダイアログを表示
      cy.get('[data-testid="journal-entry-delete"]').click();
      cy.get('.modal').should('be.visible');

      // 削除ボタンをクリック
      cy.get('[data-testid="confirm-modal-confirm"]').click();

      // Then: 削除成功後、ダッシュボードにリダイレクトされる
      cy.url({ timeout: 15000 }).should('eq', Cypress.config().baseUrl + '/');
      cy.get('[data-testid="dashboard"]').should('be.visible');
    });
  });

  describe('アクセス制御', () => {
    it('一般ユーザーも仕訳を削除できる', () => {
      // Given: 一般ユーザーでログインして仕訳を作成
      cy.login('user', 'Password123!');
      cy.get('[data-testid="dashboard"]').should('be.visible');

      createTestJournalEntry('2024-05-04', 'ユーザー削除テスト', '2000');
      cy.get('[data-testid="journal-entry-success"]').should('be.visible');

      // When: 仕訳一覧から摘要で検索して編集ページに遷移して削除
      navigateToEditViaList('ユーザー削除テスト');

      cy.get('[data-testid="journal-entry-delete"]').click();
      cy.get('.modal').should('be.visible');
      cy.get('[data-testid="confirm-modal-confirm"]').click();

      // Then: 削除が成功する
      cy.url({ timeout: 15000 }).should('eq', Cypress.config().baseUrl + '/');
    });
  });

  describe('エラーハンドリング', () => {
    it('存在しない仕訳を削除しようとするとエラーが表示される', () => {
      // Given: 管理者でログイン
      cy.login('admin', 'Password123!');
      cy.get('[data-testid="dashboard"]').should('be.visible');

      // When: 存在しない仕訳の編集ページにアクセス
      cy.visit('/journal/entries/99999/edit');

      // Then: エラーメッセージが表示される
      cy.get('[data-testid="edit-journal-entry-page"]').should('be.visible');
      cy.contains('仕訳が見つかりません').should('be.visible');
    });
  });
});
