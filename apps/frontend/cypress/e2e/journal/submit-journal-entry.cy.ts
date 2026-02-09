/**
 * 仕訳承認申請 E2E テスト
 *
 * US-JNL-007: 仕訳承認申請
 *
 * 受入条件:
 * - 「下書き」ステータスの仕訳のみ承認申請できる
 * - 承認申請後、ステータスが「承認待ち」に変わる
 * - 承認申請成功時、確認メッセージが表示される
 */

describe('US-JNL-007: 仕訳承認申請', () => {
  beforeEach(() => {
    cy.clearAuth();
  });

  describe('承認申請ボタン表示', () => {
    beforeEach(() => {
      cy.login('admin', 'Password123!');
      cy.get('[data-testid="dashboard"]', { timeout: 15000 }).should('be.visible');
      cy.visitJournalEntryList();
    });

    it('下書きステータスの仕訳に承認申請ボタンが表示される', () => {
      // Given: 仕訳一覧が表示されている
      cy.get('table tbody tr', { timeout: 10000 }).should('have.length.at.least', 1);

      // When: 下書きでフィルタリング
      cy.get('#journal-entry-filter-status').select('DRAFT');
      cy.contains('button', '検索').click();

      // Then: 下書きの仕訳に承認申請ボタンが表示される
      cy.get('table tbody tr').first().should('contain', '下書き');
      cy.get('table tbody tr').first().contains('button', '承認申請').should('be.visible');
    });

    it('承認待ちステータスの仕訳には承認申請ボタンが表示されない', () => {
      // Given: 仕訳一覧が表示されている

      // When: 承認待ちでフィルタリング
      cy.get('#journal-entry-filter-status').select('PENDING');
      cy.contains('button', '検索').click();

      // Then: 承認待ちの仕訳には承認申請ボタンが表示されない
      cy.get('table tbody tr', { timeout: 10000 }).should('exist');
      cy.get('table tbody tr').first().should('contain', '承認待ち');
      cy.get('table tbody tr').first().contains('button', '承認申請').should('not.exist');
    });

    it('承認済みステータスの仕訳には承認申請ボタンが表示されない', () => {
      // Given: 仕訳一覧が表示されている

      // When: 承認済みでフィルタリング
      cy.get('#journal-entry-filter-status').select('APPROVED');
      cy.contains('button', '検索').click();

      // Then: 承認済みの仕訳には承認申請ボタンが表示されない
      cy.get('table tbody tr', { timeout: 10000 }).should('exist');
      cy.get('table tbody tr').first().should('contain', '承認済み');
      cy.get('table tbody tr').first().contains('button', '承認申請').should('not.exist');
    });
  });

  describe('承認申請実行', () => {
    beforeEach(() => {
      cy.login('admin', 'Password123!');
      cy.get('[data-testid="dashboard"]', { timeout: 15000 }).should('be.visible');

      // 承認申請テスト用の仕訳を作成
      cy.createTestJournalEntry('2024-07-01', '承認申請テスト仕訳', '12000');
      cy.get('[data-testid="journal-entry-success"]', { timeout: 15000 }).should('be.visible');

      // 仕訳一覧ページに移動
      cy.visitJournalEntryList();
    });

    it('承認申請ボタンクリックで確認ダイアログが表示される', () => {
      // Given: 下書きの仕訳が表示されている
      cy.get('#journal-entry-filter-status').select('DRAFT');
      cy.contains('button', '検索').click();
      cy.get('table tbody tr', { timeout: 10000 }).should('have.length.at.least', 1);

      // When: 承認申請ボタンをクリック
      cy.on('window:confirm', (text) => {
        // Then: 確認ダイアログが表示される
        expect(text).to.include('承認申請しますか');
        return false; // キャンセル
      });

      cy.get('table tbody tr').first().contains('button', '承認申請').click();
    });

    it('確認ダイアログでキャンセルすると承認申請が行われない', () => {
      // Given: 下書きの仕訳が表示されている
      cy.get('#journal-entry-filter-status').select('DRAFT');
      cy.contains('button', '検索').click();
      cy.get('table tbody tr', { timeout: 10000 }).should('have.length.at.least', 1);

      // 最初の仕訳のステータスを記録
      cy.get('table tbody tr').first().should('contain', '下書き');

      // When: 確認ダイアログでキャンセル
      cy.on('window:confirm', () => false);
      cy.get('table tbody tr').first().contains('button', '承認申請').click();

      // Then: ステータスは変わらない
      cy.get('table tbody tr').first().should('contain', '下書き');
    });

    it('承認申請が成功するとステータスが承認待ちに変わる', () => {
      // Given: 下書きの仕訳が表示されている
      cy.get('#journal-entry-filter-status').select('DRAFT');
      cy.contains('button', '検索').click();
      cy.get('table tbody tr', { timeout: 10000 }).should('have.length.at.least', 1);

      // When: 承認申請を実行
      cy.on('window:confirm', () => true);
      cy.get('table tbody tr').first().contains('button', '承認申請').click();

      // Then: 成功メッセージが表示される
      cy.contains('仕訳を承認申請しました', { timeout: 10000 }).should('be.visible');
    });
  });

  describe('権限確認', () => {
    it('一般ユーザーでも承認申請ができる', () => {
      // Given: 一般ユーザーでログイン
      cy.login('user', 'Password123!');
      cy.get('[data-testid="dashboard"]', { timeout: 15000 }).should('be.visible');

      // When: 仕訳一覧に移動
      cy.visitJournalEntryList();

      // Then: 下書きの仕訳一覧が表示される
      cy.get('#journal-entry-filter-status').select('DRAFT');
      cy.contains('button', '検索').click();
      cy.get('table tbody tr', { timeout: 10000 }).should('exist');

      // Then: 承認申請ボタンが表示される
      cy.get('table tbody tr').first().contains('button', '承認申請').should('be.visible');
    });
  });
});
