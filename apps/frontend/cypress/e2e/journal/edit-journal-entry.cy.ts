/**
 * 仕訳編集 E2E テスト
 *
 * US-JNL-002: 仕訳編集
 *
 * 受入条件:
 * - 経理担当者以上は仕訳編集画面にアクセスできる
 * - 仕訳日付、摘要を編集できる
 * - 借方科目、借方金額、貸方科目、貸方金額を編集できる
 * - 貸借の合計金額が一致しないと保存できない
 * - 楽観的ロックエラー時は適切なエラーが表示される
 * - 保存成功時、確認メッセージが表示される
 * - 下書き状態の仕訳のみ編集可能
 */

/**
 * 仕訳を登録するヘルパー関数
 * 勘定科目リストから動的に選択する
 */
const createTestJournalEntry = (date: string, description: string, amount: string) => {
  cy.visit('/journal/entries/new');
  cy.get('[data-testid="journal-entry-form"]').should('be.visible');

  // 勘定科目が読み込まれるのを待つ（MSW 環境でも安定動作）
  cy.get('[data-testid="journal-entry-account-0"] option', { timeout: 15000 })
    .should('have.length.greaterThan', 1);

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

describe('US-JNL-002: 仕訳編集', () => {
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

  describe('アクセス制御', () => {
    it('未認証ユーザーは仕訳編集ページにアクセスできない', () => {
      // Given: 未認証状態

      // When: 仕訳編集ページに直接アクセス
      cy.visit('/journal/entries/1/edit');

      // Then: ログインページにリダイレクトされる
      cy.url().should('include', '/login');
      cy.get('[data-testid="login-page"]').should('be.visible');
    });

    it('一般ユーザーは仕訳編集ページにアクセスできる', () => {
      // Given: 一般ユーザーでログインし、仕訳を作成
      cy.login('user', 'Password123!');
      cy.get('[data-testid="dashboard"]').should('be.visible');

      createTestJournalEntry('2024-01-15', 'ユーザーテスト仕訳', '5000');
      cy.get('[data-testid="journal-entry-success"]').should('be.visible');

      // When: 仕訳一覧から摘要で検索して編集画面に遷移
      navigateToEditViaList('ユーザーテスト仕訳');

      // Then: 編集ページが表示される
      cy.get('[data-testid="edit-journal-entry-page"]').should('be.visible');
    });

    it('管理者は仕訳入力ページにアクセスできる', () => {
      // Given: 管理者でログインし、仕訳を作成
      cy.login('admin', 'Password123!');
      cy.get('[data-testid="dashboard"]').should('be.visible');

      // When: 仕訳入力ページにアクセス
      cy.visit('/journal/entries/new');

      // Then: 仕訳入力ページが表示される
      cy.get('[data-testid="journal-entry-form"]').should('be.visible');
    });
  });

  describe('仕訳編集フォーム', () => {
    beforeEach(() => {
      // 管理者でログインして仕訳を登録
      cy.login('admin', 'Password123!');
      cy.get('[data-testid="dashboard"]').should('be.visible');

      createTestJournalEntry('2024-02-01', '編集テスト用仕訳', '10000');
      cy.get('[data-testid="journal-entry-success"]').should('be.visible');

      // 仕訳一覧から摘要で検索して編集画面に遷移
      navigateToEditViaList('編集テスト用仕訳');
    });

    it('既存の仕訳情報が表示される', () => {
      // Then: 仕訳編集フォームが表示される
      cy.get('[data-testid="edit-journal-entry-page"]').should('be.visible');
      cy.get('[data-testid="journal-entry-edit-form"]').should('be.visible');

      // 既存の値が表示される（日付と摘要が入力済み）
      cy.get('[data-testid="journal-entry-date-input"]').should('not.have.value', '');
      cy.get('[data-testid="journal-entry-description-input"]').should('not.have.value', '');
    });

    it('仕訳日と摘要を編集できる', () => {
      // When: 仕訳日と摘要を編集
      cy.get('[data-testid="journal-entry-date-input"]').clear().type('2024-02-15');
      cy.get('[data-testid="journal-entry-description-input"]').clear().type('編集後の摘要');

      // Then: 入力値が反映される
      cy.get('[data-testid="journal-entry-date-input"]').should('have.value', '2024-02-15');
      cy.get('[data-testid="journal-entry-description-input"]').should('have.value', '編集後の摘要');
    });

    it('金額を編集できる', () => {
      // When: 金額を変更（両方の行を同額に更新）
      cy.get('[data-testid^="journal-entry-debit-"]').first().clear().type('20000');
      cy.get('[data-testid^="journal-entry-credit-"]').last().clear().type('20000');

      // Then: 差額が0になる
      cy.get('[data-testid="journal-entry-diff"]').should('contain', '¥0');
    });

    it('行を追加できる', () => {
      // 初期状態の行数を確認
      cy.get('[data-testid^="journal-entry-account-"]').should('have.length', 2);

      // When: 行追加ボタンをクリック
      cy.get('[data-testid="journal-entry-add-line"]').click();

      // Then: 3行に増える
      cy.get('[data-testid^="journal-entry-account-"]').should('have.length', 3);
    });
  });

  describe('貸借バランスチェック', () => {
    beforeEach(() => {
      // 管理者でログインして仕訳を登録
      cy.login('admin', 'Password123!');
      cy.get('[data-testid="dashboard"]').should('be.visible');

      createTestJournalEntry('2024-03-01', 'バランステスト仕訳', '5000');
      cy.get('[data-testid="journal-entry-success"]').should('be.visible');

      // 仕訳一覧から摘要で検索して編集画面に遷移
      navigateToEditViaList('バランステスト仕訳');
    });

    it('貸借が一致している場合、保存ボタンが有効になる', () => {
      // Then: 初期状態で差額が0、保存ボタンが有効
      cy.get('[data-testid="journal-entry-diff"]').should('contain', '¥0');
      cy.get('[data-testid="journal-entry-submit"]').should('not.be.disabled');
    });

    it('貸借が一致していない場合、保存ボタンが無効になる', () => {
      // When: 借方金額のみ変更して不一致状態に
      cy.get('[data-testid^="journal-entry-debit-"]').first().clear().type('10000');

      // Then: 差額が表示され、保存ボタンが無効
      cy.get('[data-testid="journal-entry-diff"]').should('contain', '+¥5,000');
      cy.get('[data-testid="journal-entry-submit"]').should('be.disabled');
    });
  });

  describe('仕訳編集保存', () => {
    beforeEach(() => {
      // 管理者でログインして仕訳を登録
      cy.login('admin', 'Password123!');
      cy.get('[data-testid="dashboard"]').should('be.visible');

      createTestJournalEntry('2024-04-01', '保存テスト仕訳', '8000');
      cy.get('[data-testid="journal-entry-success"]').should('be.visible');

      // 仕訳一覧から摘要で検索して編集画面に遷移
      navigateToEditViaList('保存テスト仕訳');
    });

    it('編集した仕訳を保存できる', () => {
      // When: 摘要を編集して保存
      cy.get('[data-testid="journal-entry-description-input"]').clear().type('編集済み仕訳');
      cy.get('[data-testid="journal-entry-submit"]').click();

      // Then: 保存成功後、編集ページから離れる
      cy.url({ timeout: 15000 }).should('not.include', '/edit');
    });

    it('キャンセルボタンで前のページに戻る', () => {
      // When: キャンセルボタンをクリック
      cy.get('[data-testid="journal-entry-cancel"]').click();

      // Then: 前のページに戻る
      cy.url().should('not.include', '/edit');
    });
  });

  describe('エラーハンドリング', () => {
    it('存在しない仕訳編集ページにアクセスするとエラーが表示される', () => {
      // Given: 管理者でログイン
      cy.login('admin', 'Password123!');
      cy.get('[data-testid="dashboard"]').should('be.visible');

      // When: 存在しない仕訳の編集ページにアクセス
      cy.visit('/journal/entries/99999/edit');

      // Then: エラーメッセージが表示される
      cy.get('[data-testid="edit-journal-entry-page"]').should('be.visible');
      cy.contains('仕訳が見つかりません').should('be.visible');
    });

    it('不正なIDの仕訳編集ページにアクセスするとエラーが表示される', () => {
      // Given: 管理者でログイン
      cy.login('admin', 'Password123!');
      cy.get('[data-testid="dashboard"]').should('be.visible');

      // When: 不正なIDの仕訳編集ページにアクセス
      cy.visit('/journal/entries/invalid/edit');

      // Then: エラーメッセージが表示される
      cy.get('[data-testid="edit-journal-entry-page"]').should('be.visible');
      cy.contains('仕訳が見つかりません').should('be.visible');
    });
  });
});
