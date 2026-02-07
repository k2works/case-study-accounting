/**
 * ユーザー一覧表示 E2E テスト
 *
 * US-AUTH-006: ユーザー一覧表示
 *
 * 受入条件:
 * - ユーザーID、氏名、ロール、最終ログイン日時を一覧表示
 * - ロールでフィルター
 * - ユーザーIDまたは氏名で検索
 * - 一覧から編集画面へ遷移
 */
describe('US-AUTH-006: ユーザー一覧表示', () => {
  // テスト用ユーザーID（handlers.ts の mockUserRecords に対応）
  const testUsers = {
    navigation: { id: '101', username: 'user_edit_nav', displayName: 'ナビゲーション', role: 'USER' },
    manager: { id: '102', username: 'user_edit_readonly', displayName: 'リードオンリー', role: 'MANAGER' },
    noLogin: { id: '103', username: 'user_edit_display', displayName: '表示名テスト', role: 'USER' },
    viewer: { id: '104', username: 'user_edit_role', displayName: 'ロールテスト', role: 'VIEWER' },
    admin: { id: '105', username: 'user_edit_password', displayName: 'パスワードテスト', role: 'ADMIN' },
  };

  const loginAsAdminAndVisitUserList = () => {
    cy.login('admin', 'Password123!');
    cy.get('[data-testid="dashboard"]').should('be.visible');
    cy.visit('/users');
    cy.get('[data-testid="user-list-page"]').should('be.visible');
    cy.get('[data-testid="user-list"]').should('be.visible');
  };

  beforeEach(() => {
    // 各テスト前に認証情報をクリア
    cy.clearAuth();
  });

  describe('ユーザー一覧の表示', () => {
    it('ユーザーID、氏名、ロール、最終ログイン日時が表示される', () => {
      // Given: 管理者でログイン
      loginAsAdminAndVisitUserList();

      // Then: テーブルヘッダーに必要な項目が表示される
      cy.get('[data-testid="user-list"]').within(() => {
        cy.contains('th', 'ユーザー ID').should('be.visible');
        cy.contains('th', 'ユーザー名').should('be.visible');
        cy.contains('th', '表示名').should('be.visible');
        cy.contains('th', 'ロール').should('be.visible');
        cy.contains('th', '最終ログイン').should('be.visible');
      });

      // Then: ユーザーデータが表示される
      cy.get('[data-testid="user-list"]').within(() => {
        // ID が表示される
        cy.contains('td', testUsers.navigation.id).should('be.visible');
        // ユーザー名が表示される
        cy.contains('td', testUsers.navigation.username).should('be.visible');
        // 表示名が表示される
        cy.contains('td', testUsers.navigation.displayName).should('be.visible');
        // ロールが表示される
        cy.contains('td', testUsers.navigation.role).should('be.visible');
      });
    });

    it('最終ログイン日時がある場合はフォーマットされて表示される', () => {
      // Given: 管理者でログイン
      loginAsAdminAndVisitUserList();

      // Then: 最終ログイン日時が表示される（フォーマット済み）
      // handlers.ts で '2024-01-15T10:30:00' → 日本語形式で表示
      cy.get('[data-testid="user-list"]').within(() => {
        cy.contains('td', '2024/1/15').should('be.visible');
      });
    });

    it('最終ログイン日時がない場合は「-」が表示される', () => {
      // Given: 管理者でログイン
      loginAsAdminAndVisitUserList();

      // Then: 最終ログインがない場合は「-」が表示される
      // user_edit_display (id: 103) は lastLoginAt が null
      cy.get('[data-testid="user-list"]').within(() => {
        cy.contains('td', '-').should('be.visible');
      });
    });
  });

  describe('ロールでフィルター', () => {
    it('管理者ロールでフィルターすると該当ユーザーのみ表示される', () => {
      // Given: ユーザー一覧ページが表示されている
      loginAsAdminAndVisitUserList();

      // When: ロールを「管理者」に変更して検索
      cy.get('[data-testid="user-filter-role"]').select('ADMIN');
      cy.get('[data-testid="user-filter-search"]').click();

      // Then: ADMIN ロールのユーザーのみ表示される
      cy.get('[data-testid="user-list"]').within(() => {
        cy.contains('td', testUsers.admin.displayName).should('be.visible');
        cy.contains('td', testUsers.navigation.displayName).should('not.exist');
        cy.contains('td', testUsers.manager.displayName).should('not.exist');
      });
    });

    it('マネージャーロールでフィルターすると該当ユーザーのみ表示される', () => {
      // Given: ユーザー一覧ページが表示されている
      loginAsAdminAndVisitUserList();

      // When: ロールを「マネージャー」に変更して検索
      cy.get('[data-testid="user-filter-role"]').select('MANAGER');
      cy.get('[data-testid="user-filter-search"]').click();

      // Then: MANAGER ロールのユーザーのみ表示される
      cy.get('[data-testid="user-list"]').within(() => {
        cy.contains('td', testUsers.manager.displayName).should('be.visible');
        cy.contains('td', testUsers.navigation.displayName).should('not.exist');
        cy.contains('td', testUsers.admin.displayName).should('not.exist');
      });
    });

    it('リセットボタンでフィルターをクリアすると全ユーザーが表示される', () => {
      // Given: ロールでフィルター済み
      loginAsAdminAndVisitUserList();
      cy.get('[data-testid="user-filter-role"]').select('ADMIN');
      cy.get('[data-testid="user-filter-search"]').click();
      cy.get('[data-testid="user-list"]').within(() => {
        cy.contains('td', testUsers.navigation.displayName).should('not.exist');
      });

      // When: リセットボタンをクリック
      cy.get('[data-testid="user-filter-reset"]').click();

      // Then: 全ユーザーが表示される
      cy.get('[data-testid="user-list"]').within(() => {
        cy.contains('td', testUsers.navigation.displayName).should('be.visible');
        cy.contains('td', testUsers.admin.displayName).should('be.visible');
      });
    });
  });

  describe('キーワードで検索', () => {
    it('ユーザー名で検索できる', () => {
      // Given: ユーザー一覧ページが表示されている
      loginAsAdminAndVisitUserList();

      // When: ユーザー名の一部で検索
      cy.get('[data-testid="user-filter-keyword"]').type('nav');
      cy.get('[data-testid="user-filter-search"]').click();

      // Then: 該当ユーザーのみ表示される
      cy.get('[data-testid="user-list"]').within(() => {
        cy.contains('td', testUsers.navigation.username).should('be.visible');
        cy.contains('td', testUsers.manager.username).should('not.exist');
      });
    });

    it('表示名で検索できる', () => {
      // Given: ユーザー一覧ページが表示されている
      loginAsAdminAndVisitUserList();

      // When: 表示名の一部で検索
      cy.get('[data-testid="user-filter-keyword"]').type('ナビ');
      cy.get('[data-testid="user-filter-search"]').click();

      // Then: 該当ユーザーのみ表示される
      cy.get('[data-testid="user-list"]').within(() => {
        cy.contains('td', testUsers.navigation.displayName).should('be.visible');
        cy.contains('td', testUsers.manager.displayName).should('not.exist');
      });
    });

    it('Enter キーで検索を実行できる', () => {
      // Given: ユーザー一覧ページが表示されている
      loginAsAdminAndVisitUserList();

      // When: キーワードを入力して Enter キーを押す
      cy.get('[data-testid="user-filter-keyword"]').type('ロール{enter}');

      // Then: 該当ユーザーのみ表示される
      cy.get('[data-testid="user-list"]').within(() => {
        cy.contains('td', testUsers.viewer.displayName).should('be.visible');
      });
    });

    it('ロールとキーワードを組み合わせて検索できる', () => {
      // Given: ユーザー一覧ページが表示されている
      loginAsAdminAndVisitUserList();

      // When: ロールを USER に設定し、キーワードで検索
      cy.get('[data-testid="user-filter-role"]').select('USER');
      cy.get('[data-testid="user-filter-keyword"]').type('表示');
      cy.get('[data-testid="user-filter-search"]').click();

      // Then: USER ロールかつ表示名に「表示」を含むユーザーのみ表示される
      cy.get('[data-testid="user-list"]').within(() => {
        cy.contains('td', testUsers.noLogin.displayName).should('be.visible');
        cy.contains('td', testUsers.navigation.displayName).should('not.exist');
      });
    });
  });

  describe('編集画面への遷移', () => {
    it('編集ボタンをクリックすると編集画面に遷移する', () => {
      // Given: ユーザー一覧ページが表示されている
      loginAsAdminAndVisitUserList();

      // When: 編集ボタンをクリック
      cy.get(`[data-testid="user-edit-${testUsers.navigation.id}"]`).click();

      // Then: ユーザー編集画面に遷移する
      cy.url().should('include', `/users/${testUsers.navigation.id}/edit`);
    });
  });

  describe('権限チェック', () => {
    it('管理者以外はユーザー一覧ページにアクセスできない', () => {
      // Given: 一般ユーザーでログイン
      cy.login('user', 'Password123!');
      cy.get('[data-testid="dashboard"]').should('be.visible');

      // When: ユーザー一覧ページに直接アクセス
      cy.visit('/users');

      // Then: ホームにリダイレクトされる
      cy.url().should('not.include', '/users');
    });

    it('未認証ユーザーはログインページにリダイレクトされる', () => {
      // When: 未認証でユーザー一覧ページにアクセス
      cy.visit('/users');

      // Then: ログインページにリダイレクトされる
      cy.url().should('include', '/login');
    });
  });
});
