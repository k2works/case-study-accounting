/**
 * ユーザー編集 E2E テスト
 *
 * US-AUTH-004: ユーザー編集
 *
 * 受入条件:
 * - ユーザー一覧ページからユーザー編集ページへ遷移できる
 * - ユーザー情報（username, email）は読み取り専用
 * - 表示名、ロール、パスワードを更新できる
 * - 更新成功時、確認メッセージが表示される
 * - 必須項目が空の場合はバリデーションエラーが表示される
 */
describe('US-AUTH-004: ユーザー編集', () => {
  const userIds = {
    navigation: '101',
    readonly: '102',
    displayName: '103',
    role: '104',
    password: '105',
    validation: '106',
  };

  const loginAsAdminAndVisitUserList = () => {
    cy.login('admin', 'Password123!');
    cy.get('[data-testid="dashboard"]').should('be.visible');
    cy.visit('/users');
    cy.get('[data-testid="user-list-page"]').should('be.visible');
    cy.get('[data-testid="user-list"]').should('be.visible');
  };

  const navigateToUserEditPage = (userId: string) => {
    loginAsAdminAndVisitUserList();
    cy.get(`[data-testid="user-edit-${userId}"]`).should('be.visible').click();
    cy.url().should('include', `/users/${userId}/edit`);
    cy.get('[data-testid="user-edit-page"]').should('be.visible');
    cy.get('[data-testid="user-edit-form"]').should('be.visible');
  };

  beforeEach(() => {
    // 各テスト前に認証情報をクリア
    cy.clearAuth();
  });

  it('ユーザー一覧ページからユーザー編集ページへ遷移できる', () => {
    // Given: ユーザー一覧ページが表示されている
    loginAsAdminAndVisitUserList();

    // When: 編集ボタンをクリック
    cy.get(`[data-testid="user-edit-${userIds.navigation}"]`).click();

    // Then: ユーザー編集ページが表示される
    cy.url().should('include', `/users/${userIds.navigation}/edit`);
    cy.get('[data-testid="user-edit-page"]').should('be.visible');
    cy.get('[data-testid="user-edit-form"]').should('be.visible');
  });

  it('ユーザー情報の表示と読み取り専用状態を確認できる', () => {
    // Given: ユーザー編集ページが表示されている
    navigateToUserEditPage(userIds.readonly);

    // Then: username と email は読み取り専用
    cy.get('[data-testid="user-edit-username-input"]').should('be.disabled');
    cy.get('[data-testid="user-edit-email-input"]').should('be.disabled');
    cy.get('[data-testid="user-edit-username-input"]').should('have.value', 'user_edit_readonly');
    cy.get('[data-testid="user-edit-email-input"]').should('have.value', 'readonly@example.com');
  });

  it('表示名を編集して保存すると成功メッセージが表示される', () => {
    // Given: ユーザー編集ページが表示されている
    navigateToUserEditPage(userIds.displayName);

    // When: 表示名を変更して保存
    cy.get('[data-testid="user-edit-display-name-input"]').clear().type('表示名更新テスト');
    cy.get('[data-testid="user-edit-submit"]').click();

    // Then: ユーザー一覧ページに遷移して成功メッセージが表示される
    cy.get('[data-testid="user-list-page"]').should('be.visible');
    cy.get('[data-testid="success-notification"]').should('contain', 'ユーザー更新が完了しました');
  });

  it('ロールを変更して保存すると成功メッセージが表示される', () => {
    // Given: ユーザー編集ページが表示されている
    navigateToUserEditPage(userIds.role);

    // When: ロールを変更して保存
    cy.get('[data-testid="user-edit-role-select"]').select('MANAGER');
    cy.get('[data-testid="user-edit-submit"]').click();

    // Then: ユーザー一覧ページに遷移して成功メッセージが表示される
    cy.get('[data-testid="user-list-page"]').should('be.visible');
    cy.get('[data-testid="success-notification"]').should('contain', 'ユーザー更新が完了しました');
  });

  it('パスワードを変更して保存すると成功メッセージが表示される', () => {
    // Given: ユーザー編集ページが表示されている
    navigateToUserEditPage(userIds.password);

    // When: パスワードを入力して保存
    cy.get('[data-testid="user-edit-password-input"]').clear().type('NewPass123!');
    cy.get('[data-testid="user-edit-submit"]').click();

    // Then: ユーザー一覧ページに遷移して成功メッセージが表示される
    cy.get('[data-testid="user-list-page"]').should('be.visible');
    cy.get('[data-testid="success-notification"]').should('contain', 'ユーザー更新が完了しました');
  });

  it('必須項目が空の場合はバリデーションエラーが表示される', () => {
    // Given: ユーザー編集ページが表示されている
    navigateToUserEditPage(userIds.validation);

    // When: 表示名とロールを未入力にして保存
    cy.get('[data-testid="user-edit-display-name-input"]').clear();
    cy.get('[data-testid="user-edit-role-select"]').select('');
    cy.get('[data-testid="user-edit-submit"]').click();

    // Then: バリデーションエラーが表示される
    cy.get('[data-testid="user-edit-display-name-error"]').should('be.visible');
    cy.get('[data-testid="user-edit-display-name-error"]').should(
      'contain',
      '表示名を入力してください'
    );
    cy.get('[data-testid="user-edit-role-error"]').should('be.visible');
    cy.get('[data-testid="user-edit-role-error"]').should('contain', 'ロールを選択してください');
  });
});
