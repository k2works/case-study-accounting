/**
 * 勘定科目編集 E2E テスト
 *
 * US-MST-002: 勘定科目編集
 *
 * 受入条件:
 * - 科目名、勘定科目種別を編集できる
 * - 科目コードは変更できない
 * - 仕訳で使用されている科目の種別は変更できない（スタブ実装）
 * - 編集成功時、確認メッセージが表示される
 */
describe('US-MST-002: 勘定科目編集', () => {
  beforeEach(() => {
    // 各テスト前に認証情報をクリア
    cy.clearAuth();
  });

  describe('アクセス制御', () => {
    it('一般ユーザーは勘定科目編集ページにアクセスできない', () => {
      // Given: 一般ユーザーでログイン
      cy.login('user', 'Password123!');
      // ログイン完了を待機
      cy.get('[data-testid="dashboard"]').should('be.visible');

      // When: 勘定科目編集ページに直接アクセス
      cy.visit('/master/accounts/1/edit');

      // Then: ダッシュボードにリダイレクトされる
      cy.url().should('not.include', '/master/accounts/1/edit');
      cy.get('[data-testid="dashboard"]').should('be.visible');
    });

    it('未認証ユーザーは勘定科目編集ページにアクセスできない', () => {
      // Given: 未認証状態

      // When: 勘定科目編集ページに直接アクセス
      cy.visit('/master/accounts/1/edit');

      // Then: ログインページにリダイレクトされる
      cy.url().should('include', '/login');
      cy.get('[data-testid="login-page"]').should('be.visible');
    });

    it('管理者は勘定科目編集ページにアクセスできる', () => {
      // Given: 管理者でログイン
      cy.login('admin', 'Password123!');
      // ログイン完了を待機
      cy.get('[data-testid="dashboard"]').should('be.visible');

      // When: 勘定科目編集ページにアクセス
      cy.visit('/master/accounts/1/edit');

      // Then: 勘定科目編集ページが表示される
      cy.get('[data-testid="edit-account-page"]').should('be.visible');
      cy.get('[data-testid="edit-account-form"]').should('be.visible');
    });

    it('経理責任者は勘定科目編集ページにアクセスできる', () => {
      // Given: 経理責任者でログイン
      cy.login('manager', 'Password123!');
      // ログイン完了を待機
      cy.get('[data-testid="dashboard"]').should('be.visible');

      // When: 勘定科目編集ページにアクセス
      cy.visit('/master/accounts/1/edit');

      // Then: 勘定科目編集ページが表示される
      cy.get('[data-testid="edit-account-page"]').should('be.visible');
      cy.get('[data-testid="edit-account-form"]').should('be.visible');
    });
  });

  describe('勘定科目編集フォーム', () => {
    beforeEach(() => {
      // 管理者でログインして勘定科目編集ページにアクセス
      cy.login('admin', 'Password123!');
      // ログイン完了を待機
      cy.get('[data-testid="dashboard"]').should('be.visible');
      cy.visit('/master/accounts/1/edit');
      cy.get('[data-testid="edit-account-form"]').should('be.visible');
    });

    it('科目コードは変更できない（disabled状態）', () => {
      // Given: 勘定科目編集フォームが表示されている

      // Then: 科目コードフィールドは無効化されている
      cy.get('[data-testid="edit-account-code-input"]').should('be.disabled');
      cy.get('[data-testid="edit-account-code-input"]').should('have.value', '1000');
    });

    it('科目名を編集できる', () => {
      // Given: 勘定科目編集フォームが表示されている

      // When: 科目名を変更
      cy.get('[data-testid="edit-account-name-input"]').clear().type('編集後の科目名');

      // Then: 科目名が変更される
      cy.get('[data-testid="edit-account-name-input"]').should('have.value', '編集後の科目名');
    });

    it('勘定科目種別を編集できる', () => {
      // Given: 勘定科目編集フォームが表示されている

      // When: 勘定科目種別を変更
      cy.get('[data-testid="edit-account-type-select"]').select('LIABILITY');

      // Then: 勘定科目種別が変更される
      cy.get('[data-testid="edit-account-type-select"]').should('have.value', 'LIABILITY');
    });

    it('科目名、勘定科目種別を編集して保存できる', () => {
      // Given: 勘定科目編集フォームが表示されている

      // When: 科目名と種別を変更して保存
      cy.get('[data-testid="edit-account-name-input"]').clear().type('更新テスト科目');
      cy.get('[data-testid="edit-account-type-select"]').select('EXPENSE');
      cy.get('[data-testid="edit-account-submit"]').click();

      // Then: 勘定科目一覧ページに遷移する（成功時のリダイレクト）
      cy.url().should('include', '/master/accounts');
      cy.url().should('not.include', '/edit');
    });

    it('編集成功時、勘定科目一覧ページに遷移する', () => {
      // Given: 勘定科目編集フォームが表示されている

      // When: 有効な情報で更新
      cy.get('[data-testid="edit-account-name-input"]').clear().type('成功テスト科目');
      cy.get('[data-testid="edit-account-type-select"]').select('REVENUE');
      cy.get('[data-testid="edit-account-submit"]').click();

      // Then: 勘定科目一覧ページに遷移する
      cy.url().should('include', '/master/accounts');
      cy.url().should('not.include', '/edit');
      // 勘定科目一覧が表示される
      cy.get('[data-testid="account-list"]').should('be.visible');
    });
  });

  describe('バリデーション', () => {
    beforeEach(() => {
      // 管理者でログインして勘定科目編集ページにアクセス
      cy.login('admin', 'Password123!');
      // ログイン完了を待機
      cy.get('[data-testid="dashboard"]').should('be.visible');
      cy.visit('/master/accounts/1/edit');
      cy.get('[data-testid="edit-account-form"]').should('be.visible');
    });

    it('科目名が空の場合はバリデーションエラーが表示される', () => {
      // Given: 勘定科目編集フォームが表示されている

      // When: 科目名を空にして保存
      cy.get('[data-testid="edit-account-name-input"]').clear();
      cy.get('[data-testid="edit-account-submit"]').click();

      // Then: バリデーションエラーが表示される
      cy.get('[data-testid="edit-account-name-error"]').should('be.visible');
      cy.get('[data-testid="edit-account-name-error"]').should('contain', '勘定科目名を入力してください');
    });

    it('勘定科目種別が選択されていない場合はバリデーションエラーが表示される', () => {
      // Given: 勘定科目編集フォームが表示されている

      // When: 勘定科目種別を未選択にして保存
      cy.get('[data-testid="edit-account-type-select"]').select('');
      cy.get('[data-testid="edit-account-submit"]').click();

      // Then: バリデーションエラーが表示される
      cy.get('[data-testid="edit-account-type-error"]').should('be.visible');
      cy.get('[data-testid="edit-account-type-error"]').should('contain', '勘定科目種別を選択してください');
    });
  });

  describe('勘定科目一覧からの編集遷移', () => {
    beforeEach(() => {
      // 管理者でログインして勘定科目一覧ページにアクセス
      cy.login('admin', 'Password123!');
      // ログイン完了を待機
      cy.get('[data-testid="dashboard"]').should('be.visible');
      cy.visit('/master/accounts');
      cy.get('[data-testid="account-list"]').should('be.visible');
    });

    it('勘定科目一覧から編集ボタンで編集ページに遷移できる', () => {
      // Given: 勘定科目一覧が表示されている

      // When: 編集ボタンをクリック
      cy.contains('button', '編集').first().click();

      // Then: 勘定科目編集ページに遷移する
      cy.url().should('include', '/master/accounts/');
      cy.url().should('include', '/edit');
      cy.get('[data-testid="edit-account-page"]').should('be.visible');
      cy.get('[data-testid="edit-account-form"]').should('be.visible');
    });
  });
});
