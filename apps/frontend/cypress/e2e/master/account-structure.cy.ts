/**
 * 勘定科目構成管理 E2E テスト
 *
 * US-MST-005: 勘定科目構成登録
 *
 * 受入条件:
 * - 親科目と子科目を選択して登録できる
 * - 循環参照が発生しないようバリデーションされる
 * - 表示順を設定できる
 * - 登録成功時、確認メッセージが表示される
 */
describe('US-MST-005: 勘定科目構成登録', () => {
  beforeEach(() => {
    cy.clearAuth();
  });

  describe('一覧表示', () => {
    beforeEach(() => {
      cy.login('admin', 'Password123!');
      cy.get('[data-testid="dashboard"]').should('be.visible');
      cy.visit('/master/account-structures');
      cy.get('[data-testid="account-structure-list-page"]', { timeout: 15000 }).should(
        'be.visible'
      );
    });

    it('勘定科目構成一覧が表示される', () => {
      // Then: テーブルヘッダーに必要なカラムが表示される
      cy.contains('th', '勘定科目コード').should('be.visible');
      cy.contains('th', '勘定科目名').should('be.visible');
      cy.contains('th', 'パス').should('be.visible');
      cy.contains('th', '階層').should('be.visible');
      cy.contains('th', '親科目コード').should('be.visible');
      cy.contains('th', '表示順').should('be.visible');

      // Then: テーブルにデータが表示される
      cy.get('table tbody tr').should('have.length.at.least', 1);
    });

    it('新規登録ボタンが表示される', () => {
      // Then: 新規登録ボタンが表示される
      cy.contains('button', '新規登録').should('be.visible');
    });

    it('操作カラムに編集・削除ボタンが表示される', () => {
      // Then: 操作カラムに編集・削除ボタンが表示される
      cy.contains('th', '操作').should('be.visible');
      cy.get('table tbody tr').first().contains('button', '編集').should('be.visible');
      cy.get('table tbody tr').first().contains('button', '削除').should('be.visible');
    });
  });

  describe('新規登録', () => {
    beforeEach(() => {
      cy.login('admin', 'Password123!');
      cy.get('[data-testid="dashboard"]').should('be.visible');
      cy.visit('/master/account-structures/new');
    });

    it('新規登録フォームが表示される', () => {
      // Then: フォームフィールドが表示される
      cy.get('#accountCode').should('be.visible');
      cy.get('#parentAccountCode').should('be.visible');
      cy.get('#displayOrder').should('be.visible');
      cy.contains('button', '登録').should('be.visible');
      cy.contains('button', '戻る').should('be.visible');
    });

    it('親科目と子科目を選択して登録できる', () => {
      // Given: 新規登録フォームが表示されている

      // When: 勘定科目コード、親科目コード、表示順を入力して登録
      cy.get('#accountCode').clear().type('2000');
      cy.get('#parentAccountCode').clear().type('1000');
      cy.get('#displayOrder').clear().type('10');
      cy.contains('button', '登録').click();

      // Then: 一覧ページに遷移し、成功メッセージが表示される
      cy.url({ timeout: 10000 }).should('include', '/master/account-structures');
      cy.contains('勘定科目構成を登録しました', { timeout: 10000 }).should('be.visible');
    });

    it('表示順を設定して登録できる', () => {
      // Given: 新規登録フォームが表示されている

      // When: 表示順を設定して登録
      cy.get('#accountCode').clear().type('3001');
      cy.get('#displayOrder').clear().type('5');
      cy.contains('button', '登録').click();

      // Then: 一覧ページに遷移する
      cy.url({ timeout: 10000 }).should('include', '/master/account-structures');
    });

    it('勘定科目コードが空の場合バリデーションエラーが表示される', () => {
      // Given: 新規登録フォームが表示されている

      // When: 勘定科目コードを空にして登録
      cy.get('#accountCode').clear();
      cy.contains('button', '登録').click();

      // Then: バリデーションエラーが表示される
      cy.contains('勘定科目コードを入力してください').should('be.visible');
    });

    it('戻るボタンで一覧ページに戻れる', () => {
      // When: 戻るボタンをクリック
      cy.contains('button', '戻る').click();

      // Then: 一覧ページに遷移する
      cy.url().should('include', '/master/account-structures');
      cy.url().should('not.include', '/new');
    });
  });

  describe('削除', () => {
    beforeEach(() => {
      cy.login('admin', 'Password123!');
      cy.get('[data-testid="dashboard"]').should('be.visible');
      cy.visit('/master/account-structures');
      cy.get('[data-testid="account-structure-list-page"]', { timeout: 15000 }).should(
        'be.visible'
      );
      cy.get('table tbody tr').should('have.length.at.least', 1);
    });

    it('子構成がない場合、削除できる', () => {
      // Given: 子構成のない勘定科目構成が存在する（1001 は子なし）
      // When: 削除ボタンをクリックし確認ダイアログで OK
      cy.on('window:confirm', () => true);
      cy.get('table tbody tr').last().contains('button', '削除').click();

      // Then: 成功メッセージが表示される
      cy.contains('勘定科目構成を削除しました', { timeout: 10000 }).should('be.visible');
    });

    it('削除確認ダイアログでキャンセルした場合、削除されない', () => {
      // When: 削除ボタンをクリックし確認ダイアログでキャンセル
      cy.on('window:confirm', () => false);
      cy.get('table tbody tr').first().contains('button', '削除').click();

      // Then: テーブルにデータが残っている
      cy.get('table tbody tr').should('have.length.at.least', 1);
    });
  });

  describe('アクセス制御', () => {
    it('一般ユーザーは勘定科目構成ページにアクセスできない', () => {
      // Given: 一般ユーザーでログイン
      cy.login('user', 'Password123!');
      cy.get('[data-testid="dashboard"]').should('be.visible');

      // When: 勘定科目構成ページに直接アクセス
      cy.visit('/master/account-structures');

      // Then: ダッシュボードにリダイレクトされる
      cy.url().should('not.include', '/master/account-structures');
      cy.get('[data-testid="dashboard"]').should('be.visible');
    });

    it('未認証ユーザーは勘定科目構成ページにアクセスできない', () => {
      // When: 未認証で勘定科目構成ページに直接アクセス
      cy.visit('/master/account-structures');

      // Then: ログインページにリダイレクトされる
      cy.url().should('include', '/login');
    });

    it('経理責任者は勘定科目構成ページにアクセスできる', () => {
      // Given: 経理責任者でログイン
      cy.login('manager', 'Password123!');
      cy.get('[data-testid="dashboard"]').should('be.visible');

      // When: 勘定科目構成ページにアクセス
      cy.visit('/master/account-structures');

      // Then: 勘定科目構成一覧が表示される
      cy.get('[data-testid="account-structure-list-page"]', { timeout: 15000 }).should(
        'be.visible'
      );
    });
  });
});
