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
  // 勘定科目APIのインターセプト設定
  cy.intercept('GET', '/api/accounts').as('getAccounts');

  cy.visit('/journal/entries/new');
  cy.get('[data-testid="journal-entry-form"]').should('be.visible');

  // 勘定科目APIのレスポンスを待つ
  cy.wait('@getAccounts', { timeout: 15000 });

  // 勘定科目が読み込まれるのを待つ（デフォルトオプション + 勘定科目）
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
    let createdJournalEntryId: number;

    beforeEach(() => {
      // 管理者でログインして仕訳を登録
      cy.login('admin', 'Password123!');
      cy.get('[data-testid="dashboard"]').should('be.visible');

      // 仕訳を登録（APIインターセプトを使用してIDを取得）
      cy.intercept('POST', '/api/journal-entries').as('createJournalEntry');
      createTestJournalEntry('2024-05-01', '削除テスト用仕訳', '3000');

      // APIレスポンスを待機してIDを取得
      cy.wait('@createJournalEntry').then((interception) => {
        if (interception.response?.body?.journalEntryId) {
          createdJournalEntryId = interception.response.body.journalEntryId;
        }
      });

      cy.get('[data-testid="journal-entry-success"]').should('be.visible');
    });

    it('仕訳編集画面に削除ボタンが表示される', () => {
      // When: 作成した仕訳の編集ページにアクセス
      cy.then(() => {
        cy.visit(`/journal/entries/${createdJournalEntryId}/edit`);
      });

      // Then: 編集ページに削除ボタンが表示される
      cy.get('[data-testid="edit-journal-entry-page"]').should('be.visible');
      cy.get('[data-testid="journal-entry-delete"]').should('be.visible');
    });
  });

  describe('削除確認ダイアログ', () => {
    let createdJournalEntryId: number;

    beforeEach(() => {
      // 管理者でログインして仕訳を登録
      cy.login('admin', 'Password123!');
      cy.get('[data-testid="dashboard"]').should('be.visible');

      cy.intercept('POST', '/api/journal-entries').as('createJournalEntry');
      createTestJournalEntry('2024-05-02', 'ダイアログテスト仕訳', '4000');

      cy.wait('@createJournalEntry').then((interception) => {
        if (interception.response?.body?.journalEntryId) {
          createdJournalEntryId = interception.response.body.journalEntryId;
        }
      });

      cy.get('[data-testid="journal-entry-success"]').should('be.visible');
    });

    it('削除ボタンをクリックすると確認ダイアログが表示される', () => {
      // Given: 仕訳編集ページを開く
      cy.then(() => {
        cy.visit(`/journal/entries/${createdJournalEntryId}/edit`);
      });
      cy.get('[data-testid="journal-entry-edit-form"]').should('be.visible');

      // When: 削除ボタンをクリック
      cy.get('[data-testid="journal-entry-delete"]').click();

      // Then: 確認ダイアログが表示される
      cy.get('.modal').should('be.visible');
      cy.contains('仕訳の削除').should('be.visible');
      cy.contains('この仕訳を削除してもよろしいですか').should('be.visible');
    });

    it('確認ダイアログでキャンセルすると削除されない', () => {
      // Given: 仕訳編集ページを開く
      cy.then(() => {
        cy.visit(`/journal/entries/${createdJournalEntryId}/edit`);
      });
      cy.get('[data-testid="journal-entry-edit-form"]').should('be.visible');

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
    let createdJournalEntryId: number;

    beforeEach(() => {
      // 管理者でログインして仕訳を登録
      cy.login('admin', 'Password123!');
      cy.get('[data-testid="dashboard"]').should('be.visible');

      cy.intercept('POST', '/api/journal-entries').as('createJournalEntry');
      createTestJournalEntry('2024-05-03', '削除実行テスト仕訳', '5000');

      cy.wait('@createJournalEntry').then((interception) => {
        if (interception.response?.body?.journalEntryId) {
          createdJournalEntryId = interception.response.body.journalEntryId;
        }
      });

      cy.get('[data-testid="journal-entry-success"]').should('be.visible');
    });

    it('確認ダイアログで削除を確定すると仕訳が削除される', () => {
      // Given: 仕訳編集ページを開く
      cy.then(() => {
        cy.visit(`/journal/entries/${createdJournalEntryId}/edit`);
      });
      cy.get('[data-testid="journal-entry-edit-form"]').should('be.visible');

      // When: 削除ボタンをクリックして確認ダイアログを表示
      cy.intercept('DELETE', `/api/journal-entries/${createdJournalEntryId}`).as(
        'deleteJournalEntry'
      );

      cy.get('[data-testid="journal-entry-delete"]').click();
      cy.get('.modal').should('be.visible');

      // 削除ボタンをクリック
      cy.get('[data-testid="confirm-modal-confirm"]').click();

      // Then: 削除APIが呼ばれ、ダッシュボードにリダイレクトされる
      cy.wait('@deleteJournalEntry').its('response.statusCode').should('eq', 200);
      cy.url().should('eq', Cypress.config().baseUrl + '/');
      cy.get('[data-testid="dashboard"]').should('be.visible');
    });
  });

  describe('アクセス制御', () => {
    it('一般ユーザーも仕訳を削除できる', () => {
      // Given: 一般ユーザーでログインして仕訳を作成
      cy.login('user', 'Password123!');
      cy.get('[data-testid="dashboard"]').should('be.visible');

      cy.intercept('POST', '/api/journal-entries').as('createJournalEntry');
      createTestJournalEntry('2024-05-04', 'ユーザー削除テスト', '2000');

      let journalEntryId: number;
      cy.wait('@createJournalEntry').then((interception) => {
        if (interception.response?.body?.journalEntryId) {
          journalEntryId = interception.response.body.journalEntryId;
        }
      });

      cy.get('[data-testid="journal-entry-success"]').should('be.visible');

      // When: 編集ページにアクセスして削除
      cy.then(() => {
        cy.visit(`/journal/entries/${journalEntryId}/edit`);
      });
      cy.get('[data-testid="journal-entry-edit-form"]').should('be.visible');

      cy.intercept('DELETE', `/api/journal-entries/*`).as('deleteJournalEntry');

      cy.get('[data-testid="journal-entry-delete"]').click();
      cy.get('.modal').should('be.visible');
      cy.get('[data-testid="confirm-modal-confirm"]').click();

      // Then: 削除が成功する
      cy.wait('@deleteJournalEntry').its('response.statusCode').should('eq', 200);
      cy.url().should('eq', Cypress.config().baseUrl + '/');
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
