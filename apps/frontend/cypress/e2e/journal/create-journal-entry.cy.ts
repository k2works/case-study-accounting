/**
 * 仕訳入力 E2E テスト
 *
 * US-JNL-001: 仕訳入力
 *
 * 受入条件:
 * - 仕訳日付、摘要を入力できる
 * - 借方科目、借方金額、貸方科目、貸方金額を入力できる
 * - 複数の明細行を追加できる
 * - 貸借の合計金額が一致しないと保存できない
 * - 存在しない勘定科目は選択できない
 * - 保存成功時、確認メッセージが表示される
 * - 保存された仕訳のステータスは「下書き」になる
 */
describe('US-JNL-001: 仕訳入力', () => {
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
    it('未認証ユーザーは仕訳入力ページにアクセスできない', () => {
      // Given: 未認証状態

      // When: 仕訳入力ページに直接アクセス
      cy.visit('/journal/entries/new');

      // Then: ログインページにリダイレクトされる
      cy.url().should('include', '/login');
      cy.get('[data-testid="login-page"]').should('be.visible');
    });

    it('一般ユーザーは仕訳入力ページにアクセスできる', () => {
      // Given: 一般ユーザーでログイン
      cy.login('user', 'Password123!');
      // ログイン完了を待機
      cy.get('[data-testid="dashboard"]').should('be.visible');

      // When: 仕訳入力ページにアクセス
      cy.visit('/journal/entries/new');

      // Then: 仕訳入力ページが表示される
      cy.get('[data-testid="journal-entry-form"]').should('be.visible');
    });

    it('管理者は仕訳入力ページにアクセスできる', () => {
      // Given: 管理者でログイン
      cy.login('admin', 'Password123!');
      // ログイン完了を待機
      cy.get('[data-testid="dashboard"]').should('be.visible');

      // When: 仕訳入力ページにアクセス
      cy.visit('/journal/entries/new');

      // Then: 仕訳入力ページが表示される
      cy.get('[data-testid="journal-entry-form"]').should('be.visible');
    });
  });

  describe('仕訳入力フォーム', () => {
    beforeEach(() => {
      // 管理者でログインして仕訳入力ページにアクセス
      cy.login('admin', 'Password123!');
      // ログイン完了を待機
      cy.get('[data-testid="dashboard"]').should('be.visible');

      cy.visit('/journal/entries/new');
      cy.get('[data-testid="journal-entry-form"]').should('be.visible');
      // 勘定科目が読み込まれるのを待つ（MSW 環境でも安定動作）
      cy.get('[data-testid="journal-entry-account-0"] option', { timeout: 15000 })
        .should('have.length.greaterThan', 1);
    });

    it('仕訳日付、摘要を入力できる', () => {
      // Given: 仕訳入力フォームが表示されている

      // When: 仕訳日と摘要を入力
      cy.get('[data-testid="journal-entry-date-input"]').type('2024-01-31');
      cy.get('[data-testid="journal-entry-description-input"]').type('売上計上');

      // Then: 入力値が反映される
      cy.get('[data-testid="journal-entry-date-input"]').should('have.value', '2024-01-31');
      cy.get('[data-testid="journal-entry-description-input"]').should('have.value', '売上計上');
    });

    it('借方科目、借方金額を入力できる', () => {
      // Given: 仕訳入力フォームが表示されている

      // When: 1行目に勘定科目と借方金額を入力（インデックスで選択）
      cy.get('[data-testid="journal-entry-account-0"]').select(1);
      cy.get('[data-testid="journal-entry-debit-0"]').type('1000');

      // Then: 入力値が反映される
      cy.get('[data-testid="journal-entry-account-0"]').should('not.have.value', '');
      cy.get('[data-testid="journal-entry-debit-0"]').should('have.value', '1000');
    });

    it('貸方科目、貸方金額を入力できる', () => {
      // Given: 仕訳入力フォームが表示されている

      // When: 1行目に勘定科目と貸方金額を入力（インデックスで選択）
      cy.get('[data-testid="journal-entry-account-0"]').select(2);
      cy.get('[data-testid="journal-entry-credit-0"]').type('1000');

      // Then: 入力値が反映される
      cy.get('[data-testid="journal-entry-account-0"]').should('not.have.value', '');
      cy.get('[data-testid="journal-entry-credit-0"]').should('have.value', '1000');
    });

    it('複数の明細行を追加できる', () => {
      // Given: 仕訳入力フォームが表示されている（初期状態で1行）

      // When: 行追加ボタンをクリック
      cy.get('[data-testid="journal-entry-add-line"]').click();
      cy.get('[data-testid="journal-entry-add-line"]').click();

      // Then: 3行に増える
      cy.get('[data-testid^="journal-entry-account-"]').should('have.length', 3);
    });

    it('明細行を削除できる', () => {
      // Given: 2行の明細がある
      cy.get('[data-testid="journal-entry-add-line"]').click();
      cy.get('[data-testid^="journal-entry-account-"]').should('have.length', 2);

      // When: 2行目を削除
      cy.get('[data-testid^="journal-entry-remove-"]').last().click();

      // Then: 1行に戻る
      cy.get('[data-testid^="journal-entry-account-"]').should('have.length', 1);
    });
  });

  describe('貸借バランスチェック', () => {
    beforeEach(() => {
      // 管理者でログインして仕訳入力ページにアクセス
      cy.login('admin', 'Password123!');
      // ログイン完了を待機
      cy.get('[data-testid="dashboard"]').should('be.visible');

      cy.visit('/journal/entries/new');
      cy.get('[data-testid="journal-entry-form"]').should('be.visible');
      // 勘定科目が読み込まれるのを待つ（MSW 環境でも安定動作）
      cy.get('[data-testid="journal-entry-account-0"] option', { timeout: 15000 })
        .should('have.length.greaterThan', 1);
    });

    it('貸借が一致している場合、保存ボタンが有効になる', () => {
      // Given: 仕訳入力フォームが表示されている

      // When: 貸借が一致する仕訳を入力
      cy.get('[data-testid="journal-entry-date-input"]').type('2024-01-31');
      cy.get('[data-testid="journal-entry-description-input"]').type('売上計上');

      // 借方: 現金 1000円（インデックスで選択）
      cy.get('[data-testid="journal-entry-account-0"]').select(1);
      cy.get('[data-testid="journal-entry-debit-0"]').type('1000');

      // 行追加して貸方: 売上 1000円
      cy.get('[data-testid="journal-entry-add-line"]').click();
      cy.get('[data-testid="journal-entry-account-1"]').select(2);
      cy.get('[data-testid="journal-entry-credit-1"]').type('1000');

      // Then: 差額が¥0になり、保存ボタンが有効
      cy.get('[data-testid="journal-entry-diff"]').should('contain', '¥0');
      cy.get('[data-testid="journal-entry-submit"]').should('not.be.disabled');
    });

    it('貸借が一致していない場合、保存ボタンが無効になる', () => {
      // Given: 仕訳入力フォームが表示されている

      // When: 貸借が一致しない仕訳を入力
      cy.get('[data-testid="journal-entry-date-input"]').type('2024-01-31');
      cy.get('[data-testid="journal-entry-description-input"]').type('売上計上');

      // 借方: 現金 1000円のみ（貸方なし）
      cy.get('[data-testid="journal-entry-account-0"]').select(1);
      cy.get('[data-testid="journal-entry-debit-0"]').type('1000');

      // Then: 差額が+¥1,000と表示され、保存ボタンが無効
      cy.get('[data-testid="journal-entry-diff"]').should('contain', '+¥1,000');
      cy.get('[data-testid="journal-entry-submit"]').should('be.disabled');
    });
  });

  describe('仕訳保存', () => {
    beforeEach(() => {
      // 管理者でログインして仕訳入力ページにアクセス
      cy.login('admin', 'Password123!');
      // ログイン完了を待機
      cy.get('[data-testid="dashboard"]').should('be.visible');

      cy.visit('/journal/entries/new');
      cy.get('[data-testid="journal-entry-form"]').should('be.visible');
      // 勘定科目が読み込まれるのを待つ（MSW 環境でも安定動作）
      cy.get('[data-testid="journal-entry-account-0"] option', { timeout: 15000 })
        .should('have.length.greaterThan', 1);
    });

    it('有効な仕訳を保存すると成功メッセージが表示される', () => {
      // Given: 仕訳入力フォームが表示されている

      // When: 有効な仕訳を入力して保存
      cy.get('[data-testid="journal-entry-date-input"]').type('2024-01-31');
      cy.get('[data-testid="journal-entry-description-input"]').type('売上計上');

      // 借方: 現金 1000円（インデックスで選択）
      cy.get('[data-testid="journal-entry-account-0"]').select(1);
      cy.get('[data-testid="journal-entry-debit-0"]').type('1000');

      // 行追加して貸方: 売上 1000円
      cy.get('[data-testid="journal-entry-add-line"]').click();
      cy.get('[data-testid="journal-entry-account-1"]').select(2);
      cy.get('[data-testid="journal-entry-credit-1"]').type('1000');

      cy.get('[data-testid="journal-entry-submit"]').click();

      // Then: 成功メッセージが表示される
      cy.get('[data-testid="journal-entry-success"]').should('be.visible');
      cy.get('[data-testid="journal-entry-success"]').should('contain', '仕訳登録が完了しました');
    });

    it('キャンセルボタンで前のページに戻る', () => {
      // Given: 仕訳入力フォームが表示されている

      // When: キャンセルボタンをクリック
      cy.get('[data-testid="journal-entry-cancel"]').click();

      // Then: 前のページ（ダッシュボード）に戻る
      cy.url().should('not.include', '/journal/entries/new');
    });
  });
});
