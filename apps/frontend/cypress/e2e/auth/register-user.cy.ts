/**
 * ユーザー登録 E2E テスト
 *
 * US-AUTH-003: ユーザー登録
 *
 * 受入条件:
 * - ユーザーID、氏名、パスワード、ロールを入力して登録できる
 * - ユーザーIDは一意である必要がある
 * - パスワードは8文字以上で、英数字を含む必要がある
 * - ロールは管理者、経理責任者、経理担当者、閲覧者から選択できる
 * - 登録成功時、確認メッセージが表示される
 */
describe('US-AUTH-003: ユーザー登録', () => {
  beforeEach(() => {
    // 各テスト前に認証情報をクリア
    cy.clearAuth();
  });

  describe('アクセス制御', () => {
    it('管理者権限がないユーザーはユーザー登録ページにアクセスできない', () => {
      // Given: 一般ユーザーでログイン
      cy.login('user', 'Password123!');
      // ログイン完了を待機（ダッシュボードが表示されるまで）
      cy.get('[data-testid="dashboard"]').should('be.visible');

      // When: ユーザー登録ページに直接アクセス
      cy.visit('/system/users');

      // Then: ダッシュボードにリダイレクトされる
      cy.url().should('not.include', '/system/users');
      cy.get('[data-testid="dashboard"]').should('be.visible');
    });

    it('未認証ユーザーはユーザー登録ページにアクセスできない', () => {
      // Given: 未認証状態

      // When: ユーザー登録ページに直接アクセス
      cy.visit('/system/users');

      // Then: ログインページにリダイレクトされる
      cy.url().should('include', '/login');
      cy.get('[data-testid="login-page"]').should('be.visible');
    });

    it('管理者はユーザー登録ページにアクセスできる', () => {
      // Given: 管理者でログイン
      cy.login('admin', 'Password123!');
      // ログイン完了を待機（ダッシュボードが表示されるまで）
      cy.get('[data-testid="dashboard"]').should('be.visible');

      // When: ユーザー登録ページにアクセス
      cy.visit('/system/users');

      // Then: ユーザー登録ページが表示される
      cy.get('[data-testid="register-user-page"]').should('be.visible');
      cy.get('[data-testid="register-form"]').should('be.visible');
    });
  });

  describe('ユーザー登録フォーム', () => {
    beforeEach(() => {
      // 管理者でログインしてユーザー登録ページにアクセス
      cy.login('admin', 'Password123!');
      // ログイン完了を待機（ダッシュボードが表示されるまで）
      cy.get('[data-testid="dashboard"]').should('be.visible');
      cy.visit('/system/users');
      cy.get('[data-testid="register-form"]').should('be.visible');
    });

    it('ユーザーID、氏名、パスワード、ロールを入力して登録できる', () => {
      // Given: ユーザー登録フォームが表示されている
      const uniqueUsername = `testuser_${Date.now()}`;

      // When: 必要な情報を入力して登録
      cy.get('[data-testid="register-username-input"]').clear().type(uniqueUsername);
      cy.get('[data-testid="register-email-input"]').clear().type(`${uniqueUsername}@example.com`);
      cy.get('[data-testid="register-password-input"]').clear().type('Password123!');
      cy.get('[data-testid="register-confirm-password-input"]').clear().type('Password123!');
      cy.get('[data-testid="register-display-name-input"]').clear().type('テストユーザー');
      cy.get('[data-testid="register-role-select"]').select('USER');
      cy.get('[data-testid="register-submit"]').click();

      // Then: 登録成功メッセージが表示される
      cy.get('[data-testid="register-success"]').should('be.visible');
      cy.get('[data-testid="register-success"]').should('contain', 'ユーザー登録が完了しました');
    });

    it('ロールは管理者、経理責任者、経理担当者、閲覧者から選択できる', () => {
      // Given: ユーザー登録フォームが表示されている

      // Then: すべてのロールオプションが選択可能
      cy.get('[data-testid="register-role-select"]').should('be.visible');

      // ADMIN（管理者）
      cy.get('[data-testid="register-role-select"]').select('ADMIN');
      cy.get('[data-testid="register-role-select"]').should('have.value', 'ADMIN');

      // MANAGER（経理責任者）
      cy.get('[data-testid="register-role-select"]').select('MANAGER');
      cy.get('[data-testid="register-role-select"]').should('have.value', 'MANAGER');

      // USER（経理担当者）
      cy.get('[data-testid="register-role-select"]').select('USER');
      cy.get('[data-testid="register-role-select"]').should('have.value', 'USER');

      // VIEWER（閲覧者）
      cy.get('[data-testid="register-role-select"]').select('VIEWER');
      cy.get('[data-testid="register-role-select"]').should('have.value', 'VIEWER');
    });

    it('登録成功時、確認メッセージが表示される', () => {
      // Given: ユーザー登録フォームが表示されている
      const uniqueUsername = `successuser_${Date.now()}`;

      // When: 有効な情報で登録
      cy.get('[data-testid="register-username-input"]').clear().type(uniqueUsername);
      cy.get('[data-testid="register-email-input"]').clear().type(`${uniqueUsername}@example.com`);
      cy.get('[data-testid="register-password-input"]').clear().type('ValidPass123!');
      cy.get('[data-testid="register-confirm-password-input"]').clear().type('ValidPass123!');
      cy.get('[data-testid="register-display-name-input"]').clear().type('成功テスト');
      cy.get('[data-testid="register-role-select"]').select('VIEWER');
      cy.get('[data-testid="register-submit"]').click();

      // Then: 成功メッセージが表示され、フォームがリセットされる
      cy.get('[data-testid="register-success"]').should('be.visible');
      cy.get('[data-testid="register-username-input"]').should('have.value', '');
      cy.get('[data-testid="register-email-input"]').should('have.value', '');
    });
  });

  describe('バリデーション', () => {
    beforeEach(() => {
      // 管理者でログインしてユーザー登録ページにアクセス
      cy.login('admin', 'Password123!');
      // ログイン完了を待機（ダッシュボードが表示されるまで）
      cy.get('[data-testid="dashboard"]').should('be.visible');
      cy.visit('/system/users');
      cy.get('[data-testid="register-form"]').should('be.visible');
    });

    it('ユーザー名が空の場合はバリデーションエラーが表示される', () => {
      // Given: ユーザー登録フォームが表示されている

      // When: ユーザー名を空にして登録
      cy.get('[data-testid="register-username-input"]').clear();
      cy.get('[data-testid="register-email-input"]').clear().type('test@example.com');
      cy.get('[data-testid="register-password-input"]').clear().type('Password123!');
      cy.get('[data-testid="register-confirm-password-input"]').clear().type('Password123!');
      cy.get('[data-testid="register-display-name-input"]').clear().type('テスト');
      cy.get('[data-testid="register-role-select"]').select('USER');
      cy.get('[data-testid="register-submit"]').click();

      // Then: バリデーションエラーが表示される
      cy.get('[data-testid="username-error"]').should('be.visible');
      cy.get('[data-testid="username-error"]').should('contain', 'ユーザー名を入力してください');
    });

    it('パスワードが8文字未満の場合はバリデーションエラーが表示される', () => {
      // Given: ユーザー登録フォームが表示されている

      // When: 短いパスワードを入力して登録
      cy.get('[data-testid="register-username-input"]').clear().type('testuser');
      cy.get('[data-testid="register-email-input"]').clear().type('test@example.com');
      cy.get('[data-testid="register-password-input"]').clear().type('short'); // 8文字未満
      cy.get('[data-testid="register-confirm-password-input"]').clear().type('short');
      cy.get('[data-testid="register-display-name-input"]').clear().type('テスト');
      cy.get('[data-testid="register-role-select"]').select('USER');
      cy.get('[data-testid="register-submit"]').click();

      // Then: パスワードのバリデーションエラーが表示される
      cy.get('[data-testid="password-error"]').should('be.visible');
      cy.get('[data-testid="password-error"]').should('contain', 'パスワードは8文字以上です');
    });

    it('パスワードが空の場合はバリデーションエラーが表示される', () => {
      // Given: ユーザー登録フォームが表示されている

      // When: パスワードを空にして登録
      cy.get('[data-testid="register-username-input"]').clear().type('testuser');
      cy.get('[data-testid="register-email-input"]').clear().type('test@example.com');
      cy.get('[data-testid="register-password-input"]').clear();
      cy.get('[data-testid="register-confirm-password-input"]').clear();
      cy.get('[data-testid="register-display-name-input"]').clear().type('テスト');
      cy.get('[data-testid="register-role-select"]').select('USER');
      cy.get('[data-testid="register-submit"]').click();

      // Then: パスワードのバリデーションエラーが表示される
      cy.get('[data-testid="password-error"]').should('be.visible');
      cy.get('[data-testid="password-error"]').should('contain', 'パスワードを入力してください');
    });

    it('パスワードと確認用パスワードが一致しない場合はバリデーションエラーが表示される', () => {
      // Given: ユーザー登録フォームが表示されている

      // When: 異なるパスワードを入力して登録
      cy.get('[data-testid="register-username-input"]').clear().type('testuser');
      cy.get('[data-testid="register-email-input"]').clear().type('test@example.com');
      cy.get('[data-testid="register-password-input"]').clear().type('Password123!');
      cy.get('[data-testid="register-confirm-password-input"]').clear().type('DifferentPass123!');
      cy.get('[data-testid="register-display-name-input"]').clear().type('テスト');
      cy.get('[data-testid="register-role-select"]').select('USER');
      cy.get('[data-testid="register-submit"]').click();

      // Then: 確認用パスワードのバリデーションエラーが表示される
      cy.get('[data-testid="confirm-password-error"]').should('be.visible');
      cy.get('[data-testid="confirm-password-error"]').should('contain', 'パスワードが一致しません');
    });

    it('表示名が空の場合はバリデーションエラーが表示される', () => {
      // Given: ユーザー登録フォームが表示されている

      // When: 表示名を空にして登録
      cy.get('[data-testid="register-username-input"]').clear().type('testuser');
      cy.get('[data-testid="register-email-input"]').clear().type('test@example.com');
      cy.get('[data-testid="register-password-input"]').clear().type('Password123!');
      cy.get('[data-testid="register-confirm-password-input"]').clear().type('Password123!');
      cy.get('[data-testid="register-display-name-input"]').clear();
      cy.get('[data-testid="register-role-select"]').select('USER');
      cy.get('[data-testid="register-submit"]').click();

      // Then: 表示名のバリデーションエラーが表示される
      cy.get('[data-testid="display-name-error"]').should('be.visible');
      cy.get('[data-testid="display-name-error"]').should('contain', '表示名を入力してください');
    });

    it('ロールが選択されていない場合はバリデーションエラーが表示される', () => {
      // Given: ユーザー登録フォームが表示されている

      // When: ロールを選択せずに登録
      cy.get('[data-testid="register-username-input"]').clear().type('testuser');
      cy.get('[data-testid="register-email-input"]').clear().type('test@example.com');
      cy.get('[data-testid="register-password-input"]').clear().type('Password123!');
      cy.get('[data-testid="register-confirm-password-input"]').clear().type('Password123!');
      cy.get('[data-testid="register-display-name-input"]').clear().type('テスト');
      // ロールは選択しない
      cy.get('[data-testid="register-submit"]').click();

      // Then: ロールのバリデーションエラーが表示される
      cy.get('[data-testid="role-error"]').should('be.visible');
      cy.get('[data-testid="role-error"]').should('contain', 'ロールを選択してください');
    });

    it('メールアドレスの形式が不正な場合はバリデーションエラーが表示される', () => {
      // Given: ユーザー登録フォームが表示されている

      // When: 不正なメールアドレスを入力して登録
      cy.get('[data-testid="register-username-input"]').clear().type('testuser');
      cy.get('[data-testid="register-email-input"]').clear().type('invalid-email');
      cy.get('[data-testid="register-password-input"]').clear().type('Password123!');
      cy.get('[data-testid="register-confirm-password-input"]').clear().type('Password123!');
      cy.get('[data-testid="register-display-name-input"]').clear().type('テスト');
      cy.get('[data-testid="register-role-select"]').select('USER');
      cy.get('[data-testid="register-submit"]').click();

      // Then: メールアドレスのバリデーションエラーが表示される
      cy.get('[data-testid="email-error"]').should('be.visible');
      cy.get('[data-testid="email-error"]').should('contain', 'メールアドレスの形式が不正です');
    });
  });

  describe('一意性チェック', () => {
    beforeEach(() => {
      // 管理者でログインしてユーザー登録ページにアクセス
      cy.login('admin', 'Password123!');
      // ログイン完了を待機（ダッシュボードが表示されるまで）
      cy.get('[data-testid="dashboard"]').should('be.visible');
      cy.visit('/system/users');
      cy.get('[data-testid="register-form"]').should('be.visible');
    });

    it('既存のユーザーIDで登録しようとするとエラーが表示される', () => {
      // Given: 既存のユーザー「admin」が存在する

      // When: 既存のユーザーIDで登録を試みる
      cy.get('[data-testid="register-username-input"]').clear().type('admin');
      cy.get('[data-testid="register-email-input"]').clear().type('admin_new@example.com');
      cy.get('[data-testid="register-password-input"]').clear().type('Password123!');
      cy.get('[data-testid="register-confirm-password-input"]').clear().type('Password123!');
      cy.get('[data-testid="register-display-name-input"]').clear().type('管理者重複テスト');
      cy.get('[data-testid="register-role-select"]').select('USER');
      cy.get('[data-testid="register-submit"]').click();

      // Then: エラーメッセージが表示される
      cy.get('[data-testid="register-error"]').should('be.visible');
    });
  });
});
