/**
 * 補助元帳照会 E2E テスト
 *
 * US-LDG-002: 補助元帳照会
 *
 * 受入条件:
 * - 勘定科目と補助科目を選択して元帳を表示できる
 * - 日付、仕訳番号、摘要、借方金額、貸方金額、残高が表示される
 * - 期間を指定して絞り込みできる
 */

describe('US-LDG-002: 補助元帳照会', () => {
  beforeEach(() => {
    cy.clearAuth();
  });

  describe('画面表示', () => {
    beforeEach(() => {
      cy.login('admin', 'Password123!');
      cy.get('[data-testid="dashboard"]', { timeout: 15000 }).should('be.visible');
      cy.visit('/subsidiary-ledger');
      cy.get('[data-testid="subsidiary-ledger-page"]', { timeout: 15000 }).should('be.visible');
    });

    it('補助元帳照会ページが表示される', () => {
      // Then: ページタイトルが表示される
      cy.contains('h1', '補助元帳照会').should('be.visible');

      // Then: フィルタUIが表示される
      cy.get('[data-testid="subsidiary-ledger-filter"]').should('be.visible');
      cy.get('#subsidiary-ledger-filter-account').should('be.visible');
      cy.get('#subsidiary-ledger-filter-sub-account').should('be.visible');
      cy.get('#subsidiary-ledger-filter-date-from').should('be.visible');
      cy.get('#subsidiary-ledger-filter-date-to').should('be.visible');
      cy.contains('button', '照会').should('be.visible');
    });

    it('勘定科目ドロップダウンに選択肢が表示される', () => {
      // Then: 勘定科目の選択肢が読み込まれる
      cy.get('#subsidiary-ledger-filter-account option', { timeout: 15000 }).should(
        'have.length.greaterThan',
        1
      );

      // Then: プレースホルダが表示される
      cy.get('#subsidiary-ledger-filter-account')
        .contains('option', '勘定科目を選択')
        .should('exist');
    });
  });

  describe('勘定科目と補助科目を選択して元帳を表示できる', () => {
    beforeEach(() => {
      cy.login('admin', 'Password123!');
      cy.get('[data-testid="dashboard"]', { timeout: 15000 }).should('be.visible');
      cy.visit('/subsidiary-ledger');
      cy.get('[data-testid="subsidiary-ledger-page"]', { timeout: 15000 }).should('be.visible');
    });

    it('勘定科目を選択すると補助元帳が表示される', () => {
      // When: 勘定科目を選択
      cy.get('#subsidiary-ledger-filter-account option', { timeout: 15000 }).should(
        'have.length.greaterThan',
        1
      );
      cy.get('#subsidiary-ledger-filter-account').select(1);

      // Then: 元帳データが表示される
      cy.get('[data-testid="subsidiary-ledger-summary"]', { timeout: 15000 }).should('be.visible');
      cy.get('[data-testid="subsidiary-ledger-table"]', { timeout: 15000 }).should('be.visible');
    });

    it('補助科目を入力して照会できる', () => {
      // When: 勘定科目を選択し補助科目を入力
      cy.get('#subsidiary-ledger-filter-account option', { timeout: 15000 }).should(
        'have.length.greaterThan',
        1
      );
      cy.get('#subsidiary-ledger-filter-account').select(1);
      cy.get('#subsidiary-ledger-filter-sub-account').type('A001');
      cy.contains('button', '照会').click();

      // Then: 元帳データが表示される
      cy.get('[data-testid="subsidiary-ledger-table"]', { timeout: 15000 }).should('be.visible');
    });
  });

  describe('日付、仕訳番号、摘要、借方金額、貸方金額、残高が表示される', () => {
    beforeEach(() => {
      cy.login('admin', 'Password123!');
      cy.get('[data-testid="dashboard"]', { timeout: 15000 }).should('be.visible');
      cy.visit('/subsidiary-ledger');
      cy.get('[data-testid="subsidiary-ledger-page"]', { timeout: 15000 }).should('be.visible');
    });

    it('テーブルに必要なカラムが表示される', () => {
      // When: 勘定科目を選択
      cy.get('#subsidiary-ledger-filter-account option', { timeout: 15000 }).should(
        'have.length.greaterThan',
        1
      );
      cy.get('#subsidiary-ledger-filter-account').select(1);

      // Then: テーブルヘッダーに必要なカラムが表示される
      cy.get('[data-testid="subsidiary-ledger-table"]', { timeout: 15000 }).should('be.visible');
      cy.contains('th', '日付').should('be.visible');
      cy.contains('th', '仕訳番号').should('be.visible');
      cy.contains('th', '摘要').should('be.visible');
      cy.contains('th', '借方').should('be.visible');
      cy.contains('th', '貸方').should('be.visible');
      cy.contains('th', '残高').should('be.visible');
    });

    it('サマリ情報が表示される', () => {
      // When: 勘定科目を選択
      cy.get('#subsidiary-ledger-filter-account option', { timeout: 15000 }).should(
        'have.length.greaterThan',
        1
      );
      cy.get('#subsidiary-ledger-filter-account').select(1);

      // Then: サマリ情報が表示される
      cy.get('[data-testid="subsidiary-ledger-summary"]', { timeout: 15000 }).should('be.visible');
      cy.get('[data-testid="subsidiary-ledger-summary"]').should('contain', '前期繰越');
      cy.get('[data-testid="subsidiary-ledger-summary"]').should('contain', '借方合計');
      cy.get('[data-testid="subsidiary-ledger-summary"]').should('contain', '貸方合計');
      cy.get('[data-testid="subsidiary-ledger-summary"]').should('contain', '期末残高');
    });
  });

  describe('期間を指定して絞り込みできる', () => {
    beforeEach(() => {
      cy.login('admin', 'Password123!');
      cy.get('[data-testid="dashboard"]', { timeout: 15000 }).should('be.visible');
      cy.visit('/subsidiary-ledger');
      cy.get('[data-testid="subsidiary-ledger-page"]', { timeout: 15000 }).should('be.visible');
    });

    it('開始日と終了日を指定してフィルタリングできる', () => {
      // Given: 勘定科目を選択してテーブルが表示されるのを待つ
      cy.get('#subsidiary-ledger-filter-account option', { timeout: 15000 }).should(
        'have.length.greaterThan',
        1
      );
      cy.get('#subsidiary-ledger-filter-account').select(1);
      cy.get('[data-testid="subsidiary-ledger-table"]', { timeout: 15000 }).should('be.visible');

      // When: 期間を指定して照会
      cy.get('#subsidiary-ledger-filter-date-from').clear().type('2024-04-01');
      cy.get('#subsidiary-ledger-filter-date-to').clear().type('2024-04-30');
      cy.contains('button', '照会').click();

      // Then: フィルタリングが実行される
      cy.get('[data-testid="subsidiary-ledger-table"]', { timeout: 15000 }).should('be.visible');
    });
  });

  describe('アクセス制御', () => {
    it('一般ユーザーも補助元帳ページにアクセスできる', () => {
      cy.login('user', 'Password123!');
      cy.get('[data-testid="dashboard"]', { timeout: 15000 }).should('be.visible');
      cy.visit('/subsidiary-ledger');
      cy.get('[data-testid="subsidiary-ledger-page"]', { timeout: 15000 }).should('be.visible');
    });

    it('未認証ユーザーは補助元帳ページにアクセスできない', () => {
      cy.visit('/subsidiary-ledger');
      cy.url().should('include', '/login');
      cy.get('[data-testid="login-page"]', { timeout: 15000 }).should('be.visible');
    });
  });

  describe('エラーハンドリング', () => {
    beforeEach(() => {
      cy.login('admin', 'Password123!');
      cy.get('[data-testid="dashboard"]', { timeout: 15000 }).should('be.visible');
      cy.visit('/subsidiary-ledger');
      cy.get('[data-testid="subsidiary-ledger-page"]', { timeout: 15000 }).should('be.visible');
    });

    it('勘定科目を選択せずに照会するとエラーメッセージが表示される', () => {
      cy.contains('button', '照会').click();
      cy.contains('勘定科目を選択してください').should('be.visible');
    });
  });
});
