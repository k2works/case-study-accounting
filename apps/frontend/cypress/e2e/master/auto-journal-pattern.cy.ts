/**
 * 自動仕訳パターン管理 E2E テスト
 *
 * US-MST-007: 自動仕訳パターン登録
 * US-MST-008: 自動仕訳パターン編集
 *
 * 受入条件（US-MST-007）:
 * - パターンコード、パターン名、ソーステーブル名を入力して登録できる
 * - 明細行を追加して借方・貸方パターンを定義できる
 * - 登録成功時、確認メッセージが表示される
 *
 * 受入条件（US-MST-008）:
 * - パターン名、ソーステーブル名、説明を編集できる
 * - 明細行を追加・削除して更新できる
 * - 有効/無効を切り替えられる
 * - 編集成功時、確認メッセージが表示される
 */
describe('US-MST-007/008: 自動仕訳パターン管理', () => {
  beforeEach(() => {
    cy.clearAuth();
  });

  describe('一覧表示', () => {
    beforeEach(() => {
      cy.login('admin', 'Password123!');
      cy.get('[data-testid="dashboard"]').should('be.visible');
      cy.visit('/master/auto-journal-patterns');
      cy.get('[data-testid="auto-journal-pattern-list-page"]', { timeout: 15000 }).should(
        'be.visible'
      );
    });

    it('自動仕訳パターン一覧が表示される', () => {
      // Then: ページタイトルが表示される
      cy.contains('h1', '自動仕訳パターン一覧').should('be.visible');
    });

    it('新規登録ボタンが表示される', () => {
      // Then: 新規登録ボタンが表示される
      cy.contains('button', '新規登録').should('be.visible');
    });
  });

  describe('新規登録（US-MST-007）', () => {
    beforeEach(() => {
      cy.login('admin', 'Password123!');
      cy.get('[data-testid="dashboard"]').should('be.visible');
      cy.visit('/master/auto-journal-patterns/new');
      cy.get('[data-testid="create-auto-journal-pattern-page"]', { timeout: 15000 }).should(
        'be.visible'
      );
    });

    it('新規登録フォームが表示される', () => {
      // Then: フォームフィールドが表示される
      cy.get('[data-testid="pattern-code-input"]').should('be.visible');
      cy.get('[data-testid="pattern-name-input"]').should('be.visible');
      cy.get('[data-testid="source-table-input"]').should('be.visible');
      cy.get('[data-testid="description-input"]').should('be.visible');
      cy.get('[data-testid="add-item-button"]').should('be.visible');
      cy.get('[data-testid="create-pattern-submit"]').should('be.visible');
    });

    it('明細行が 1 行表示されている', () => {
      // Then: 初期状態で 1 行の明細行がある
      cy.get('[data-testid="item-row-0"]').should('be.visible');
    });

    it('管理者がパターンを登録できる（明細 2 行）', () => {
      // When: パターン情報を入力
      cy.get('[data-testid="pattern-code-input"]').clear().type('TEST001');
      cy.get('[data-testid="pattern-name-input"]').clear().type('テスト売上パターン');
      cy.get('[data-testid="source-table-input"]').clear().type('sales');
      cy.get('[data-testid="description-input"]').clear().type('テスト用の自動仕訳パターン');

      // When: 明細 1 行目を入力（借方）
      cy.get('[data-testid="item-row-0"]').within(() => {
        cy.get('select').select('D');
        cy.get('input[placeholder*="勘定科目"]').clear().type('1100');
        cy.get('input[placeholder*="計算式"]').clear().type('amount');
        cy.get('input[placeholder*="摘要"]').clear().type('売上 {id}');
      });

      // When: 明細 2 行目を追加（貸方）
      cy.get('[data-testid="add-item-button"]').click();
      cy.get('[data-testid="item-row-1"]').within(() => {
        cy.get('select').select('C');
        cy.get('input[placeholder*="勘定科目"]').clear().type('4100');
        cy.get('input[placeholder*="計算式"]').clear().type('amount');
        cy.get('input[placeholder*="摘要"]').clear().type('売上 {id}');
      });

      // When: 登録ボタンをクリック
      cy.get('[data-testid="create-pattern-submit"]').click();

      // Then: 成功メッセージが表示される
      cy.contains('自動仕訳パターン登録が完了しました', { timeout: 10000 }).should('be.visible');
    });

    it('重複コードで登録するとエラーが表示される', () => {
      // Given: 同じコードで再度登録を試みる
      cy.get('[data-testid="pattern-code-input"]').clear().type('TEST001');
      cy.get('[data-testid="pattern-name-input"]').clear().type('重複テスト');
      cy.get('[data-testid="source-table-input"]').clear().type('sales');

      cy.get('[data-testid="item-row-0"]').within(() => {
        cy.get('select').select('D');
        cy.get('input[placeholder*="勘定科目"]').clear().type('1100');
        cy.get('input[placeholder*="計算式"]').clear().type('amount');
      });

      cy.get('[data-testid="create-pattern-submit"]').click();

      // Then: エラーメッセージが表示される
      cy.contains('既に使用されています', { timeout: 10000 }).should('be.visible');
    });

    it('必須フィールドが空の場合バリデーションエラーが表示される', () => {
      // When: 空のまま送信
      cy.get('[data-testid="pattern-code-input"]').clear();
      cy.get('[data-testid="pattern-name-input"]').clear();
      cy.get('[data-testid="source-table-input"]').clear();
      cy.get('[data-testid="create-pattern-submit"]').click();

      // Then: バリデーションエラーが表示される
      cy.contains('パターンコードを入力してください').should('be.visible');
    });
  });

  describe('編集（US-MST-008）', () => {
    beforeEach(() => {
      cy.login('admin', 'Password123!');
      cy.get('[data-testid="dashboard"]').should('be.visible');
      cy.visit('/master/auto-journal-patterns');
      cy.get('[data-testid="auto-journal-pattern-list-page"]', { timeout: 15000 }).should(
        'be.visible'
      );
      // データが存在することを確認
      cy.get('table tbody tr').should('have.length.at.least', 1);
    });

    it('一覧の編集ボタンから編集ページに遷移できる', () => {
      // When: 編集ボタンをクリック
      cy.get('table tbody tr').first().contains('button', '編集').click();

      // Then: 編集ページが表示される
      cy.get('[data-testid="edit-auto-journal-pattern-page"]', { timeout: 10000 }).should(
        'be.visible'
      );
      cy.contains('h1', '自動仕訳パターン編集').should('be.visible');
    });

    it('パターンコードは変更できない', () => {
      // Given: 編集ページに遷移
      cy.get('table tbody tr').first().contains('button', '編集').click();
      cy.get('[data-testid="edit-auto-journal-pattern-page"]', { timeout: 10000 }).should(
        'be.visible'
      );

      // Then: パターンコードは disabled
      cy.get('[data-testid="pattern-code-input"]').should('be.disabled');
      cy.get('[data-testid="pattern-code-input"]').invoke('val').should('not.be.empty');
    });

    it('既存パターンを編集できる', () => {
      // Given: 編集ページに遷移
      cy.get('table tbody tr').first().contains('button', '編集').click();
      cy.get('[data-testid="edit-auto-journal-pattern-page"]', { timeout: 10000 }).should(
        'be.visible'
      );

      // When: パターン名を変更して更新
      cy.get('[data-testid="pattern-name-input"]').clear().type('更新済みパターン');
      cy.get('[data-testid="edit-pattern-submit"]').click();

      // Then: 一覧ページに遷移し、成功メッセージが表示される
      cy.url({ timeout: 10000 }).should('include', '/master/auto-journal-patterns');
      cy.url().should('not.include', '/edit');
      cy.contains('自動仕訳パターンを更新しました', { timeout: 10000 }).should('be.visible');
    });

    it('明細行を追加して更新できる', () => {
      // Given: 編集ページに遷移
      cy.get('table tbody tr').first().contains('button', '編集').click();
      cy.get('[data-testid="edit-auto-journal-pattern-page"]', { timeout: 10000 }).should(
        'be.visible'
      );

      // When: 行を追加
      cy.get('[data-testid="add-item-button"]').click();

      // When: 新しい行を入力
      cy.get('[data-testid^="item-row-"]').last().within(() => {
        cy.get('select').select('C');
        cy.get('input[placeholder*="勘定科目"]').clear().type('5100');
        cy.get('input[placeholder*="計算式"]').clear().type('tax_amount');
      });

      // When: 更新
      cy.get('[data-testid="edit-pattern-submit"]').click();

      // Then: 一覧ページに遷移
      cy.url({ timeout: 10000 }).should('include', '/master/auto-journal-patterns');
      cy.url().should('not.include', '/edit');
    });
  });

  describe('削除', () => {
    beforeEach(() => {
      cy.login('admin', 'Password123!');
      cy.get('[data-testid="dashboard"]').should('be.visible');
      cy.visit('/master/auto-journal-patterns');
      cy.get('[data-testid="auto-journal-pattern-list-page"]', { timeout: 15000 }).should(
        'be.visible'
      );
      cy.get('table tbody tr').should('have.length.at.least', 1);
    });

    it('パターンを削除できる', () => {
      // When: 削除ボタンをクリックし確認ダイアログで OK
      cy.on('window:confirm', () => true);
      cy.get('table tbody tr').first().contains('button', '削除').click();

      // Then: 成功メッセージが表示される
      cy.contains('削除しました', { timeout: 10000 }).should('be.visible');
    });

    it('削除確認ダイアログでキャンセルした場合、削除されない', () => {
      // Given: 行数を記録
      cy.get('table tbody tr').then(($rows) => {
        const rowCount = $rows.length;

        // When: 削除ボタンをクリックし確認ダイアログでキャンセル
        cy.on('window:confirm', () => false);
        cy.get('table tbody tr').first().contains('button', '削除').click();

        // Then: 行数が変わらない
        cy.get('table tbody tr').should('have.length', rowCount);
      });
    });
  });

  describe('アクセス制御', () => {
    it('一般ユーザーは自動仕訳パターンページにアクセスできない', () => {
      // Given: 一般ユーザーでログイン
      cy.login('user', 'Password123!');
      cy.get('[data-testid="dashboard"]').should('be.visible');

      // When: 自動仕訳パターンページに直接アクセス
      cy.visit('/master/auto-journal-patterns');

      // Then: ダッシュボードにリダイレクトされる
      cy.url().should('not.include', '/master/auto-journal-patterns');
      cy.get('[data-testid="dashboard"]').should('be.visible');
    });

    it('未認証ユーザーは自動仕訳パターンページにアクセスできない', () => {
      // When: 未認証で直接アクセス
      cy.visit('/master/auto-journal-patterns');

      // Then: ログインページにリダイレクトされる
      cy.url().should('include', '/login');
    });

    it('経理責任者は自動仕訳パターンページにアクセスできる', () => {
      // Given: 経理責任者でログイン
      cy.login('manager', 'Password123!');
      cy.get('[data-testid="dashboard"]').should('be.visible');

      // When: 自動仕訳パターンページにアクセス
      cy.visit('/master/auto-journal-patterns');

      // Then: 一覧が表示される
      cy.get('[data-testid="auto-journal-pattern-list-page"]', { timeout: 15000 }).should(
        'be.visible'
      );
    });
  });
});
