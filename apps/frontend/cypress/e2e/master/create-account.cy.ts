/**
 * 勘定科目登録 E2E テスト
 *
 * US-MST-001: 勘定科目登録
 *
 * 受入条件:
 * - 科目コード、科目名、勘定科目種別を入力して登録できる
 * - 科目コードは一意である必要がある
 * - 勘定科目種別は資産、負債、純資産、収益、費用から選択できる
 * - 登録成功時、確認メッセージが表示される
 */
describe('US-MST-001: 勘定科目登録', () => {
  beforeEach(() => {
    // 各テスト前に認証情報をクリア
    cy.clearAuth();
  });

  describe('アクセス制御', () => {
    it('一般ユーザーは勘定科目登録ページにアクセスできない', () => {
      // Given: 一般ユーザーでログイン
      cy.login('user', 'Password123!');
      // ログイン完了を待機
      cy.get('[data-testid="dashboard"]').should('be.visible');

      // When: 勘定科目登録ページに直接アクセス
      cy.visit('/master/accounts/new');

      // Then: ダッシュボードにリダイレクトされる
      cy.url().should('not.include', '/master/accounts/new');
      cy.get('[data-testid="dashboard"]').should('be.visible');
    });

    it('未認証ユーザーは勘定科目登録ページにアクセスできない', () => {
      // Given: 未認証状態

      // When: 勘定科目登録ページに直接アクセス
      cy.visit('/master/accounts/new');

      // Then: ログインページにリダイレクトされる
      cy.url().should('include', '/login');
      cy.get('[data-testid="login-page"]').should('be.visible');
    });

    it('管理者は勘定科目登録ページにアクセスできる', () => {
      // Given: 管理者でログイン
      cy.login('admin', 'Password123!');
      // ログイン完了を待機
      cy.get('[data-testid="dashboard"]').should('be.visible');

      // When: 勘定科目登録ページにアクセス
      cy.visit('/master/accounts/new');

      // Then: 勘定科目登録ページが表示される
      cy.get('[data-testid="create-account-page"]').should('be.visible');
      cy.get('[data-testid="create-account-form"]').should('be.visible');
    });

    it('経理責任者は勘定科目登録ページにアクセスできる', () => {
      // Given: 経理責任者でログイン
      cy.login('manager', 'Password123!');
      // ログイン完了を待機
      cy.get('[data-testid="dashboard"]').should('be.visible');

      // When: 勘定科目登録ページにアクセス
      cy.visit('/master/accounts/new');

      // Then: 勘定科目登録ページが表示される
      cy.get('[data-testid="create-account-page"]').should('be.visible');
      cy.get('[data-testid="create-account-form"]').should('be.visible');
    });
  });

  describe('勘定科目登録フォーム', () => {
    beforeEach(() => {
      // 管理者でログインして勘定科目登録ページにアクセス
      cy.login('admin', 'Password123!');
      // ログイン完了を待機
      cy.get('[data-testid="dashboard"]').should('be.visible');
      cy.visit('/master/accounts/new');
      cy.get('[data-testid="create-account-form"]').should('be.visible');
    });

    it('科目コード、科目名、勘定科目種別を入力して登録できる', () => {
      // Given: 勘定科目登録フォームが表示されている
      const uniqueCode = `${Date.now()}`.slice(-4);

      // When: 必要な情報を入力して登録
      cy.get('[data-testid="create-account-code-input"]').clear().type(uniqueCode);
      cy.get('[data-testid="create-account-name-input"]').clear().type('テスト勘定科目');
      cy.get('[data-testid="create-account-type-select"]').select('ASSET');
      cy.get('[data-testid="create-account-submit"]').click();

      // Then: 登録成功メッセージが表示される
      cy.get('[data-testid="create-account-success"]').should('be.visible');
      cy.get('[data-testid="create-account-success"]').should('contain', '勘定科目登録が完了しました');
    });

    it('勘定科目種別は資産、負債、純資産、収益、費用から選択できる', () => {
      // Given: 勘定科目登録フォームが表示されている

      // Then: すべての勘定科目種別オプションが選択可能
      cy.get('[data-testid="create-account-type-select"]').should('be.visible');

      // ASSET（資産）
      cy.get('[data-testid="create-account-type-select"]').select('ASSET');
      cy.get('[data-testid="create-account-type-select"]').should('have.value', 'ASSET');

      // LIABILITY（負債）
      cy.get('[data-testid="create-account-type-select"]').select('LIABILITY');
      cy.get('[data-testid="create-account-type-select"]').should('have.value', 'LIABILITY');

      // EQUITY（純資産）
      cy.get('[data-testid="create-account-type-select"]').select('EQUITY');
      cy.get('[data-testid="create-account-type-select"]').should('have.value', 'EQUITY');

      // REVENUE（収益）
      cy.get('[data-testid="create-account-type-select"]').select('REVENUE');
      cy.get('[data-testid="create-account-type-select"]').should('have.value', 'REVENUE');

      // EXPENSE（費用）
      cy.get('[data-testid="create-account-type-select"]').select('EXPENSE');
      cy.get('[data-testid="create-account-type-select"]').should('have.value', 'EXPENSE');
    });

    it('登録成功時、確認メッセージが表示され、フォームがリセットされる', () => {
      // Given: 勘定科目登録フォームが表示されている
      const uniqueCode = `${Date.now()}`.slice(-4);

      // When: 有効な情報で登録
      cy.get('[data-testid="create-account-code-input"]').clear().type(uniqueCode);
      cy.get('[data-testid="create-account-name-input"]').clear().type('成功テスト科目');
      cy.get('[data-testid="create-account-type-select"]').select('REVENUE');
      cy.get('[data-testid="create-account-submit"]').click();

      // Then: 成功メッセージが表示され、フォームがリセットされる
      cy.get('[data-testid="create-account-success"]').should('be.visible');
      cy.get('[data-testid="create-account-code-input"]').should('have.value', '');
      cy.get('[data-testid="create-account-name-input"]').should('have.value', '');
    });
  });

  describe('バリデーション', () => {
    beforeEach(() => {
      // 管理者でログインして勘定科目登録ページにアクセス
      cy.login('admin', 'Password123!');
      // ログイン完了を待機
      cy.get('[data-testid="dashboard"]').should('be.visible');
      cy.visit('/master/accounts/new');
      cy.get('[data-testid="create-account-form"]').should('be.visible');
    });

    it('科目コードが空の場合はバリデーションエラーが表示される', () => {
      // Given: 勘定科目登録フォームが表示されている

      // When: 科目コードを空にして登録
      cy.get('[data-testid="create-account-code-input"]').clear();
      cy.get('[data-testid="create-account-name-input"]').clear().type('テスト科目');
      cy.get('[data-testid="create-account-type-select"]').select('ASSET');
      cy.get('[data-testid="create-account-submit"]').click();

      // Then: バリデーションエラーが表示される
      cy.get('[data-testid="create-account-code-error"]').should('be.visible');
      cy.get('[data-testid="create-account-code-error"]').should('contain', '勘定科目コードを入力してください');
    });

    it('科目コードが4桁の数字でない場合はバリデーションエラーが表示される', () => {
      // Given: 勘定科目登録フォームが表示されている

      // When: 不正な形式の科目コードを入力して登録
      cy.get('[data-testid="create-account-code-input"]').clear().type('ABC');
      cy.get('[data-testid="create-account-name-input"]').clear().type('テスト科目');
      cy.get('[data-testid="create-account-type-select"]').select('ASSET');
      cy.get('[data-testid="create-account-submit"]').click();

      // Then: バリデーションエラーが表示される
      cy.get('[data-testid="create-account-code-error"]').should('be.visible');
      cy.get('[data-testid="create-account-code-error"]').should('contain', '勘定科目コードは4桁の数字で入力してください');
    });

    it('科目名が空の場合はバリデーションエラーが表示される', () => {
      // Given: 勘定科目登録フォームが表示されている

      // When: 科目名を空にして登録
      cy.get('[data-testid="create-account-code-input"]').clear().type('1234');
      cy.get('[data-testid="create-account-name-input"]').clear();
      cy.get('[data-testid="create-account-type-select"]').select('ASSET');
      cy.get('[data-testid="create-account-submit"]').click();

      // Then: バリデーションエラーが表示される
      cy.get('[data-testid="create-account-name-error"]').should('be.visible');
      cy.get('[data-testid="create-account-name-error"]').should('contain', '勘定科目名を入力してください');
    });

    it('勘定科目種別が選択されていない場合はバリデーションエラーが表示される', () => {
      // Given: 勘定科目登録フォームが表示されている

      // When: 勘定科目種別を選択せずに登録
      cy.get('[data-testid="create-account-code-input"]').clear().type('1234');
      cy.get('[data-testid="create-account-name-input"]').clear().type('テスト科目');
      // 勘定科目種別は選択しない
      cy.get('[data-testid="create-account-submit"]').click();

      // Then: バリデーションエラーが表示される
      cy.get('[data-testid="create-account-type-error"]').should('be.visible');
      cy.get('[data-testid="create-account-type-error"]').should('contain', '勘定科目種別を選択してください');
    });
  });

  describe('一意性チェック', () => {
    beforeEach(() => {
      // 管理者でログインして勘定科目登録ページにアクセス
      cy.login('admin', 'Password123!');
      // ログイン完了を待機
      cy.get('[data-testid="dashboard"]').should('be.visible');
      cy.visit('/master/accounts/new');
      cy.get('[data-testid="create-account-form"]').should('be.visible');
    });

    it('既存の科目コードで登録しようとするとエラーが表示される', () => {
      // Given: 既存の科目コード「1000」が存在する（MSW モックデータ）

      // When: 既存の科目コードで登録を試みる
      cy.get('[data-testid="create-account-code-input"]').clear().type('1000');
      cy.get('[data-testid="create-account-name-input"]').clear().type('重複テスト科目');
      cy.get('[data-testid="create-account-type-select"]').select('ASSET');
      cy.get('[data-testid="create-account-submit"]').click();

      // Then: エラーメッセージが表示される
      cy.get('[data-testid="create-account-error"]').should('be.visible');
      cy.get('[data-testid="create-account-error"]').should('contain', '勘定科目コードは既に使用されています');
    });
  });
});
