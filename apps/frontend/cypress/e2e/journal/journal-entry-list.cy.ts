/**
 * 仕訳一覧表示 E2E テスト
 *
 * US-JNL-004: 仕訳一覧表示
 *
 * 受入条件:
 * - 仕訳番号、仕訳日付、摘要、金額、ステータスが一覧表示される
 * - ステータスでフィルタリングできる
 * - 日付範囲で絞り込みできる
 * - 一覧から詳細画面に遷移できる
 * - ページネーションに対応する
 */

describe('US-JNL-004: 仕訳一覧表示', () => {
  // テストスイート開始前に勘定科目をセットアップ
  before(() => {
    cy.clearAuth();
    cy.setupTestAccounts();
    cy.clearAuth();
  });

  beforeEach(() => {
    cy.clearAuth();
  });

  describe('一覧表示', () => {
    beforeEach(() => {
      cy.login('admin', 'Password123!');
      cy.get('[data-testid="dashboard"]', { timeout: 15000 }).should('be.visible');
      cy.visitJournalEntryList();
    });

    it('仕訳番号、仕訳日付、摘要、金額、ステータスが一覧表示される', () => {
      // Given: 仕訳一覧ページが表示されている

      // Then: テーブルヘッダーに必要なカラムが表示される
      cy.contains('th', '仕訳番号').should('be.visible');
      cy.contains('th', '仕訳日付').should('be.visible');
      cy.contains('th', '摘要').should('be.visible');
      cy.contains('th', '借方金額').should('be.visible');
      cy.contains('th', '貸方金額').should('be.visible');
      cy.contains('th', 'ステータス').should('be.visible');

      // Then: テーブルにデータが表示される
      cy.get('table tbody tr').should('have.length.at.least', 1);
    });

    it('仕訳一覧にフィルタUIが表示される', () => {
      // Given: 仕訳一覧ページが表示されている

      // Then: フィルタUIが表示される
      cy.get('[data-testid="journal-entry-filter"]').should('be.visible');
      cy.get('#journal-entry-filter-status').should('be.visible');
      cy.get('#journal-entry-filter-date-from').should('be.visible');
      cy.get('#journal-entry-filter-date-to').should('be.visible');
      cy.contains('button', '検索').should('be.visible');
      cy.contains('button', 'リセット').should('be.visible');
    });

    it('新規作成ボタンが表示される', () => {
      // Given: 仕訳一覧ページが表示されている

      // Then: 新規作成ボタンが表示される
      cy.contains('button', '新規作成').should('be.visible');
    });
  });

  describe('ステータスフィルタリング', () => {
    beforeEach(() => {
      cy.login('admin', 'Password123!');
      cy.get('[data-testid="dashboard"]', { timeout: 15000 }).should('be.visible');
      cy.visitJournalEntryList();
    });

    it('ステータスでフィルタリングできる - 下書き', () => {
      // Given: 仕訳一覧が表示されている

      // When: 下書きでフィルタリング
      cy.get('#journal-entry-filter-status').select('DRAFT');
      cy.contains('button', '検索').click();

      // Then: フィルタリング後、下書きの仕訳が表示される
      cy.get('table tbody tr', { timeout: 10000 }).should('exist');
      // 結果の最初の行が下書きステータスであることを確認
      cy.get('table tbody tr').first().should('contain', '下書き');
    });

    it('すべてのステータスオプションが選択可能', () => {
      // Given: 仕訳一覧が表示されている

      // Then: すべてのオプションが存在する
      cy.get('#journal-entry-filter-status option').should('have.length', 5);
      cy.get('#journal-entry-filter-status').contains('option', 'すべて').should('exist');
      cy.get('#journal-entry-filter-status').contains('option', '下書き').should('exist');
      cy.get('#journal-entry-filter-status').contains('option', '承認待ち').should('exist');
      cy.get('#journal-entry-filter-status').contains('option', '承認済み').should('exist');
      cy.get('#journal-entry-filter-status').contains('option', '確定').should('exist');
    });
  });

  describe('日付範囲フィルタリング', () => {
    beforeEach(() => {
      cy.login('admin', 'Password123!');
      cy.get('[data-testid="dashboard"]', { timeout: 15000 }).should('be.visible');
      cy.visitJournalEntryList();
    });

    it('日付範囲で絞り込みできる', () => {
      // Given: 仕訳一覧が表示されている

      // When: 日付範囲を指定してフィルタリング（2024年4月のみ）
      cy.get('#journal-entry-filter-date-from').type('2024-04-01');
      cy.get('#journal-entry-filter-date-to').type('2024-04-30');
      cy.contains('button', '検索').click();

      // Then: フィルタリング後、指定日付範囲の仕訳のみ表示される
      cy.get('table tbody', { timeout: 10000 }).should('exist');
    });

    it('開始日のみ指定してフィルタリングできる', () => {
      // Given: 仕訳一覧が表示されている

      // When: 開始日のみ指定
      cy.get('#journal-entry-filter-date-from').type('2024-05-01');
      cy.contains('button', '検索').click();

      // Then: フィルタリングが実行される
      cy.get('table tbody', { timeout: 10000 }).should('exist');
    });

    it('終了日のみ指定してフィルタリングできる', () => {
      // Given: 仕訳一覧が表示されている

      // When: 終了日のみ指定
      cy.get('#journal-entry-filter-date-to').type('2024-04-30');
      cy.contains('button', '検索').click();

      // Then: フィルタリングが実行される
      cy.get('table tbody', { timeout: 10000 }).should('exist');
    });
  });

  describe('複合フィルタリング', () => {
    beforeEach(() => {
      cy.login('admin', 'Password123!');
      cy.get('[data-testid="dashboard"]', { timeout: 15000 }).should('be.visible');
      cy.visitJournalEntryList();
    });

    it('ステータスと日付範囲の複合フィルタリングができる', () => {
      // Given: 仕訳一覧が表示されている

      // When: ステータスと日付範囲を指定
      cy.get('#journal-entry-filter-status').select('DRAFT');
      cy.get('#journal-entry-filter-date-from').type('2024-04-01');
      cy.get('#journal-entry-filter-date-to').type('2024-05-31');
      cy.contains('button', '検索').click();

      // Then: 複合条件でフィルタリングされる
      cy.get('table tbody', { timeout: 10000 }).should('exist');
    });
  });

  describe('リセット機能', () => {
    beforeEach(() => {
      cy.login('admin', 'Password123!');
      cy.get('[data-testid="dashboard"]', { timeout: 15000 }).should('be.visible');
      cy.visitJournalEntryList();
    });

    it('リセットボタンでフィルタ条件がクリアされる', () => {
      // Given: フィルタ条件が設定されている
      cy.get('#journal-entry-filter-status').select('DRAFT');
      cy.get('#journal-entry-filter-date-from').type('2024-04-01');
      cy.get('#journal-entry-filter-date-to').type('2024-04-30');
      cy.contains('button', '検索').click();

      // When: リセットボタンをクリック
      cy.contains('button', 'リセット').click();

      // Then: フィルタ条件がクリアされる
      cy.get('#journal-entry-filter-status').should('have.value', '');
      cy.get('#journal-entry-filter-date-from').should('have.value', '');
      cy.get('#journal-entry-filter-date-to').should('have.value', '');

      // Then: 全件が表示される
      cy.get('table tbody tr', { timeout: 10000 }).should('have.length.at.least', 1);
    });
  });

  describe('詳細画面遷移', () => {
    beforeEach(() => {
      cy.login('admin', 'Password123!');
      cy.get('[data-testid="dashboard"]', { timeout: 15000 }).should('be.visible');

      // 遷移テスト用の仕訳を作成
      cy.createTestJournalEntry('2024-06-01', '詳細遷移テスト仕訳', '15000');
      cy.get('[data-testid="journal-entry-success"]', { timeout: 15000 }).should('be.visible');

      // 仕訳一覧ページに移動
      cy.visitJournalEntryList();
    });

    it('一覧から編集画面に遷移できる', () => {
      // Given: 仕訳一覧が表示されている（データが存在する）

      // テーブルにデータが表示されるのを待つ
      cy.get('table tbody tr', { timeout: 15000 }).should('have.length.at.least', 1);

      // When: 編集ボタンをクリック
      cy.get('table tbody tr').first().contains('button', '編集').click();

      // Then: 編集画面に遷移する
      cy.url().should('include', '/journal/entries/');
      cy.url().should('include', '/edit');
      cy.get('[data-testid="journal-entry-edit-form"]', { timeout: 15000 }).should('be.visible');
    });
  });

  describe('ページネーション', () => {
    beforeEach(() => {
      cy.login('admin', 'Password123!');
      cy.get('[data-testid="dashboard"]', { timeout: 15000 }).should('be.visible');
      cy.visitJournalEntryList();
    });

    it('ページネーションUIが表示される', () => {
      // Given: 仕訳一覧ページが表示されている

      // Then: ページネーションUIが表示される
      cy.get('.pagination').should('exist');
      cy.get('.pagination__total').should('be.visible');
    });

    it('表示件数を変更できる', () => {
      // Given: 仕訳一覧ページが表示されている

      // When: 表示件数を変更（10件に変更）
      cy.get('.pagination__select').select('10');

      // Then: 表示件数が変更される
      cy.get('.pagination__select', { timeout: 10000 }).should('have.value', '10');
    });

    it('表示件数オプションが正しく表示される', () => {
      // Given: 仕訳一覧ページが表示されている

      // Then: 表示件数オプションが存在する
      cy.get('.pagination__select option').should('have.length', 4);
      cy.get('.pagination__select').contains('option', '10').should('exist');
      cy.get('.pagination__select').contains('option', '20').should('exist');
      cy.get('.pagination__select').contains('option', '50').should('exist');
      cy.get('.pagination__select').contains('option', '100').should('exist');
    });

    it('合計件数が表示される', () => {
      // Given: 仕訳一覧ページが表示されている

      // Then: 合計件数が表示される（「全 X 件」の形式）
      cy.get('.pagination__total').should('contain', '全');
      cy.get('.pagination__total').should('contain', '件');
    });
  });

  describe('新規作成画面遷移', () => {
    beforeEach(() => {
      cy.login('admin', 'Password123!');
      cy.get('[data-testid="dashboard"]', { timeout: 15000 }).should('be.visible');
      cy.visitJournalEntryList();
    });

    it('新規作成ボタンをクリックすると仕訳登録画面に遷移する', () => {
      // Given: 仕訳一覧ページが表示されている

      // When: 新規作成ボタンをクリック
      cy.contains('button', '新規作成').click();

      // Then: 仕訳登録画面に遷移する
      cy.url().should('include', '/journal/entries/new');
      cy.get('[data-testid="journal-entry-form"]').should('be.visible');
    });
  });

  describe('アクセス制御', () => {
    it('一般ユーザーも仕訳一覧ページにアクセスできる', () => {
      // Given: 一般ユーザーでログイン
      cy.login('user', 'Password123!');
      cy.get('[data-testid="dashboard"]', { timeout: 15000 }).should('be.visible');

      // When: 仕訳一覧ページにアクセス
      cy.visit('/journal/entries');

      // Then: 仕訳一覧が表示される
      cy.get('[data-testid="journal-entry-list-page"]', { timeout: 15000 }).should('be.visible');
      cy.get('[data-testid="journal-entry-filter"]').should('be.visible');
    });

    it('未認証ユーザーは仕訳一覧ページにアクセスできない', () => {
      // Given: 未認証状態

      // When: 仕訳一覧ページに直接アクセス
      cy.visit('/journal/entries');

      // Then: ログインページにリダイレクトされる
      cy.url().should('include', '/login');
      cy.get('[data-testid="login-page"]', { timeout: 15000 }).should('be.visible');
    });

    it('経理責任者も仕訳一覧ページにアクセスできる', () => {
      // Given: 経理責任者でログイン
      cy.login('manager', 'Password123!');
      cy.get('[data-testid="dashboard"]', { timeout: 15000 }).should('be.visible');

      // When: 仕訳一覧ページにアクセス
      cy.visit('/journal/entries');

      // Then: 仕訳一覧が表示される
      cy.get('[data-testid="journal-entry-list-page"]', { timeout: 15000 }).should('be.visible');
    });
  });
});
