/**
 * 総勘定元帳照会 E2E テスト
 *
 * US-LDG-001: 総勘定元帳照会
 *
 * 受入条件:
 * - 勘定科目を選択して元帳を表示できる
 * - 日付、仕訳番号、摘要、借方金額、貸方金額、残高が表示される
 * - 期間を指定して絞り込みできる
 * - 仕訳の詳細画面に遷移できる
 */

describe('US-LDG-001: 総勘定元帳照会', () => {
  // MSW モードでは勘定科目はモックデータで提供されるため、setupTestAccounts は不要
  beforeEach(() => {
    cy.clearAuth();
  });

  describe('画面表示', () => {
    beforeEach(() => {
      cy.login('admin', 'Password123!');
      cy.get('[data-testid="dashboard"]', { timeout: 15000 }).should('be.visible');
      cy.visit('/general-ledger');
      cy.get('[data-testid="general-ledger-page"]', { timeout: 15000 }).should('be.visible');
    });

    it('総勘定元帳照会ページが表示される', () => {
      // Given: ログイン済み

      // Then: ページタイトルが表示される
      cy.contains('h1', '総勘定元帳照会').should('be.visible');

      // Then: フィルタUIが表示される
      cy.get('[data-testid="general-ledger-filter"]').should('be.visible');
      cy.get('#general-ledger-filter-account').should('be.visible');
      cy.get('#general-ledger-filter-date-from').should('be.visible');
      cy.get('#general-ledger-filter-date-to').should('be.visible');
      cy.contains('button', '照会').should('be.visible');
    });

    it('勘定科目ドロップダウンに選択肢が表示される', () => {
      // Given: 総勘定元帳ページが表示されている

      // Then: 勘定科目の選択肢が読み込まれる
      cy.get('#general-ledger-filter-account option', { timeout: 15000 }).should(
        'have.length.greaterThan',
        1
      );

      // Then: プレースホルダが表示される
      cy.get('#general-ledger-filter-account').contains('option', '勘定科目を選択').should('exist');
    });
  });

  describe('勘定科目を選択して元帳を表示できる', () => {
    beforeEach(() => {
      cy.login('admin', 'Password123!');
      cy.get('[data-testid="dashboard"]', { timeout: 15000 }).should('be.visible');

      // 総勘定元帳ページに移動（MSW モックデータを使用）
      cy.visit('/general-ledger');
      cy.get('[data-testid="general-ledger-page"]', { timeout: 15000 }).should('be.visible');
    });

    it('勘定科目を選択すると元帳が表示される', () => {
      // Given: 総勘定元帳ページが表示されている

      // When: 勘定科目を選択
      cy.get('#general-ledger-filter-account option', { timeout: 15000 }).should(
        'have.length.greaterThan',
        1
      );
      cy.get('#general-ledger-filter-account').select(1); // 最初の勘定科目を選択

      // Then: 元帳データが表示される
      cy.get('[data-testid="general-ledger-summary"]', { timeout: 15000 }).should('be.visible');
      cy.get('[data-testid="general-ledger-table"]', { timeout: 15000 }).should('be.visible');
    });

    it('日付、仕訳番号、摘要、借方金額、貸方金額、残高が表示される', () => {
      // Given: 総勘定元帳ページが表示されている

      // When: 勘定科目を選択
      cy.get('#general-ledger-filter-account option', { timeout: 15000 }).should(
        'have.length.greaterThan',
        1
      );
      cy.get('#general-ledger-filter-account').select(1);

      // Then: テーブルヘッダーに必要なカラムが表示される
      cy.get('[data-testid="general-ledger-table"]', { timeout: 15000 }).should('be.visible');
      cy.contains('th', '日付').should('be.visible');
      cy.contains('th', '仕訳番号').should('be.visible');
      cy.contains('th', '摘要').should('be.visible');
      cy.contains('th', '借方').should('be.visible');
      cy.contains('th', '貸方').should('be.visible');
      cy.contains('th', '残高').should('be.visible');
    });

    it('サマリ情報（前期繰越、借方合計、貸方合計、期末残高）が表示される', () => {
      // Given: 総勘定元帳ページが表示されている

      // When: 勘定科目を選択
      cy.get('#general-ledger-filter-account option', { timeout: 15000 }).should(
        'have.length.greaterThan',
        1
      );
      cy.get('#general-ledger-filter-account').select(1);

      // Then: サマリ情報が表示される
      cy.get('[data-testid="general-ledger-summary"]', { timeout: 15000 }).should('be.visible');
      cy.get('[data-testid="general-ledger-summary"]').should('contain', '前期繰越');
      cy.get('[data-testid="general-ledger-summary"]').should('contain', '借方合計');
      cy.get('[data-testid="general-ledger-summary"]').should('contain', '貸方合計');
      cy.get('[data-testid="general-ledger-summary"]').should('contain', '期末残高');
    });
  });

  describe('期間を指定して絞り込みできる', () => {
    beforeEach(() => {
      cy.login('admin', 'Password123!');
      cy.get('[data-testid="dashboard"]', { timeout: 15000 }).should('be.visible');
      cy.visit('/general-ledger');
      cy.get('[data-testid="general-ledger-page"]', { timeout: 15000 }).should('be.visible');
    });

    it('開始日と終了日を指定してフィルタリングできる', () => {
      // Given: 勘定科目を選択してテーブルが表示されるのを待つ
      cy.get('#general-ledger-filter-account option', { timeout: 15000 }).should(
        'have.length.greaterThan',
        1
      );
      cy.get('#general-ledger-filter-account').select(1);
      cy.wait(1000); // データ取得を待つ
      cy.get('[data-testid="general-ledger-table"]', { timeout: 15000 }).should('be.visible');

      // When: 期間を指定して照会
      cy.get('#general-ledger-filter-date-from').clear().type('2024-04-01');
      cy.get('#general-ledger-filter-date-to').clear().type('2024-04-30');
      cy.contains('button', '照会').click();

      // Then: フィルタリングが実行される（ローディング後にテーブルが再表示される）
      cy.wait(1000); // 再取得を待つ
      cy.get('[data-testid="general-ledger-table"]', { timeout: 15000 }).should('be.visible');
    });

    it('開始日のみ指定してフィルタリングできる', () => {
      // Given: 勘定科目を選択してテーブルが表示されるのを待つ
      cy.get('#general-ledger-filter-account option', { timeout: 15000 }).should(
        'have.length.greaterThan',
        1
      );
      cy.get('#general-ledger-filter-account').select(1);
      cy.wait(1000);
      cy.get('[data-testid="general-ledger-table"]', { timeout: 15000 }).should('be.visible');

      // When: 開始日のみ指定
      cy.get('#general-ledger-filter-date-from').clear().type('2024-01-01');
      cy.contains('button', '照会').click();

      // Then: フィルタリングが実行される
      cy.wait(1000);
      cy.get('[data-testid="general-ledger-table"]', { timeout: 15000 }).should('be.visible');
    });

    it('終了日のみ指定してフィルタリングできる', () => {
      // Given: 勘定科目を選択してテーブルが表示されるのを待つ
      cy.get('#general-ledger-filter-account option', { timeout: 15000 }).should(
        'have.length.greaterThan',
        1
      );
      cy.get('#general-ledger-filter-account').select(1);
      cy.wait(1000);
      cy.get('[data-testid="general-ledger-table"]', { timeout: 15000 }).should('be.visible');

      // When: 終了日のみ指定
      cy.get('#general-ledger-filter-date-to').clear().type('2024-12-31');
      cy.contains('button', '照会').click();

      // Then: フィルタリングが実行される
      cy.wait(1000);
      cy.get('[data-testid="general-ledger-table"]', { timeout: 15000 }).should('be.visible');
    });
  });

  describe('仕訳の詳細画面に遷移できる', () => {
    beforeEach(() => {
      cy.login('admin', 'Password123!');
      cy.get('[data-testid="dashboard"]', { timeout: 15000 }).should('be.visible');

      // 総勘定元帳ページに移動（MSW モックデータを使用）
      cy.visit('/general-ledger');
      cy.get('[data-testid="general-ledger-page"]', { timeout: 15000 }).should('be.visible');
    });

    it('仕訳番号をクリックすると仕訳詳細画面に遷移する', () => {
      // Given: 勘定科目を選択してデータが表示されている
      cy.get('#general-ledger-filter-account option', { timeout: 15000 }).should(
        'have.length.greaterThan',
        1
      );
      cy.get('#general-ledger-filter-account').select(1);
      cy.wait(1000); // データ取得を待つ
      cy.get('[data-testid="general-ledger-table"]', { timeout: 15000 }).should('be.visible');

      // テーブルにデータが表示されるのを待つ
      cy.get('[data-testid="general-ledger-table"] table tbody tr', { timeout: 15000 })
        .should('have.length.at.least', 1);

      // When: 仕訳番号のリンクをクリック
      cy.get('[data-testid="general-ledger-table"] table tbody tr').first().find('a').first().click();

      // Then: 仕訳編集画面に遷移する
      cy.url().should('include', '/journal/entries/');
      cy.url().should('include', '/edit');
      cy.get('[data-testid="journal-entry-edit-form"]', { timeout: 15000 }).should('be.visible');
    });
  });

  describe('ページネーション', () => {
    beforeEach(() => {
      cy.login('admin', 'Password123!');
      cy.get('[data-testid="dashboard"]', { timeout: 15000 }).should('be.visible');
      cy.visit('/general-ledger');
      cy.get('[data-testid="general-ledger-page"]', { timeout: 15000 }).should('be.visible');
    });

    it('勘定科目を選択するとページネーションUIが表示される', () => {
      // Given: 勘定科目を選択
      cy.get('#general-ledger-filter-account option', { timeout: 15000 }).should(
        'have.length.greaterThan',
        1
      );
      cy.get('#general-ledger-filter-account').select(1);

      // Then: ページネーションUIが表示される
      cy.get('.pagination', { timeout: 15000 }).should('exist');
    });

    it('表示件数を変更できる', () => {
      // Given: 勘定科目を選択してテーブルが表示されるのを待つ
      cy.get('#general-ledger-filter-account option', { timeout: 15000 }).should(
        'have.length.greaterThan',
        1
      );
      cy.get('#general-ledger-filter-account').select(1);
      cy.wait(1000); // データ取得を待つ
      cy.get('[data-testid="general-ledger-table"]', { timeout: 15000 }).should('be.visible');
      cy.get('.pagination', { timeout: 15000 }).should('be.visible');

      // When: 表示件数を変更
      cy.get('.pagination__select').should('be.visible').select('10');

      // Then: 表示件数が変更される（再レンダリング後に確認）
      cy.wait(1000); // 再レンダリングを待つ
      cy.get('.pagination__select', { timeout: 10000 }).should('have.value', '10');
    });
  });

  describe('アクセス制御', () => {
    it('一般ユーザーも総勘定元帳ページにアクセスできる', () => {
      // Given: 一般ユーザーでログイン
      cy.login('user', 'Password123!');
      cy.get('[data-testid="dashboard"]', { timeout: 15000 }).should('be.visible');

      // When: 総勘定元帳ページにアクセス
      cy.visit('/general-ledger');

      // Then: 総勘定元帳ページが表示される
      cy.get('[data-testid="general-ledger-page"]', { timeout: 15000 }).should('be.visible');
      cy.get('[data-testid="general-ledger-filter"]').should('be.visible');
    });

    it('未認証ユーザーは総勘定元帳ページにアクセスできない', () => {
      // Given: 未認証状態

      // When: 総勘定元帳ページに直接アクセス
      cy.visit('/general-ledger');

      // Then: ログインページにリダイレクトされる
      cy.url().should('include', '/login');
      cy.get('[data-testid="login-page"]', { timeout: 15000 }).should('be.visible');
    });

    it('経理責任者も総勘定元帳ページにアクセスできる', () => {
      // Given: 経理責任者でログイン
      cy.login('manager', 'Password123!');
      cy.get('[data-testid="dashboard"]', { timeout: 15000 }).should('be.visible');

      // When: 総勘定元帳ページにアクセス
      cy.visit('/general-ledger');

      // Then: 総勘定元帳ページが表示される
      cy.get('[data-testid="general-ledger-page"]', { timeout: 15000 }).should('be.visible');
    });
  });

  describe('エラーハンドリング', () => {
    beforeEach(() => {
      cy.login('admin', 'Password123!');
      cy.get('[data-testid="dashboard"]', { timeout: 15000 }).should('be.visible');
      cy.visit('/general-ledger');
      cy.get('[data-testid="general-ledger-page"]', { timeout: 15000 }).should('be.visible');
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
