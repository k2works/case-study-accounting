/**
 * 日次残高照会 E2E テスト
 *
 * US-LDG-003: 日次残高照会
 *
 * 受入条件:
 * - 勘定科目を選択して日次残高を表示できる
 * - 日付、借方合計、貸方合計、残高が表示される
 * - 期間を指定して絞り込みできる
 * - 残高の推移をグラフで表示できる
 */

describe('US-LDG-003: 日次残高照会', () => {
  beforeEach(() => {
    cy.clearAuth();
  });

  describe('画面表示', () => {
    beforeEach(() => {
      cy.login('admin', 'Password123!');
      cy.get('[data-testid="dashboard"]', { timeout: 15000 }).should('be.visible');
      cy.visit('/ledger/daily-balance');
      cy.get('[data-testid="daily-balance-page"]', { timeout: 15000 }).should('be.visible');
    });

    it('日次残高照会ページが表示される', () => {
      // Given: ログイン済み

      // Then: ページタイトルが表示される
      cy.contains('h1', '日次残高照会').should('be.visible');

      // Then: フィルタUIが表示される
      cy.get('[data-testid="daily-balance-filter"]').should('be.visible');
      cy.get('#daily-balance-filter-account').should('be.visible');
      cy.get('#daily-balance-filter-date-from').should('be.visible');
      cy.get('#daily-balance-filter-date-to').should('be.visible');
      cy.contains('button', '照会').should('be.visible');
    });

    it('勘定科目ドロップダウンに選択肢が表示される', () => {
      // Given: 日次残高ページが表示されている

      // Then: 勘定科目の選択肢が読み込まれる
      cy.get('#daily-balance-filter-account option', { timeout: 15000 }).should(
        'have.length.greaterThan',
        1
      );

      // Then: プレースホルダが表示される
      cy.get('#daily-balance-filter-account').contains('option', '勘定科目を選択').should('exist');
    });
  });

  describe('勘定科目を選択して日次残高を表示できる', () => {
    beforeEach(() => {
      cy.login('admin', 'Password123!');
      cy.get('[data-testid="dashboard"]', { timeout: 15000 }).should('be.visible');
      cy.visit('/ledger/daily-balance');
      cy.get('[data-testid="daily-balance-page"]', { timeout: 15000 }).should('be.visible');
    });

    it('勘定科目を選択すると日次残高が表示される', () => {
      // Given: 日次残高ページが表示されている

      // When: 勘定科目を選択
      cy.get('#daily-balance-filter-account option', { timeout: 15000 }).should(
        'have.length.greaterThan',
        1
      );
      cy.get('#daily-balance-filter-account').select(1);

      // Then: 日次残高データが表示される
      cy.get('[data-testid="daily-balance-summary"]', { timeout: 15000 }).should('be.visible');
      cy.get('[data-testid="daily-balance-table"]', { timeout: 15000 }).should('be.visible');
    });

    it('日付、借方合計、貸方合計、残高が表示される', () => {
      // Given: 日次残高ページが表示されている

      // When: 勘定科目を選択
      cy.get('#daily-balance-filter-account option', { timeout: 15000 }).should(
        'have.length.greaterThan',
        1
      );
      cy.get('#daily-balance-filter-account').select(1);

      // Then: テーブルヘッダーに必要なカラムが表示される
      cy.get('[data-testid="daily-balance-table"]', { timeout: 15000 }).should('be.visible');
      cy.contains('th', '日付').should('be.visible');
      cy.contains('th', '借方合計').should('be.visible');
      cy.contains('th', '貸方合計').should('be.visible');
      cy.contains('th', '残高').should('be.visible');
    });

    it('サマリ情報（期首残高、借方合計、貸方合計、期末残高）が表示される', () => {
      // Given: 日次残高ページが表示されている

      // When: 勘定科目を選択
      cy.get('#daily-balance-filter-account option', { timeout: 15000 }).should(
        'have.length.greaterThan',
        1
      );
      cy.get('#daily-balance-filter-account').select(1);

      // Then: サマリ情報が表示される
      cy.get('[data-testid="daily-balance-summary"]', { timeout: 15000 }).should('be.visible');
      cy.get('[data-testid="daily-balance-summary"]').should('contain', '期首残高');
      cy.get('[data-testid="daily-balance-summary"]').should('contain', '借方合計');
      cy.get('[data-testid="daily-balance-summary"]').should('contain', '貸方合計');
      cy.get('[data-testid="daily-balance-summary"]').should('contain', '期末残高');
    });
  });

  describe('期間を指定して絞り込みできる', () => {
    beforeEach(() => {
      cy.login('admin', 'Password123!');
      cy.get('[data-testid="dashboard"]', { timeout: 15000 }).should('be.visible');
      cy.visit('/ledger/daily-balance');
      cy.get('[data-testid="daily-balance-page"]', { timeout: 15000 }).should('be.visible');
    });

    it('開始日と終了日を指定してフィルタリングできる', () => {
      // Given: 勘定科目を選択してテーブルが表示されるのを待つ
      cy.get('#daily-balance-filter-account option', { timeout: 15000 }).should(
        'have.length.greaterThan',
        1
      );
      cy.get('#daily-balance-filter-account').select(1);
      cy.wait(1000);
      cy.get('[data-testid="daily-balance-table"]', { timeout: 15000 }).should('be.visible');

      // When: 期間を指定して照会
      cy.get('#daily-balance-filter-date-from').clear().type('2024-04-01');
      cy.get('#daily-balance-filter-date-to').clear().type('2024-04-30');
      cy.contains('button', '照会').click();

      // Then: フィルタリングが実行される
      cy.wait(1000);
      cy.get('[data-testid="daily-balance-table"]', { timeout: 15000 }).should('be.visible');
    });

    it('開始日のみ指定してフィルタリングできる', () => {
      // Given: 勘定科目を選択してテーブルが表示されるのを待つ
      cy.get('#daily-balance-filter-account option', { timeout: 15000 }).should(
        'have.length.greaterThan',
        1
      );
      cy.get('#daily-balance-filter-account').select(1);
      cy.wait(1000);
      cy.get('[data-testid="daily-balance-table"]', { timeout: 15000 }).should('be.visible');

      // When: 開始日のみ指定
      cy.get('#daily-balance-filter-date-from').clear().type('2024-01-01');
      cy.contains('button', '照会').click();

      // Then: フィルタリングが実行される
      cy.wait(1000);
      cy.get('[data-testid="daily-balance-table"]', { timeout: 15000 }).should('be.visible');
    });

    it('終了日のみ指定してフィルタリングできる', () => {
      // Given: 勘定科目を選択してテーブルが表示されるのを待つ
      cy.get('#daily-balance-filter-account option', { timeout: 15000 }).should(
        'have.length.greaterThan',
        1
      );
      cy.get('#daily-balance-filter-account').select(1);
      cy.wait(1000);
      cy.get('[data-testid="daily-balance-table"]', { timeout: 15000 }).should('be.visible');

      // When: 終了日のみ指定
      cy.get('#daily-balance-filter-date-to').clear().type('2024-12-31');
      cy.contains('button', '照会').click();

      // Then: フィルタリングが実行される
      cy.wait(1000);
      cy.get('[data-testid="daily-balance-table"]', { timeout: 15000 }).should('be.visible');
    });
  });

  describe('残高の推移をグラフで表示できる', () => {
    beforeEach(() => {
      cy.login('admin', 'Password123!');
      cy.get('[data-testid="dashboard"]', { timeout: 15000 }).should('be.visible');
      cy.visit('/ledger/daily-balance');
      cy.get('[data-testid="daily-balance-page"]', { timeout: 15000 }).should('be.visible');
    });

    it('勘定科目を選択すると残高推移グラフが表示される', () => {
      // Given: 日次残高ページが表示されている

      // When: 勘定科目を選択
      cy.get('#daily-balance-filter-account option', { timeout: 15000 }).should(
        'have.length.greaterThan',
        1
      );
      cy.get('#daily-balance-filter-account').select(1);

      // Then: グラフが表示される
      cy.get('[data-testid="daily-balance-chart"]', { timeout: 15000 }).should('be.visible');
    });

    it('グラフにはRechartsのLineChartが含まれる', () => {
      // Given: 日次残高ページが表示されている

      // When: 勘定科目を選択
      cy.get('#daily-balance-filter-account option', { timeout: 15000 }).should(
        'have.length.greaterThan',
        1
      );
      cy.get('#daily-balance-filter-account').select(1);

      // Then: グラフが表示され、SVG要素が含まれる（Rechartsはcanvasではなくsvgを使用）
      cy.get('[data-testid="daily-balance-chart"]', { timeout: 15000 }).should('be.visible');
      cy.get('[data-testid="daily-balance-chart"] svg').should('exist');
    });
  });

  describe('アクセス制御', () => {
    it('一般ユーザーも日次残高ページにアクセスできる', () => {
      // Given: 一般ユーザーでログイン
      cy.login('user', 'Password123!');
      cy.get('[data-testid="dashboard"]', { timeout: 15000 }).should('be.visible');

      // When: 日次残高ページにアクセス
      cy.visit('/ledger/daily-balance');

      // Then: 日次残高ページが表示される
      cy.get('[data-testid="daily-balance-page"]', { timeout: 15000 }).should('be.visible');
      cy.get('[data-testid="daily-balance-filter"]').should('be.visible');
    });

    it('未認証ユーザーは日次残高ページにアクセスできない', () => {
      // Given: 未認証状態

      // When: 日次残高ページに直接アクセス
      cy.visit('/ledger/daily-balance');

      // Then: ログインページにリダイレクトされる
      cy.url().should('include', '/login');
      cy.get('[data-testid="login-page"]', { timeout: 15000 }).should('be.visible');
    });

    it('経理責任者も日次残高ページにアクセスできる', () => {
      // Given: 経理責任者でログイン
      cy.login('manager', 'Password123!');
      cy.get('[data-testid="dashboard"]', { timeout: 15000 }).should('be.visible');

      // When: 日次残高ページにアクセス
      cy.visit('/ledger/daily-balance');

      // Then: 日次残高ページが表示される
      cy.get('[data-testid="daily-balance-page"]', { timeout: 15000 }).should('be.visible');
    });
  });

  describe('エラーハンドリング', () => {
    beforeEach(() => {
      cy.login('admin', 'Password123!');
      cy.get('[data-testid="dashboard"]', { timeout: 15000 }).should('be.visible');
      cy.visit('/ledger/daily-balance');
      cy.get('[data-testid="daily-balance-page"]', { timeout: 15000 }).should('be.visible');
    });

    it('勘定科目を選択せずに照会するとエラーメッセージが表示される', () => {
      // Given: 勘定科目が選択されていない

      // When: 照会ボタンをクリック
      cy.contains('button', '照会').click();

      // Then: エラーメッセージが表示される
      cy.contains('勘定科目を選択してください').should('be.visible');
    });
  });
});
