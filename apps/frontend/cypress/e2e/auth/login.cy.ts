/**
 * 認証 E2E テスト
 *
 * US-AUTH-001: ログイン
 * US-AUTH-002: ログアウト
 */
describe('認証機能', () => {
  beforeEach(() => {
    // 各テスト前に認証情報をクリア
    cy.clearAuth();
  });

  describe('US-AUTH-001: ログイン', () => {
    it('正しい認証情報でログインできる', () => {
      // Given: ログインページにアクセス
      cy.visit('/login');
      cy.get('[data-testid="login-page"]').should('be.visible');

      // When: 正しい認証情報を入力してログイン
      cy.get('[data-testid="username-input"]').clear().type('admin');
      cy.get('[data-testid="password-input"]').clear().type('Password123!');
      cy.get('[data-testid="login-submit"]').click();

      // Then: ダッシュボードに遷移し、ユーザー名が表示される
      cy.url().should('not.include', '/login');
      cy.get('[data-testid="dashboard"]').should('be.visible');
      cy.get('[data-testid="header-username"]').should('contain', 'admin');
    });

    it('間違った認証情報でログインするとエラーが表示される', () => {
      // Given: ログインページにアクセス
      cy.visit('/login');

      // When: 間違った認証情報を入力してログイン
      cy.get('[data-testid="username-input"]').clear().type('admin');
      cy.get('[data-testid="password-input"]').clear().type('WrongPassword123!');
      cy.get('[data-testid="login-submit"]').click();

      // Then: エラーメッセージが表示され、ログインページに留まる
      cy.get('[data-testid="login-error"]').should('be.visible');
      cy.get('[data-testid="login-page"]').should('be.visible');
      cy.url().should('include', '/login');
    });

    it('ユーザー名が空の場合はバリデーションエラーが表示される', () => {
      // Given: ログインページにアクセス
      cy.visit('/login');

      // When: ユーザー名を空にしてログイン
      cy.get('[data-testid="username-input"]').clear();
      cy.get('[data-testid="password-input"]').clear().type('Password123!');
      cy.get('[data-testid="login-submit"]').click();

      // Then: バリデーションエラーが表示される
      cy.get('[data-testid="username-error"]').should('be.visible');
      cy.get('[data-testid="username-error"]').should(
        'contain',
        'ユーザー名を入力してください'
      );
    });

    it('パスワードが空の場合はバリデーションエラーが表示される', () => {
      // Given: ログインページにアクセス
      cy.visit('/login');

      // When: パスワードを空にしてログイン
      cy.get('[data-testid="username-input"]').clear().type('admin');
      cy.get('[data-testid="password-input"]').clear();
      cy.get('[data-testid="login-submit"]').click();

      // Then: バリデーションエラーが表示される
      cy.get('[data-testid="password-error"]').should('be.visible');
      cy.get('[data-testid="password-error"]').should(
        'contain',
        'パスワードを入力してください'
      );
    });

    it('パスワードが8文字未満の場合はバリデーションエラーが表示される', () => {
      // Given: ログインページにアクセス
      cy.visit('/login');

      // When: 短いパスワードを入力してログイン
      cy.get('[data-testid="username-input"]').clear().type('admin');
      cy.get('[data-testid="password-input"]').clear().type('short');
      cy.get('[data-testid="login-submit"]').click();

      // Then: バリデーションエラーが表示される
      cy.get('[data-testid="password-error"]').should('be.visible');
      cy.get('[data-testid="password-error"]').should(
        'contain',
        'パスワードは8文字以上です'
      );
    });

    it('未認証状態で保護されたページにアクセスするとログインページにリダイレクトされる', () => {
      // Given: 未認証状態

      // When: ダッシュボードに直接アクセス
      cy.visit('/');

      // Then: ログインページにリダイレクトされる
      cy.url().should('include', '/login');
      cy.get('[data-testid="login-page"]').should('be.visible');
    });

    it('ログイン成功後、JWT トークンがローカルストレージに保存される', () => {
      // Given: ログインページにアクセス
      cy.visit('/login');

      // When: 正しい認証情報でログイン
      cy.get('[data-testid="username-input"]').clear().type('admin');
      cy.get('[data-testid="password-input"]').clear().type('Password123!');
      cy.get('[data-testid="login-submit"]').click();

      // Then: トークンがローカルストレージに保存される
      cy.url().should('not.include', '/login');
      cy.window().then((win) => {
        expect(win.localStorage.getItem('accessToken')).to.not.be.null;
        expect(win.localStorage.getItem('user')).to.not.be.null;
      });
    });

    it('一般ユーザーでログインできる', () => {
      // Given: ログインページにアクセス
      cy.visit('/login');

      // When: 一般ユーザーの認証情報でログイン
      cy.get('[data-testid="username-input"]').clear().type('user');
      cy.get('[data-testid="password-input"]').clear().type('Password123!');
      cy.get('[data-testid="login-submit"]').click();

      // Then: ダッシュボードに遷移し、ユーザー名が表示される
      cy.url().should('not.include', '/login');
      cy.get('[data-testid="dashboard"]').should('be.visible');
      cy.get('[data-testid="header-username"]').should('contain', 'user');
    });
  });

  describe('US-AUTH-002: ログアウト', () => {
    beforeEach(() => {
      // 各テスト前にログイン
      cy.login('admin', 'Password123!');
      cy.url().should('not.include', '/login');
    });

    it('ログアウトボタンをクリックするとログアウトできる', () => {
      // Given: ログイン済み状態
      cy.get('[data-testid="dashboard"]').should('be.visible');

      // When: ログアウトボタンをクリック
      cy.logout();

      // Then: ログインページに遷移する
      cy.url().should('include', '/login');
      cy.get('[data-testid="login-page"]').should('be.visible');
    });

    it('ログアウト後、ローカルストレージから認証情報がクリアされる', () => {
      // Given: ログイン済み状態でトークンが存在する
      cy.window().then((win) => {
        expect(win.localStorage.getItem('accessToken')).to.not.be.null;
      });

      // When: ログアウト
      cy.logout();

      // Then: ローカルストレージから認証情報がクリアされる
      cy.window().then((win) => {
        expect(win.localStorage.getItem('accessToken')).to.be.null;
        expect(win.localStorage.getItem('refreshToken')).to.be.null;
        expect(win.localStorage.getItem('user')).to.be.null;
      });
    });

    it('ログアウト後、保護されたページにアクセスするとログインページにリダイレクトされる', () => {
      // Given: ログイン済み状態
      cy.get('[data-testid="dashboard"]').should('be.visible');

      // When: ログアウトしてからダッシュボードにアクセス
      cy.logout();
      cy.url().should('include', '/login');
      cy.visit('/');

      // Then: ログインページにリダイレクトされる
      cy.url().should('include', '/login');
      cy.get('[data-testid="login-page"]').should('be.visible');
    });
  });

  describe('認証状態の維持', () => {
    it('ログイン後、ページをリロードしても認証状態が維持される', () => {
      // Given: ログイン済み状態
      cy.login('admin', 'Password123!');
      cy.get('[data-testid="dashboard"]').should('be.visible');

      // When: ページをリロード
      cy.reload();

      // Then: 認証状態が維持されダッシュボードが表示される
      cy.get('[data-testid="dashboard"]').should('be.visible');
      cy.get('[data-testid="header-username"]').should('contain', 'admin');
    });

    it('ログイン済みの状態でログインページにアクセスするとダッシュボードにリダイレクトされる', () => {
      // Given: ログイン済み状態
      cy.login('admin', 'Password123!');
      cy.get('[data-testid="dashboard"]').should('be.visible');

      // When: ログインページにアクセス
      cy.visit('/login');

      // Then: ダッシュボードにリダイレクトされる
      cy.url().should('not.include', '/login');
      cy.get('[data-testid="dashboard"]').should('be.visible');
    });
  });
});
